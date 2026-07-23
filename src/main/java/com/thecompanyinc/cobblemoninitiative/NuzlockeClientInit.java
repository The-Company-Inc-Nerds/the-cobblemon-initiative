package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackClient;
import com.thecompanyinc.cobblemoninitiative.screen.DaycareSelectionScreen;
import com.thecompanyinc.cobblemoninitiative.screen.LoadingOverlayScreen;
import com.thecompanyinc.cobblemoninitiative.screen.NicknamePromptScreen;
import com.thecompanyinc.cobblemoninitiative.screen.SacrificeSelectionScreen;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuzlockeClientInit implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiativeInit.MOD_ID);

  /** Latest unconsumed picker-open request from the server (payload-driven since
   *  0.6.0-alpha.6 — the tick poll opens it once no other screen is up). */
  private static volatile InitiativePayloads.PickerOpenPayload pendingPicker = null;

  /** Queued nickname offers — a QUEUE, not a single field: multi-gift moments
   *  (fossil pair, back-to-back captures) arrive faster than screens close, and
   *  each new teammate deserves its own prompt. Drained one per screen-free tick. */
  private static final Queue<InitiativePayloads.NicknamePromptPayload> pendingNicknames =
    new ConcurrentLinkedQueue<>();

  /** A victory-watcher scene queued by the server, held until the Cobblemon battle GUI has
   *  fully closed — playing it while the GUI still owned the camera hid the rig's ascent
   *  ("it doesn't climb after the battle"). Cleared once bounced back or on disconnect. */
  private static volatile String pendingVictoryWatcher = null;

  @Override
  public void onInitializeClient() {
    LOGGER.info("Nuzlocke client initialized!");

    // Quest tracking — ] / [ keybinds + the in-process waypoint poller.
    QuestTrackClient.init();

    // Server-driven picker opens. Not opened here directly: the tick poll below waits
    // for screen == null so it sequences AFTER Easy NPC's deferred CLOSE_DIALOG — a
    // dialog-button deposit flows straight into the picker (same behaviour as the old
    // singleplayer static-flag bridge, now dedicated-server-safe).
    ClientPlayNetworking.registerGlobalReceiver(
      InitiativePayloads.PickerOpenPayload.TYPE,
      (payload, context) -> pendingPicker = payload);

    ClientPlayNetworking.registerGlobalReceiver(
      InitiativePayloads.NicknamePromptPayload.TYPE,
      (payload, context) -> pendingNicknames.add(payload));

    // Victory-watcher scene: held (below) until the battle GUI closes, then bounced back.
    ClientPlayNetworking.registerGlobalReceiver(
      InitiativePayloads.VictoryWatcherPayload.TYPE,
      (payload, context) -> pendingVictoryWatcher = payload.scene());

    // Live-earn achievement toast (backfills never send this — see AchievementToasts).
    // Popped straight away; no screen sequencing needed, a toast overlays whatever's up.
    ClientPlayNetworking.registerGlobalReceiver(
      InitiativePayloads.AchievementToastPayload.TYPE,
      (payload, context) ->
        com.thecompanyinc.cobblemoninitiative.achievement.client.AchievementToasts.show(
          payload.advancement(), payload.title()));

    // First-join install loading overlay (black screen + bar) shown while the pack
    // auto-installs, then dismissed as the opening cutscene starts. setScreen must run on
    // the client thread, so marshal off the network thread with mc.execute.
    ClientPlayNetworking.registerGlobalReceiver(
      InitiativePayloads.InstallOverlayPayload.TYPE,
      (payload, context) -> {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
          switch (payload.phase()) {
            case InitiativePayloads.InstallOverlayPayload.PHASE_OPEN -> {
              if (!(mc.screen instanceof LoadingOverlayScreen)) {
                mc.setScreen(new LoadingOverlayScreen());
              }
            }
            case InitiativePayloads.InstallOverlayPayload.PHASE_DONE -> {
              // Open-then-mark if a lost/raced "open" (vs the vanilla level-load screen) meant
              // the overlay never appeared — better a brief filled bar than no overlay at all.
              if (!(mc.screen instanceof LoadingOverlayScreen)) {
                mc.setScreen(new LoadingOverlayScreen());
              }
              if (mc.screen instanceof LoadingOverlayScreen los) los.markDone();
            }
            case InitiativePayloads.InstallOverlayPayload.PHASE_CLOSE -> {
              if (mc.screen instanceof LoadingOverlayScreen) mc.setScreen(null);
            }
            default -> {}
          }
        });
      });

    // Undelivered requests die with the session — a queued nickname offer (or an
    // unconsumed picker-open) from a previous world would reference state this
    // server never owned, popping a ghost screen on the next join.
    net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT
      .register((handler, client) -> {
        pendingNicknames.clear();
        pendingPicker = null;
        pendingVictoryWatcher = null;
        // A mid-install disconnect must never strand the player on the black overlay.
        if (client.screen instanceof LoadingOverlayScreen) client.setScreen(null);
      });

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player != null && client.screen == null) {
        if (NuzlockeInit.consumePendingSacrifice()) {
          client.setScreen(new SacrificeSelectionScreen());
        } else if (pendingPicker != null) {
          InitiativePayloads.PickerOpenPayload request = pendingPicker;
          pendingPicker = null;
          if (InitiativePayloads.FACILITY_MOMCARE.equals(request.facility())) {
            client.setScreen(new DaycareSelectionScreen(
              "Mom's Care", 0xFF77DD, request.freeSlots(), "Leave with Mom",
              InitiativePayloads.FACILITY_MOMCARE));
          } else {
            client.setScreen(new DaycareSelectionScreen(
              "Sango Daycare", 0x55FF55, request.freeSlots(), "Board Selected",
              InitiativePayloads.FACILITY_DAYCARE));
          }
        } else if (com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getBattle() == null) {
          // Cobblemon's battle GUI is only a Screen while the move menu is open —
          // between opens screen == null even mid-battle, and a modal there would
          // feed the battle keybinds into the EditBox. Offers hold until it ends.
          InitiativePayloads.NicknamePromptPayload offer = pendingNicknames.poll();
          if (offer != null) {
            client.setScreen(new NicknamePromptScreen(offer.monUuid(), offer.speciesName()));
          }
        }

        // Victory-watcher: once the battle GUI is truly gone (getBattle()==null), ask the
        // server to play the scene. Separate from the else-if chain above — it sends a
        // packet, opens no screen — so a gym win with nothing else queued fires it at once.
        // Re-check screen==null: a branch above may have opened a modal THIS tick, and the
        // scene must not start behind it.
        if (pendingVictoryWatcher != null && client.screen == null
            && com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getBattle() == null) {
          String scene = pendingVictoryWatcher;
          pendingVictoryWatcher = null;
          ClientPlayNetworking.send(new InitiativePayloads.VictoryWatcherReadyPayload(scene));
        }
      }
    });
  }

  public static void triggerSacrificeSelection() {
    Minecraft.getInstance().execute(() ->
      Minecraft.getInstance().setScreen(new SacrificeSelectionScreen())
    );
  }
}

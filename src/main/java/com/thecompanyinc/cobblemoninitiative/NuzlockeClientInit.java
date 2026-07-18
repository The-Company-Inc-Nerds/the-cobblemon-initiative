package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackClient;
import com.thecompanyinc.cobblemoninitiative.screen.DaycareSelectionScreen;
import com.thecompanyinc.cobblemoninitiative.screen.SacrificeSelectionScreen;
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

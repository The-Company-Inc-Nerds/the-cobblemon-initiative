package com.thecompanyinc.cobblemoninitiative.network;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Real S2C/C2S payloads for the party-picker flow, replacing the same-JVM static-flag
 * bridges ({@code DaycareManager.pendingPicker} + the screen's
 * {@code getSingleplayerServer()} confirm) that made the daycare/momcare pickers
 * singleplayer-only — a dedicated-server client could never see the picker, which is
 * what kept the {@code daycare_board} e2e scenario parked. Fabric networking works
 * identically on the integrated server, so this is the single code path for both.
 *
 * <p>Flow: the deposit command validates server-side and sends {@link PickerOpenPayload}
 * (with the REAL free-slot count — the old client bridge guessed it); the client tick
 * poll opens the picker once no other screen is up (sequencing after Easy NPC's deferred
 * dialog close, unchanged); confirm sends {@link PickerDepositPayload} with the chosen
 * party mon uuids; the server receiver routes to the facility manager, which re-validates
 * everything (slots, last-mon guard, ownership) exactly as before.
 */
public final class InitiativePayloads {

  public static final String FACILITY_DAYCARE = "daycare";
  public static final String FACILITY_MOMCARE = "momcare";

  /** S2C: open the party picker for a facility with the given free-slot count. */
  public record PickerOpenPayload(String facility, int freeSlots)
    implements CustomPacketPayload {

    public static final Type<PickerOpenPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "picker_open"));

    public static final StreamCodec<FriendlyByteBuf, PickerOpenPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PickerOpenPayload::facility,
        ByteBufCodecs.VAR_INT, PickerOpenPayload::freeSlots,
        PickerOpenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** C2S: the picker's confirmed selection (party mon uuids) for a facility. */
  public record PickerDepositPayload(String facility, List<UUID> monUuids)
    implements CustomPacketPayload {

    public static final Type<PickerDepositPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "picker_deposit"));

    public static final StreamCodec<FriendlyByteBuf, PickerDepositPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PickerDepositPayload::facility,
        UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), PickerDepositPayload::monUuids,
        PickerDepositPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** S2C: offer the nickname prompt for a freshly-gained mon. Fire-and-forget —
   *  nothing server-side waits, so a bot/crashed client is a silent no-op. */
  public record NicknamePromptPayload(UUID monUuid, String speciesName)
    implements CustomPacketPayload {

    public static final Type<NicknamePromptPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "nickname_prompt"));

    public static final StreamCodec<FriendlyByteBuf, NicknamePromptPayload> CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, NicknamePromptPayload::monUuid,
        ByteBufCodecs.STRING_UTF8, NicknamePromptPayload::speciesName,
        NicknamePromptPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** C2S: the chosen nickname (blank = keep the species name). */
  public record NicknameSetPayload(UUID monUuid, String nickname)
    implements CustomPacketPayload {

    public static final Type<NicknameSetPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "nickname_set"));

    public static final StreamCodec<FriendlyByteBuf, NicknameSetPayload> CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, NicknameSetPayload::monUuid,
        ByteBufCodecs.STRING_UTF8, NicknameSetPayload::nickname,
        NicknameSetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /**
   * S2C: pop a client-side achievement toast for a LIVE earn. Carries the advancement id
   * (the client resolves its display for the vanilla AdvancementToast) plus a title
   * fallback used if the advancement is not synced client-side. Sent ONLY on live earns —
   * silent mid-run backfills never send this, which is what keeps them toast-free while the
   * advancement JSONs stay {@code show_toast:false}.
   */
  public record AchievementToastPayload(String advancement, String title)
    implements CustomPacketPayload {

    public static final Type<AchievementToastPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "achievement_toast"));

    public static final StreamCodec<FriendlyByteBuf, AchievementToastPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, AchievementToastPayload::advancement,
        ByteBufCodecs.STRING_UTF8, AchievementToastPayload::title,
        AchievementToastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /**
   * S2C: drive the first-join install loading overlay (black screen + progress bar) shown
   * while the pack auto-installs, BEFORE the opening cutscene. Fire-and-forget; a bare-mod or
   * bot client simply never receives it. {@code phase}: {@code "open"} shows the overlay and
   * lets it animate toward {@code progress}; {@code "done"} fills it to 1.0 and holds; {@code
   * "close"} dismisses it (AutoInstall closes it the same tick it plays the opening cutscene).
   */
  public record InstallOverlayPayload(String phase, float progress)
    implements CustomPacketPayload {

    public static final String PHASE_OPEN = "open";
    public static final String PHASE_DONE = "done";
    public static final String PHASE_CLOSE = "close";

    public static final Type<InstallOverlayPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "install_overlay"));

    public static final StreamCodec<FriendlyByteBuf, InstallOverlayPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, InstallOverlayPayload::phase,
        ByteBufCodecs.FLOAT, InstallOverlayPayload::progress,
        InstallOverlayPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /**
   * S2C: hand the client a victory-watcher cutscene to play ONCE its Cobblemon battle GUI has
   * fully closed. The scene used to be fired server-side straight from {@code BATTLE_VICTORY},
   * but the battle GUI still owned the camera on that tick, so the spectator rig climbed unseen
   * and the reveal read as "it doesn't climb after the battle." The client now holds this until
   * {@code CobblemonClient.getBattle() == null} (the same signal the nickname prompt waits on),
   * then bounces {@link VictoryWatcherReadyPayload} back so the scene plays exactly like the
   * {@code /cutscene play} command path does.
   */
  public record VictoryWatcherPayload(String scene) implements CustomPacketPayload {

    public static final Type<VictoryWatcherPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "victory_watcher"));

    public static final StreamCodec<FriendlyByteBuf, VictoryWatcherPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, VictoryWatcherPayload::scene,
        VictoryWatcherPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** C2S: the client's battle GUI has closed — play the queued victory-watcher scene now. */
  public record VictoryWatcherReadyPayload(String scene) implements CustomPacketPayload {

    public static final Type<VictoryWatcherReadyPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "victory_watcher_ready"));

    public static final StreamCodec<FriendlyByteBuf, VictoryWatcherReadyPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, VictoryWatcherReadyPayload::scene,
        VictoryWatcherReadyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  private InitiativePayloads() {}

  /** Common init: register payload types + the server-side deposit receiver. */
  public static void register() {
    PayloadTypeRegistry.playS2C().register(PickerOpenPayload.TYPE, PickerOpenPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(PickerDepositPayload.TYPE, PickerDepositPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(NicknamePromptPayload.TYPE, NicknamePromptPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(NicknameSetPayload.TYPE, NicknameSetPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(AchievementToastPayload.TYPE, AchievementToastPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(InstallOverlayPayload.TYPE, InstallOverlayPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(VictoryWatcherPayload.TYPE, VictoryWatcherPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(VictoryWatcherReadyPayload.TYPE, VictoryWatcherReadyPayload.CODEC);

    // Fabric 1.21 play receivers run on the server thread; the managers re-validate the
    // whole request, so a stale/forged payload degrades to a polite refusal.
    ServerPlayNetworking.registerGlobalReceiver(PickerDepositPayload.TYPE, (payload, context) -> {
      ServerPlayer player = context.player();
      switch (payload.facility()) {
        case FACILITY_DAYCARE ->
          InitiativeInit.getDaycareManager().deposit(player.getUUID(), payload.monUuids());
        case FACILITY_MOMCARE ->
          InitiativeInit.getMomCareManager().deposit(player.getUUID(), payload.monUuids());
        default -> InitiativeInit.LOGGER.warn(
          "[Network] Unknown picker facility '{}' from {}.",
          payload.facility(), player.getName().getString());
      }
    });

    ServerPlayNetworking.registerGlobalReceiver(NicknameSetPayload.TYPE, (payload, context) ->
      com.thecompanyinc.cobblemoninitiative.nickname.NicknameManager.applyNickname(
        context.player(), payload.monUuid(), payload.nickname()));

    // The client bounces this once its Cobblemon battle GUI has closed. InitiativeInit validates
    // the scene, plays it, and latches the one-shot tag only on a successful start.
    ServerPlayNetworking.registerGlobalReceiver(VictoryWatcherReadyPayload.TYPE, (payload, context) ->
      InitiativeInit.playVictoryWatcherConfirmed(context.player(), payload.scene()));
  }

  /** Server→client: open the picker (replaces the static-flag bridge). */
  public static void sendPickerOpen(ServerPlayer player, String facility, int freeSlots) {
    ServerPlayNetworking.send(player, new PickerOpenPayload(facility, freeSlots));
  }

  /** Server→client: offer the nickname prompt for a new acquisition. */
  public static void sendNicknamePrompt(ServerPlayer player, UUID monUuid, String speciesName) {
    ServerPlayNetworking.send(player, new NicknamePromptPayload(monUuid, speciesName));
  }

  /** Server→client: pop a live-earn achievement toast (fire-and-forget). */
  public static void sendAchievementToast(ServerPlayer player, String advancement, String title) {
    ServerPlayNetworking.send(player, new AchievementToastPayload(advancement, title));
  }

  /** Server→client: drive the first-join install loading overlay (fire-and-forget). */
  public static void sendInstallOverlay(ServerPlayer player, String phase, float progress) {
    ServerPlayNetworking.send(player, new InstallOverlayPayload(phase, progress));
  }

  /** Server→client: queue a victory-watcher scene, played once the client's battle GUI clears. */
  public static void sendVictoryWatcher(ServerPlayer player, String scene) {
    ServerPlayNetworking.send(player, new VictoryWatcherPayload(scene));
  }
}

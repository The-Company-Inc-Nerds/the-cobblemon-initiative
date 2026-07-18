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

  private InitiativePayloads() {}

  /** Common init: register payload types + the server-side deposit receiver. */
  public static void register() {
    PayloadTypeRegistry.playS2C().register(PickerOpenPayload.TYPE, PickerOpenPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(PickerDepositPayload.TYPE, PickerDepositPayload.CODEC);

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
  }

  /** Server→client: open the picker (replaces the static-flag bridge). */
  public static void sendPickerOpen(ServerPlayer player, String facility, int freeSlots) {
    ServerPlayNetworking.send(player, new PickerOpenPayload(facility, freeSlots));
  }
}

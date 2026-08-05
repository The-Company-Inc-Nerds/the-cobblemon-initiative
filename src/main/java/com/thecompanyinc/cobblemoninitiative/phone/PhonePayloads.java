package com.thecompanyinc.cobblemoninitiative.phone;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * PokePhone S2C/C2S trio (the victory-watcher round-trip is the precedent, see
 * {@code network/InitiativePayloads}): the server OFFERS a ringing call, the client answers
 * with an ACTION, and only an accepted call ever ships its full script (a declined ring
 * never round-trips the pages). Every C2S is validated against the live per-player session
 * in {@link PhoneCallManager} — the client only ever sends ids and INDICES; all commands
 * are server-authored script data, so a forged payload degrades to a logged no-op.
 */
public final class PhonePayloads {

  /** S2C: a call has started ringing — everything the splash view needs, plus the ring
   *  window length so the client can expire its live offer in step with the server. */
  public record PhoneOfferPayload(
    String callId, String caller, String subtitle, String avatar, int accent, int ringTicks
  ) implements CustomPacketPayload {

    public static final Type<PhoneOfferPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "phone_offer"));

    public static final StreamCodec<FriendlyByteBuf, PhoneOfferPayload> CODEC = StreamCodec.of(
      (buf, p) -> {
        buf.writeUtf(p.callId());
        buf.writeUtf(p.caller());
        buf.writeUtf(p.subtitle());
        buf.writeUtf(p.avatar());
        buf.writeVarInt(p.accent());
        buf.writeVarInt(p.ringTicks());
      },
      buf -> new PhoneOfferPayload(
        buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
        buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** S2C: the accepted call's full script — sequential pages plus the choice LABELS
   *  (their commands stay server-side; the client answers with an index). */
  public record PhoneOpenPayload(String callId, List<String> pages, List<String> choiceLabels)
    implements CustomPacketPayload {

    public static final Type<PhoneOpenPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "phone_open"));

    public static final StreamCodec<FriendlyByteBuf, PhoneOpenPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PhoneOpenPayload::callId,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PhoneOpenPayload::pages,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PhoneOpenPayload::choiceLabels,
        PhoneOpenPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /** C2S: the player's action on the call named by {@code callId}. {@code choice} is the
   *  0-based choice index for CHOOSE, ignored otherwise. */
  public record PhoneActionPayload(String callId, int action, int choice)
    implements CustomPacketPayload {

    public static final int ACTION_ANSWER = 0;
    public static final int ACTION_DECLINE = 1;
    public static final int ACTION_CHOOSE = 2;
    public static final int ACTION_COMPLETE = 3;
    public static final int ACTION_ABORT = 4;

    public static final Type<PhoneActionPayload> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath(InitiativeInit.MOD_ID, "phone_action"));

    public static final StreamCodec<FriendlyByteBuf, PhoneActionPayload> CODEC =
      StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PhoneActionPayload::callId,
        ByteBufCodecs.VAR_INT, PhoneActionPayload::action,
        ByteBufCodecs.VAR_INT, PhoneActionPayload::choice,
        PhoneActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  private PhonePayloads() {}

  /** Common init: register the payload types + the server-side action receiver. */
  public static void register() {
    PayloadTypeRegistry.playS2C().register(PhoneOfferPayload.TYPE, PhoneOfferPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(PhoneOpenPayload.TYPE, PhoneOpenPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(PhoneActionPayload.TYPE, PhoneActionPayload.CODEC);

    // Fabric 1.21 play receivers run on the server thread — a plain event-dispatch path,
    // so the manager's completion commands may performPrefixedCommand directly (the
    // nested-dispatch trap only bites callers already inside a command context).
    ServerPlayNetworking.registerGlobalReceiver(PhoneActionPayload.TYPE, (payload, context) -> {
      ServerPlayer player = context.player();
      PhoneCallManager manager = InitiativeInit.getPhoneCallManager();
      if (manager == null) return;
      switch (payload.action()) {
        case PhoneActionPayload.ACTION_ANSWER -> manager.answer(player, payload.callId());
        case PhoneActionPayload.ACTION_DECLINE -> manager.decline(player, payload.callId());
        case PhoneActionPayload.ACTION_CHOOSE ->
          manager.choose(player, payload.callId(), payload.choice());
        case PhoneActionPayload.ACTION_COMPLETE -> manager.complete(player, payload.callId());
        case PhoneActionPayload.ACTION_ABORT -> manager.abort(player, payload.callId());
        default -> InitiativeInit.LOGGER.warn(
          "[Phone] Unknown action {} from {}.", payload.action(), player.getName().getString());
      }
    });
  }

  /** Server→client: the splash data for a ringing call. */
  public static void sendOffer(
    ServerPlayer player, String callId, String caller, String subtitle,
    String avatar, int accent, int ringTicks
  ) {
    ServerPlayNetworking.send(
      player, new PhoneOfferPayload(callId, caller, subtitle, avatar, accent, ringTicks));
  }

  /** Server→client: the accepted call's pages + choice labels. */
  public static void sendOpen(
    ServerPlayer player, String callId, List<String> pages, List<String> choiceLabels
  ) {
    ServerPlayNetworking.send(player, new PhoneOpenPayload(callId, pages, choiceLabels));
  }
}

package com.thecompanyinc.cobblemoninitiative.phone;

import com.mojang.blaze3d.platform.InputConstants;
import com.thecompanyinc.cobblemoninitiative.screen.PhoneCallScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Client half of the PokePhone: the "Answer PokePhone" keybind (registered default P; the
 * shipped pack rebinds it to Enter via options.txt) plus the offer/open receivers. While
 * an offer is live the client renders the flashing ring actionbar itself, so the prompt
 * always names the LIVE binding. Pressing the key opens the SPLASH locally from the offer
 * data — ANSWER is only sent when ACCEPT is clicked, so a decline never round-trips the
 * full script (QuestTrackClient is the keybind precedent).
 */
public final class PhoneCallClient {

  private static KeyMapping answerKey;

  /** The ring currently offered by the server; cleared on answer/decline/expiry. */
  private static volatile PhonePayloads.PhoneOfferPayload liveOffer;
  private static int offerTicksLeft;

  private PhoneCallClient() {}

  public static void init() {
    answerKey = KeyBindingHelper.registerKeyBinding(
      new KeyMapping(
        "key.cobblemon-initiative.phone_answer",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_P,
        "key.category.cobblemon-initiative"
      )
    );

    ClientPlayNetworking.registerGlobalReceiver(
      PhonePayloads.PhoneOfferPayload.TYPE,
      (payload, context) -> {
        liveOffer = payload;
        offerTicksLeft = payload.ringTicks();
      });

    // The accepted call's script. Normally the splash is already up (ACCEPT bounced the
    // ANSWER); a dev-hook `phone answer` arrives with no screen — open straight into the
    // conversation view (bots never get here; a real player driving the dev path does).
    ClientPlayNetworking.registerGlobalReceiver(
      PhonePayloads.PhoneOpenPayload.TYPE,
      (payload, context) -> {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
          PhonePayloads.PhoneOfferPayload offer = liveOffer;
          liveOffer = null; // the ring is over either way
          PhoneCallScreen screen;
          if (mc.screen instanceof PhoneCallScreen open
              && open.callId().equals(payload.callId())) {
            screen = open;
          } else {
            screen = (offer != null && offer.callId().equals(payload.callId()))
              ? new PhoneCallScreen(offer.callId(), offer.caller(), offer.subtitle(),
                  offer.avatar(), offer.accent())
              : new PhoneCallScreen(payload.callId(), payload.callId(), "",
                  PhoneCallScripts.AVATAR_INITIALS, PhoneCallScripts.DEFAULT_ACCENT);
            mc.setScreen(screen);
          }
          screen.openConversation(payload.pages(), payload.choiceLabels());
        });
      });

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      // Expire the offer in step with the server's ring window: after a miss the keybind
      // must go quiet again (the server-side requeue owns the re-ring).
      if (liveOffer != null && --offerTicksLeft <= 0) {
        liveOffer = null;
      }
      PhonePayloads.PhoneOfferPayload ring = liveOffer;
      if (ring != null) {
        // Flashing gold actionbar, alternating bright/dim every half second, phased from
        // the server's ring window so the cadence matches the chime. Rendered here (not
        // server-side) so the prompt names the live binding after any rebind. Not gated
        // on client.screen == null — the old server-sent prompt showed over screens too.
        boolean bright = ((ring.ringTicks() - offerTicksLeft) / 10) % 2 == 0;
        String key = answerKey.getTranslatedKeyMessage().getString();
        client.gui.setOverlayMessage(
          Component.literal(bright
            ? "§6§l☎ Incoming — " + ring.caller() + "  §r§e[" + key + "] to answer"
            : "§7☎ Incoming — " + ring.caller() + "  §8[" + key + "] to answer"),
          false);
      }
      while (answerKey.consumeClick()) {
        PhonePayloads.PhoneOfferPayload offer = liveOffer;
        // Guard: no live offer (or a modal already up) = the key does nothing.
        if (offer != null && client.screen == null && client.player != null) {
          client.setScreen(new PhoneCallScreen(
            offer.callId(), offer.caller(), offer.subtitle(),
            offer.avatar(), offer.accent()));
        }
      }
    });

    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      liveOffer = null; // an offer from a previous session must never reopen
      if (client.screen instanceof PhoneCallScreen) client.setScreen(null);
    });
  }

  /** The screen consumed (or refused) the offer — stop the keybind reopening the splash. */
  public static void clearOffer() {
    liveOffer = null;
  }
}

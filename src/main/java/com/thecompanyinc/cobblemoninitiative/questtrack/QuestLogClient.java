package com.thecompanyinc.cobblemoninitiative.questtrack;

import com.mojang.blaze3d.platform.InputConstants;
import com.thecompanyinc.cobblemoninitiative.screen.QuestLogScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * Client half of the Quest Log: the "Open Quest Log (Shift+key)" bind (default T) and the
 * END_CLIENT_TICK chord handler that opens {@link QuestLogScreen}. Client-only, no server
 * half — the screen reads everything off the vanilla-synced scoreboard
 * (PhoneCallClient is the wiring precedent, QuestTrackClient the keybind one).
 *
 * <p><b>THE SHIFT CHORD — read before touching.</b> Vanilla chat also owns T, and vanilla
 * resolves each physical key to exactly ONE KeyMapping: {@code KeyMapping.MAP} is keyed by
 * {@code InputConstants.Key}, {@code KeyMapping.click()} bumps only {@code MAP.get(key)},
 * and {@code resetMapping()} refills MAP from the name-keyed {@code ALL} HashMap — so
 * whether {@code key.chat} or this bind receives T's clicks is hash-order roulette that
 * Fabric API's keybinding module does not touch (bytecode-verified: 1.21.1 merged jar +
 * fabric-key-binding-api-v1 1.0.47). The handler therefore covers BOTH outcomes, acting
 * only when {@link Screen#hasShiftDown()}:
 *
 * <ul>
 *   <li><b>This bind won MAP[T]</b> — {@code consumeClick()} fires here. Shift held →
 *       open the log. Shift NOT held → chat never saw its click ({@code handleKeybinds}
 *       polled {@code keyChat.consumeClick()} and got nothing), so we re-emit it by
 *       opening the ChatScreen ourselves: a streamer whose T key stops opening chat is a
 *       broken pack. (Gated on {@code keyChat.same(ourKey)} — after a rebind to a
 *       non-conflicting key there is nothing to re-emit.)</li>
 *   <li><b>Chat won MAP[T]</b> — our clicks never arrive; the raw GLFW edge-poll below
 *       detects the fresh physical press instead. On Shift+T vanilla will have ALREADY
 *       opened the chat screen during {@code handleKeybinds} earlier in this same tick —
 *       {@code Minecraft.tick} runs handleKeybinds mid-tick and Fabric's END_CLIENT_TICK
 *       injects at the tick's RETURN (both verified in bytecode/mixin source), so our
 *       later {@code setScreen(new QuestLogScreen())} deterministically REPLACES it.
 *       That replacement IS the mechanism, not an accident. Plain T leaves chat alone
 *       because the shift check fails.</li>
 * </ul>
 *
 * <p>A ChatScreen is only ever replaced when it opened THIS tick (the screen was null at
 * the end of the previous tick): a chat the player is already typing in — where Shift+T
 * is just a capital T — is never hijacked. Any other screen already up (a mod modal, a
 * container) → skip entirely. Rebinding to a non-conflicting key removes the chat
 * interplay on both paths, but the Shift requirement stays — which is why the bind is
 * named "Open Quest Log (Shift+key)" in lang. Closing (Shift+key again, or ESC) lives in
 * {@link QuestLogScreen#keyPressed}, since an open screen owns the key events.
 */
public final class QuestLogClient {

  private static KeyMapping questLogKey;
  /** Raw GLFW state of the bound key last tick — edge detector for the chat-won-MAP path. */
  private static boolean keyWasPhysicallyDown;
  /** True when NO screen was up at the end of the previous tick — discriminates "chat
   *  opened this tick from this press" from "player already typing", and keeps a press
   *  consumed by an open screen (e.g. the log's own toggle-close) from re-firing here. */
  private static boolean screenWasNullLastTick = true;

  private QuestLogClient() {}

  public static void init() {
    questLogKey = KeyBindingHelper.registerKeyBinding(
      new KeyMapping(
        "key.cobblemon-initiative.quest_log",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_T,
        "key.category.cobblemon-initiative"
      )
    );

    ClientTickEvents.END_CLIENT_TICK.register(QuestLogClient::onEndTick);
  }

  /** For QuestLogScreen's toggle-close and footer hint. */
  public static KeyMapping keyMapping() {
    return questLogKey;
  }

  private static void onEndTick(Minecraft client) {
    boolean chatCouldBeOurs = screenWasNullLastTick;

    // Raw physical state of the bound key (KEYSYM binds only; unbound = never down).
    InputConstants.Key bound = KeyBindingHelper.getBoundKeyOf(questLogKey);
    boolean physicallyDown =
      !questLogKey.isUnbound() &&
      bound.getType() == InputConstants.Type.KEYSYM &&
      InputConstants.isKeyDown(client.getWindow().getWindow(), bound.getValue());
    boolean freshPress = physicallyDown && !keyWasPhysicallyDown;
    keyWasPhysicallyDown = physicallyDown;

    // Path 1 — this bind owns the key's click slot (see class comment).
    boolean acted = false;
    while (questLogKey.consumeClick()) {
      if (client.player == null) continue;
      if (Screen.hasShiftDown()) {
        if (client.screen == null || client.screen instanceof ChatScreen) {
          client.setScreen(new QuestLogScreen());
          acted = true;
        }
      } else if (
        client.screen == null && client.options.keyChat.same(questLogKey)
      ) {
        // We stole chat's click — hand T back to vanilla. Same tick as handleKeybinds
        // would have, and the press's char event predates the screen, so nothing leaks
        // into the input line.
        client.setScreen(new ChatScreen(""));
        acted = true;
      }
    }

    // Path 2 — chat owns the click slot: catch the press by raw edge instead. Gated on
    // screenWasNullLastTick, which does double duty: (a) only a ChatScreen that appeared
    // THIS tick — from this very press — is replaced, and (b) a press that some screen's
    // keyPressed already consumed (including QuestLogScreen's own Shift+T toggle-close,
    // which leaves screen == null by the time this runs) never re-opens the log.
    if (
      !acted &&
      freshPress &&
      chatCouldBeOurs &&
      Screen.hasShiftDown() &&
      client.player != null &&
      (client.screen == null || client.screen instanceof ChatScreen)
    ) {
      client.setScreen(new QuestLogScreen());
    }

    screenWasNullLastTick = client.screen == null;
  }
}

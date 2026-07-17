package com.thecompanyinc.cobblemoninitiative.devtools.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

/**
 * Ring buffer of everything the player would have READ on screen: chat, system messages,
 * action-bar overlays, and title/subtitle cards. The driver's {@code hud.chat} op drains
 * it with a sequence cursor so Python-side waits ({@code wait_chat(regex)}) are lossless
 * even between polls.
 *
 * <p>Sources — chosen so each message lands exactly ONCE:
 * <ul>
 *   <li>{@code CHAT} / {@code GAME(overlay=false)} fabric events — player + system chat</li>
 *   <li>{@link com.thecompanyinc.cobblemoninitiative.mixin.GuiTitleMixin} — titles,
 *       subtitles, AND action-bar overlays. Overlays are captured at
 *       {@code Gui.setOverlayMessage} (not the GAME event) because {@code /title actionbar}
 *       bypasses ChatListener entirely; the Gui setter is the one point both the packet
 *       path and the displayClientMessage path funnel through.</li>
 * </ul>
 */
public final class HudLog {

  public record Entry(long seq, String kind, String text) {}

  private static final int CAPACITY = 1000;
  private static final ArrayDeque<Entry> BUFFER = new ArrayDeque<>(CAPACITY);
  private static long nextSeq = 0;

  private HudLog() {}

  static void register() {
    ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, ts) ->
      push("chat", message.getString()));
    ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
      if (!overlay) push("system", message.getString()); // overlay comes via the Gui mixin
    });
  }

  /** Mixin + event feed point. No-op unless the driver is enabled (release safety). */
  public static void push(String kind, String text) {
    if (!TestDriverClient.isEnabled()) return;
    synchronized (BUFFER) {
      if (BUFFER.size() >= CAPACITY) BUFFER.pollFirst();
      BUFFER.addLast(new Entry(nextSeq++, kind, text));
    }
  }

  /** Entries with seq > since (pass -1 for everything retained). */
  public static List<Entry> since(long since) {
    synchronized (BUFFER) {
      List<Entry> out = new ArrayList<>();
      for (Entry e : BUFFER) {
        if (e.seq() > since) out.add(e);
      }
      return out;
    }
  }
}

package com.thecompanyinc.cobblemoninitiative.questtrack;

import java.util.ArrayDeque;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

/**
 * CLIENT-ONLY session record of every title/subtitle card that has flashed on screen —
 * liberation ceremonies, GRID ONLINE, badge moments, cutscene cards — feeding the Quest
 * Log's RECORD tab so stream viewers can review moments they missed. Fed by
 * GuiTitleRecordMixin (the Gui setters are the single funnel both packet paths share).
 *
 * <p><b>Pairing model:</b> a datapack card is {@code title ... subtitle} THEN
 * {@code title ... title} — vanilla sets the subtitle before the title fires. So
 * {@link #onSubtitle} only buffers; {@link #onTitle} commits a record, adopting the
 * buffered subtitle iff it is fresh (see {@link #PAIR_WINDOW_TICKS}).
 *
 * <p><b>Session record by design — the stream is the archive.</b> No persistence: the
 * ring lives in memory and clears on disconnect so a new world starts clean.
 *
 * <p>Client-thread only, no synchronization: the Gui setters run on the client thread
 * (title packets are marshalled there) and the screen renders there too.
 */
public final class EventRecordLog {

  /** Ring capacity — the last 100 cards; a full stream session with headroom. */
  private static final int CAPACITY = 100;

  /**
   * Max age (client ticks, ~3s) a buffered subtitle may have and still pair with an
   * incoming title. Stale-subtitle guard: a later BARE title (e.g. a lone "DAY 2"
   * card) must not adopt an old card's subtitle that happened to linger in the buffer.
   */
  private static final int PAIR_WINDOW_TICKS = 60;

  /** Newest-FIRST ring buffer — {@code addFirst} on capture, trimmed at the tail. */
  private static final ArrayDeque<Entry> RECORDS = new ArrayDeque<>();

  /** The subtitle capture buffer (see class comment): last subtitle + its client tick. */
  private static Component bufferedSubtitle;
  private static int bufferedSubtitleTick;

  private EventRecordLog() {}

  public static void init() {
    // Session-only by design (see class comment): clear on disconnect so a new
    // world/server starts with a blank record. NuzlockeClientInit precedent.
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
  }

  /** Buffer only — the paired title, if any, is about to follow (class comment). */
  public static void onSubtitle(Component subtitle, int clientTick) {
    if (subtitle == null) return;
    bufferedSubtitle = subtitle;
    bufferedSubtitleTick = clientTick;
  }

  /** Commit point: pair with the (fresh) buffered subtitle, noise-filter, record. */
  public static void onTitle(Component title, int clientTick) {
    if (title == null) return;

    // Adopt the buffered subtitle ONLY inside the freshness window; either way the
    // buffer is cleared — a title event ENDS any pairing window, consumed or stale,
    // so one subtitle can never attach to two titles.
    Component subtitle = null;
    if (
      bufferedSubtitle != null &&
      clientTick - bufferedSubtitleTick <= PAIR_WINDOW_TICKS
    ) {
      subtitle = bufferedSubtitle;
    }
    bufferedSubtitle = null;

    // ── noise filter ──────────────────────────────────────────────────────────
    String plain = title.getString().strip();
    // Rule 1: empty/whitespace-only titles are screen-clears and spacers, not moments.
    if (plain.isEmpty()) return;
    // Rule 2a: fewer than 3 plain chars = stray glyphs/punctuation, never a real card.
    if (plain.length() < 3) return;
    // Rule 2b: bare integers are countdown digits from timed trials ("3", "2", "300").
    if (plain.matches("[+-]?\\d+")) return;
    // (Actionbars are filtered at the SOURCE — GuiTitleRecordMixin never captures
    // setOverlayMessage: fees/hints would bury the actual moments.)

    // Rule 3: identical title+subtitle to the previous record = a re-firing timer /
    // refresh loop — collapse into the newest entry's xN counter instead of a new row.
    String subtitlePlain = subtitle == null ? "" : subtitle.getString();
    Entry newest = RECORDS.peekFirst();
    if (
      newest != null &&
      newest.titlePlain.equals(title.getString()) &&
      newest.subtitlePlain.equals(subtitlePlain)
    ) {
      newest.count++;
      return;
    }

    RECORDS.addFirst(new Entry(title, subtitle, currentWorldDay()));
    while (RECORDS.size() > CAPACITY) RECORDS.removeLast();
  }

  /** Newest first — exactly the RECORD tab's draw order. Copied (≤100 refs, per-frame
   *  copy is cheap) so a mid-iteration capture can never bite the render loop. */
  public static List<Entry> newestFirst() {
    return List.copyOf(RECORDS);
  }

  public static void clear() {
    RECORDS.clear();
    bufferedSubtitle = null;
  }

  /** In-world day of capture: dayTime/24000 is 0-based, the stream counts from Day 1. */
  private static long currentWorldDay() {
    ClientLevel level = Minecraft.getInstance().level;
    return level == null ? 1 : level.getDayTime() / 24000L + 1;
  }

  /**
   * One recorded card. Components are kept verbatim — their colors/styling arrived
   * with the packet and the RECORD tab re-renders them as seen. {@code count} mutates
   * in place for the xN collapse; the plain strings are precomputed for that compare.
   */
  public static final class Entry {

    private final Component title;
    private final Component subtitle; // null = bare title card
    private final long worldDay;
    private final String titlePlain;
    private final String subtitlePlain;
    private int count = 1;

    Entry(Component title, Component subtitle, long worldDay) {
      this.title = title;
      this.subtitle = subtitle;
      this.worldDay = worldDay;
      this.titlePlain = title.getString();
      this.subtitlePlain = subtitle == null ? "" : subtitle.getString();
    }

    public Component title() {
      return title;
    }

    /** Nullable — a bare title card has no subtitle line. */
    public Component subtitle() {
      return subtitle;
    }

    public long worldDay() {
      return worldDay;
    }

    /** How many consecutive times this exact card fired (timer re-fires collapse). */
    public int count() {
      return count;
    }
  }
}

package com.thecompanyinc.cobblemoninitiative.screen;

import com.thecompanyinc.cobblemoninitiative.questtrack.EventRecordLog;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestLogClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import org.lwjgl.glfw.GLFW;

/**
 * The Quest Log — a Shift+T review overlay for the stream: the runner opens it so viewers
 * can read the full quest ledger, the recovered-memory count, and the dex tally at their
 * own pace. CLIENT-ONLY and read-only: everything on it is already synced through the
 * vanilla scoreboard (1.20.3+ score sync carries each line's per-score display component),
 * so there are no packets and no server half — this screen just re-renders what the
 * sidebar already knows. Flat corporate draw-call style (PhoneCallScreen house style).
 *
 * <p>Data contract (ENGINE_FINDINGS §sidebar — never re-derive quest conditions):
 * the active-quest list is every {@code ci_quest} holder WITH a score, excluding
 * {@code #}-prefixed scratch holders, sorted score DESC (q.main=100 first, then the
 * side slots) — identical order to the sidebar. Each line renders the synced display
 * component verbatim: gold "▶ …" main, gray "• …" sides, and the tracker's aqua ▶
 * re-style included for free, since QuestTrackManager edits that same display.
 *
 * <p>Two tabs since 0.7.0-alpha.20 — <b>LEDGER</b> (the quest view above, the default)
 * and <b>RECORD</b>, a newest-first session history of every title/subtitle card that
 * has flashed on screen (fed by GuiTitleRecordMixin → {@link EventRecordLog}), so
 * viewers can review liberation ceremonies / GRID ONLINE / badge moments they missed.
 * Click the tab labels or cycle with the Tab key.
 */
public class QuestLogScreen extends Screen {

  private static final String QUEST_OBJECTIVE = "ci_quest";
  private static final String MAIN_HOLDER = "q.main";
  private static final String MEMORY_OBJECTIVE = "memory_fragment";
  private static final String DEX_OBJECTIVE = "dex_caught";
  private static final int MEMORY_MAX = 10;

  private static final int PANEL_W = 260;
  private static final int PAD = 10; // inner horizontal padding
  private static final int LINE_H = 10;
  private static final int MAIN_GAP = 4; // extra spacing under the q.main block
  private static final int QUEST_GAP = 2; // spacing between side-quest blocks
  private static final int TABS_H = 14; // LEDGER/RECORD tab row (label + underline)
  private static final int REC_DAY_H = 8; // 0.75-scaled "Day N" prefix line
  private static final int REC_DIVIDER_H = 7; // gap + 1px rule + gap between records

  // PhoneCallScreen palette — the flat corporate look.
  private static final int FRAME = 0xFF15151A;
  private static final int FRAME_EDGE = 0xFF3A3A44;
  private static final int INSET = 0xFF23232B;
  private static final int TEXT_MAIN = 0xFFE8E8EE;
  private static final int TEXT_DIM = 0xFF9A9AA6;
  private static final int TEXT_FAINT = 0xFF66666F;
  private static final int ACCENT_GOLD = 0xFFE0A93E; // filled memory pips + header rule
  private static final int PIP_HOLLOW = 0xFF4A4A55;

  private double scrollY;
  private int maxScroll; // recomputed every frame from the live line list
  private int ticksOpen;

  /** Header tabs. LEDGER is the default on open — the RECORD tab is the review extra. */
  private enum Tab {
    LEDGER,
    RECORD,
  }

  private Tab tab = Tab.LEDGER;

  // Tab-label hit rects, cached by render each frame (immediate-mode hit testing —
  // no widgets on this screen, mouseClicked tests against last frame's layout).
  private int tabsLabelY;
  private int ledgerTabX;
  private int ledgerTabW;
  private int recordTabX;
  private int recordTabW;

  public QuestLogScreen() {
    super(Component.literal("Quest Log"));
  }

  /**
   * NON-pausing on purpose: this is a review surface the streamer leaves up while
   * talking to chat — the world stays live behind the dark gradient (PokeballDeathScreen
   * precedent: pausing the integrated server also freezes Cobblemon's showdown backlog,
   * and a paused stream frame reads as a crash to viewers).
   */
  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void tick() {
    ticksOpen++;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    // Tab cycles LEDGER ⇄ RECORD (noted in the footer). Intercepted before super so
    // vanilla's Tab focus-traversal never sees it (this screen has no widgets anyway).
    if (keyCode == GLFW.GLFW_KEY_TAB) {
      switchTab(tab == Tab.LEDGER ? Tab.RECORD : Tab.LEDGER);
      return true;
    }
    // Shift + the bound key closes the log again (the toggle half of the chord). The
    // few-tick grace swallows GLFW key-repeat from the opening press still being held —
    // without it a slightly long Shift+T would open and instantly re-close. ESC closes
    // via the default shouldCloseOnEsc path.
    KeyMapping key = QuestLogClient.keyMapping();
    if (
      key != null &&
      key.matches(keyCode, scanCode) &&
      Screen.hasShiftDown() &&
      ticksOpen > 5
    ) {
      onClose();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  /** Clickable header tabs — hit-tested against the label rects render cached. */
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0 && mouseY >= tabsLabelY - 2 && mouseY < tabsLabelY + 12) {
      if (mouseX >= ledgerTabX && mouseX < ledgerTabX + ledgerTabW) {
        switchTab(Tab.LEDGER);
        return true;
      }
      if (mouseX >= recordTabX && mouseX < recordTabX + recordTabW) {
        switchTab(Tab.RECORD);
        return true;
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void switchTab(Tab target) {
    if (tab != target) {
      tab = target;
      scrollY = 0; // unrelated lists — each tab opens at its own top
    }
  }

  @Override
  public boolean mouseScrolled(
    double mouseX,
    double mouseY,
    double horizontal,
    double vertical
  ) {
    if (maxScroll > 0) {
      scrollY = clamp(scrollY - vertical * LINE_H, 0, maxScroll);
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
  }

  /**
   * No-op on purpose (PokeballDeathScreen precedent): the translucent gradient is drawn
   * FIRST in {@link #render} instead, because the default {@code super.render()} calls
   * renderBackground and would repaint the dim OVER the panel.
   */
  @Override
  public void renderBackground(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {}

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Translucent dim, world visible behind — PokeballDeathScreen's gradient style.
    graphics.fillGradient(0, 0, width, height, 0x90000008, 0xC8000008);

    Objective objective = questObjective();
    List<QuestBlock> blocks = tab == Tab.LEDGER ? buildBlocks(objective) : List.of();
    List<RecordBlock> records = tab == Tab.RECORD ? buildRecords() : List.of();
    // The record empty-state wraps (wider than the panel), so it's split like content.
    List<FormattedCharSequence> recordEmpty = tab == Tab.RECORD && records.isEmpty()
      ? font.split(
        Component.literal("Nothing on the record yet. Make some history."),
        PANEL_W - PAD * 2)
      : List.of();

    // ── layout ────────────────────────────────────────────────────────────────
    int contentH = 0;
    if (tab == Tab.LEDGER) {
      for (QuestBlock block : blocks) {
        contentH += block.lines.size() * LINE_H + (block.main ? MAIN_GAP : QUEST_GAP);
      }
      if (blocks.isEmpty()) contentH = LINE_H; // the empty-state line
    } else {
      for (int i = 0; i < records.size(); i++) {
        RecordBlock rec = records.get(i);
        contentH += REC_DAY_H +
        (rec.titleLines.size() + rec.subtitleLines.size()) * LINE_H;
        if (i < records.size() - 1) contentH += REC_DIVIDER_H;
      }
      if (records.isEmpty()) contentH = recordEmpty.size() * LINE_H;
    }

    int headerH = 22; // title + corporate underline rule
    int memoryH = 38; // label/count row + pips row + pokédex line
    int dividerH = 7;
    int footerH = 16;
    int chromeH = 8 + headerH + TABS_H + memoryH + dividerH + footerH + 6;

    int panelH = Math.min(chromeH + contentH + 4, height - 24); // height-clamped
    int viewH = panelH - chromeH - 4;
    maxScroll = Math.max(0, contentH - viewH);
    scrollY = clamp(scrollY, 0, maxScroll);

    int px = (width - PANEL_W) / 2;
    int py = (height - panelH) / 2;
    int left = px + PAD;
    int innerW = PANEL_W - PAD * 2;

    graphics.fill(px, py, px + PANEL_W, py + panelH, FRAME);
    graphics.renderOutline(px, py, PANEL_W, panelH, FRAME_EDGE);

    // ── header: objective display name (synced) if present, else QUEST LOG ────
    int y = py + 8;
    Component title = objective != null
      ? objective.getDisplayName()
      : Component.literal("QUEST LOG");
    graphics.drawString(font, title, left, y, TEXT_MAIN, false);
    graphics.fill(left, y + 11, px + PANEL_W - PAD, y + 12, ACCENT_GOLD);
    y += headerH;

    // ── tabs: LEDGER | RECORD — flat corporate labels, gold underline = active ─
    tabsLabelY = y;
    ledgerTabX = left;
    ledgerTabW = font.width("LEDGER");
    recordTabX = left + ledgerTabW + 16;
    recordTabW = font.width("RECORD");
    drawTab(graphics, "LEDGER", ledgerTabX, y, tab == Tab.LEDGER);
    drawTab(graphics, "RECORD", recordTabX, y, tab == Tab.RECORD);
    y += TABS_H;

    // ── memory row: 10 pips off the local memory_fragment score ───────────────
    int memories = clampInt(localScore(MEMORY_OBJECTIVE), 0, MEMORY_MAX);
    graphics.drawString(font, "RECOVERED MEMORY", left, y, TEXT_DIM, false);
    String count = memories + " / " + MEMORY_MAX;
    graphics.drawString(
      font, count, px + PANEL_W - PAD - font.width(count), y, TEXT_MAIN, false);
    int pipY = y + 11;
    for (int i = 0; i < MEMORY_MAX; i++) {
      boolean filled = i < memories;
      graphics.drawString(
        font,
        filled ? "◆" : "◇",
        left + i * 11,
        pipY,
        filled ? ACCENT_GOLD : PIP_HOLLOW,
        false
      );
    }
    // Smaller gray dex tally (pose-scaled — the font has no small size).
    graphics.pose().pushPose();
    graphics.pose().translate(left, y + 24, 0);
    graphics.pose().scale(0.75f, 0.75f, 1.0f);
    graphics.drawString(
      font,
      "Pokédex: " + localScore(DEX_OBJECTIVE) + " registered",
      0, 0, TEXT_DIM, false);
    graphics.pose().popPose();
    y += memoryH;

    graphics.fill(left, y + 2, px + PANEL_W - PAD, y + 3, FRAME_EDGE);
    y += dividerH;

    // ── quest lines: scissored, wheel-scrolled region ─────────────────────────
    int contentTop = y;
    graphics.fill(px + 2, contentTop - 2, px + PANEL_W - 2, contentTop + viewH + 2, INSET);
    graphics.enableScissor(px + 2, contentTop, px + PANEL_W - 2, contentTop + viewH);
    int lineY = contentTop - (int) scrollY;
    if (tab == Tab.LEDGER) {
      if (blocks.isEmpty()) {
        graphics.drawString(
          font, "The ledger is blank. For now.", left, lineY, TEXT_DIM, false);
      } else {
        for (QuestBlock block : blocks) {
          for (FormattedCharSequence line : block.lines) {
            graphics.drawString(font, line, left, lineY, TEXT_MAIN, false);
            lineY += LINE_H;
          }
          lineY += block.main ? MAIN_GAP : QUEST_GAP;
        }
      }
    } else if (records.isEmpty()) {
      for (FormattedCharSequence line : recordEmpty) {
        graphics.drawString(font, line, left, lineY, TEXT_DIM, false);
        lineY += LINE_H;
      }
    } else {
      for (int i = 0; i < records.size(); i++) {
        RecordBlock rec = records.get(i);
        // Small "Day N" prefix (0.75 pose-scale, the dex-line idiom — the font has
        // no small size); the xN repeat counter rides the same mini-line, right-aligned.
        graphics.pose().pushPose();
        graphics.pose().translate(left, lineY, 0);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);
        graphics.drawString(font, rec.dayPrefix, 0, 0, TEXT_FAINT, false);
        if (rec.repeat != null) {
          graphics.drawString(
            font,
            rec.repeat,
            (int) ((PANEL_W - PAD * 2) / 0.75f) - font.width(rec.repeat),
            0,
            TEXT_DIM,
            false
          );
        }
        graphics.pose().popPose();
        lineY += REC_DAY_H;
        // Title verbatim — its colors/styling arrived with the packet; TEXT_MAIN is
        // only the base for unstyled runs.
        for (FormattedCharSequence line : rec.titleLines) {
          graphics.drawString(font, line, left, lineY, TEXT_MAIN, false);
          lineY += LINE_H;
        }
        // Subtitle slightly dimmed (base color — packet styling still wins where set).
        for (FormattedCharSequence line : rec.subtitleLines) {
          graphics.drawString(font, line, left, lineY, TEXT_DIM, false);
          lineY += LINE_H;
        }
        if (i < records.size() - 1) {
          // Thin divider between entries.
          graphics.fill(left, lineY + 2, px + PANEL_W - PAD, lineY + 3, FRAME_EDGE);
          lineY += REC_DIVIDER_H;
        }
      }
    }
    graphics.disableScissor();

    // Slim scroll thumb on the panel's right edge when the list overflows.
    if (maxScroll > 0) {
      int trackH = viewH;
      int thumbH = Math.max(8, trackH * viewH / contentH);
      int thumbY = contentTop + (int) ((trackH - thumbH) * (scrollY / maxScroll));
      graphics.fill(px + PANEL_W - 4, thumbY, px + PANEL_W - 2, thumbY + thumbH, FRAME_EDGE);
    }

    // ── footer hint (per tab — Tab-cycling is noted here) ─────────────────────
    KeyMapping key = QuestLogClient.keyMapping();
    String keyName = key != null ? key.getTranslatedKeyMessage().getString() : "T";
    String hint = tab == Tab.LEDGER
      ? "[ / ] track · Tab switch · Shift+" + keyName + " close"
      : "Tab switch · Shift+" + keyName + " close";
    graphics.drawCenteredString(
      font, hint, px + PANEL_W / 2, py + panelH - footerH + 2, TEXT_FAINT);

    super.render(graphics, mouseX, mouseY, delta); // widgets (none) + narration
  }

  /** One flat tab label: active = light text + accent-gold underline, inactive = dark gray. */
  private void drawTab(GuiGraphics graphics, String label, int x, int y, boolean active) {
    graphics.drawString(font, label, x, y, active ? TEXT_MAIN : TEXT_FAINT, false);
    if (active) {
      graphics.fill(x, y + 10, x + font.width(label), y + 11, ACCENT_GOLD);
    }
  }

  /**
   * RECORD tab blocks off {@link EventRecordLog#newestFirst()} (already newest first).
   * Components split verbatim so packet styling survives the wrap; rebuilt per frame
   * like the quest blocks — the ring caps at 100, the split is cheap.
   */
  private List<RecordBlock> buildRecords() {
    List<RecordBlock> out = new ArrayList<>();
    int innerW = PANEL_W - PAD * 2;
    for (EventRecordLog.Entry entry : EventRecordLog.newestFirst()) {
      out.add(
        new RecordBlock(
          "Day " + entry.worldDay(),
          entry.count() > 1 ? "x" + entry.count() : null,
          font.split(entry.title(), innerW),
          entry.subtitle() != null
            ? font.split(entry.subtitle(), innerW)
            : List.<FormattedCharSequence>of()
        )
      );
    }
    return out;
  }

  // ── scoreboard reads (client-side; everything below is vanilla-synced) ──────

  private Objective questObjective() {
    Minecraft mc = this.minecraft;
    if (mc == null || mc.level == null) return null;
    return mc.level.getScoreboard().getObjective(QUEST_OBJECTIVE);
  }

  /**
   * The active list, sidebar-identical: every non-scratch holder with a ci_quest score,
   * score DESC. {@link PlayerScoreEntry#isHidden()} IS the {@code #}-prefix scratch
   * check (bytecode: {@code owner.startsWith("#")}), and {@link PlayerScoreEntry#ownerName()}
   * is exactly "synced display component, else literal raw holder name". Rebuilt per
   * frame so live macro numbers (and the tracker's aqua ▶) stay current; the list is a
   * handful of lines, the split is cheap.
   */
  private List<QuestBlock> buildBlocks(Objective objective) {
    List<QuestBlock> blocks = new ArrayList<>();
    Minecraft mc = this.minecraft;
    if (objective == null || mc == null || mc.level == null) return blocks;

    Scoreboard scoreboard = mc.level.getScoreboard();
    List<PlayerScoreEntry> entries = new ArrayList<>();
    for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
      if (!entry.isHidden()) entries.add(entry);
    }
    entries.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());

    for (PlayerScoreEntry entry : entries) {
      blocks.add(
        new QuestBlock(
          MAIN_HOLDER.equals(entry.owner()),
          font.split(entry.ownerName(), PANEL_W - PAD * 2)
        )
      );
    }
    return blocks;
  }

  /** The LOCAL player's score on an objective; missing objective/score = 0. */
  private int localScore(String objectiveName) {
    Minecraft mc = this.minecraft;
    if (mc == null || mc.level == null || mc.player == null) return 0;
    Scoreboard scoreboard = mc.level.getScoreboard();
    Objective objective = scoreboard.getObjective(objectiveName);
    if (objective == null) return 0;
    // Entity implements ScoreHolder (1.20.3+), and score sync keys on the profile name —
    // the local player resolves its own synced scores directly.
    ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(mc.player, objective);
    return info == null ? 0 : info.value();
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private static int clampInt(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  /** One quest's wrapped display lines; main = the q.main block (wider trailing gap). */
  private record QuestBlock(boolean main, List<FormattedCharSequence> lines) {}

  /** One recorded card, pre-wrapped; repeat = the small "xN" counter, null when 1. */
  private record RecordBlock(
    String dayPrefix,
    String repeat,
    List<FormattedCharSequence> titleLines,
    List<FormattedCharSequence> subtitleLines
  ) {}
}

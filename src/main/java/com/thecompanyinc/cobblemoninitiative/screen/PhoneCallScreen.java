package com.thecompanyinc.cobblemoninitiative.screen;

import com.thecompanyinc.cobblemoninitiative.phone.PhoneCallClient;
import com.thecompanyinc.cobblemoninitiative.phone.PhoneCallScripts;
import com.thecompanyinc.cobblemoninitiative.phone.PhonePayloads;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

/**
 * The PokePhone — a full-screen smartphone over a dimmed vignette (0.7.0-alpha.20,
 * replacing the invisible-caller Easy NPC dialog delivery). Two views: the INCOMING CALL
 * splash (avatar + ACCEPT/DECLINE) and the conversation (typewriter pages, then choices
 * or END CALL). All draw-call rendering, no textures (PokeballDeathScreen house style).
 *
 * <p>Opened locally at the SPLASH from the answer keybind using the ring-offer data; the
 * full script only round-trips once ACCEPT is clicked (a decline never ships pages).
 */
public class PhoneCallScreen extends Screen {

  private enum View { SPLASH, CALL }

  private static final int PHONE_W = 200;
  private static final int PHONE_H = 270;
  private static final int FRAME = 0xFF15151A;
  private static final int FRAME_EDGE = 0xFF3A3A44;
  private static final int INSET = 0xFF23232B;
  private static final int TEXT_MAIN = 0xFFE8E8EE;
  private static final int TEXT_DIM = 0xFF9A9AA6;
  private static final int REVEAL_CHARS_PER_TICK = 2;

  private final String callId;
  private final String caller;
  private final String subtitle;
  private final boolean unknownCaller;
  private final int accent; // ARGB

  private View view = View.SPLASH;
  private List<String> pages = List.of();
  private List<String> choiceLabels = List.of();
  private int pageIndex;
  private int revealChars;
  private boolean lastPageButtonsBuilt;
  private boolean answerSent;
  private boolean terminalSent;
  private int ticks;
  private int callTicks;
  private final Random glitch = new Random();

  public PhoneCallScreen(
    String callId, String caller, String subtitle, String avatar, int accentRgb
  ) {
    super(Component.literal("PokePhone"));
    this.callId = callId;
    this.caller = caller;
    this.subtitle = subtitle;
    this.unknownCaller = PhoneCallScripts.AVATAR_UNKNOWN.equals(avatar);
    this.accent = 0xFF000000 | accentRgb;
  }

  public String callId() {
    return callId;
  }

  /**
   * Answering is a deliberate act, the server's in-battle ring guard means no battle can be
   * live under this screen, and pausing is the hardcore-safe choice — nothing in the world
   * moves while the player reads. The C2S sends all fire on click BEFORE the screen closes,
   * so they flush the moment the integrated server unpauses.
   */
  @Override
  public boolean isPauseScreen() {
    return true;
  }

  /** ESC = ABORT: the server requeues the call (owed-call pattern) — never a silent burn. */
  @Override
  public void onClose() {
    sendTerminal(PhonePayloads.PhoneActionPayload.ACTION_ABORT, 0);
    super.onClose();
  }

  @Override
  public void removed() {
    // A screen replaced/dismissed from outside (disconnect teardown, another modal) is
    // also "without completing" — same ABORT requeue. No-op after any terminal send.
    sendTerminal(PhonePayloads.PhoneActionPayload.ACTION_ABORT, 0);
    super.removed();
  }

  /** Server confirmed the answer — swap the splash for the conversation view. */
  public void openConversation(List<String> pages, List<String> choiceLabels) {
    this.view = View.CALL;
    this.pages = pages;
    this.choiceLabels = choiceLabels;
    this.pageIndex = 0;
    this.revealChars = 0;
    this.callTicks = 0;
    this.lastPageButtonsBuilt = false;
    rebuildButtons();
  }

  @Override
  protected void init() {
    rebuildButtons();
  }

  @Override
  public void tick() {
    ticks++;
    if (view != View.CALL || pages.isEmpty()) return;
    callTicks++;
    int len = pages.get(pageIndex).length();
    if (revealChars < len) {
      int before = revealChars;
      revealChars = Math.min(len, revealChars + REVEAL_CHARS_PER_TICK);
      if (revealChars / 6 != before / 6) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
          SoundEvents.NOTE_BLOCK_HAT.value(),
          1.5f + (revealChars % 3) * 0.12f, 0.25f));
      }
    }
    if (revealChars >= len && isLastPage() && !lastPageButtonsBuilt) {
      rebuildButtons(); // the choice/END CALL row appears once the page finishes
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) return true;
    // Click anywhere = reveal the full page at once.
    if (view == View.CALL && !pages.isEmpty() && revealChars < pages.get(pageIndex).length()) {
      revealChars = pages.get(pageIndex).length();
      if (isLastPage() && !lastPageButtonsBuilt) rebuildButtons();
      return true;
    }
    return false;
  }

  // ── layout ───────────────────────────────────────────────────────────────────────

  private int phoneH() {
    return Math.min(PHONE_H, height - 10);
  }

  private int phoneX() {
    return (width - PHONE_W) / 2;
  }

  private int phoneY() {
    return (height - phoneH()) / 2;
  }

  private int screenX() {
    return phoneX() + 7;
  }

  private int screenY() {
    return phoneY() + 9;
  }

  private int screenW() {
    return PHONE_W - 14;
  }

  private int screenH() {
    return phoneH() - 18;
  }

  private boolean isLastPage() {
    return pageIndex >= pages.size() - 1;
  }

  // ── buttons ──────────────────────────────────────────────────────────────────────

  private void rebuildButtons() {
    clearWidgets();
    int sx = screenX();
    int sw = screenW();
    int bottom = screenY() + screenH();
    if (view == View.SPLASH) {
      int bw = (sw - 12) / 2;
      FlatButton accept = new FlatButton(
        sx + 4, bottom - 26, bw, 20, Component.literal("ACCEPT"),
        0xFF1E6B34, 0xFF2A8A46, 0xFFDFFFE6, this::onAccept);
      FlatButton declineBtn = new FlatButton(
        sx + 8 + bw, bottom - 26, bw, 20, Component.literal("DECLINE"),
        0xFF6B1E1E, 0xFF8A2A2A, 0xFFFFE0E0, this::onDecline);
      accept.active = !answerSent;
      declineBtn.active = !answerSent;
      addRenderableWidget(accept);
      addRenderableWidget(declineBtn);
      return;
    }
    if (pages.isEmpty()) return;
    if (!isLastPage()) {
      addRenderableWidget(new FlatButton(
        sx + sw - 32, bottom - 20, 28, 16, Component.literal("▸"),
        0xFF2A2A32, 0xFF3A3A46, TEXT_MAIN, this::advancePage));
      return;
    }
    // Last page: choices (or END CALL) appear once the typewriter finishes.
    if (revealChars < pages.get(pageIndex).length()) return;
    lastPageButtonsBuilt = true;
    if (choiceLabels.isEmpty()) {
      addRenderableWidget(new FlatButton(
        sx + 4, bottom - 22, sw - 8, 18, Component.literal("END CALL"),
        0xFF6B1E1E, 0xFF8A2A2A, 0xFFFFE0E0,
        () -> closeWith(PhonePayloads.PhoneActionPayload.ACTION_COMPLETE, 0)));
      return;
    }
    int rowH = 18;
    int y = bottom - 4 - choiceLabels.size() * (rowH + 2);
    for (int i = 0; i < choiceLabels.size(); i++) {
      final int idx = i;
      addRenderableWidget(new FlatButton(
        sx + 4, y + i * (rowH + 2), sw - 8, rowH, Component.literal(choiceLabels.get(i)),
        0xFF2A2A32, 0xFF3A3A46, TEXT_MAIN,
        () -> closeWith(PhonePayloads.PhoneActionPayload.ACTION_CHOOSE, idx)));
    }
  }

  private void onAccept() {
    if (answerSent) return;
    answerSent = true;
    PhoneCallClient.clearOffer();
    send(PhonePayloads.PhoneActionPayload.ACTION_ANSWER, 0);
    rebuildButtons(); // grey the pair out while the open payload is in flight
  }

  private void onDecline() {
    closeWith(PhonePayloads.PhoneActionPayload.ACTION_DECLINE, 0);
  }

  private void advancePage() {
    pageIndex++;
    revealChars = 0;
    lastPageButtonsBuilt = false;
    rebuildButtons();
  }

  private void closeWith(int action, int choice) {
    sendTerminal(action, choice);
    if (minecraft != null) minecraft.setScreen(null);
  }

  private void sendTerminal(int action, int choice) {
    if (terminalSent) return;
    terminalSent = true;
    PhoneCallClient.clearOffer();
    send(action, choice);
  }

  private void send(int action, int choice) {
    if (Minecraft.getInstance().getConnection() != null) {
      ClientPlayNetworking.send(
        new PhonePayloads.PhoneActionPayload(callId, action, choice));
    }
  }

  // ── rendering ────────────────────────────────────────────────────────────────────

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Dimmed vignette, world visible behind (PokeballDeathScreen's translucent style).
    graphics.fillGradient(0, 0, width, height, 0xA8000008, 0xD8000008);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    int px = phoneX();
    int py = phoneY();
    int ph = phoneH();
    int sx = screenX();
    int sy = screenY();
    int sw = screenW();
    int sh = screenH();

    // Phone body: dark frame, speaker slit, lighter screen inset.
    graphics.fill(px, py, px + PHONE_W, py + ph, FRAME);
    graphics.renderOutline(px, py, PHONE_W, ph, FRAME_EDGE);
    graphics.fill(px + PHONE_W / 2 - 18, py + 3, px + PHONE_W / 2 + 18, py + 5, FRAME_EDGE);
    graphics.fill(sx, sy, sx + sw, sy + sh, INSET);

    renderStatusBar(graphics, sx, sy, sw);
    if (view == View.SPLASH) {
      renderSplash(graphics, sx, sy, sw, sh);
    } else {
      renderConversation(graphics, sx, sy, sw, sh);
    }

    super.render(graphics, mouseX, mouseY, delta); // widgets on top
  }

  private void renderStatusBar(GuiGraphics graphics, int sx, int sy, int sw) {
    for (int i = 0; i < 4; i++) {
      int barH = 2 + i * 2;
      graphics.fill(sx + 4 + i * 3, sy + 11 - barH, sx + 6 + i * 3, sy + 11, 0xFFB9B9C4);
    }
    graphics.drawString(font, "The Company, Inc.", sx + 20, sy + 3, TEXT_DIM, false);
    if (view == View.CALL) {
      int seconds = callTicks / 20;
      String timer = String.format("%02d:%02d", seconds / 60, seconds % 60);
      graphics.drawString(font, timer, sx + sw - font.width(timer) - 4, sy + 3, TEXT_MAIN, false);
    }
    graphics.fill(sx, sy + 13, sx + sw, sy + 14, FRAME_EDGE);
  }

  private void renderSplash(GuiGraphics graphics, int sx, int sy, int sw, int sh) {
    int cx = sx + sw / 2;

    // Pulsing handset glyph.
    float pulse = 1.6f + 0.35f * (float) Math.sin(ticks * 0.35);
    graphics.pose().pushPose();
    graphics.pose().translate(cx, sy + 30, 0);
    graphics.pose().scale(pulse, pulse, 1.0f);
    graphics.drawCenteredString(font, "☎", 0, -4, accent);
    graphics.pose().popPose();
    graphics.drawCenteredString(font, "Incoming call", cx, sy + 44, TEXT_DIM);

    // Avatar disc.
    int ay = sy + 76;
    int radius = 20;
    int discColor = unknownCaller ? 0xFF2E2E38 : accent;
    for (int dy = -radius; dy <= radius; dy++) {
      int dx = (int) Math.sqrt(radius * radius - dy * dy);
      graphics.fill(cx - dx, ay + dy, cx + dx, ay + dy + 1, discColor);
    }
    if (unknownCaller) {
      // Glitch blocks — reseeded every few ticks so the static crawls.
      glitch.setSeed(ticks / 3L);
      String blocks = "▓▒░█";
      for (int i = 0; i < 5; i++) {
        String ch = String.valueOf(blocks.charAt(glitch.nextInt(blocks.length())));
        graphics.drawString(font,
          ch,
          cx - 10 + glitch.nextInt(16),
          ay - 12 + glitch.nextInt(20),
          0xFF8A8A98, false);
      }
    } else {
      String initial = caller.isEmpty() ? "?" : caller.substring(0, 1).toUpperCase();
      graphics.pose().pushPose();
      graphics.pose().translate(cx, ay, 0);
      graphics.pose().scale(2.0f, 2.0f, 1.0f);
      graphics.drawCenteredString(font, initial, 0, -4, 0xFFFFFFFF);
      graphics.pose().popPose();
    }

    // Caller name (NUMBER WITHHELD flickers for a masked caller) + subtitle.
    int nameY = ay + radius + 10;
    graphics.pose().pushPose();
    graphics.pose().translate(cx, nameY, 0);
    graphics.pose().scale(1.5f, 1.5f, 1.0f);
    if (unknownCaller) {
      boolean flick = ticks % 17 < 2;
      graphics.drawCenteredString(font, "NUMBER WITHHELD",
        flick ? 1 : 0, 0, flick ? 0xFF77778A : 0xFFDDDDE6);
    } else {
      graphics.drawCenteredString(font, caller, 0, 0, TEXT_MAIN);
    }
    graphics.pose().popPose();
    if (!subtitle.isEmpty()) {
      graphics.drawCenteredString(font, subtitle, cx, nameY + 18, TEXT_DIM);
    }
    if (answerSent) {
      graphics.drawCenteredString(font, "Connecting…", cx, sy + sh - 40, TEXT_DIM);
    }
  }

  private void renderConversation(GuiGraphics graphics, int sx, int sy, int sw, int sh) {
    int cx = sx + sw / 2;
    String name = unknownCaller ? "NUMBER WITHHELD" : caller;
    graphics.drawCenteredString(font, name, cx, sy + 18, TEXT_MAIN);
    graphics.fill(cx - 24, sy + 28, cx + 24, sy + 29, accent);

    if (pages.isEmpty()) return;
    String visible = pages.get(pageIndex).substring(0, revealChars);
    int textTop = sy + 34;
    int textBottom = sy + sh - 30 - (isLastPage() && !choiceLabels.isEmpty()
      ? choiceLabels.size() * 20 : 0);
    List<FormattedCharSequence> lines = font.split(FormattedText.of(visible), sw - 12);
    // Auto-scroll: keep the newest typewriter line in view when a page overruns the area.
    int areaH = textBottom - textTop;
    int overflow = Math.max(0, lines.size() * 10 - areaH);
    graphics.enableScissor(sx + 2, textTop, sx + sw - 2, textBottom);
    int y = textTop - overflow;
    for (FormattedCharSequence line : lines) {
      graphics.drawString(font, line, sx + 6, y, TEXT_MAIN, false);
      y += 10;
    }
    graphics.disableScissor();

    // Page-dot indicator.
    int dots = pages.size();
    int dotY = sy + sh - 28;
    int startX = cx - (dots * 8 - 4) / 2;
    for (int i = 0; i < dots; i++) {
      graphics.fill(startX + i * 8, dotY, startX + i * 8 + 4, dotY + 4,
        i == pageIndex ? accent : 0xFF4A4A55);
    }
  }

  /** Flat draw-call button (no textures) — the phone's dark UI style. */
  private class FlatButton extends AbstractButton {

    private final int base;
    private final int hover;
    private final int textColor;
    private final Runnable action;

    FlatButton(
      int x, int y, int w, int h, Component label,
      int base, int hover, int textColor, Runnable action
    ) {
      super(x, y, w, h, label);
      this.base = base;
      this.hover = hover;
      this.textColor = textColor;
      this.action = action;
    }

    @Override
    public void onPress() {
      action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
      graphics.fill(getX(), getY(), getX() + width, getY() + height,
        active && isHoveredOrFocused() ? hover : base);
      graphics.renderOutline(getX(), getY(), width, height, FRAME_EDGE);
      graphics.drawCenteredString(font, getMessage(),
        getX() + width / 2, getY() + (height - 8) / 2, active ? textColor : 0xFF777788);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
      defaultButtonNarrationText(output);
    }
  }
}

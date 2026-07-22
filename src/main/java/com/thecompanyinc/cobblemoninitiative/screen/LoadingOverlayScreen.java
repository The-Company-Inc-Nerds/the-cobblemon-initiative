package com.thecompanyinc.cobblemoninitiative.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The first-join install loading overlay: a black screen with a single amber progress bar,
 * shown while the pack auto-installs (world provisioning + chunk streaming) BEFORE the opening
 * cutscene. Server-driven via {@code InstallOverlayPayload} — {@link #markDone()} fills the bar
 * on the "done" packet, and AutoInstall dismisses the screen the same tick it plays the opening
 * cutscene, so the title card lands on a clean handoff instead of over a half-streamed town.
 *
 * <p>{@link #isPauseScreen()} is {@code false} ON PURPOSE: in single-player a pause screen halts
 * the integrated server, which would freeze the very install this screen is waiting on. It also
 * swallows input (a Screen suppresses movement) so the player can't walk blind behind the black.
 *
 * <p>Deliberately withholds the bright title — that reveal belongs to the opening cutscene's
 * {@code startTitle}. Here the brand is a muted grey ledger line, matching the cold-open tone.
 */
public class LoadingOverlayScreen extends Screen {

  /** Ticks the bar eases across before it parks at {@link #HOLD_PROGRESS} awaiting "done".
   * Roughly the AutoInstall settle window (140t) so the bar reads as real world-loading. */
  private static final int ANIM_TICKS = 130;
  /** The bar waits here until the server confirms the install finished — a real loading feel,
   * never a bar that hits 100% and then just sits there. */
  private static final float HOLD_PROGRESS = 0.9f;
  /** Failsafe: if the "close" packet never arrives (server crash mid-install), dismiss anyway
   * so the player is never trapped staring at a black screen. Generous (120s) so a slow cold-disk
   * first-gen install — which keeps the live server ticking and WILL send "close" eventually —
   * completes before the failsafe blanks the overlay to a half-streamed world; the failsafe only
   * really matters if the integrated server is truly dead, in which case the SP game is frozen
   * anyway and dismissing to black-then-menu is the right outcome. */
  private static final int MAX_TICKS = 20 * 120; // 120s

  private static final int COL_BLACK = 0xFF000000;
  private static final int COL_BAR_BORDER = 0xFF3A3A3A;
  private static final int COL_BAR_TRACK = 0xFF141414;
  private static final int COL_BAR_FILL = 0xFFE8B84B; // ledger amber
  private static final int COL_BRAND = 0xFF9A9A9A;
  private static final int COL_LABEL = 0xFF6E6E6E;

  private int ticks = 0;
  private boolean done = false;
  private float target = 0f;
  private float display = 0f;

  public LoadingOverlayScreen() {
    super(Component.literal("Provisioning"));
  }

  /** Server confirmed the install finished — fill to 100% and hold until "close". */
  public void markDone() {
    this.done = true;
  }

  @Override
  public void tick() {
    ticks++;
    if (done) {
      target = 1.0f;
    } else {
      float f = Math.min(1f, ticks / (float) ANIM_TICKS);
      // easeOutCubic, capped below 1.0 so it visibly waits on the server's "done".
      target = HOLD_PROGRESS * (1f - (float) Math.pow(1f - f, 3));
    }
    if (ticks > MAX_TICKS && this.minecraft != null) {
      this.minecraft.setScreen(null);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Ease the drawn value toward the target every frame for a smooth fill.
    display += (target - display) * Math.min(1f, partialTick * 0.5f + 0.15f);
    display = Mth.clamp(display, 0f, 1f);

    graphics.fill(0, 0, this.width, this.height, COL_BLACK);

    int cx = this.width / 2;
    int barW = Math.min(300, this.width - 80);
    int barH = 6;
    int bx = cx - barW / 2;
    int by = this.height / 2 + 8;

    graphics.drawCenteredString(this.font,
      Component.literal("§8THE COBBLEMON INITIATIVE"), cx, by - 26, COL_BRAND);

    graphics.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, COL_BAR_BORDER);
    graphics.fill(bx, by, bx + barW, by + barH, COL_BAR_TRACK);
    int fillW = (int) (barW * display);
    if (fillW > 0) graphics.fill(bx, by, bx + fillW, by + barH, COL_BAR_FILL);

    String pct = (int) (display * 100f) + "%";
    graphics.drawCenteredString(this.font,
      Component.literal("§8PROVISIONING LEDGER…  " + pct), cx, by + barH + 8, COL_LABEL);
  }

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // No dirt/blur panorama — the render() fill owns the whole frame.
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    return true; // swallow — nothing is interactive during provisioning
  }
}

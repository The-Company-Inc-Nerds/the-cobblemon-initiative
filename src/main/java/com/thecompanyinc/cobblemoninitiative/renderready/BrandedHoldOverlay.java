package com.thecompanyinc.cobblemoninitiative.renderready;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The corporate join-hold overlay: a flat dark full-screen (phone/quest-log palette) with the
 * Company letterhead, an amnesiac-voice status line, a subtle terrain-progress readout, and —
 * on fresh installs — the provisioning bar routed in from the server's install phases. Driven
 * entirely by {@link RenderReadyClient}; this class renders and holds no state machine.
 *
 * <p>A real {@code net.minecraft.client.gui.screens.Overlay} (the {@code Minecraft#setOverlay}
 * slot the mojang loading overlay uses): renders above every screen while the level keeps
 * rendering (and compiling sections) underneath. {@link #isPauseScreen()} MUST return false —
 * the vanilla default is true and {@code Minecraft.runTick} ORs the overlay's value into the
 * single-player pause flag (bytecode-verified), which would freeze the integrated server and
 * the very install/cutscene this cover is waiting on.
 */
public class BrandedHoldOverlay extends Overlay {

  // Flat house palette (PhoneCallScreen family) + ledger amber for the letterhead.
  private static final int COL_BACK = 0xFF0E0E12;
  private static final int COL_BRAND = 0xFFE8B84B;
  private static final int COL_TEXT_DIM = 0xFF9A9AA6;
  private static final int COL_TEXT_FAINT = 0xFF63636E;
  private static final int COL_BAR_BORDER = 0xFF3A3A44;
  private static final int COL_BAR_TRACK = 0xFF17171C;
  private static final int COL_SKIP_FILL = 0xFF23232B;
  private static final int COL_SKIP_EDGE = 0xFF3A3A44;

  private static final String BRAND = "THE COMPANY, INC.";
  private static final String VOICE_LINE = "Preparing your onboarding";

  /** Frame-eased provisioning-bar fill (the target comes from the client manager). */
  private float barDisplay = 0f;

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    Minecraft mc = Minecraft.getInstance();
    int w = graphics.guiWidth();
    int h = graphics.guiHeight();
    float alpha = RenderReadyClient.overlayAlpha(partialTick);
    if (alpha <= 0f) return;

    graphics.fill(0, 0, w, h, withAlpha(COL_BACK, alpha));
    // The font renderer treats a ~zero alpha component as opaque — skip text on the last
    // fade frames instead of flashing it back in.
    if (alpha < 0.05f) return;

    int cx = w / 2;
    int cy = h / 2;

    // Letterhead: enlarged 2x, ledger amber.
    graphics.pose().pushPose();
    graphics.pose().translate(cx, cy - 34, 0);
    graphics.pose().scale(2.0f, 2.0f, 1.0f);
    graphics.drawCenteredString(mc.font, Component.literal(BRAND), 0, 0, withAlpha(COL_BRAND, alpha));
    graphics.pose().popPose();

    // In-voice line + animated ellipsis.
    int dots = (RenderReadyClient.holdTicks() / 8) % 4;
    graphics.drawCenteredString(mc.font,
      Component.literal(VOICE_LINE + ".".repeat(dots)), cx, cy - 8, withAlpha(COL_TEXT_DIM, alpha));

    // Provisioning bar — only when the server's install phases are routed into this hold.
    if (RenderReadyClient.installActive()) {
      float target = RenderReadyClient.installBarTarget();
      barDisplay += (target - barDisplay) * Math.min(1f, partialTick * 0.5f + 0.15f);
      barDisplay = Mth.clamp(barDisplay, 0f, 1f);

      int barW = Math.min(300, w - 80);
      int barH = 6;
      int bx = cx - barW / 2;
      int by = cy + 14;
      graphics.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, withAlpha(COL_BAR_BORDER, alpha));
      graphics.fill(bx, by, bx + barW, by + barH, withAlpha(COL_BAR_TRACK, alpha));
      int fillW = (int) (barW * barDisplay);
      if (fillW > 0) graphics.fill(bx, by, bx + fillW, by + barH, withAlpha(COL_BRAND, alpha));
      graphics.drawCenteredString(mc.font,
        Component.literal("PROVISIONING LEDGER…  " + (int) (barDisplay * 100f) + "%"),
        cx, by + barH + 6, withAlpha(COL_TEXT_FAINT, alpha));
    }

    // Subtle terrain readout (display only — the gate lives in RenderReadyClient).
    graphics.drawCenteredString(mc.font,
      Component.literal("TERRAIN " + RenderReadyClient.terrainPercent() + "%  ·  "
        + RenderReadyClient.sectionsOnline() + " SECTORS ONLINE"),
      cx, h - 28, withAlpha(COL_TEXT_FAINT, alpha));

    // Skip affordance (stream safety valve) — only after ESC/click past the 10s reveal.
    if (RenderReadyClient.skipRevealed()) {
      int[] r = skipRect(mc);
      boolean hover = RenderReadyClient.guiMouseX(mc) >= r[0]
        && RenderReadyClient.guiMouseX(mc) < r[0] + r[2]
        && RenderReadyClient.guiMouseY(mc) >= r[1]
        && RenderReadyClient.guiMouseY(mc) < r[1] + r[3];
      graphics.fill(r[0] - 1, r[1] - 1, r[0] + r[2] + 1, r[1] + r[3] + 1,
        withAlpha(hover ? COL_TEXT_DIM : COL_SKIP_EDGE, alpha));
      graphics.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], withAlpha(COL_SKIP_FILL, alpha));
      graphics.drawCenteredString(mc.font, Component.literal("SKIP ▸"),
        r[0] + r[2] / 2, r[1] + (r[3] - 8) / 2,
        withAlpha(hover ? 0xFFE8E8EE : COL_TEXT_DIM, alpha));
    }
  }

  /** The Skip button rect in GUI coords ({x, y, w, h}) — shared with the manager's hit test. */
  static int[] skipRect(Minecraft mc) {
    int w = mc.getWindow().getGuiScaledWidth();
    int h = mc.getWindow().getGuiScaledHeight();
    return new int[] {w - 64 - 12, h - 18 - 12, 64, 18};
  }

  private static int withAlpha(int argb, float alpha) {
    return (Mth.clamp((int) (alpha * 255f), 0, 255) << 24) | (argb & 0x00FFFFFF);
  }

  /**
   * CRITICAL: the {@code Overlay} default is {@code true}, and {@code Minecraft.runTick} ORs
   * it into the single-player pause flag — leaving it would pause the integrated server and
   * deadlock the install/cutscene behind this very cover.
   */
  @Override
  public boolean isPauseScreen() {
    return false;
  }
}

package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.devtools.client.HudLog;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * DEV-ONLY (test driver) — feeds title/subtitle/action-bar text into {@link HudLog} so
 * headless scenarios can assert cutscene cards and overlay messages. {@code HudLog.push}
 * is a static-boolean no-op unless {@code CI_DRIVER_PORT} is set, so this costs nothing
 * in normal play. Overlay is captured HERE (not the fabric GAME event) because
 * {@code /title actionbar} bypasses ChatListener — the Gui setter is the single funnel
 * both packet paths share. Strips with the devtools package at 1.0.0.
 */
@Mixin(Gui.class)
public class GuiTitleMixin {

  @Inject(method = "setTitle", at = @At("HEAD"))
  private void ci$onSetTitle(Component title, CallbackInfo ci) {
    HudLog.push("title", title.getString());
  }

  @Inject(method = "setSubtitle", at = @At("HEAD"))
  private void ci$onSetSubtitle(Component subtitle, CallbackInfo ci) {
    HudLog.push("subtitle", subtitle.getString());
  }

  @Inject(method = "setOverlayMessage", at = @At("HEAD"))
  private void ci$onSetOverlay(Component message, boolean animateColor, CallbackInfo ci) {
    HudLog.push("overlay", message.getString());
  }
}

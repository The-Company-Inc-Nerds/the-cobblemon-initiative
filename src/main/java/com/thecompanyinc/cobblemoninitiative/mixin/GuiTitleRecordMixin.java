package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.questtrack.EventRecordLog;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CLIENT capture feeding the Quest Log's RECORD tab: every title/subtitle card that
 * flashes on screen goes into {@link EventRecordLog} so viewers can review moments
 * they missed. The Gui setters are the single funnel both packet paths share
 * ({@link GuiTitleMixin} precedent — but that one is DEV-ONLY and strips with the
 * devtools package at 1.0.0, so the shipping record capture lives here, apart).
 *
 * <p>Order matters: an mcfunction card is {@code title ... subtitle} THEN
 * {@code title ... title} — vanilla sets the subtitle BEFORE the title fires. So
 * setSubtitle only buffers (component + client tick), and setTitle commits the
 * record, pairing with the buffer iff fresh (staleness guard in the holder).
 *
 * <p>{@code setOverlayMessage} (action bar) is deliberately NOT captured — far too
 * noisy: utility fees, hints and ambient prompts would bury the actual moments.
 */
@Mixin(Gui.class)
public class GuiTitleRecordMixin {

  @Inject(method = "setTitle", at = @At("HEAD"))
  private void ci$recordTitle(Component title, CallbackInfo ci) {
    EventRecordLog.onTitle(title, ((Gui) (Object) this).getGuiTicks());
  }

  @Inject(method = "setSubtitle", at = @At("HEAD"))
  private void ci$recordSubtitle(Component subtitle, CallbackInfo ci) {
    EventRecordLog.onSubtitle(subtitle, ((Gui) (Object) this).getGuiTicks());
  }
}

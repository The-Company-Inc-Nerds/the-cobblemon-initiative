package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.questtrack.QuestSidebarAutoHide;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Auto-hide for the ci_quest sidebar. {@code displayScoreboardSidebar(GuiGraphics,
 * Objective)} is the vanilla method that paints ONE sidebar (signature verified against
 * the 1.21.1 mojmap merged jar); its caller {@code renderScoreboardSidebar} resolves
 * which objective is on display, so cancelling here — and only when that objective is
 * literally {@code ci_quest} — can never touch another sidebar. The timer lives in
 * {@link QuestSidebarAutoHide} (change-hash / keybind / quest-log-close triggers).
 *
 * <p><b>AutoHUD coexistence:</b> no crash risk — we cancel the draw call, AutoHUD
 * wraps the same layer in its own move/fade transforms, and a transform around a
 * cancelled draw is a no-op. But if AutoHUD's scoreboard MODULE is enabled the two
 * visibility timers fight (sidebar only visible when both agree) — the pack's
 * harvested autohud.json5 should keep {@code scoreboard.active} off;
 * QuestSidebarAutoHide.init() logs a breadcrumb warn when AutoHUD is present.
 */
@Mixin(Gui.class)
public class GuiSidebarMixin {

  @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
  private void cobblemonInitiative$autoHideQuestSidebar(
    GuiGraphics guiGraphics, Objective objective, CallbackInfo ci
  ) {
    if (QuestSidebarAutoHide.shouldHide(objective)) ci.cancel();
  }
}

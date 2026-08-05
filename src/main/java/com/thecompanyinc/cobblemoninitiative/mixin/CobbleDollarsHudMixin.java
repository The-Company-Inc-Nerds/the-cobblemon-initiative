package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.compat.cobbledollars.CobbleDollarsHudWatcher;
import fr.harmex.cobbledollars.common.client.CobbleDollarsClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Only-on-change gate for the CobbleDollars 2.0.0-Beta-5.1 balance HUD.
 *
 * <p><b>Why THIS method</b> (bytecode-verified against the pinned jar,
 * {@code curse.maven:cobbledollars-859232:6604561}): the mod's persistent balance
 * element renders via its own client mixin {@code fabric.mixin.GuiMixin} —
 * {@code Gui.renderCameraOverlays} HEAD → {@code CobbleDollarsClient.INSTANCE
 * .beforeChatRender(GuiGraphics, DeltaTracker)} → {@code getOverlay().render(...)}
 * ({@code CobbleDollarsOverlay}). {@code beforeChatRender}'s entire body is that one
 * delegation (21 bytes), so a HEAD cancel here skips the overlay draw completely.
 * The overlay's own {@code render} is NOT a safe target: it overrides vanilla
 * {@code Gui.render}, so the release jar names it {@code method_1753} while the
 * dev-remapped jar names it {@code render} — an environment-dependent name.
 * {@code beforeChatRender} is a plain mod-own Kotlin method, stable in both.
 *
 * <p>{@code remap = false} because every referenced name is CobbleDollars' own (the
 * MoveBackToHomeGoalMixin precedent); the handler's GuiGraphics/DeltaTracker params
 * are OUR references and loom remaps them to the runtime names as usual. Compile
 * visibility comes from the {@code modCompileOnly} pin in build.gradle.kts (same
 * artifact as the modRuntimeOnly line — the Easy NPC pattern), and
 * {@link InitiativeMixinPlugin} skips this mixin when cobbledollars is absent so a
 * bare-mod dev runtime never tries to apply it.
 *
 * <p><b>AutoHUD coexistence:</b> safe — we only cancel CobbleDollars' own draw call.
 * AutoHUD moves/fades the vanilla HUD components it manages (hotbar, health,
 * scoreboard, …) and never knows about this custom overlay, so there is nothing for
 * the two to fight over on this element.
 *
 * <p>Re-verify {@code beforeChatRender} (and the GuiMixin delegation chain) on any
 * CobbleDollars version bump.
 */
@Mixin(value = CobbleDollarsClient.class, remap = false)
public abstract class CobbleDollarsHudMixin {

  @Inject(method = "beforeChatRender", at = @At("HEAD"), cancellable = true)
  private void cobblemonInitiative$onlyOnChange(
    GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci
  ) {
    if (CobbleDollarsHudWatcher.shouldHide()) ci.cancel();
  }
}

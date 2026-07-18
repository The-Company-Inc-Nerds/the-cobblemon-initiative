package com.thecompanyinc.cobblemoninitiative.mixin;

import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.thecompanyinc.cobblemoninitiative.compat.ShowdownWedgeTrap;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps the JS→Java showdown callback in a try/catch — the vanilla body is exactly
 * {@code ShowdownInterpreter.interpretMessage(UUID.fromString(battleId), message)}
 * (bytecode-verified against the pinned Cobblemon 1.7.3), with no exception handling:
 * any Java throwable unwinds through the Graal boundary and permanently wedges the JS
 * battle engine (the ~1-in-10-battles stall of 2026-07-17). Re-implementing the
 * one-liner inside a catch keeps the engine alive and hands the fault to
 * {@link ShowdownWedgeTrap} as trigger evidence.
 */
@Mixin(value = GraalShowdownService.class, remap = false)
public abstract class GraalShowdownServiceMixin {

  @Inject(method = "sendFromShowdown", at = @At("HEAD"), cancellable = true)
  private void cobblemonInitiative$containInterpretFault(
    String battleId, String message, CallbackInfo ci
  ) {
    ci.cancel();
    try {
      ShowdownInterpreter.INSTANCE.interpretMessage(UUID.fromString(battleId), message);
    } catch (Throwable t) {
      ShowdownWedgeTrap.record(battleId, message, t);
    }
  }
}

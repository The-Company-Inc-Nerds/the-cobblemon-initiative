package com.thecompanyinc.cobblemoninitiative.mixin;

import de.markusbordihn.easynpc.entity.easynpc.ai.goal.MoveBackToHomeGoal;
import de.markusbordihn.easynpc.entity.easynpc.data.NavigationDataCapable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Crash guard for flying NPC bases (0.7.0-alpha.1 marsh playtest server kill).
 *
 * <p>Easy NPC 6.25.0's {@code MoveBackToHomeGoal.reachedHome()} takes the
 * no-home branch ({@code hasHomePosition()} treats the preset default Home
 * {0,0,0} as "no home") and calls {@code getGroundPathNavigation().isDone()}
 * with no null check (MoveBackToHomeGoal.java:128). {@code easy_npc:fairy}
 * and {@code easy_npc:allay} bodies create a FlyingPathNavigation, so the
 * instanceof-GroundPathNavigation cast inside {@code getGroundPathNavigation()}
 * returns null → NPE on the server tick thread → hard crash, behind a
 * nextInt(240) random gate (~12 s median after the chunk loads).
 *
 * <p>Returning {@code true} ("already home") makes {@code canUse()} refuse and
 * {@code getPosition()} bail — bytecode-verified as the only paths into the
 * goal, and the only {@code getGroundPathNavigation()} call site in the goal
 * package. The mixin is load-bearing even after the content-side fix (no
 * flying etype compiles with a home objective anymore): a persisted body that
 * saved the bad ObjectiveDataSet re-registers the goal on entity load, and
 * preset re-import orphans-but-never-stops registered goal instances (see
 * ENGINE_FINDINGS §2). Names are the mod's own (not intermediary), hence
 * {@code remap = false}; re-verify {@code navigationData}/{@code reachedHome}
 * on any Easy NPC version bump.
 */
@Mixin(value = MoveBackToHomeGoal.class, remap = false)
public abstract class MoveBackToHomeGoalMixin {

  @Shadow @Final private NavigationDataCapable<?> navigationData;

  @Inject(method = "reachedHome", at = @At("HEAD"), cancellable = true)
  private void cobblemonInitiative$guardFlyingNavigation(CallbackInfoReturnable<Boolean> cir) {
    // Guard ONLY the no-home branch — the valid-home branch is pure BlockPos math and
    // start() drives the mob's OWN (flying) navigation, so a flying NPC with a real
    // in-world Home must keep homing. hasHomePosition() is a side-effect-free default.
    if (navigationData == null
        || (!navigationData.hasHomePosition() && navigationData.getGroundPathNavigation() == null)) {
      cir.setReturnValue(true);
    }
  }
}

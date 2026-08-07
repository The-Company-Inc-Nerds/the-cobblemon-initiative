package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freezes a deliberately-suspended (NoGravity) falling block so it hangs in place forever
 * instead of running the vanilla fall/land/despawn tick. Vanilla {@link FallingBlockEntity}
 * drops itself as an item and despawns once {@code time > 600} (~30s); the prop-hunt barrels
 * (see {@code prophunt/PropHuntManager}) need to stay put for the whole round, rendered in the
 * distinctive falling-block style so they read as game pieces.
 *
 * <p>Gate is {@link net.minecraft.world.entity.Entity#isNoGravity()} rather than an entity tag
 * on purpose: NoGravity is a synced entity flag, so BOTH the server and the client freeze in
 * step (entity {@code Tags} are server-only, so a tag gate would let the client tick despawn the
 * block out from under the render). Vanilla never summons NoGravity falling blocks, and a
 * datapack that does wants exactly this float-forever behaviour, so the broad gate is benign.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockPropMixin {

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void ci$freezeSuspendedProp(CallbackInfo ci) {
    FallingBlockEntity self = (FallingBlockEntity) (Object) this;
    if (self.isNoGravity()) {
      self.setDeltaMovement(Vec3.ZERO); // belt-and-suspenders: never drift even if nudged
      ci.cancel();
    }
  }
}

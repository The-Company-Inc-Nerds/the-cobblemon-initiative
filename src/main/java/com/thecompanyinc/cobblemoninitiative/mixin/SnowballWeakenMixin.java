package com.thecompanyinc.cobblemoninitiative.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.safari.SafariManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Safari snowball weaken: a round player's snowball hit on a tracked lure
 * ({@code ci_safari_lure}) shaves a config fraction of the Pokémon's max HP (floors
 * at 1 — a snowball can never KO) instead of the vanilla 0-damage hurt. The hit is
 * cancelled once handled so vanilla hurt/knockback never disturbs the lure (a busy
 * mid-capture entity must not take a hurt pulse); the outer {@code onHit} still
 * discards the projectile. Everything else passes through untouched.
 */
@Mixin(Snowball.class)
public class SnowballWeakenMixin {

  @Inject(
    method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
    at = @At("HEAD"),
    cancellable = true
  )
  private void cobblemonInitiative$safariWeaken(EntityHitResult result, CallbackInfo ci) {
    Snowball self = (Snowball) (Object) this;
    if (self.level().isClientSide()) return;
    if (!(result.getEntity() instanceof PokemonEntity mon)) return;
    if (!mon.getTags().contains(SafariManager.LURE_TAG)) return;
    if (!(self.getOwner() instanceof ServerPlayer thrower)) return;

    SafariManager manager = InitiativeInit.getSafariManager();
    if (manager == null || !manager.hasSession(thrower.getUUID())) return;

    if (manager.onSnowballWeaken(thrower, mon)) {
      ci.cancel();
    }
  }
}

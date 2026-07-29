package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.protection.TownBuildProtection;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Town build-lock (placement half): cancel a {@link BlockItem} placement whose target sits
 * inside a protected TOWN safe zone. Hooking {@code place} — not {@code useOn} — means we only
 * ever fire on an ACTUAL placement (vanilla resolves block interaction priority before calling
 * {@code place}), so opening a chest/door while holding a stack of blocks is never affected.
 *
 * @see TownBuildProtection
 */
@Mixin(BlockItem.class)
public class BlockItemMixin {

  @Inject(
    method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
    at = @At("HEAD"),
    cancellable = true
  )
  private void cobblemonInitiative$blockTownPlacement(
    BlockPlaceContext context,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    Level level = context.getLevel();
    if (TownBuildProtection.blocksPlacement(context.getPlayer(), level, context.getClickedPos())) {
      cir.setReturnValue(InteractionResult.FAIL);
    }
  }
}

package com.thecompanyinc.cobblemoninitiative.mixin;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.lootchest.LootChestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records chests placed by a player's hand so the loot-chest mechanic can tell
 * them apart from chests that ship with the map (structures / worldedit /
 * {@code /setblock} — none of which call {@code setPlacedBy}).
 */
@Mixin(Block.class)
public class ChestPlacementMixin {

  @Inject(method = "setPlacedBy", at = @At("HEAD"))
  private void cobblemonInitiative$recordChestPlacement(
    Level level,
    BlockPos pos,
    BlockState state,
    LivingEntity placer,
    ItemStack stack,
    CallbackInfo ci
  ) {
    if (level.isClientSide()) return;
    if (!(state.getBlock() instanceof ChestBlock)) return;
    if (!(placer instanceof ServerPlayer)) return;

    LootChestManager manager = InitiativeInit.getLootChestManager();
    if (manager != null) {
      manager.onChestPlacedByPlayer(pos.immutable());
    }
  }
}

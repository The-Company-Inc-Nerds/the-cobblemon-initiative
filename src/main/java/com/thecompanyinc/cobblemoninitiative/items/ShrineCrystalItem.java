package com.thecompanyinc.cobblemoninitiative.items;

import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ShrineCrystalItem extends Item {

  private final String pokemonSpecies;
  private final int pokemonLevel;
  private final String shrineType;

  public ShrineCrystalItem(
    String shrineType,
    String pokemonSpecies,
    int pokemonLevel
  ) {
    super(
      new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()
    );
    this.shrineType = shrineType;
    this.pokemonSpecies = pokemonSpecies;
    this.pokemonLevel = pokemonLevel;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();

    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }

    ServerLevel serverLevel = (ServerLevel) level;
    BlockPos pos = context.getClickedPos().above();

    playExplosionEffects(serverLevel, pos);

    serverLevel.getServer().execute(() -> {
      scheduleSpawn(serverLevel, pos, context);
    });

    context.getItemInHand().shrink(1);

    return InteractionResult.CONSUME;
  }

  private void playExplosionEffects(ServerLevel level, BlockPos pos) {
    double x = pos.getX() + 0.5;
    double y = pos.getY() + 1.0;
    double z = pos.getZ() + 0.5;

    level.playSound(
      null,
      pos,
      SoundEvents.END_PORTAL_SPAWN,
      SoundSource.BLOCKS,
      1.0f,
      1.0f
    );
    level.playSound(
      null,
      pos,
      SoundEvents.GENERIC_EXPLODE.value(),
      SoundSource.BLOCKS,
      0.5f,
      1.2f
    );

    for (int i = 0; i < 50; i++) {
      double offsetX = (level.random.nextDouble() - 0.5) * 2;
      double offsetY = level.random.nextDouble() * 2;
      double offsetZ = (level.random.nextDouble() - 0.5) * 2;

      level.sendParticles(
        ParticleTypes.END_ROD,
        x + offsetX,
        y + offsetY,
        z + offsetZ,
        1,
        0,
        0.1,
        0,
        0.05
      );
      level.sendParticles(
        ParticleTypes.DRAGON_BREATH,
        x + offsetX,
        y + offsetY,
        z + offsetZ,
        1,
        0,
        0,
        0,
        0.02
      );
    }

    switch (shrineType) {
      case "fire" -> level.sendParticles(
        ParticleTypes.FLAME,
        x,
        y,
        z,
        100,
        1,
        1,
        1,
        0.1
      );
      case "ice" -> level.sendParticles(
        ParticleTypes.SNOWFLAKE,
        x,
        y,
        z,
        100,
        1,
        1,
        1,
        0.1
      );
      case "ground" -> level.sendParticles(
        ParticleTypes.DUST_PLUME,
        x,
        y,
        z,
        100,
        1,
        1,
        1,
        0.1
      );
      case "dragon" -> level.sendParticles(
        ParticleTypes.DRAGON_BREATH,
        x,
        y,
        z,
        100,
        1,
        1,
        1,
        0.1
      );
      case "fairy" -> level.sendParticles(
        ParticleTypes.CHERRY_LEAVES,
        x,
        y,
        z,
        100,
        1,
        1,
        1,
        0.1
      );
    }
  }

  private void scheduleSpawn(
    ServerLevel level,
    BlockPos pos,
    UseOnContext context
  ) {
    String command = String.format(
      "spawnpokemon %s level=%d",
      pokemonSpecies,
      pokemonLevel
    );

    String fullCommand = String.format(
      "execute positioned %d %d %d run %s",
      pos.getX(),
      pos.getY(),
      pos.getZ(),
      command
    );

    AchievementsInit.LOGGER.info("Spawning legendary: {}", fullCommand);

    level
      .getServer()
      .getCommands()
      .performPrefixedCommand(
        level
          .getServer()
          .createCommandSourceStack()
          .withPosition(pos.getCenter())
          .withPermission(4),
        command
      );

    level.playSound(
      null,
      pos,
      SoundEvents.ENDER_DRAGON_GROWL,
      SoundSource.HOSTILE,
      1.0f,
      1.0f
    );

    if (context.getPlayer() != null) {
      context
        .getPlayer()
        .sendSystemMessage(
          Component.literal(
            "§d§lA legendary " + pokemonSpecies + " has appeared!"
          )
        );
    }
  }

  @Override
  public void appendHoverText(
    ItemStack stack,
    TooltipContext context,
    List<Component> tooltipComponents,
    TooltipFlag tooltipFlag
  ) {
    tooltipComponents.add(
      Component.literal(
        "§7A crystallized essence of " + shrineType + " energy."
      )
    );
    tooltipComponents.add(Component.literal(""));
    tooltipComponents.add(
      Component.literal("§ePlacing this crystal will summon")
    );
    tooltipComponents.add(Component.literal("§ea powerful legendary Pokémon!"));
    tooltipComponents.add(Component.literal(""));
    tooltipComponents.add(Component.literal("§c§oUse with caution..."));
    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }
}

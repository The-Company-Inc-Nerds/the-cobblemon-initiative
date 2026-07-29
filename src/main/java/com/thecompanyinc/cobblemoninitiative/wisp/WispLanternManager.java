package com.thecompanyinc.cobblemoninitiative.wisp;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Wisp-Lantern (N7 / P1 / P2 playtest). The keepsake Bryn hands you — a soul lantern
 * carrying the {@code ci_wisp_lantern} custom-data marker — becomes a verb: hold it up at the
 * Sunken Ship in Mystic Marsh <b>after dark</b> and the wisps rise, waking the marsh-shadow that
 * sleeps in the wreck. A wild <b>Marshadow</b> (Lv&nbsp;20) coalesces inside the hull for you to
 * battle and catch — the marsh keeps one secret for the trainer who walks the dark with a lantern.
 *
 * <p>The lantern is NOT consumed (it re-summons if the wisp remembers to fade). Away from the ship
 * the item behaves like a normal soul lantern; near the ship in daylight it just gives a hint.
 */
public final class WispLanternManager {

  private WispLanternManager() {}

  private static final String MARKER = "ci_wisp_lantern";

  /** Sunken Ship anchor — where the player holds up the lantern (playtest pin P1). */
  private static final Vec3 SHIP = new Vec3(740.3, 63.0, 2500.6);
  private static final double SHIP_RADIUS = 18.0;

  /** Ghost spawn inside the wreck (playtest pin P2). */
  private static final BlockPos GHOST_POS = new BlockPos(740, 62, 2508);
  private static final String GHOST_SPECIES = "marshadow";
  private static final int GHOST_LEVEL = 20;
  /** Skip the summon if this species is already loitering in the wreck (no stacking). */
  private static final double GHOST_PRESENT_RADIUS = 10.0;

  /** {@code UseBlockCallback} handler. Fast PASS for anything that is not a wisp-lantern. */
  public static InteractionResult onUseBlock(
    Player player,
    Level level,
    InteractionHand hand,
    BlockHitResult hit
  ) {
    if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
      return InteractionResult.PASS;
    }
    ItemStack stack = player.getItemInHand(hand);
    if (!isWispLantern(stack)) return InteractionResult.PASS;

    // Only special near the Sunken Ship; anywhere else it is a plain lantern.
    if (player.position().distanceToSqr(SHIP) > SHIP_RADIUS * SHIP_RADIUS) {
      return InteractionResult.PASS;
    }

    // Consume the interaction near the ship so the keepsake is never placed/depleted there.
    if (!isNight(serverLevel)) {
      player.displayClientMessage(
        Component.literal("§7The wisp-lantern hangs quiet. The marsh only answers it after dark."),
        true
      );
      return InteractionResult.SUCCESS;
    }

    revealWisps(serverLevel, player);

    if (!ghostPresent(serverLevel)) {
      summonGhost(serverLevel);
      player.displayClientMessage(
        Component.literal("§dThe wisps gather in the wreck — and something old wakes to their light."),
        true
      );
    } else {
      player.displayClientMessage(
        Component.literal("§7The wisps drift toward the sunken ship. Something is already stirring there."),
        true
      );
    }
    return InteractionResult.SUCCESS;
  }

  private static boolean isWispLantern(ItemStack stack) {
    if (stack.isEmpty()) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(MARKER);
  }

  private static boolean isNight(ServerLevel level) {
    long t = level.getDayTime() % 24000L;
    return t >= 13000L && t <= 23000L;
  }

  private static boolean ghostPresent(ServerLevel level) {
    AABB box = new AABB(GHOST_POS).inflate(GHOST_PRESENT_RADIUS);
    for (PokemonEntity mon : level.getEntitiesOfClass(PokemonEntity.class, box)) {
      // Match the species by its resource id path (what `spawnpokemon` uses), NOT the localized
      // display name, so the dedup stays correct for any id whose name differs from its path.
      if (GHOST_SPECIES.equalsIgnoreCase(
            mon.getPokemon().getSpecies().getResourceIdentifier().getPath())) {
        return true;
      }
    }
    return false;
  }

  private static void summonGhost(ServerLevel level) {
    Vec3 at = Vec3.atBottomCenterOf(GHOST_POS);
    level.getServer().getCommands().performPrefixedCommand(
      level.getServer().createCommandSourceStack()
        .withPosition(at)
        .withPermission(4)
        .withSuppressedOutput(),
      "spawnpokemon " + GHOST_SPECIES + " level=" + GHOST_LEVEL
    );
    InitiativeInit.LOGGER.info(
      "[WispLantern] Summoned {} L{} at {}", GHOST_SPECIES, GHOST_LEVEL, GHOST_POS
    );
    level.playSound(
      null, GHOST_POS, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0f, 0.6f
    );
  }

  private static void revealWisps(ServerLevel level, Player player) {
    Vec3 c = player.position();
    level.playSound(
      null, player.blockPosition(), SoundEvents.CANDLE_AMBIENT,
      SoundSource.PLAYERS, 1.0f, 0.7f
    );
    // A ring of soul flame + soul particles drifting up around the player and toward the wreck.
    for (int i = 0; i < 60; i++) {
      double ox = (level.random.nextDouble() - 0.5) * SHIP_RADIUS;
      double oz = (level.random.nextDouble() - 0.5) * SHIP_RADIUS;
      double oy = level.random.nextDouble() * 3.0;
      level.sendParticles(
        ParticleTypes.SOUL_FIRE_FLAME,
        c.x + ox, c.y + oy, c.z + oz, 1, 0, 0.02, 0, 0.01
      );
      level.sendParticles(
        ParticleTypes.SOUL,
        c.x + ox, c.y + oy, c.z + oz, 1, 0, 0.03, 0, 0.01
      );
    }
  }
}

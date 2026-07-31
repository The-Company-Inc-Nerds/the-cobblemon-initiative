package com.thecompanyinc.cobblemoninitiative.noble;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * "A Giggle in the Grass" — replaces the old wisp NPC (deleted 2026-07-31) with a shimmering tuft of
 * grass in the Kalahar Oasis (P5 dev note). A single grass block at {@link #GRASS_POS} shines with
 * enchant / end-rod particles every few ticks; BREAKING it lends the player the borrowed wings
 * ({@code noble/mew_wings}) and starts the Mew chase. One-shot by nature — once the tuft is broken it
 * stops shining, and it only re-triggers if grass grows back AND Mew is not yet befriended
 * ({@code defeated_noble_mew} storyFlag &lt; 1). The tuft sits inside the Mew arena
 * (center 1780/114/4220, r20), so {@link NobleEncounterManager#start} finds its ring.
 */
public final class MewGrassManager {

  private MewGrassManager() {}

  /** The shimmering grass tuft (P5: ~1790.5/114/4212.6). */
  private static final BlockPos GRASS_POS = new BlockPos(1790, 114, 4212);
  private static final double CX = 1790.5, CY = 114.4, CZ = 4212.6;
  private static final int SHINE_PERIOD = 6;
  private static final String MEW_FLAG = "defeated_noble_mew";

  private static boolean initialized;

  public static void register() {
    if (initialized) return;
    initialized = true;
    ServerTickEvents.END_SERVER_TICK.register(MewGrassManager::shineTick);
    PlayerBlockBreakEvents.AFTER.register(MewGrassManager::onBlockBroken);
  }

  private static boolean isGrass(BlockState s) {
    return s.is(Blocks.TALL_GRASS) || s.is(Blocks.SHORT_GRASS)
        || s.is(Blocks.FERN) || s.is(Blocks.LARGE_FERN);
  }

  /** Shimmer the tuft so players notice it — only while grass is actually present at the spot. */
  private static void shineTick(MinecraftServer server) {
    ServerLevel overworld = server.overworld();
    if (overworld.getGameTime() % SHINE_PERIOD != 0) return;
    if (!isGrass(overworld.getBlockState(GRASS_POS))) return;
    overworld.sendParticles(ParticleTypes.ENCHANT, CX, CY + 0.3, CZ, 5, 0.28, 0.4, 0.28, 0.05);
    overworld.sendParticles(ParticleTypes.END_ROD, CX, CY + 0.2, CZ, 1, 0.16, 0.28, 0.16, 0.0);
    overworld.sendParticles(ParticleTypes.HAPPY_VILLAGER, CX, CY + 0.15, CZ, 2, 0.24, 0.24, 0.24, 0.0);
  }

  private static void onBlockBroken(
      Level level, Player player, BlockPos pos, BlockState state, BlockEntity be) {
    if (!(level instanceof ServerLevel serverLevel)) return;
    if (!(player instanceof ServerPlayer sp)) return;
    if (!pos.equals(GRASS_POS) || !isGrass(state)) return;
    MinecraftServer server = sp.getServer();
    if (server == null) return;
    if (nobleMewDefeated(server, sp)) return; // already befriended — no re-chase

    // Lend the borrowed wings (aid for the chase, guarded against re-grant), then start the chase.
    // Runs the same mew_wings function + noble start the old chase-giver's button used, so the beat
    // is unchanged — only its trigger moved from an NPC to breaking the shimmering grass.
    var src = server.createCommandSourceStack()
      .withEntity(sp).withLevel(serverLevel).withPermission(2).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "function cobblemon_initiative:noble/mew_wings");
    if (NobleEncounterInit.getManager().start(sp, "mew")) {
      sp.displayClientMessage(Component.literal("§d§oA giggle in the grass…"), true);
    }
  }

  private static boolean nobleMewDefeated(MinecraftServer server, ServerPlayer player) {
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(MEW_FLAG);
    if (obj == null) return false;
    ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
    return info != null && info.value() >= 1;
  }
}

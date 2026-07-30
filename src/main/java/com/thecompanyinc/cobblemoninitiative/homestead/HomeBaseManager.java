package com.thecompanyinc.cobblemoninitiative.homestead;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.HomeBaseConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * The build-a-home reward. Every block a player places counts (town placement is blocked by the
 * build-lock, so a running count only ever reflects their own base out in the wild). When it passes
 * {@link HomeBaseConfig#getBlockThreshold()} the game decides they have made a home: a Latios
 * appears near the home town and Mom calls to tell them a strange Pokémon has turned up. Interacting
 * with the Latios adds it to the team — a flying mount that makes travel easy.
 *
 * <p>One-shot per player ({@code home_base_built} tag). Counting is a persistent scoreboard so it
 * survives relogs. Creative placements do not count.
 */
public final class HomeBaseManager {

  private HomeBaseManager() {}

  /** Where the Latios appears — beside the home town (Sango). */
  private static final BlockPos LATIOS_POS = new BlockPos(2603, 108, 2862);
  private static final String LATIOS_PRESET = "easy_npc:preset/humanoid/latios_gift.npc.snbt";
  private static final String TAG_BUILT = "home_base_built";
  private static final String OBJ = "ci_home_blocks";

  /** Called from the block-placed mixin for every player-placed block. */
  public static void onBlockPlaced(ServerPlayer sp) {
    if (sp == null || sp.getAbilities().instabuild) return; // creative building does not count
    if (sp.getTags().contains(TAG_BUILT)) return;
    HomeBaseConfig cfg = HomeBaseConfig.get();
    if (!cfg.isEnabled()) return;

    int count = bump(sp);
    if (count >= cfg.getBlockThreshold()) {
      trigger(sp);
    }
  }

  private static int bump(ServerPlayer sp) {
    MinecraftServer server = sp.getServer();
    if (server == null) return 0;
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(OBJ);
    if (obj == null) {
      obj = sb.addObjective(OBJ, ObjectiveCriteria.DUMMY, Component.literal("Home Blocks"),
        ObjectiveCriteria.RenderType.INTEGER, false, null);
    }
    ScoreAccess score = sb.getOrCreatePlayerScore(sp, obj);
    int v = score.get() + 1;
    score.set(v);
    return v;
  }

  private static void trigger(ServerPlayer sp) {
    sp.addTag(TAG_BUILT);
    MinecraftServer server = sp.getServer();
    if (server == null) return;

    // Spawn the Latios gift body beside the home town (one-time, guarded by the tag above).
    Vec3 at = Vec3.atBottomCenterOf(LATIOS_POS);
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withPosition(at).withPermission(2).withSuppressedOutput(),
      "easy_npc preset import_new data " + LATIOS_PRESET
        + " " + LATIOS_POS.getX() + " " + LATIOS_POS.getY() + " " + LATIOS_POS.getZ());

    // Mom calls about it (text call).
    server.getCommands().performPrefixedCommand(
      sp.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
      "function cobblemon_initiative:phone/ring_mom_latios");

    InitiativeInit.LOGGER.info(
      "[HomeBase] {} reached the home-base threshold — Latios spawned + Mom call sent.",
      sp.getName().getString());
  }
}

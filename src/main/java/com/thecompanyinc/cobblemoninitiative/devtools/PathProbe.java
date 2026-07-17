package com.thecompanyinc.cobblemoninitiative.devtools;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * {@code /cobblemon-initiative dev path <player> <target>} — vanilla-A* route probe for the
 * client test driver. Borrows the exact pathfinding mobs use ({@link GroundPathNavigation}
 * on a throwaway zombie that is never added to the world), so the returned node list is a
 * route a walking entity can actually traverse: stairs, 1-block steps, door openings —
 * no gap-jumps, no swimming (water malus is set unwalkable to match the Walker).
 *
 * <p>Output is ONE line (RCON-parse friendly, stable prefix):
 * {@code [PATH] reached=<bool> nodes=<n> route=x,y,z;x,y,z;...}
 * plus a happy-villager particle per node so a human at the keyboard can see the trail.
 * The harness feeds {@code route} to the client driver's {@code move.path} op
 * (Walker follows node-to-node — the same short straight legs mobs use).
 *
 * <p>Limits: target must be inside loaded chunks (pathfinding reads real blocks);
 * range is capped by FOLLOW_RANGE=192 below. Partial paths still emit nodes with
 * {@code reached=false} — useful for diagnosing where a route dead-ends.
 */
final class PathProbe {

  private PathProbe() {}

  static int cmdPath(CommandSourceStack source, ServerPlayer player, BlockPos target) {
    ServerLevel level = player.serverLevel();

    Zombie dummy = EntityType.ZOMBIE.create(level);
    if (dummy == null) {
      source.sendFailure(Component.literal("[PATH] error: could not create probe entity"));
      return 0;
    }
    try {
      dummy.moveTo(player.getX(), player.getY(), player.getZ(), 0f, 0f);
      dummy.setOnGround(true);
      var followRange = dummy.getAttribute(Attributes.FOLLOW_RANGE);
      if (followRange != null) followRange.setBaseValue(192.0);
      // Match the Walker's abilities: no swimming, doors are fine (driver can click them).
      dummy.setPathfindingMalus(PathType.WATER, -1.0f);

      GroundPathNavigation nav = new GroundPathNavigation(dummy, level);
      nav.setCanOpenDoors(true);
      // Visited-node budget = FOLLOW_RANGE*16 at construction; x8 lets one probe cross
      // a town AND find a staircase (verified live: budget, not geometry, truncated the
      // spawn->lab probe at the tower base). e2e_run hop-chains partials regardless.
      nav.setMaxVisitedNodesMultiplier(8.0f);
      Path path = nav.createPath(target, 1);

      if (path == null || path.getNodeCount() == 0) {
        source.sendSuccess(
          () -> Component.literal("[PATH] reached=false nodes=0 route="), false);
        return 0;
      }

      boolean reached = path.canReach();
      StringBuilder route = new StringBuilder();
      for (int i = 0; i < path.getNodeCount(); i++) {
        var node = path.getNode(i);
        if (i > 0) route.append(';');
        route.append(node.x).append(',').append(node.y).append(',').append(node.z);
        level.sendParticles(
          ParticleTypes.HAPPY_VILLAGER,
          node.x + 0.5, node.y + 0.3, node.z + 0.5, 3, 0.15, 0.15, 0.15, 0.0);
      }
      int n = path.getNodeCount();
      source.sendSuccess(
        () -> Component.literal("[PATH] reached=" + reached + " nodes=" + n
                                + " route=" + route),
        false);
      return n;
    } finally {
      dummy.discard();
    }
  }
}

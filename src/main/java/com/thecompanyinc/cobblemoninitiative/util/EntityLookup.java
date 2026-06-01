package com.thecompanyinc.cobblemoninitiative.util;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Shared ray-cast utility for finding entities a player is looking at. */
public final class EntityLookup {

  private EntityLookup() {}

  /**
   * Ray-marches from the player's eye position and returns the first
   * non-player entity whose bounding box the ray intersects, or {@code null}.
   */
  public static Entity getEntityLookedAt(ServerPlayer player, double maxRange) {
    Vec3 start = player.getEyePosition();
    Vec3 look = player.getViewVector(1.0f);
    Vec3 end = start.add(look.scale(maxRange));

    AABB searchBox = player.getBoundingBox()
      .expandTowards(look.scale(maxRange))
      .inflate(1.0);

    List<Entity> candidates = player.level().getEntities(
      player, searchBox, e -> !e.isSpectator() && e.isAlive()
    );

    Entity closest = null;
    double closestSq = Double.MAX_VALUE;

    for (Entity e : candidates) {
      Optional<Vec3> hit = e.getBoundingBox().inflate(0.2).clip(start, end);
      if (hit.isPresent()) {
        double sq = start.distanceToSqr(hit.get());
        if (sq < closestSq) {
          closestSq = sq;
          closest = e;
        }
      }
    }
    return closest;
  }
}

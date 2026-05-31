package com.thecompanyinc.cobblemoninitiative.npcsight;

import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.joml.Vector3f;

public class NpcSightManager {

  // Scoreboard objective shared with legacy datapack consumers
  private static final String SCOREBOARD_OBJ = "can_see_player";

  // NPC must be within this dot-product threshold to "see" in stationary mode.
  // cos(60°) = 0.5  →  120° total FOV
  private static final double STATIONARY_FOV_DOT = 0.5;

  // Run full sight check every N server ticks (~4×/sec at 20 TPS)
  private static final int TICK_INTERVAL = 5;

  // Minimum milliseconds between dialog opens for the same NPC
  private static final long DIALOG_COOLDOWN_MS = 5_000L;

  private final NpcSightStorage storage;
  private NpcSightConfig config;
  private int tickCounter = 0;

  // Lazily-resolved Easy NPC mod presence flag
  private Boolean easyNpcPresent = null;

  public NpcSightManager(NpcSightStorage storage, NpcSightConfig config) {
    this.storage = storage;
    this.config = config;
  }

  public void reloadConfig(NpcSightConfig newConfig) {
    this.config = newConfig;
  }

  public NpcSightConfig getConfig() {
    return config;
  }

  // ---------------------------------------------------------------------------
  // Server tick
  // ---------------------------------------------------------------------------

  public void tick(MinecraftServer server) {
    if (++tickCounter < TICK_INTERVAL) return;
    tickCounter = 0;

    // Snapshot to a list so storage can be mutated safely from commands
    List<NpcSightData> snapshot = new ArrayList<>(storage.getAll());
    for (NpcSightData data : snapshot) {
      processNpc(server, data);
    }
  }

  // ---------------------------------------------------------------------------
  // Per-NPC processing
  // ---------------------------------------------------------------------------

  private void processNpc(MinecraftServer server, NpcSightData data) {
    Entity npc = findEntity(server, data.uuid);

    if (npc == null || !npc.isAlive()) {
      data.canSeePlayer = false;
      return;
    }

    ServerLevel level = (ServerLevel) npc.level();
    int range = data.getEffectiveSightRange(config.getDefaultSightRange());

    ServerPlayer nearestPlayer = findNearestPlayer(level, npc, range);

    boolean canSee;
    if (nearestPlayer == null) {
      canSee = false;
    } else if (data.mode == NpcSightData.SightMode.STATIONARY) {
      canSee = checkStationaryLOS(npc, nearestPlayer);
    } else {
      // TRACKING: rotate head toward player, then check LOS from current pos
      rotateToward(npc, nearestPlayer);
      canSee = checkLOS(level, npc, nearestPlayer);
    }

    data.canSeePlayer = canSee;

    updateScoreboard(server, npc, canSee);

    if (config.isDebugMode() && nearestPlayer != null) {
      drawDebugRay(level, npc, nearestPlayer, canSee);
    }

    if (canSee && nearestPlayer != null && data.hasDialog()) {
      double dist = npc.distanceTo(nearestPlayer);
      if (dist <= config.getDialogRange()) {
        long now = System.currentTimeMillis();
        if (now - data.lastDialogTriggerMs >= DIALOG_COOLDOWN_MS) {
          data.lastDialogTriggerMs = now;
          triggerDialog(server, npc, nearestPlayer, data.dialogName);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Entity lookup
  // ---------------------------------------------------------------------------

  private Entity findEntity(MinecraftServer server, java.util.UUID uuid) {
    for (ServerLevel level : server.getAllLevels()) {
      Entity e = level.getEntity(uuid);
      if (e != null) return e;
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // Player search
  // ---------------------------------------------------------------------------

  private ServerPlayer findNearestPlayer(ServerLevel level, Entity npc, double range) {
    ServerPlayer nearest = null;
    double nearestSq = range * range;
    for (ServerPlayer p : level.players()) {
      if (p.isSpectator() || !p.isAlive()) continue;
      double sq = npc.distanceToSqr(p);
      if (sq < nearestSq) {
        nearestSq = sq;
        nearest = p;
      }
    }
    return nearest;
  }

  // ---------------------------------------------------------------------------
  // LOS checks
  // ---------------------------------------------------------------------------

  /**
   * STATIONARY mode: player must be inside the NPC's forward 120° FOV AND
   * there must be an unobstructed line between their eye positions.
   */
  private boolean checkStationaryLOS(Entity npc, Player player) {
    Vec3 forward = npc.getViewVector(1.0f);
    Vec3 toPlayer = player.getEyePosition().subtract(npc.getEyePosition()).normalize();
    if (forward.dot(toPlayer) < STATIONARY_FOV_DOT) return false;
    return checkLOS((ServerLevel) npc.level(), npc, player);
  }

  /**
   * Casts a ray between the two eye positions using block collider shapes.
   * Returns {@code true} when nothing is in the way.
   */
  private boolean checkLOS(ServerLevel level, Entity npc, Player player) {
    Vec3 from = npc.getEyePosition();
    Vec3 to = player.getEyePosition();
    BlockHitResult result = level.clip(
      new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, npc)
    );
    return result.getType() == HitResult.Type.MISS;
  }

  // ---------------------------------------------------------------------------
  // Head tracking
  // ---------------------------------------------------------------------------

  /**
   * Points the NPC toward the target player.
   * Prefers the Mob look-control (smooth, AI-friendly); falls back to direct
   * yaw/pitch assignment for non-Mob LivingEntities.
   */
  private void rotateToward(Entity npc, Player player) {
    if (npc instanceof Mob mob) {
      mob.getLookControl().setLookAt(player);
      // Also snap body so the NPC doesn't stand sideways
      Vec3 dir = player.position().subtract(npc.position());
      if (dir.horizontalDistanceSqr() > 0.001) {
        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        npc.setYRot(yaw);
      }
    } else if (npc instanceof LivingEntity living) {
      Vec3 from = living.getEyePosition();
      Vec3 to = player.getEyePosition();
      Vec3 dir = to.subtract(from);
      double hDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
      float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
      float pitch = (float) -Math.toDegrees(Math.atan2(dir.y, hDist));
      living.setYRot(yaw);
      living.setXRot(pitch);
    }
  }

  // ---------------------------------------------------------------------------
  // Scoreboard update
  // ---------------------------------------------------------------------------

  private void updateScoreboard(MinecraftServer server, Entity npc, boolean canSee) {
    try {
      Scoreboard sb = server.getScoreboard();
      Objective obj = sb.getObjective(SCOREBOARD_OBJ);
      if (obj == null) {
        obj = sb.addObjective(
          SCOREBOARD_OBJ,
          ObjectiveCriteria.DUMMY,
          Component.literal("can_see_player"),
          ObjectiveCriteria.RenderType.INTEGER,
          false,
          null
        );
      }
      sb.getOrCreatePlayerScore(
        ScoreHolder.forNameOnly(npc.getScoreboardName()),
        obj
      ).set(canSee ? 1 : 0);
    } catch (Exception e) {
      AchievementsInit.LOGGER.debug(
        "[NPC Sight] Scoreboard update failed: {}",
        e.getMessage()
      );
    }
  }

  // ---------------------------------------------------------------------------
  // Debug particle ray
  // ---------------------------------------------------------------------------

  /**
   * Draws dust particles along the ray from NPC eye to player eye.
   * Green = clear LOS, red = blocked.
   */
  private void drawDebugRay(
    ServerLevel level, Entity npc, Player player, boolean canSee
  ) {
    Vec3 from = npc.getEyePosition();
    Vec3 to = player.getEyePosition();
    Vec3 delta = to.subtract(from);
    double len = delta.length();
    if (len < 0.01) return;

    Vector3f color = canSee
      ? new Vector3f(0.2f, 1.0f, 0.2f)   // green
      : new Vector3f(1.0f, 0.2f, 0.2f);  // red
    DustParticleOptions dust = new DustParticleOptions(color, 0.5f);

    Vec3 stepVec = delta.normalize().scale(0.5);
    int steps = Math.min((int) (len / 0.5) + 1, 512);
    Vec3 pos = from;
    for (int i = 0; i < steps; i++) {
      level.sendParticles(dust, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
      pos = pos.add(stepVec);
    }
  }

  // ---------------------------------------------------------------------------
  // Easy NPC dialog trigger
  // ---------------------------------------------------------------------------

  private boolean isEasyNpcPresent() {
    if (easyNpcPresent == null) {
      easyNpcPresent = FabricLoader.getInstance().isModLoaded("easynpc")
        || FabricLoader.getInstance().isModLoaded("easy_npc");
    }
    return easyNpcPresent;
  }

  private void triggerDialog(
    MinecraftServer server, Entity npc, ServerPlayer player, String dialogName
  ) {
    if (!isEasyNpcPresent()) return;

    try {
      String cmd = String.format(
        "easy_npc dialog open %s %s %s",
        npc.getUUID(), player.getName().getString(), dialogName
      );
      CommandSourceStack src = server
        .createCommandSourceStack()
        .withPermission(4)
        .withSuppressedOutput();
      server.getCommands().performPrefixedCommand(src, cmd);
    } catch (Exception e) {
      AchievementsInit.LOGGER.debug(
        "[NPC Sight] Dialog trigger failed for {}: {}",
        npc.getUUID(), e.getMessage()
      );
    }
  }
}

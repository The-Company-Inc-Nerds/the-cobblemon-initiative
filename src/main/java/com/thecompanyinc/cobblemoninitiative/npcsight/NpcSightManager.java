package com.thecompanyinc.cobblemoninitiative.npcsight;

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

  // NPC must be within this dot-product threshold to see the player.
  // cos(60°) = 0.5  →  120° total FOV
  private static final double FOV_DOT = 0.5;

  // Run full sight check every N server ticks (~4×/sec at 20 TPS)
  private static final int TICK_INTERVAL = 5;

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

    // +1 so the NPC's own standing block is not counted against the range
    ServerPlayer nearestPlayer = findNearestPlayer(level, npc, range + 1);

    boolean canSee = nearestPlayer != null && checkSight(npc, nearestPlayer);

    data.canSeePlayer = canSee;

    updateScoreboard(server, npc, canSee);

    if (config.isDebugMode() && nearestPlayer != null) {
      drawDebugRay(level, npc, nearestPlayer, canSee);
    }

    switch (data.effectiveMode()) {
      case NpcSightData.MODE_PURSUE -> handlePursue(server, npc, nearestPlayer, canSee, data);
      case NpcSightData.MODE_APPROACH_ONCE -> handleApproachOnce(server, npc, nearestPlayer, canSee, data);
      default -> handleDialog(server, npc, nearestPlayer, canSee, data);
    }
  }

  // ---------------------------------------------------------------------------
  // Behaviour modes
  // ---------------------------------------------------------------------------

  /** DIALOG (default): open the configured dialog once per "seen session". */
  private void handleDialog(
    MinecraftServer server, Entity npc, ServerPlayer nearestPlayer, boolean canSee, NpcSightData data
  ) {
    // Safety net: if this NPC was switched out of PURSUE/APPROACH_ONCE while following,
    // drop the lingering FOLLOW_PLAYER objective.
    if (data.pursuing) {
      stopFollow(server, npc);
      data.pursuing = false;
    }

    // Reset session flag when the NPC loses sight so the next session can fire
    if (!canSee) {
      data.dialogFiredThisSession = false;
      return;
    }
    if (nearestPlayer == null || data.dialogFiredThisSession) return;

    // Per-entity dialog takes priority; fall back to the global default
    String effectiveDialog = data.hasDialog() ? data.dialogName : config.getDefaultDialogName();
    if (effectiveDialog == null || effectiveDialog.isBlank()) return;

    // +1 so the NPC's own standing block is not counted against the range
    if (npc.distanceTo(nearestPlayer) <= config.getDialogRange() + 1) {
      data.dialogFiredThisSession = true;
      triggerDialog(server, npc, nearestPlayer, effectiveDialog);
    }
  }

  /**
   * PURSUE: walk toward the player while in sight (sight-gated FOLLOW_PLAYER objective). The
   * battle itself is the preset's {@code ON_DISTANCE_TOUCH} action — no Java battle trigger.
   */
  private void handlePursue(
    MinecraftServer server, Entity npc, ServerPlayer nearestPlayer, boolean canSee, NpcSightData data
  ) {
    boolean shouldPursue = canSee && nearestPlayer != null && !standDown(data, nearestPlayer);
    if (shouldPursue) {
      if (!data.pursuing) {
        startFollow(server, npc, nearestPlayer);
        data.pursuing = true;
      }
    } else if (data.pursuing) {
      stopFollow(server, npc);
      data.pursuing = false;
    }
  }

  /**
   * APPROACH_ONCE: the first time it ever sees the player, walk up and open the dialog once,
   * tag the player (so the preset can switch to post-meeting dialog), then never auto-approach
   * again. The one-shot latch ({@link NpcSightData#fired}) is persisted immediately.
   */
  private void handleApproachOnce(
    MinecraftServer server, Entity npc, ServerPlayer nearestPlayer, boolean canSee, NpcSightData data
  ) {
    if (data.fired || !canSee || nearestPlayer == null || standDown(data, nearestPlayer)) {
      if (data.pursuing) {
        stopFollow(server, npc);
        data.pursuing = false;
      }
      return;
    }

    // Has sight and not yet fired → walk up to the player
    if (!data.pursuing) {
      startFollow(server, npc, nearestPlayer);
      data.pursuing = true;
    }

    // Arrived? (+1 so the NPC's own standing block is not counted against the range)
    if (npc.distanceTo(nearestPlayer) <= config.getDialogRange() + 1) {
      String effectiveDialog = data.hasDialog() ? data.dialogName : config.getDefaultDialogName();
      if (effectiveDialog != null && !effectiveDialog.isBlank()) {
        // Open the (intro) dialog first, then add the meet-tag so the *next* interaction
        // resolves to the post-meeting dialog via its PLAYER_TAG condition.
        triggerDialog(server, npc, nearestPlayer, effectiveDialog);
      }
      if (data.hasMeetTag()) {
        runCommand(server, "tag " + playerName(nearestPlayer) + " add " + data.meetTag);
      }
      data.fired = true;
      stopFollow(server, npc);
      data.pursuing = false;
      storage.put(data); // persist the one-shot latch immediately
    }
  }

  /** True when the nearest player carries this NPC's configured stand-down tag. */
  private boolean standDown(NpcSightData data, ServerPlayer player) {
    return data.hasStopTag() && player != null && player.getTags().contains(data.stopTag);
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
   * Player must be inside the NPC's forward 120° FOV AND have an unobstructed
   * line of sight between their eye positions.
   */
  private boolean checkSight(Entity npc, Player player) {
    Vec3 forward = npc.getViewVector(1.0f);
    Vec3 toPlayer = player.getEyePosition().subtract(npc.getEyePosition()).normalize();
    if (forward.dot(toPlayer) < FOV_DOT) return false;
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
      NpcSightInit.LOGGER.debug(
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
    runCommand(server, String.format(
      "easy_npc dialog open %s %s %s", npc.getUUID(), playerName(player), dialogName));
  }

  // ---------------------------------------------------------------------------
  // Sight-gated pursuit (Easy NPC FOLLOW_PLAYER objective toggled at runtime)
  // ---------------------------------------------------------------------------

  /** Add a FOLLOW_PLAYER objective so the NPC paths toward the (named) player. */
  private void startFollow(MinecraftServer server, Entity npc, ServerPlayer player) {
    if (!isEasyNpcPresent()) return;
    runCommand(server, String.format(
      "easy_npc objective %s set follow player %s", npc.getUUID(), playerName(player)));
  }

  /** Remove the FOLLOW_PLAYER objective and halt any in-progress path. */
  private void stopFollow(MinecraftServer server, Entity npc) {
    if (!isEasyNpcPresent()) return;
    runCommand(server, String.format("easy_npc objective %s remove follow player", npc.getUUID()));
    runCommand(server, String.format("easy_npc navigation reset %s", npc.getUUID()));
  }

  /** The player's account name — what FOLLOW_PLAYER resolves against and a valid command target. */
  private static String playerName(ServerPlayer player) {
    return player.getGameProfile().getName();
  }

  /** Runs a server command at permission level 4 with output suppressed; logs failures at debug. */
  private void runCommand(MinecraftServer server, String cmd) {
    try {
      CommandSourceStack src = server
        .createCommandSourceStack()
        .withPermission(4)
        .withSuppressedOutput();
      server.getCommands().performPrefixedCommand(src, cmd);
    } catch (Exception e) {
      NpcSightInit.LOGGER.debug("[NPC Sight] Command failed '{}': {}", cmd, e.getMessage());
    }
  }
}

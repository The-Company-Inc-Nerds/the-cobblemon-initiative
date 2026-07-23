package com.thecompanyinc.cobblemoninitiative.npcsight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
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
  // Player tag set by an engage:touch battle trigger while a forced trainer battle runs
  // (cleared in both onwin branches) — pursuers stand down while it is present.
  private static final String IN_TRAINER_BATTLE_TAG = "in_trainer_battle";

  // Dead band above followStopDistance before a holding pursuer resumes the chase, so an NPC
  // parked at the hold distance doesn't jitter start/stop when the player drifts a hair.
  private static final double FOLLOW_RESUME_HYSTERESIS = 2.0;

  private final NpcSightStorage storage;
  private NpcSightConfig config;
  private int tickCounter = 0;

  // Tag-keyed sight profiles (round 13e): placement NPCs are discovered by tag each tick,
  // so no per-world `npcsight add <uuid>` step is needed. sessionData caches the per-entity
  // behaviour state (pursuing / dialog-fired) between ticks, keyed by the entity's runtime
  // uuid; entries are evicted when the entity is no longer discovered.
  private java.util.List<NpcSightProfile> profiles = new java.util.ArrayList<>();
  private final java.util.Map<java.util.UUID, NpcSightData> sessionData = new java.util.HashMap<>();

  // Lazily-resolved Easy NPC mod presence flag
  private Boolean easyNpcPresent = null;

  public NpcSightManager(NpcSightStorage storage, NpcSightConfig config) {
    this.storage = storage;
    this.config = config;
  }

  public void reloadConfig(NpcSightConfig newConfig) {
    this.config = newConfig;
  }

  /** Install the compiler-emitted tag-keyed sight profiles (loaded at SERVER_STARTED). */
  public void loadProfiles(java.util.List<NpcSightProfile> newProfiles) {
    this.profiles = (newProfiles == null) ? new java.util.ArrayList<>() : newProfiles;
    this.sessionData.clear();
  }

  public int profileCount() {
    return profiles.size();
  }

  /**
   * Evict one entity's cached session state so the next discovery tick rebuilds it fresh
   * from its profile. Without this, a tag-profile NPC's APPROACH_ONCE {@code fired} latch
   * (and pursue state) survives {@code /npcsight remove}+re-add — the sessionData entry is
   * only evicted on despawn or SERVER_STARTED (TODO 2026-07-17 re-test finding).
   */
  public boolean clearSession(java.util.UUID uuid) {
    return sessionData.remove(uuid) != null;
  }

  /** Drop every cached session so all profile NPCs re-evaluate fresh (/npcsight reload). */
  public void clearAllSessions() {
    sessionData.clear();
  }

  public NpcSightConfig getConfig() {
    return config;
  }

  // ---------------------------------------------------------------------------
  // Server tick
  // ---------------------------------------------------------------------------

  public void tick(MinecraftServer server) {
    // Camera tweens advance EVERY tick — the sight interval gate below would turn the
    // smooth turn back into a slideshow.
    tickFaceTweens(server);

    if (++tickCounter < config.getTickInterval()) return;
    tickCounter = 0;

    // Snapshot to a list so storage can be mutated safely from commands
    List<NpcSightData> snapshot = new ArrayList<>(storage.getAll());
    for (NpcSightData data : snapshot) {
      processNpc(server, data);
    }

    // Tag-based profiles: discover live entities by tag and process each with its cached
    // session data. Automatic — no per-world uuid registration.
    if (!profiles.isEmpty()) {
      processProfiles(server);
    }
  }

  /** Discover every live entity carrying a profile tag and process it (round 13e). */
  private void processProfiles(MinecraftServer server) {
    java.util.Set<java.util.UUID> seen = new java.util.HashSet<>();
    for (ServerLevel level : server.getAllLevels()) {
      for (Entity e : level.getAllEntities()) {
        if (!e.isAlive() || e instanceof Player) continue;
        NpcSightProfile profile = matchProfile(e);
        if (profile == null) continue;
        seen.add(e.getUUID());
        NpcSightData data = sessionData.computeIfAbsent(e.getUUID(), profile::toData);
        processNpcWithEntity(server, data, e);
      }
    }
    // Evict cache entries for entities no longer present (despawned sentries etc.)
    sessionData.keySet().removeIf(u -> !seen.contains(u));
  }

  /** First profile whose tag the entity carries, or null. */
  private NpcSightProfile matchProfile(Entity e) {
    java.util.Set<String> tags = e.getTags();
    if (tags.isEmpty()) return null;
    for (NpcSightProfile p : profiles) {
      if (tags.contains(p.tag)) return p;
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // Per-NPC processing
  // ---------------------------------------------------------------------------

  private void processNpc(MinecraftServer server, NpcSightData data) {
    processNpcWithEntity(server, data, findEntity(server, data.uuid));
  }

  /** Shared per-NPC processing; the entity is resolved by the caller (uuid lookup for the
   *  registered path, direct hand-off for the tag-discovery path — avoids a re-scan). */
  private void processNpcWithEntity(MinecraftServer server, NpcSightData data, Entity npc) {
    if (npc == null || !npc.isAlive()) {
      data.canSeePlayer = false;
      return;
    }

    ServerLevel level = (ServerLevel) npc.level();
    String mode = data.effectiveMode();
    int range = data.getEffectiveSightRange(config.getDefaultSightRange());
    // Sight-based challengers (PURSUE) spot the player from farther so the ambush triggers
    // across an open path — widen to the pursuit range, keeping any larger per-NPC override.
    if (NpcSightData.MODE_PURSUE.equals(mode)) {
      range = Math.max(range, config.getPursuitSightRange());
    }

    // +1 so the NPC's own standing block is not counted against the range
    ServerPlayer nearestPlayer = findNearestPlayer(level, npc, range + 1);

    boolean canSee = nearestPlayer != null && checkSight(npc, nearestPlayer);

    data.canSeePlayer = canSee;

    updateScoreboard(server, npc, canSee);

    if (config.isDebugMode() && nearestPlayer != null) {
      drawDebugRay(level, npc, nearestPlayer, canSee);
    }

    switch (mode) {
      case NpcSightData.MODE_PURSUE -> handlePursue(server, npc, nearestPlayer, canSee, data);
      case NpcSightData.MODE_APPROACH_ONCE -> handleApproachOnce(server, npc, nearestPlayer, canSee, data);
      case NpcSightData.MODE_PASSIVE -> { /* scoreboard-only: the quest tick reads it */ }
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
   * PURSUE: walk toward the player while in sight (sight-gated FOLLOW_PLAYER objective), but
   * HOLD at {@link NpcSightConfig#getFollowStopDistance()} instead of pathing into the player —
   * otherwise the trainer walks to Easy NPC's ~2-block FOLLOW StopDistance and keeps shuffling
   * into you while its dialog is open (showrunner 2026-07-08). The forced battle is the preset's
   * {@code ON_DISTANCE_VERY_CLOSE} action (4-block band), which the hold distance sits inside, so
   * the ambush still fires; a gated/declined battle just leaves the trainer standing a few blocks
   * off. Resume the chase only once the player pulls back past the hold + hysteresis dead band.
   */
  private void handlePursue(
    MinecraftServer server, Entity npc, ServerPlayer nearestPlayer, boolean canSee, NpcSightData data
  ) {
    if (!canSee || nearestPlayer == null || standDown(data, nearestPlayer)) {
      if (data.pursuing) {
        stopFollow(server, npc);
        data.pursuing = false;
      }
      return;
    }

    double dist = npc.distanceTo(nearestPlayer);
    double hold = config.getFollowStopDistance();
    if (dist <= hold) {
      // Arrived at conversation range — hold position, do not close the last blocks.
      if (data.pursuing) {
        stopFollow(server, npc);
        data.pursuing = false;
        // The trainer just ran you down: snap the camera to face them as the preset's
        // ON_DISTANCE_VERY_CLOSE forced battle fires. The pursuing->arrived transition
        // is the once-per-chase latch; a player who walked up themselves never chased,
        // so they are never snapped.
        facePlayerAt(npc, nearestPlayer);
      }
    } else if (dist > hold + FOLLOW_RESUME_HYSTERESIS) {
      // Player pulled away — (re)start the chase.
      if (!data.pursuing) {
        startFollow(server, npc, nearestPlayer);
        data.pursuing = true;
      }
    }
    // Between hold and hold+hysteresis: keep the current state (no start/stop jitter).
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

    // Arrived? Fire+stop by the time she reaches the hold distance, so she opens the meeting
    // a few blocks off rather than shuffling into the player. dialogRange+1 keeps the NPC's own
    // standing block from counting; the hold distance is the floor so a small dialogRange can't
    // let her overshoot.
    double arrive = Math.max(config.getDialogRange() + 1, config.getFollowStopDistance());
    if (npc.distanceTo(nearestPlayer) <= arrive) {
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

  /** True when the pursuer should stop chasing: the player carries this NPC's stand-down
   *  tag (defeated), OR the player is in a forced trainer battle (round 13d — the
   *  engage:touch trigger sets in_trainer_battle at battle start and both onwin branches
   *  clear it; without this the pursuer kept walking into the player mid-battle). */
  private boolean standDown(NpcSightData data, ServerPlayer player) {
    if (player == null) return false;
    if (player.getTags().contains(IN_TRAINER_BATTLE_TAG)) return true;
    return data.hasStopTag() && player.getTags().contains(data.stopTag);
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
    double fovDot = Math.cos(Math.toRadians(config.getFovDegrees() / 2.0));
    if (forward.dot(toPlayer) < fovDot) return false;
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

    double rayStep = Math.max(0.1, config.getDebugRayStep());
    Vec3 stepVec = delta.normalize().scale(rayStep);
    int steps = Math.min((int) (len / rayStep) + 1, config.getDebugRayMaxSteps());
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
    // Whip the player's camera around to face whoever is talking to them — the
    // "caught" beat for both DIALOG greeters and APPROACH_ONCE walk-ups (Mom).
    // ServerPlayer.lookAt sends ClientboundPlayerLookAtPacket — the exact path
    // vanilla `/tp … facing entity` uses — a one-shot snap; the mouse is free the
    // next frame. Fire-once per seen-session is inherited from the callers'
    // dialogFiredThisSession / fired latches.
    facePlayerAt(npc, player);
    runCommand(server, String.format(
      "easy_npc dialog open %s %s %s", npc.getUUID(), playerName(player), dialogName));
  }

  /**
   * Turn the player's camera to the NPC's eyes — SMOOTHLY (showrunner request
   * 2026-07-17: the old one-shot lookAt snap on Mom's walk-up was jarring). Starts a
   * per-tick eased tween; each step re-sends the same ClientboundPlayerLookAtPacket
   * vanilla `/tp … facing entity` uses, aimed at a point that slides toward the NPC
   * (ease-out: 35% of the remaining arc per tick ≈ settled in ~0.5s). The target is
   * re-resolved every tick, so a run-down spotter stays centered while still moving.
   * If the client-reported rotation strays from what we last commanded, the player is
   * steering — the tween aborts instantly rather than fight the mouse.
   */
  private void facePlayerAt(Entity npc, ServerPlayer player) {
    faceTweens.put(player.getUUID(), new FaceTween(npc.getUUID(), 30));
  }

  /** Active camera tweens by player uuid — see {@link #facePlayerAt}. */
  private final Map<UUID, FaceTween> faceTweens = new HashMap<>();

  private static final class FaceTween {
    final UUID npcId;
    int ticksLeft;
    float lastYaw;
    float lastPitch;
    boolean primed;

    FaceTween(UUID npcId, int ticks) {
      this.npcId = npcId;
      this.ticksLeft = ticks;
    }
  }

  private void tickFaceTweens(MinecraftServer server) {
    if (faceTweens.isEmpty()) return;
    Iterator<Map.Entry<UUID, FaceTween>> it = faceTweens.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<UUID, FaceTween> e = it.next();
      ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
      FaceTween tw = e.getValue();
      if (player == null || --tw.ticksLeft < 0) {
        it.remove();
        continue;
      }
      Entity npc = null;
      for (ServerLevel level : server.getAllLevels()) {
        npc = level.getEntity(tw.npcId);
        if (npc != null) break;
      }
      if (npc == null || !npc.isAlive()) {
        it.remove();
        continue;
      }

      // Never fight the mouse: the client echoes our commanded rotation back within a
      // tick, so a big deviation from the last command means deliberate player input.
      if (tw.primed) {
        float dyaw = Math.abs(Mth.degreesDifference(player.getYRot(), tw.lastYaw));
        float dpitch = Math.abs(player.getXRot() - tw.lastPitch);
        if (dyaw > 25.0f || dpitch > 25.0f) {
          it.remove();
          continue;
        }
      }

      Vec3 eye = player.getEyePosition();
      Vec3 target = npc.getEyePosition().subtract(eye).normalize();
      Vec3 current = player.getLookAngle().normalize();
      double dot = Mth.clamp(current.dot(target), -1.0, 1.0);
      double angle = Math.toDegrees(Math.acos(dot));
      if (angle < 2.5) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, npc, EntityAnchorArgument.Anchor.EYES);
        it.remove();
      } else {
        // Rotate by a capped ANGLE per tick — vector nlerp degenerates near 180°
        // (the interpolant collapses toward zero length, then normalization whips
        // the whole arc in one tick = still a snap; caught by live yaw sampling).
        double stepDeg = Mth.clamp(angle * 0.35, 4.0, 24.0);
        Vec3 axis = current.cross(target);
        if (axis.lengthSqr() < 1.0e-6) {
          axis = Math.abs(current.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
          axis = axis.subtract(current.scale(axis.dot(current)));
        }
        axis = axis.normalize();
        double rad = Math.toRadians(stepDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        // Rodrigues: v' = v cosθ + (k×v) sinθ + k (k·v)(1−cosθ)
        Vec3 stepDir = current.scale(cos)
            .add(axis.cross(current).scale(sin))
            .add(axis.scale(axis.dot(current) * (1.0 - cos)))
            .normalize();
        player.lookAt(EntityAnchorArgument.Anchor.EYES, eye.add(stepDir.scale(8.0)));
        tw.lastYaw = player.getYRot();
        tw.lastPitch = player.getXRot();
        tw.primed = true;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Sight-gated pursuit (Easy NPC FOLLOW_PLAYER objective toggled at runtime)
  // ---------------------------------------------------------------------------

  /** Add a FOLLOW_PLAYER objective so the NPC paths toward the (named) player. */
  private void startFollow(MinecraftServer server, Entity npc, ServerPlayer player) {
    if (!isEasyNpcPresent()) return;
    runCommand(server, String.format(
      "easy_npc objective %s set follow player %s", npc.getUUID(), playerName(player)));
    applyPursuitSpeed(npc);
  }

  /** Resource id for the transient movement-speed modifier applied while a PURSUE-mode NPC is
   *  actively chasing. Transient (never written to entity NBT) so it self-clears if the NPC
   *  unloads mid-chase — no risk of a permanently sped-up trainer surviving a reload. */
  private static final net.minecraft.resources.ResourceLocation PURSUIT_SPEED_MODIFIER =
    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cobblemon_initiative", "sight_pursuit");

  /** Speed the NPC up while it runs the player down (amount is config-tunable, 0 disables). */
  private void applyPursuitSpeed(Entity npc) {
    double bonus = config.getPursuitSpeedBonus();
    if (bonus <= 0.0 || !(npc instanceof net.minecraft.world.entity.LivingEntity living)) return;
    net.minecraft.world.entity.ai.attributes.AttributeInstance speed =
      living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
    if (speed == null || speed.hasModifier(PURSUIT_SPEED_MODIFIER)) return;
    speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
      PURSUIT_SPEED_MODIFIER, bonus,
      net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
  }

  /** Drop the pursuit speed boost when the chase ends (arrival hold, stand-down, teardown). */
  private void removePursuitSpeed(Entity npc) {
    if (!(npc instanceof net.minecraft.world.entity.LivingEntity living)) return;
    net.minecraft.world.entity.ai.attributes.AttributeInstance speed =
      living.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
    if (speed != null) speed.removeModifier(PURSUIT_SPEED_MODIFIER);
  }

  /**
   * Remove the FOLLOW_PLAYER objective and halt any in-progress path.
   *
   * <p>{@code objective remove} soft-fails (sendFailure + result 0, no throw) once the backing
   * ObjectiveDataSet entry is gone — a preset import mid-follow replaces the data set WITHOUT
   * unregistering the live goal (Mom's first-join walk-up races the auto-install repaint
   * exactly this way) — leaving the orphaned goal pathing forever, unreachable by command.
   * So any follow goal still left in the mob's goal selector afterwards is purged directly.
   * (Nested inside another command's execution the remove is only queued and the purge
   * actually precedes it; the end state is identical — the queued remove then drops the data
   * entry whose goal is already gone.)
   */
  private void stopFollow(MinecraftServer server, Entity npc) {
    removePursuitSpeed(npc);
    if (!isEasyNpcPresent()) return;
    boolean removed = runVerifiedCommand(
      server, String.format("easy_npc objective %s remove follow player", npc.getUUID()));
    int purged = purgeFollowGoals(npc);
    if (purged > 0) {
      NpcSightInit.LOGGER.warn(
        "[NPC Sight] Purged {} follow goal(s) left on {} (remove reported success={}) —"
          + " likely orphaned by a preset import mid-follow.",
        purged, npc.getUUID(), removed);
    }
    runCommand(server, String.format("easy_npc navigation reset %s", npc.getUUID()));
  }

  /**
   * Drop any Easy NPC FollowLivingEntityGoal still registered on the mob — matched by class
   * name so Easy NPC stays a runtime-only dependency. Scope caveat: FOLLOW_OWNER and
   * FOLLOW_ENTITY_BY_UUID construct the same goal class and would be purged too, and Easy
   * NPC's periodic refresh skips entries it still believes are registered — safe only
   * because no shipped preset carries a FOLLOW objective and the one datapack escort
   * (tenants_of_record) re-arms its follow on a ~100-tick cadence.
   */
  public static int purgeFollowGoals(Entity npc) {
    if (!(npc instanceof net.minecraft.world.entity.Mob mob)) return 0;
    net.minecraft.world.entity.ai.goal.GoalSelector selector =
      ((com.thecompanyinc.cobblemoninitiative.mixin.MobGoalSelectorAccessor) mob).getGoalSelector();
    List<net.minecraft.world.entity.ai.goal.Goal> stale = selector.getAvailableGoals().stream()
      .map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal)
      .filter(g -> "FollowLivingEntityGoal".equals(g.getClass().getSimpleName()))
      .toList();
    stale.forEach(selector::removeGoal);
    return stale.size();
  }

  // ---------------------------------------------------------------------------
  // Pursuit teardown around preset imports
  // ---------------------------------------------------------------------------

  /**
   * Detach any follow from this sight-managed NPC before a preset import replaces its Easy
   * NPC objective data — removing the follow while its backing entry still exists keeps the
   * data set and the goal selector in sync. Unconditional rather than gated on
   * {@link NpcSightData#pursuing}: a mid-chase relog re-registers the follow from entity NBT
   * while the transient flag reboots false. No-op when the NPC is not sight-managed or not
   * loaded (an import is a no-op for an unloaded uuid, and eagerly clearing the flag would
   * disable the sight tick's own stop paths). A torn-down APPROACH_ONCE that has not fired
   * yet re-acquires on a later sight tick; a PURSUE resumes while it still has sight.
   */
  public void teardownPursuit(MinecraftServer server, java.util.UUID uuid) {
    NpcSightData stored = storage.get(uuid);
    NpcSightData session = sessionData.get(uuid);
    if (stored == null && session == null) return;
    Entity npc = findEntity(server, uuid);
    if (npc == null) return;
    stopFollow(server, npc);
    if (stored != null) stored.pursuing = false;
    if (session != null) session.pursuing = false;
  }

  /** Tear down every active pursuit — bulk preset repaints (install run / content refresh). */
  public void teardownAllPursuits(MinecraftServer server) {
    for (NpcSightData data : storage.getAll()) teardownIfPursuing(server, data);
    for (NpcSightData data : sessionData.values()) teardownIfPursuing(server, data);
  }

  private void teardownIfPursuing(MinecraftServer server, NpcSightData data) {
    if (!data.pursuing) return;
    Entity npc = findEntity(server, data.uuid);
    // Unloaded: leave the flag set — the sight tick's stop paths (stand-down, arrival hold)
    // still key on it, and there is nothing to import onto an unloaded uuid anyway.
    if (npc == null) return;
    stopFollow(server, npc);
    data.pursuing = false;
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

  /**
   * Like {@link #runCommand} but reports command success via the result callback. Brigadier
   * flags success for ANY non-throwing command, so Easy NPC's soft failures (sendFailure +
   * return 0) are only caught by also requiring a positive result. Caveat: invoked nested
   * inside another command's execution the command is merely queued — the callback fires
   * after this returns, so this reports false there; trust it only from plain tick paths.
   */
  private boolean runVerifiedCommand(MinecraftServer server, String cmd) {
    final boolean[] ok = {false};
    try {
      CommandSourceStack src = server
        .createCommandSourceStack()
        .withPermission(4)
        .withSuppressedOutput()
        .withCallback((success, result) -> {
          if (success && result > 0) ok[0] = true;
        });
      server.getCommands().performPrefixedCommand(src, cmd);
    } catch (Exception e) {
      NpcSightInit.LOGGER.debug("[NPC Sight] Command failed '{}': {}", cmd, e.getMessage());
    }
    return ok[0];
  }
}

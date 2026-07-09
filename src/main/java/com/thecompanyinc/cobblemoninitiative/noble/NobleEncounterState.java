package com.thecompanyinc.cobblemoninitiative.noble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerBossEvent;

/**
 * Per-player mutable state for one active noble encounter.
 * Session-only — never persisted (a live fight is bound to a live entity + boss bar).
 * Mirrors {@link com.thecompanyinc.cobblemoninitiative.shrine.ShrineChallengeState}.
 */
public class NobleEncounterState {

  /** Encounter lifecycle. STAGGER is an instantaneous transition, not a resting phase. */
  public enum Phase {
    INTRO,
    REALTIME,
    BATTLE,
    COMPLETE,
    FAILED,
  }

  private final UUID playerId;
  private final String nobleId;

  private Phase phase = Phase.INTRO;
  /** Wall-clock ms when the current phase was entered (INTRO hold / timers). */
  private long phaseStartMs = System.currentTimeMillis();

  // ── Arena (captured from config at start) ────────────────────────────────────
  private double arenaX;
  private double arenaY;
  private double arenaZ;
  private int arenaRadius = 20;
  private String arenaDimension = "minecraft:overworld";

  // ── Handles (all session-only) ───────────────────────────────────────────────
  /** Vanilla stagger boss bar (mirrors the Easy NPC body's health). */
  private ServerBossEvent bossBar;
  /** The Phase-1 Easy NPC body. */
  private UUID bodyUuid;
  /** Phase-2 real Cobblemon entity + its Pokémon + the battle it's in (completion matching). */
  private UUID battleEntityUuid;
  private UUID battlePokemonUuid;
  private UUID battleId;

  // ── Combat scheduling ────────────────────────────────────────────────────────
  /** attack-list index → ticks remaining before it can fire again. */
  private final Map<Integer, Integer> attackCooldowns = new HashMap<>();
  /** Global gap so telegraphs never overlap; no attack fires while > 0. */
  private int globalAttackGap = 0;
  /** Live telegraphed impacts (windup → strike). */
  private final List<PendingImpact> pendingImpacts = new ArrayList<>();
  /** Live persistent hazard fields. */
  private final List<HazardZone> hazardZones = new ArrayList<>();
  /** Live moving projectiles (element-themed, dodgeable). */
  private final List<ProjectileBolt> bolts = new ArrayList<>();
  /** Live telegraphed line beams (windup → sweep). */
  private final List<PendingBeam> beams = new ArrayList<>();

  // ── Flyer rhythm ─────────────────────────────────────────────────────────────
  private boolean airborne = true;
  private int flyerTimer = 0;

  // ── Friendly "chase" task ────────────────────────────────────────────────────
  /** Tags landed so far (chase type). */
  private int taskProgress = 0;
  /** Grace ticks before the next tag can register. */
  private int taskCooldown = 0;

  // ── Ambient theme ────────────────────────────────────────────────────────────
  private int ambientTimer = 0;

  public NobleEncounterState(UUID playerId, String nobleId) {
    this.playerId = playerId;
    this.nobleId = nobleId;
  }

  /** A telegraphed ground strike: warning particles during windup, then AoE on impact. */
  public static class PendingImpact {
    public double x, y, z;
    public double radius;
    public int ticksLeft;
    public int totalWindup;
    public float damage;
    public double knockback;
    /** If true, follow the target during windup (re-aim each tick). */
    public boolean tracking;
    /** If true, telegraph a vertical column (bolt_strike / flame pillar) too. */
    public boolean column;
  }

  /** A persistent hazard field that ticks damage/effect on players stood in it. */
  public static class HazardZone {
    public double x, y, z;
    public double radius;
    public int ticksLeft;
    public float tickDamage;
    /** 0 none, -1 pull toward center, +1 push away. */
    public int pull;
  }

  /** An element-themed traveling projectile (moved + collision-checked each tick). */
  public static class ProjectileBolt {
    public double x, y, z;
    public double vx, vy, vz;
    public int ticksLeft;
    public float damage;
  }

  /** A telegraphed line attack: warning line during windup, then damage along the segment. */
  public static class PendingBeam {
    public double sx, sy, sz;   // source
    public double dx, dy, dz;   // unit direction
    public double length;
    public double width;
    public int ticksLeft;
    public int totalWindup;
    public float damage;
  }

  // ── Getters / setters ────────────────────────────────────────────────────────

  public UUID getPlayerId() { return playerId; }
  public String getNobleId() { return nobleId; }

  public Phase getPhase() { return phase; }
  public void setPhase(Phase p) { this.phase = p; this.phaseStartMs = System.currentTimeMillis(); }
  public long getPhaseElapsedMs() { return System.currentTimeMillis() - phaseStartMs; }

  public double getArenaX() { return arenaX; }
  public double getArenaY() { return arenaY; }
  public double getArenaZ() { return arenaZ; }
  public int getArenaRadius() { return arenaRadius; }
  public String getArenaDimension() { return arenaDimension; }
  public void setArena(double x, double y, double z, int radius, String dimension) {
    this.arenaX = x; this.arenaY = y; this.arenaZ = z;
    this.arenaRadius = radius; this.arenaDimension = dimension;
  }

  public ServerBossEvent getBossBar() { return bossBar; }
  public void setBossBar(ServerBossEvent bar) { this.bossBar = bar; }

  public UUID getBodyUuid() { return bodyUuid; }
  public void setBodyUuid(UUID uuid) { this.bodyUuid = uuid; }

  public UUID getBattleEntityUuid() { return battleEntityUuid; }
  public void setBattleEntityUuid(UUID uuid) { this.battleEntityUuid = uuid; }
  public UUID getBattlePokemonUuid() { return battlePokemonUuid; }
  public void setBattlePokemonUuid(UUID uuid) { this.battlePokemonUuid = uuid; }
  public UUID getBattleId() { return battleId; }
  public void setBattleId(UUID uuid) { this.battleId = uuid; }

  public Map<Integer, Integer> getAttackCooldowns() { return attackCooldowns; }
  public int getGlobalAttackGap() { return globalAttackGap; }
  public void setGlobalAttackGap(int t) { this.globalAttackGap = t; }
  public List<PendingImpact> getPendingImpacts() { return pendingImpacts; }
  public List<HazardZone> getHazardZones() { return hazardZones; }
  public List<ProjectileBolt> getBolts() { return bolts; }
  public List<PendingBeam> getBeams() { return beams; }

  public boolean isAirborne() { return airborne; }
  public void setAirborne(boolean v) { this.airborne = v; }
  public int getFlyerTimer() { return flyerTimer; }
  public void setFlyerTimer(int t) { this.flyerTimer = t; }

  public int getTaskProgress() { return taskProgress; }
  public void setTaskProgress(int v) { this.taskProgress = v; }
  public int getTaskCooldown() { return taskCooldown; }
  public void setTaskCooldown(int v) { this.taskCooldown = v; }

  public int getAmbientTimer() { return ambientTimer; }
  public void setAmbientTimer(int t) { this.ambientTimer = t; }
}

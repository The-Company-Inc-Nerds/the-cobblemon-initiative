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

  /** Encounter lifecycle. STAGGERED is the scripted collapse cinematic between the
   * real-time fight and the catch battle (no attacks fire during it). */
  public enum Phase {
    INTRO,
    REALTIME,
    STAGGERED,
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

  // ── Escalation / feedback (REALTIME) ────────────────────────────────────────
  /** Body health fraction last tick; -1 until first observed. Drives rage-band crossings
   * and the melee hit-confirm delta. */
  private float lastHealthFraction = -1f;
  /** 0 = calm; rises as rage thresholds are crossed (never decreases — health only drops). */
  private int rageTier = 0;
  /** One-shot "stagger imminent" audio warning latch. */
  private boolean preStaggerWarned = false;
  /** Boss bar flashes white for this many ticks after a landed melee hit. */
  private int barFlashTicks = 0;
  /** Next game-time tick the low-health heartbeat may play. */
  private long nextHeartbeatTick = 0;
  /** Next game-time tick the Phase-1 music loop re-triggers; 0 = loop not running. */
  private long nextLoopTick = 0;
  /** Last whole intro second announced (element pulse countdown). */
  private int lastIntroSecond = -1;
  /** Chase-type random giggle-cry countdown. */
  private int giggleTimer = 0;

  // ── Stagger cinematic ────────────────────────────────────────────────────────
  private int staggerTicks = 0;
  /** Where the body stood at stagger — the Phase-2 mon spawns here (Y clamped to floor). */
  private double swapX, swapY, swapZ;
  /** Ticks spent waiting to open the Phase-2 battle (busy target / errored start retries). */
  private int battleOpenAttempts = 0;

  /** Sounds queued to play N ticks from now (survive phase changes; die with the state). */
  private final List<DelayedCue> delayedCues = new ArrayList<>();

  public NobleEncounterState(UUID playerId, String nobleId) {
    this.playerId = playerId;
    this.nobleId = nobleId;
  }

  /** A sound scheduled for a few ticks out (stagger stings, landing bells). */
  public static class DelayedCue {
    public int ticksLeft;
    public String soundId;
    public float volume;
    public float pitch;
    public double x, y, z;

    public DelayedCue(int ticksLeft, String soundId, float volume, float pitch, double x, double y, double z) {
      this.ticksLeft = ticksLeft;
      this.soundId = soundId;
      this.volume = volume;
      this.pitch = pitch;
      this.x = x; this.y = y; this.z = z;
    }
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
    /** Per-attack impact-sound override (JSON {@code impactSound}); null = element default. */
    public String impactSoundId;
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
    /** Per-attack impact-sound override; null = element default. */
    public String impactSoundId;
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
    /** Per-attack impact-sound override; null = element default. */
    public String impactSoundId;
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

  public float getLastHealthFraction() { return lastHealthFraction; }
  public void setLastHealthFraction(float f) { this.lastHealthFraction = f; }
  public int getRageTier() { return rageTier; }
  public void setRageTier(int t) { this.rageTier = t; }
  public boolean isPreStaggerWarned() { return preStaggerWarned; }
  public void setPreStaggerWarned(boolean v) { this.preStaggerWarned = v; }
  public int getBarFlashTicks() { return barFlashTicks; }
  public void setBarFlashTicks(int t) { this.barFlashTicks = t; }
  public long getNextHeartbeatTick() { return nextHeartbeatTick; }
  public void setNextHeartbeatTick(long t) { this.nextHeartbeatTick = t; }
  public long getNextLoopTick() { return nextLoopTick; }
  public void setNextLoopTick(long t) { this.nextLoopTick = t; }
  public int getLastIntroSecond() { return lastIntroSecond; }
  public void setLastIntroSecond(int s) { this.lastIntroSecond = s; }
  public int getGiggleTimer() { return giggleTimer; }
  public void setGiggleTimer(int t) { this.giggleTimer = t; }

  public int getStaggerTicks() { return staggerTicks; }
  public void setStaggerTicks(int t) { this.staggerTicks = t; }
  public int getBattleOpenAttempts() { return battleOpenAttempts; }
  public void setBattleOpenAttempts(int v) { this.battleOpenAttempts = v; }
  public double getSwapX() { return swapX; }
  public double getSwapY() { return swapY; }
  public double getSwapZ() { return swapZ; }
  public void setSwapPos(double x, double y, double z) { this.swapX = x; this.swapY = y; this.swapZ = z; }

  public List<DelayedCue> getDelayedCues() { return delayedCues; }
}

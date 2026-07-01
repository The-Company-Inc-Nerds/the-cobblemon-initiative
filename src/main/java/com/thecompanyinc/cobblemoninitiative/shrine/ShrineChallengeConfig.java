package com.thecompanyinc.cobblemoninitiative.shrine;

import java.util.Collections;
import java.util.List;

public class ShrineChallengeConfig {

  // Core identity
  private String id;
  private String displayName;
  private String startTitle;
  private String startSubtitle;

  /**
   * Challenge type — one of:
   *   "hydra_gauntlet"  — Dragon: 3 sequential trainer battles with healing between
   *   "fairy_tests"     — Fairy: 5 stat checks on the lead Pokémon
   *   "timed_parkour"   — Ice / Fire: complete a course before the timer expires
   *   "dark_gauntlet"   — Ground: blindness + half health + earthquakes; reach the leader
   */
  private String type;

  // ── hydra_gauntlet ──────────────────────────────────────────────────────────
  /** Trainer IDs for each stage, in order. */
  private List<String> stageTrainerIds = Collections.emptyList();

  // ── fairy_tests ─────────────────────────────────────────────────────────────
  /** Minimum friendship required to pass test 1. */
  private int friendshipThreshold = 160;
  /** Minimum fullness required to pass test 2. */
  private int fullnessThreshold = 50;
  /** Trainer ID the player must defeat for test 3 (Trial of Resolve). */
  private String cultistLeaderTrainerId;

  // ── timed_parkour ───────────────────────────────────────────────────────────
  /** Seconds the player has to reach the finish. */
  private int timeLimitSeconds = 120;

  // ── dark_gauntlet ───────────────────────────────────────────────────────────
  /** The trainer ID whose defeat completes the challenge. */
  private String targetTrainerId;
  /** How often (in seconds) an earthquake teleports the player. */
  private int earthquakeIntervalSeconds = 45;
  /** Max horizontal displacement (blocks) for each earthquake teleport. */
  private int earthquakeRadius = 20;

  // ── ice floor hazard (overlays timed_parkour) ────────────────────────────────
  /**
   * When true, this parkour also punishes stepping on un-recorded ice (structural /
   * level-design flag — marks which shrine uses the floor). The numeric tuning
   * (damage, cooldown) and a global master toggle live in {@code ShrineConfig}
   * (ModMenu), since this JSON is jar-baked and can't be retuned at runtime.
   */
  private boolean iceFloorEnabled = false;
  /** Block IDs treated as deadly ice unless the position is a recorded safe block. */
  private List<String> iceHazardBlocks = List.of(
    "minecraft:ice",
    "minecraft:packed_ice",
    "minecraft:blue_ice",
    "minecraft:frosted_ice"
  );
  /**
   * Optional pinned reset point. When omitted, the player's position at the
   * moment they start the trial is captured and used instead.
   */
  private Vec start;
  /**
   * Baked safe-path positions (version-controlled source of truth), produced by
   * {@code /cobblemon-initiative shrine <id> path export}. Each entry is [x, y, z].
   * Unioned at runtime with any positions recorded into the world save.
   */
  private int[][] safePositions;

  /** Simple position-with-facing holder for {@link #start}. */
  public static class Vec {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
  }

  // ── Getters ─────────────────────────────────────────────────────────────────

  public String getId() { return id; }
  public String getDisplayName() { return displayName; }
  public String getStartTitle() { return startTitle; }
  public String getStartSubtitle() { return startSubtitle; }
  public String getType() { return type; }
  public List<String> getStageTrainerIds() { return stageTrainerIds; }
  public int getFriendshipThreshold() { return friendshipThreshold; }
  public int getFullnessThreshold() { return fullnessThreshold; }
  public String getCultistLeaderTrainerId() { return cultistLeaderTrainerId; }
  public int getTimeLimitSeconds() { return timeLimitSeconds; }
  public String getTargetTrainerId() { return targetTrainerId; }
  public int getEarthquakeIntervalSeconds() { return earthquakeIntervalSeconds; }
  public int getEarthquakeRadius() { return earthquakeRadius; }

  public boolean isIceFloorEnabled() { return iceFloorEnabled; }
  public List<String> getIceHazardBlocks() { return iceHazardBlocks; }
  public Vec getStart() { return start; }
  public int[][] getSafePositions() { return safePositions; }
}

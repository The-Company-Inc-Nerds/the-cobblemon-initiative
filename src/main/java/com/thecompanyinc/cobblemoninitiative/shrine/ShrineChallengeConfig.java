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
}

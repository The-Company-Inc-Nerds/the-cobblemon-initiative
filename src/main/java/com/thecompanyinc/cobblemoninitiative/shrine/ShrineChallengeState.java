package com.thecompanyinc.cobblemoninitiative.shrine;

import java.util.UUID;

public class ShrineChallengeState {

  private final UUID playerId;
  private final String shrineId;

  /** Current stage index.
   *  hydra_gauntlet: 0–2 (which stage trainer to expect next)
   *  fairy_tests:    0–4 (which test is next)
   *  others:         unused (stays 0)
   */
  private int currentStageIndex = 0;

  /** Wall-clock ms when the challenge was started (parkour timer). */
  private long startTimeMs;

  // ── dark_gauntlet tick counters ──────────────────────────────────────────────

  /** Ticks since the last earthquake. Resets to 0 after each quake. */
  private int earthquakeTickCounter = 0;

  /** Ticks since blindness was last refreshed. Resets every 100 ticks (5 s). */
  private int blindnessRefreshTicks = 0;

  /** Last countdown second announced to the player (parkour). -1 = none yet. */
  private long lastAnnouncedSecond = -1;

  /**
   * UUID of the Pokémon the player registered for the fairy_tests Trial of Resolve (test 3).
   * Set when the player runs the test command at stage 2; validated on trainer defeat.
   */
  private UUID fairyTestPokemonUuid = null;

  // ── Constructor ──────────────────────────────────────────────────────────────

  public ShrineChallengeState(UUID playerId, String shrineId) {
    this.playerId = playerId;
    this.shrineId = shrineId;
    this.startTimeMs = System.currentTimeMillis();
  }

  // ── Getters / setters ────────────────────────────────────────────────────────

  public UUID getPlayerId() { return playerId; }
  public String getShrineId() { return shrineId; }

  public int getCurrentStageIndex() { return currentStageIndex; }
  public void setCurrentStageIndex(int index) { this.currentStageIndex = index; }

  public long getStartTimeMs() { return startTimeMs; }
  public void setStartTimeMs(long ms) { this.startTimeMs = ms; }

  public int getEarthquakeTickCounter() { return earthquakeTickCounter; }
  public void setEarthquakeTickCounter(int t) { this.earthquakeTickCounter = t; }
  public void incrementEarthquakeTicks() { this.earthquakeTickCounter++; }

  public int getBlindnessRefreshTicks() { return blindnessRefreshTicks; }
  public void setBlindnessRefreshTicks(int t) { this.blindnessRefreshTicks = t; }
  public void incrementBlindnessTicks() { this.blindnessRefreshTicks++; }

  public long getLastAnnouncedSecond() { return lastAnnouncedSecond; }
  public void setLastAnnouncedSecond(long s) { this.lastAnnouncedSecond = s; }

  public UUID getFairyTestPokemonUuid() { return fairyTestPokemonUuid; }
  public void setFairyTestPokemonUuid(UUID uuid) { this.fairyTestPokemonUuid = uuid; }
}

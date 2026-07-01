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

  // ── ice floor hazard ─────────────────────────────────────────────────────────

  /** Ticks of immunity remaining after a hazard hit (debounces a single misstep). */
  private int iceHitCooldown = 0;

  /** Reset point the ice floor teleports the player back to. Captured on start. */
  private double resetX;
  private double resetY;
  private double resetZ;
  private float resetYaw;
  private float resetPitch;

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

  public int getIceHitCooldown() { return iceHitCooldown; }
  public void setIceHitCooldown(int ticks) { this.iceHitCooldown = ticks; }
  public void decrementIceHitCooldown() { if (this.iceHitCooldown > 0) this.iceHitCooldown--; }

  public double getResetX() { return resetX; }
  public double getResetY() { return resetY; }
  public double getResetZ() { return resetZ; }
  public float getResetYaw() { return resetYaw; }
  public float getResetPitch() { return resetPitch; }

  public void setResetPoint(double x, double y, double z, float yaw, float pitch) {
    this.resetX = x;
    this.resetY = y;
    this.resetZ = z;
    this.resetYaw = yaw;
    this.resetPitch = pitch;
  }
}

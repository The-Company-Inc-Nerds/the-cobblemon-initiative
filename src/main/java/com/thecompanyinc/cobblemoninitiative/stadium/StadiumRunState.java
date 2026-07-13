package com.thecompanyinc.cobblemoninitiative.stadium;

import java.util.UUID;

/**
 * Mutable per-player state for one Company Exhibition Circuit run (see
 * {@link StadiumManager}). Holds the chosen level bracket, the wave cursor, the
 * countdown to the next wave dispatch, and — critically — the identity of the run's
 * own dispatched battle: victory/flee resolution matches on this battle id, never on
 * trainer names (stadium teams stay OUT of the TrainerConfig database so gym-progress
 * name matching can never fire on them).
 */
public class StadiumRunState {

  /** Where the run's wave loop currently is (driven by {@link StadiumManager#tick}). */
  public enum Phase {
    /** Counting down to the next wave dispatch. */
    COUNTDOWN,
    /**
     * The wave's tbcs battle command was dispatched; waiting to identify the battle in
     * Cobblemon's BattleRegistry (normally the same tick — the watchdog catches a
     * refused/failed dispatch, e.g. an unregistered trainer id).
     */
    AWAITING_BATTLE,
    /** The wave battle is live; Cobblemon BATTLE_VICTORY / BATTLE_FLED resolve it. */
    IN_BATTLE,
  }

  private final UUID playerId;
  private final int bracket;
  /** 0-based index of the wave currently being fought / dispatched next. */
  private int waveIndex;
  private Phase phase = Phase.COUNTDOWN;
  private int ticksToNextWave;
  /** Watchdog counter while {@link Phase#AWAITING_BATTLE}. */
  private int captureTicks;
  /** Battle id of the run's own dispatched battle (null outside IN_BATTLE). */
  private UUID battleId;

  public StadiumRunState(UUID playerId, int bracket, int firstWaveDelayTicks) {
    this.playerId = playerId;
    this.bracket = bracket;
    this.ticksToNextWave = firstWaveDelayTicks;
  }

  public UUID getPlayerId() { return playerId; }
  public int getBracket() { return bracket; }

  public int getWaveIndex() { return waveIndex; }
  public void setWaveIndex(int waveIndex) { this.waveIndex = waveIndex; }

  public Phase getPhase() { return phase; }
  public void setPhase(Phase phase) { this.phase = phase; }

  public int getTicksToNextWave() { return ticksToNextWave; }
  public void setTicksToNextWave(int ticks) { this.ticksToNextWave = ticks; }
  public int decrementTicksToNextWave() { return --ticksToNextWave; }

  public int getCaptureTicks() { return captureTicks; }
  public void resetCaptureTicks() { this.captureTicks = 0; }
  public int incrementCaptureTicks() { return ++captureTicks; }

  public UUID getBattleId() { return battleId; }
  public void setBattleId(UUID battleId) { this.battleId = battleId; }
}

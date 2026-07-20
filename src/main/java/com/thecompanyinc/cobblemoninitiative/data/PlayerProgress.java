package com.thecompanyinc.cobblemoninitiative.data;

import java.util.*;

public class PlayerProgress {

  private UUID playerId;
  private Set<String> defeatedTrainers = new HashSet<>();
  private Set<String> earnedAchievements = new HashSet<>();
  private int currentLevelCap = 15;

  /**
   * Story quests completed (holder ids that transitioned active → cleared on the
   * {@code ci_quest} scoreboard). A persisted Set — not a raw int — so counting is
   * idempotent across restarts and a re-added quest score can never double-count.
   * Backs the quest-count achievement tiers.
   */
  private Set<String> completedQuests = new HashSet<>();

  /**
   * Permanent Pokémon losses this run (faint-removal, sacrifice, duplicate release).
   * Incremented at the single loss choke point (StreamSyncEvents.pokemonLost) so it is
   * tracked ALWAYS — independent of whether the stream overlay is enabled — which the
   * "deathless / flawless" achievement tiers depend on for a truthful zero.
   */
  private int pokemonLost = 0;

  /**
   * Whether {@link #pokemonLost} is a TRUSTWORTHY run total. True for a player first seen under
   * this feature (losses counted from zero) or a save whose count was reconciled from the stream
   * stats on join; false for a legacy save that predates the counter AND had no stream stats to
   * seed from — there we cannot know if prior losses happened, so the deathless/flawless tiers
   * stay ungranted rather than false-positive. Defaults true; the loader sets it false for a
   * pre-feature record (see PlayerProgressManager).
   */
  private boolean deathsKnown = true;

  public PlayerProgress(UUID playerId) {
    this.playerId = playerId;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public Set<String> getDefeatedTrainers() {
    return defeatedTrainers;
  }

  public boolean hasDefeatedTrainer(String trainerId) {
    return defeatedTrainers.contains(trainerId);
  }

  public void addDefeatedTrainer(String trainerId) {
    defeatedTrainers.add(trainerId);
  }

  public Set<String> getEarnedAchievements() {
    return earnedAchievements;
  }

  public boolean hasAchievement(String achievementId) {
    return earnedAchievements.contains(achievementId);
  }

  public void addAchievement(String achievementId) {
    earnedAchievements.add(achievementId);
  }

  public int getCurrentLevelCap() {
    return currentLevelCap;
  }

  public void setCurrentLevelCap(int levelCap) {
    this.currentLevelCap = levelCap;
  }

  public Set<String> getCompletedQuests() {
    return completedQuests;
  }

  /** @return true if this holder had not been counted before (a genuine completion). */
  public boolean markQuestCompleted(String questHolder) {
    return completedQuests.add(questHolder);
  }

  public int getCompletedQuestCount() {
    return completedQuests.size();
  }

  public int getPokemonLost() {
    return pokemonLost;
  }

  public void incrementPokemonLost() {
    pokemonLost++;
  }

  public void setPokemonLost(int value) {
    this.pokemonLost = value;
  }

  public boolean isDeathsKnown() {
    return deathsKnown;
  }

  public void setDeathsKnown(boolean known) {
    this.deathsKnown = known;
  }
}

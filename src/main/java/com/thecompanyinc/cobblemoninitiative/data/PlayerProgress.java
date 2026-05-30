package com.thecompanyinc.cobblemoninitiative.data;

import java.util.*;

public class PlayerProgress {

  private UUID playerId;
  private Set<String> defeatedTrainers = new HashSet<>();
  private Set<String> earnedAchievements = new HashSet<>();
  private int currentLevelCap = 20;

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
}

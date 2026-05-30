package com.thecompanyinc.cobblemoninitiative.levelcap;

import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LevelCapManager {

  private final ConfigLoader configLoader;

  public LevelCapManager(ConfigLoader configLoader) {
    this.configLoader = configLoader;
  }

  public int getLevelCap(ServerPlayer player) {
    PlayerProgress progress =
      AchievementsInit.getProgressManager().getProgress(player);
    return progress.getCurrentLevelCap();
  }

  public void updateLevelCap(ServerPlayer player) {
    PlayerProgress progress =
      AchievementsInit.getProgressManager().getProgress(player);
    List<LevelCapConfig> levelCaps = configLoader.getLevelCaps();

    int newCap = 20;

    for (LevelCapConfig cap : levelCaps) {
      if (progress.hasAchievement(cap.getAchievementId())) {
        if (cap.getLevelCap() > newCap) {
          newCap = cap.getLevelCap();
        }
      }
    }

    if (newCap != progress.getCurrentLevelCap()) {
      progress.setCurrentLevelCap(newCap);
      player.sendSystemMessage(
        Component.literal("§6Level cap increased to §e" + newCap + "§6!")
      );
    }
  }

  public int getNextLevelCap(ServerPlayer player) {
    PlayerProgress progress =
      AchievementsInit.getProgressManager().getProgress(player);
    int currentCap = progress.getCurrentLevelCap();

    for (LevelCapConfig cap : configLoader.getLevelCaps()) {
      if (
        cap.getLevelCap() > currentCap &&
        !progress.hasAchievement(cap.getAchievementId())
      ) {
        return cap.getLevelCap();
      }
    }

    return 100;
  }

  public String getNextLevelCapRequirement(ServerPlayer player) {
    PlayerProgress progress =
      AchievementsInit.getProgressManager().getProgress(player);
    int currentCap = progress.getCurrentLevelCap();

    for (LevelCapConfig cap : configLoader.getLevelCaps()) {
      if (
        cap.getLevelCap() > currentCap &&
        !progress.hasAchievement(cap.getAchievementId())
      ) {
        return cap.getDescription();
      }
    }

    return "Max level reached!";
  }
}

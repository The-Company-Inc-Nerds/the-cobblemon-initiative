package com.thecompanyinc.cobblemoninitiative.levelcap;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LevelCapManager {

  private final ConfigLoader configLoader;

  public LevelCapManager(ConfigLoader configLoader) {
    this.configLoader = configLoader;
  }

  /** Cap = base (levelcaps.json root) raised by every EARNED cap achievement. Computed
   *  FRESH from achievements — never a stale persisted field. (Was returning the stored
   *  currentLevelCap, which defaulted to 20 and only refreshed on a trainer defeat, so a
   *  fresh player was clamped at 20 until their first gym win.) */
  private int computeLevelCap(PlayerProgress progress) {
    int cap = ProgressionConfig.get().getBaseLevelCap();
    for (LevelCapConfig c : configLoader.getLevelCaps()) {
      if (progress.hasAchievement(c.getAchievementId()) && c.getLevelCap() > cap) {
        cap = c.getLevelCap();
      }
    }
    return cap;
  }

  public int getLevelCap(ServerPlayer player) {
    return computeLevelCap(InitiativeInit.getProgressManager().getProgress(player));
  }

  /** UUID overload for owner-less contexts (daycare custody mons clamp against the
   *  boarding player's cap even when that player is offline). */
  public int getLevelCap(UUID playerId) {
    return computeLevelCap(InitiativeInit.getProgressManager().getProgress(playerId));
  }

  public void updateLevelCap(ServerPlayer player) {
    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
    int newCap = computeLevelCap(progress);
    if (newCap != progress.getCurrentLevelCap()) {
      progress.setCurrentLevelCap(newCap);
      player.sendSystemMessage(
        Component.literal("§6Level cap increased to §e" + newCap + "§6!")
      );
    }
  }

  public int getNextLevelCap(ServerPlayer player) {
    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
    int currentCap = progress.getCurrentLevelCap();

    for (LevelCapConfig cap : configLoader.getLevelCaps()) {
      if (
        cap.getLevelCap() > currentCap &&
        !progress.hasAchievement(cap.getAchievementId())
      ) {
        return cap.getLevelCap();
      }
    }

    return ProgressionConfig.get().getChampionLevelCap();
  }

  public String getNextLevelCapRequirement(ServerPlayer player) {
    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
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

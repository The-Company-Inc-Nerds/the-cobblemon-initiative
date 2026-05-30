package com.thecompanyinc.cobblemoninitiative.advancement;

import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import net.minecraft.advancements.CriteriaTriggers;

public class CobblemonInitiativeCriteria {

  public static TrainerDefeatedCriterion TRAINER_DEFEATED;

  public static void register() {
    TRAINER_DEFEATED = CriteriaTriggers.register(
      AchievementsInit.MOD_ID + ":trainer_defeated",
      new TrainerDefeatedCriterion()
    );

    AchievementsInit.LOGGER.info("Registered custom advancement criteria");
  }
}

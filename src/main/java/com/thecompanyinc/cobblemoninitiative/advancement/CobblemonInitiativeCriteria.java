package com.thecompanyinc.cobblemoninitiative.advancement;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.minecraft.advancements.CriteriaTriggers;

public class CobblemonInitiativeCriteria {

  public static TrainerDefeatedCriterion TRAINER_DEFEATED;

  public static void register() {
    TRAINER_DEFEATED = CriteriaTriggers.register(
      InitiativeInit.MOD_ID + ":trainer_defeated",
      new TrainerDefeatedCriterion()
    );

    InitiativeInit.LOGGER.info("Registered custom advancement criteria");
  }
}

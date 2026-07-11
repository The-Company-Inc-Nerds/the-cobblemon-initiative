package com.thecompanyinc.cobblemoninitiative.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link Mob}'s goal selector so NPC Sight can purge an Easy NPC follow goal that
 * was orphaned in it: a preset import replaces the entity's ObjectiveDataSet without
 * unregistering the goals the old set had registered, and once the backing data entry is
 * gone {@code easy_npc objective remove} hard-fails — leaving the goal pathing until the
 * entity reloads. Purging via the vanilla selector needs no Easy NPC compile-time dep.
 */
@Mixin(Mob.class)
public interface MobGoalSelectorAccessor {

  @Accessor("goalSelector")
  GoalSelector getGoalSelector();
}

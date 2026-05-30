package com.thecompanyinc.cobblemoninitiative.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class TrainerDefeatedCriterion
  extends SimpleCriterionTrigger<TrainerDefeatedCriterion.TriggerInstance>
{

  @Override
  public Codec<TriggerInstance> codec() {
    return TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer player, String trainerId) {
    this.trigger(player, instance -> instance.matches(trainerId));
  }

  public record TriggerInstance(
    Optional<ContextAwarePredicate> player,
    String trainerId
  ) implements SimpleCriterionTrigger.SimpleInstance {
    public static final Codec<TriggerInstance> CODEC =
      RecordCodecBuilder.create(instance ->
        instance
          .group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(
              "player"
            ).forGetter(TriggerInstance::player),
            Codec.STRING.fieldOf("trainer_id").forGetter(
              TriggerInstance::trainerId
            )
          )
          .apply(instance, TriggerInstance::new)
      );

    public boolean matches(String trainerId) {
      return this.trainerId.equals(trainerId);
    }
  }
}

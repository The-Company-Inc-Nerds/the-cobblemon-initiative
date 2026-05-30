package com.thecompanyinc.cobblemoninitiative;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.thecompanyinc.cobblemoninitiative.command.CobblemonInitiativeCommands;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgressManager;
import com.thecompanyinc.cobblemoninitiative.items.ModItems;
import com.thecompanyinc.cobblemoninitiative.levelcap.LevelCapManager;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AchievementsInit implements ModInitializer {

  public static final String MOD_ID = "cobblemon-initiative";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static ConfigLoader configLoader;
  private static PlayerProgressManager progressManager;
  private static LevelCapManager levelCapManager;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing AchievementsInit...");

    ModItems.register();

    FabricLoader.getInstance()
      .getModContainer(MOD_ID)
      .ifPresent(container -> {
        ResourceManagerHelper.registerBuiltinResourcePack(
          ResourceLocation.fromNamespaceAndPath(MOD_ID, "trainer_textures"),
          container,
          Component.literal("AchievementsInit Trainer Textures"),
          ResourcePackActivationType.DEFAULT_ENABLED
        );
      });

    configLoader = new ConfigLoader();
    configLoader.loadAllConfigs();

    progressManager = new PlayerProgressManager();
    levelCapManager = new LevelCapManager(configLoader);

    registerBattleEvents();

    CommandRegistrationCallback.EVENT.register(
      (dispatcher, registryAccess, environment) -> {
        CobblemonInitiativeCommands.register(dispatcher);
      }
    );

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      progressManager.loadProgress(server);
      LOGGER.info("AchievementsInit loaded player progress data.");
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      progressManager.saveProgress(server);
      LOGGER.info("AchievementsInit saved player progress data.");
    });

    LOGGER.info("AchievementsInit initialized successfully!");
  }

  private void registerBattleEvents() {
    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
      PokemonBattle battle = event.getBattle();

      for (BattleActor winner : event.getWinners()) {
        if (winner instanceof PlayerBattleActor playerActor) {
          ServerPlayer player = (ServerPlayer) playerActor.getEntity();
          if (player == null) continue;

          for (BattleActor loser : event.getLosers()) {
            String trainerName = loser.getName().getString();

            LOGGER.debug(
              "Player {} won battle against: {}",
              player.getName().getString(),
              trainerName
            );

            for (TrainerConfig trainer : configLoader.getAllTrainers()) {
              if (
                trainer.getDisplayName().equalsIgnoreCase(trainerName) ||
                trainer.getName().equalsIgnoreCase(trainerName) ||
                trainerName.contains(trainer.getDisplayName())
              ) {
                LOGGER.info(
                  "Player {} defeated trainer {} ({})",
                  player.getName().getString(),
                  trainer.getDisplayName(),
                  trainer.getId()
                );

                progressManager.onTrainerDefeated(player, trainer.getId());
                break;
              }
            }
          }
        }
      }

      return Unit.INSTANCE;
    });

    LOGGER.info("Registered Cobblemon battle victory event listener");
  }

  public static ConfigLoader getConfigLoader() {
    return configLoader;
  }

  public static PlayerProgressManager getProgressManager() {
    return progressManager;
  }

  public static LevelCapManager getLevelCapManager() {
    return levelCapManager;
  }
}

package com.thecompanyinc.cobblemoninitiative.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.LevelResource;

public class PlayerProgressManager {

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .create();
  private static final String PROGRESS_FILE_NAME =
    "cobblemon_initiative_progress.json";
  private final Map<UUID, PlayerProgress> playerProgress = new HashMap<>();

  public PlayerProgress getProgress(UUID playerId) {
    return playerProgress.computeIfAbsent(playerId, PlayerProgress::new);
  }

  public PlayerProgress getProgress(ServerPlayer player) {
    return getProgress(player.getUUID());
  }

  private Path getSavePath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(PROGRESS_FILE_NAME);
  }

  public void loadProgress(MinecraftServer server) {
    Path savePath = getSavePath(server);
    if (Files.exists(savePath)) {
      try (
        Reader reader = Files.newBufferedReader(
          savePath,
          StandardCharsets.UTF_8
        )
      ) {
        Type type = new TypeToken<
          Map<String, PlayerProgressData>
        >() {}.getType();
        Map<String, PlayerProgressData> data = GSON.fromJson(reader, type);

        if (data != null) {
          playerProgress.clear();
          for (Map.Entry<String, PlayerProgressData> entry : data.entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            PlayerProgress progress = new PlayerProgress(uuid);
            progress
              .getDefeatedTrainers()
              .addAll(entry.getValue().defeatedTrainers);
            progress
              .getEarnedAchievements()
              .addAll(entry.getValue().earnedAchievements);
            progress.setCurrentLevelCap(entry.getValue().currentLevelCap);
            playerProgress.put(uuid, progress);
          }
        }
        InitiativeInit.LOGGER.info(
          "Loaded progress for {} players",
          playerProgress.size()
        );
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("Failed to load player progress", e);
      }
    }
  }

  public void saveProgress(MinecraftServer server) {
    Path savePath = getSavePath(server);
    try {
      Files.createDirectories(savePath.getParent());

      Map<String, PlayerProgressData> data = new HashMap<>();
      for (Map.Entry<UUID, PlayerProgress> entry : playerProgress.entrySet()) {
        PlayerProgressData progressData = new PlayerProgressData();
        progressData.defeatedTrainers = new ArrayList<>(
          entry.getValue().getDefeatedTrainers()
        );
        progressData.earnedAchievements = new ArrayList<>(
          entry.getValue().getEarnedAchievements()
        );
        progressData.currentLevelCap = entry.getValue().getCurrentLevelCap();
        data.put(entry.getKey().toString(), progressData);
      }

      try (
        Writer writer = Files.newBufferedWriter(
          savePath,
          StandardCharsets.UTF_8
        )
      ) {
        GSON.toJson(data, writer);
      }
      InitiativeInit.LOGGER.debug(
        "Saved progress for {} players",
        data.size()
      );
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to save player progress", e);
    }
  }

  public boolean canBattleTrainer(ServerPlayer player, String trainerId) {
    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) return false;

    PlayerProgress progress = getProgress(player);

    for (String prereq : trainer.getPrerequisites()) {
      if (!progress.hasDefeatedTrainer(prereq)) {
        return false;
      }
    }

    return true;
  }

  public List<String> getMissingPrerequisites(
    ServerPlayer player,
    String trainerId
  ) {
    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) return Collections.emptyList();

    PlayerProgress progress = getProgress(player);
    List<String> missing = new ArrayList<>();

    for (String prereq : trainer.getPrerequisites()) {
      if (!progress.hasDefeatedTrainer(prereq)) {
        TrainerConfig prereqTrainer =
          InitiativeInit.getConfigLoader().getTrainer(prereq);
        if (prereqTrainer != null) {
          missing.add(prereqTrainer.getDisplayName());
        } else {
          missing.add(prereq);
        }
      }
    }

    return missing;
  }

  public void onTrainerDefeated(ServerPlayer player, String trainerId) {
    PlayerProgress progress = getProgress(player);

    if (progress.hasDefeatedTrainer(trainerId)) {
      InitiativeInit.LOGGER.debug(
        "Player {} already defeated {}",
        player.getName().getString(),
        trainerId
      );
      return;
    }

    progress.addDefeatedTrainer(trainerId);
    InitiativeInit.LOGGER.info(
      "Player {} defeated trainer {}",
      player.getName().getString(),
      trainerId
    );

    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer != null) {
      grantRewards(player, trainer);

      spawnDefeatPokemon(player, trainer);
    }

    grantShrineCrystal(player, trainerId);

    grantAdvancementForTrainer(player, trainerId);

    checkMultiTrainerAchievements(player);

    InitiativeInit.getLevelCapManager().updateLevelCap(player);

    // Notify shrine challenge manager — advances hydra stages, completes dark gauntlet
    if (InitiativeInit.getShrineChallengeManager() != null) {
      InitiativeInit.getShrineChallengeManager().onTrainerDefeated(
        player,
        trainerId
      );
    }

    if (player.getServer() != null) {
      saveProgress(player.getServer());
    }
  }

  private void grantRewards(ServerPlayer player, TrainerConfig trainer) {
    if (trainer.getRewards() == null) return;

    for (TrainerConfig.RewardConfig reward : trainer.getRewards()) {
      try {
        if ("item".equals(reward.getType()) && reward.getItem() != null) {
          String command = String.format(
            "give %s %s %d",
            player.getName().getString(),
            reward.getItem(),
            reward.getCount()
          );
          executeCommand(player, command);
          InitiativeInit.LOGGER.debug(
            "Granted {} x{} to {}",
            reward.getItem(),
            reward.getCount(),
            player.getName().getString()
          );
        } else if (
          "command".equals(reward.getType()) && reward.getCommand() != null
        ) {
          String command = reward
            .getCommand()
            .replace("{player}", player.getName().getString())
            .replace("{uuid}", player.getUUID().toString());
          executeCommand(player, command);
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to grant reward: {}",
          reward,
          e
        );
      }
    }
  }

  private void grantShrineCrystal(ServerPlayer player, String trainerId) {
    boolean isShrine = switch (trainerId) {
      case "fire_shrine_leader", "ground_shrine_leader", "ice_shrine_leader",
           "dragon_shrine_leader", "fairy_shrine_leader" -> true;
      default -> false;
    };

    if (isShrine) {
      // Crystal item is granted via TrainerConfig rewards; only send the flavour message here.
      player.sendSystemMessage(
        Component.literal(
          "§d§l[Shrine Crystal Obtained!] §r§7Place it to summon the legendary guardian!"
        )
      );
    }
  }

  private void spawnDefeatPokemon(ServerPlayer player, TrainerConfig trainer) {
    TrainerConfig.SpawnOnDefeatConfig spawnConfig = trainer.getSpawnOnDefeat();
    if (spawnConfig == null || spawnConfig.getSpecies() == null) {
      return;
    }

    ServerLevel level = player.serverLevel();

    int[] trainerCoords = trainer.getCoordinates();
    int[] offset = spawnConfig.getSpawnOffset();

    int spawnX, spawnY, spawnZ;
    if (trainerCoords != null && trainerCoords.length == 3) {
      spawnX =
        trainerCoords[0] +
        (offset != null && offset.length > 0 ? offset[0] : 0);
      spawnY =
        trainerCoords[1] +
        (offset != null && offset.length > 1 ? offset[1] : 1);
      spawnZ =
        trainerCoords[2] +
        (offset != null && offset.length > 2 ? offset[2] : 0);
    } else {
      spawnX =
        player.getBlockX() +
        (offset != null && offset.length > 0 ? offset[0] : 0);
      spawnY =
        player.getBlockY() +
        (offset != null && offset.length > 1 ? offset[1] : 1);
      spawnZ =
        player.getBlockZ() +
        (offset != null && offset.length > 2 ? offset[2] : 0);
    }

    playPokemonSpawnEffects(level, new BlockPos(spawnX, spawnY, spawnZ));

    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder.append("spawnpokemon ").append(spawnConfig.getSpecies());
    commandBuilder.append(" level=").append(spawnConfig.getLevel());

    if (spawnConfig.isShiny()) {
      commandBuilder.append(" shiny=true");
    }

    if (spawnConfig.getAspects() != null) {
      for (String aspect : spawnConfig.getAspects()) {
        commandBuilder.append(" ").append(aspect);
      }
    }

    String fullCommand = String.format(
      "execute positioned %d %d %d run %s",
      spawnX,
      spawnY,
      spawnZ,
      commandBuilder.toString()
    );

    InitiativeInit.LOGGER.info("Spawning defeat Pokemon: {}", fullCommand);
    executeCommand(player, fullCommand);

    String message = spawnConfig.getMessage();
    if (message == null || message.isEmpty()) {
      message = "§d§lA wild " + spawnConfig.getSpecies() + " appeared!";
    }
    player.sendSystemMessage(Component.literal(message));
  }

  private void playPokemonSpawnEffects(ServerLevel level, BlockPos pos) {
    double x = pos.getX() + 0.5;
    double y = pos.getY() + 1.0;
    double z = pos.getZ() + 0.5;

    level.playSound(
      null,
      pos,
      SoundEvents.AMETHYST_BLOCK_CHIME,
      SoundSource.NEUTRAL,
      1.0f,
      0.8f
    );
    level.playSound(
      null,
      pos,
      SoundEvents.BEACON_ACTIVATE,
      SoundSource.NEUTRAL,
      0.5f,
      1.5f
    );

    level.sendParticles(
      ParticleTypes.END_ROD,
      x,
      y,
      z,
      30,
      0.5,
      0.5,
      0.5,
      0.05
    );
    level.sendParticles(
      ParticleTypes.TOTEM_OF_UNDYING,
      x,
      y,
      z,
      20,
      0.3,
      0.3,
      0.3,
      0.1
    );
  }

  private void grantAdvancementForTrainer(
    ServerPlayer player,
    String trainerId
  ) {
    String advancementPath = switch (trainerId) {
      case "hua_zhan_leader" -> "cobblemon_initiative:gyms/badge_grass";
      case "takehara_leader" -> "cobblemon_initiative:gyms/badge_bug";
      case "mystic_leader" -> "cobblemon_initiative:gyms/badge_fairy";
      case "deepcore_leader" -> "cobblemon_initiative:gyms/badge_fighting";
      case "gaviota_leader" -> "cobblemon_initiative:gyms/badge_water";
      case "kalahar_leader" -> "cobblemon_initiative:gyms/badge_ground";
      case "cyber_leader" -> "cobblemon_initiative:gyms/badge_electric";
      case "ryujin_leader" -> "cobblemon_initiative:gyms/badge_dragon";
      case "nifl_leader" -> "cobblemon_initiative:gyms/badge_ice";
      case "scorchspire_leader" -> "cobblemon_initiative:gyms/badge_fire";
      case "fire_shrine_leader" -> "cobblemon_initiative:shrines/fire_shrine";
      case "ground_shrine_leader" -> "cobblemon_initiative:shrines/ground_shrine";
      case "ice_shrine_leader" -> "cobblemon_initiative:shrines/ice_shrine";
      case "dragon_shrine_leader" -> "cobblemon_initiative:shrines/dragon_shrine";
      case "fairy_shrine_leader" -> "cobblemon_initiative:shrines/fairy_shrine";
      case "royal_champion" -> "cobblemon_initiative:royal_league/champion";
      default -> null;
    };

    if (advancementPath != null) {
      grantAdvancement(player, advancementPath);
    }
  }

  private void checkMultiTrainerAchievements(ServerPlayer player) {
    PlayerProgress progress = getProgress(player);

    if (!progress.hasAchievement("all_badges")) {
      List<com.thecompanyinc.cobblemoninitiative.config.TrainerConfig> gymLeaders =
        InitiativeInit.getConfigLoader().getTrainersByCategory("gym").stream()
          .filter(t -> "leader".equals(t.getTrainerType()))
          .toList();
      boolean hasAllBadges = !gymLeaders.isEmpty() &&
        gymLeaders.stream().allMatch(t -> progress.hasDefeatedTrainer(t.getId()));

      if (hasAllBadges) {
        progress.addAchievement("all_badges");
        grantAdvancement(player, "cobblemon_initiative:gyms/all_badges");
        player.sendSystemMessage(
          Component.literal(
            "§6§l[Achievement Unlocked] §r§ePokemon League Qualified!"
          )
        );
        player.sendSystemMessage(
          Component.literal("§7You have collected all " + gymLeaders.size() + " Gym Badges!")
        );
        InitiativeInit.LOGGER.info(
          "Player {} earned all_badges achievement",
          player.getName().getString()
        );
      }
    }

    if (!progress.hasAchievement("all_shrines")) {
      List<com.thecompanyinc.cobblemoninitiative.config.TrainerConfig> shrineLeaders =
        InitiativeInit.getConfigLoader().getTrainersByCategory("shrine").stream()
          .filter(t -> "cult_leader".equals(t.getTrainerType()))
          .toList();
      boolean hasAllShrines = !shrineLeaders.isEmpty() &&
        shrineLeaders.stream().allMatch(t -> progress.hasDefeatedTrainer(t.getId()));

      if (hasAllShrines) {
        progress.addAchievement("all_shrines");
        grantAdvancement(player, "cobblemon_initiative:shrines/all_shrines");
        player.sendSystemMessage(
          Component.literal("§6§l[Achievement Unlocked] §r§eLegendary Seeker!")
        );
        player.sendSystemMessage(
          Component.literal("§7You have conquered all " + shrineLeaders.size() + " Legendary Shrines!")
        );
        InitiativeInit.LOGGER.info(
          "Player {} earned all_shrines achievement",
          player.getName().getString()
        );
      }
    }
  }

  private void grantAdvancement(ServerPlayer player, String advancementPath) {
    String command = String.format(
      "advancement grant %s only %s",
      player.getName().getString(),
      advancementPath
    );
    executeCommand(player, command);
    InitiativeInit.LOGGER.debug(
      "Granted advancement {} to {}",
      advancementPath,
      player.getName().getString()
    );
  }

  private void executeCommand(ServerPlayer player, String command) {
    if (player.getServer() != null) {
      player
        .getServer()
        .getCommands()
        .performPrefixedCommand(
          player.getServer().createCommandSourceStack().withPermission(4),
          command
        );
    }
  }

  private static class PlayerProgressData {

    List<String> defeatedTrainers = new ArrayList<>();
    List<String> earnedAchievements = new ArrayList<>();
    int currentLevelCap = 20;
  }
}

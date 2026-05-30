package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigLoader {

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .create();

  private final Map<String, TrainerConfig> trainers = new HashMap<>();
  private final List<LevelCapConfig> levelCaps = new ArrayList<>();

  public void loadAllConfigs() {
    loadTrainers();
    loadLevelCaps();
  }

  private void loadTrainers() {
    String[] trainerPaths = {
      "data/cobblemon-initiative/trainers/gyms/hua_zhan_city.json",
      "data/cobblemon-initiative/trainers/gyms/gaviota_port.json",
      "data/cobblemon-initiative/trainers/gyms/cyber_city.json",
      "data/cobblemon-initiative/trainers/gyms/mystic_marsh.json",
      "data/cobblemon-initiative/trainers/gyms/takehara_falls.json",
      "data/cobblemon-initiative/trainers/gyms/nifl_town.json",
      "data/cobblemon-initiative/trainers/gyms/deepcore_city.json",
      "data/cobblemon-initiative/trainers/gyms/kalahar_reach.json",
      "data/cobblemon-initiative/trainers/gyms/scorchspire.json",
      "data/cobblemon-initiative/trainers/gyms/ryujin_keep.json",
      "data/cobblemon-initiative/trainers/shrines/fire_shrine.json",
      "data/cobblemon-initiative/trainers/shrines/ground_shrine.json",
      "data/cobblemon-initiative/trainers/shrines/ice_shrine.json",
      "data/cobblemon-initiative/trainers/shrines/dragon_shrine.json",
      "data/cobblemon-initiative/trainers/shrines/fairy_shrine.json",
      "data/cobblemon-initiative/trainers/royal_league/royal_league.json",
      "data/cobblemon-initiative/trainers/battle_frontier/battle_frontier.json",
      "data/cobblemon-initiative/trainers/villain_team/villain_team.json",
    };

    for (String path : trainerPaths) {
      try {
        InputStream stream = getClass()
          .getClassLoader()
          .getResourceAsStream(path);
        if (stream != null) {
          InputStreamReader reader = new InputStreamReader(
            stream,
            StandardCharsets.UTF_8
          );
          Type listType = new TypeToken<List<TrainerConfig>>() {}.getType();
          List<TrainerConfig> trainerList = GSON.fromJson(reader, listType);

          if (trainerList != null) {
            for (TrainerConfig trainer : trainerList) {
              trainers.put(trainer.getId(), trainer);
              AchievementsInit.LOGGER.debug(
                "Loaded trainer: {}",
                trainer.getId()
              );
            }
          }
          reader.close();
        }
      } catch (Exception e) {
        AchievementsInit.LOGGER.error(
          "Failed to load trainer config: {}",
          path,
          e
        );
      }
    }

    AchievementsInit.LOGGER.info("Loaded {} trainers", trainers.size());
  }

  private void loadLevelCaps() {
    try {
      InputStream stream = getClass()
        .getClassLoader()
        .getResourceAsStream(
          "data/cobblemon-initiative/levelcaps/levelcaps.json"
        );
      if (stream != null) {
        InputStreamReader reader = new InputStreamReader(
          stream,
          StandardCharsets.UTF_8
        );
        Type listType = new TypeToken<List<LevelCapConfig>>() {}.getType();
        List<LevelCapConfig> capList = GSON.fromJson(reader, listType);

        if (capList != null) {
          levelCaps.addAll(capList);
          levelCaps.sort(Comparator.comparingInt(LevelCapConfig::getOrder));
        }
        reader.close();
      }
    } catch (Exception e) {
      AchievementsInit.LOGGER.error("Failed to load level caps config", e);
    }

    AchievementsInit.LOGGER.info(
      "Loaded {} level cap stages",
      levelCaps.size()
    );
  }

  public List<TrainerConfig> getTrainersByGroup(String group) {
    List<TrainerConfig> result = new ArrayList<>();
    for (TrainerConfig trainer : trainers.values()) {
      if (group.equals(trainer.getGroup())) {
        result.add(trainer);
      }
      else if (trainer.getId().startsWith(group + "_")) {
        result.add(trainer);
      }
    }
    return result;
  }

  public List<TrainerConfig> getTrainersByCategory(String category) {
    List<TrainerConfig> result = new ArrayList<>();
    for (TrainerConfig trainer : trainers.values()) {
      if (category.equals(trainer.getCategory())) {
        result.add(trainer);
      }
    }
    return result;
  }

  public Set<String> getAvailableGroups() {
    Set<String> groups = new HashSet<>();
    for (TrainerConfig trainer : trainers.values()) {
      if (trainer.getGroup() != null) {
        groups.add(trainer.getGroup());
      }
      String id = trainer.getId();
      if (id.contains("_")) {
        int lastUnderscore = id.lastIndexOf('_');
        if (lastUnderscore > 0) {
          String prefix = id.substring(0, lastUnderscore);
          if (prefix.contains("_")) {
            int secondLastUnderscore = prefix.lastIndexOf('_');
            groups.add(
              prefix.substring(0, secondLastUnderscore + 1).replaceAll("_$", "")
            );
          }
          groups.add(prefix);
        }
      }
    }
    groups.addAll(
      List.of(
        "hua_zhan",
        "takehara",
        "mystic",
        "deepcore",
        "gaviota",
        "kalahar",
        "cyber",
        "ryujin",
        "nifl",
        "scorchspire",
        "fire_shrine",
        "ground_shrine",
        "ice_shrine",
        "dragon_shrine",
        "fairy_shrine",
        "royal"
      )
    );
    return groups;
  }

  public TrainerConfig getTrainer(String id) {
    return trainers.get(id);
  }

  public Collection<TrainerConfig> getAllTrainers() {
    return trainers.values();
  }

  public Set<String> getTrainerIds() {
    return trainers.keySet();
  }

  public List<LevelCapConfig> getLevelCaps() {
    return levelCaps;
  }
}

package com.thecompanyinc.cobblemoninitiative.config;

import java.util.ArrayList;
import java.util.List;

public class TrainerConfig {

  private String id;
  private String name;
  private String displayName;
  private String category;
  private String location;
  private String group;
  private int[] coordinates;
  private String trainerType;
  private List<String> prerequisites = new ArrayList<>();
  private String achievementOnDefeat;
  private List<RewardConfig> rewards = new ArrayList<>();
  private SpawnOnDefeatConfig spawnOnDefeat;
  private TeamConfig team;
  private String battleFormat = "GEN_9_SINGLES";
  private AiConfig ai;
  private List<BagItemConfig> bag = new ArrayList<>();

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCategory() {
    return category;
  }

  public String getLocation() {
    return location;
  }

  public String getGroup() {
    return group;
  }

  public int[] getCoordinates() {
    return coordinates;
  }

  public String getTrainerType() {
    return trainerType;
  }

  public List<String> getPrerequisites() {
    return prerequisites;
  }

  public String getAchievementOnDefeat() {
    return achievementOnDefeat;
  }

  public List<RewardConfig> getRewards() {
    return rewards;
  }

  public SpawnOnDefeatConfig getSpawnOnDefeat() {
    return spawnOnDefeat;
  }

  public TeamConfig getTeam() {
    return team;
  }

  public String getBattleFormat() {
    return battleFormat;
  }

  public AiConfig getAi() {
    return ai;
  }

  public List<BagItemConfig> getBag() {
    return bag;
  }

  public static class SpawnOnDefeatConfig {

    private String species;
    private int level = 50;
    private boolean shiny = false;
    private String form;
    private List<String> aspects = new ArrayList<>();
    private int[] spawnOffset = { 0, 1, 0 };
    private String message;

    public String getSpecies() {
      return species;
    }

    public int getLevel() {
      return level;
    }

    public boolean isShiny() {
      return shiny;
    }

    public String getForm() {
      return form;
    }

    public List<String> getAspects() {
      return aspects;
    }

    public int[] getSpawnOffset() {
      return spawnOffset;
    }

    public String getMessage() {
      return message;
    }
  }

  public static class RewardConfig {

    private String type;
    private String item;
    private int count = 1;
    private String command;

    public String getType() {
      return type;
    }

    public String getItem() {
      return item;
    }

    public int getCount() {
      return count;
    }

    public String getCommand() {
      return command;
    }
  }

  public static class TeamConfig {

    private List<PokemonConfig> pokemon = new ArrayList<>();

    public List<PokemonConfig> getPokemon() {
      return pokemon;
    }
  }

  public static class PokemonConfig {

    private String species;
    private String nickname;
    private String gender = "GENDERLESS";
    private int level = 1;
    private String nature;
    private String ability;
    private List<String> moveset = new ArrayList<>();
    private StatConfig ivs;
    private StatConfig evs;
    private boolean shiny = false;
    private String heldItem;
    private List<String> aspects = new ArrayList<>();
    private GimmickConfig gimmicks;

    public String getSpecies() {
      return species;
    }

    public String getNickname() {
      return nickname;
    }

    public String getGender() {
      return gender;
    }

    public int getLevel() {
      return level;
    }

    public String getNature() {
      return nature;
    }

    public String getAbility() {
      return ability;
    }

    public List<String> getMoveset() {
      return moveset;
    }

    public StatConfig getIvs() {
      return ivs;
    }

    public StatConfig getEvs() {
      return evs;
    }

    public boolean isShiny() {
      return shiny;
    }

    public String getHeldItem() {
      return heldItem;
    }

    public List<String> getAspects() {
      return aspects;
    }

    public GimmickConfig getGimmicks() {
      return gimmicks;
    }
  }

  public static class StatConfig {

    private int hp = 0;
    private int atk = 0;
    private int def = 0;
    private int spa = 0;
    private int spd = 0;
    private int spe = 0;

    public int getHp() {
      return hp;
    }

    public int getAtk() {
      return atk;
    }

    public int getDef() {
      return def;
    }

    public int getSpa() {
      return spa;
    }

    public int getSpd() {
      return spd;
    }

    public int getSpe() {
      return spe;
    }
  }

  public static class GimmickConfig {

    private String tera;
    private boolean dynamax = false;
    private boolean gmax = false;

    public String getTera() {
      return tera;
    }

    public boolean isDynamax() {
      return dynamax;
    }

    public boolean isGmax() {
      return gmax;
    }
  }

  public static class AiConfig {

    private String type = "rct";
    private AiDataConfig data;

    public String getType() {
      return type;
    }

    public AiDataConfig getData() {
      return data;
    }
  }

  public static class AiDataConfig {

    private double moveBias = 1.0;
    private double statMoveBias = 0.85;
    private double switchBias = 0.5;
    private double itemBias = 0.85;
    private double maxSelectMargin = 0.25;

    public double getMoveBias() {
      return moveBias;
    }

    public double getStatMoveBias() {
      return statMoveBias;
    }

    public double getSwitchBias() {
      return switchBias;
    }

    public double getItemBias() {
      return itemBias;
    }

    public double getMaxSelectMargin() {
      return maxSelectMargin;
    }
  }

  public static class BagItemConfig {

    private String item;
    private int quantity = 1;

    public String getItem() {
      return item;
    }

    public int getQuantity() {
      return quantity;
    }
  }
}

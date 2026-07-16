package com.thecompanyinc.cobblemoninitiative.momcare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mom's friendship-care tuning (docs/PHONE_AND_CARE.md §3/§4). Leave ONE Pokémon with Mom and it
 * grows friendship (0→cap) each in-game day it boards — distinct from the Sango daycare (which
 * grows XP). A purely-loving, non-Company service; free by default ("she's your mom").
 *
 * File style follows DaycareConfig: config/cobblemon-initiative-momcare.json, defaults on first run.
 */
public class MomCareConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-momcare.json");

  private boolean enabled = true;

  /** Friendship gained per in-game day boarded (a friendship evo needs ~160+). */
  private int friendshipPerDay = 5;
  /** Global scale on the gain. */
  private double rateMultiplier = 1.0;
  /** Ceiling (Cobblemon's own max is 255). */
  private int cap = 255;
  /** Pickup fee — 0 = free (recommended; charged through the daycare pay-probe rail when >0). */
  private int fee = 0;

  public MomCareConfig() {}

  public static MomCareConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          MomCareConfig config = GSON.fromJson(reader, MomCareConfig.class);
          if (config != null) {
            return config;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading momcare config, using defaults: {}", e.getMessage());
    }

    MomCareConfig config = new MomCareConfig();
    config.save();
    return config;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      LOGGER.error("Error saving momcare config: {}", e.getMessage());
    }
  }

  /** Friendship to add per in-game day, after the multiplier (min 0). */
  public int gainPerDay() {
    return Math.max(0, (int) Math.round(friendshipPerDay * rateMultiplier));
  }

  public boolean isEnabled() { return enabled; }
  public int getFriendshipPerDay() { return friendshipPerDay; }
  public double getRateMultiplier() { return rateMultiplier; }
  public int getCap() { return cap; }
  public int getFee() { return fee; }

  public void setEnabled(boolean v) { this.enabled = v; }
  public void setFriendshipPerDay(int v) { this.friendshipPerDay = v; }
  public void setRateMultiplier(double v) { this.rateMultiplier = v; }
  public void setCap(int v) { this.cap = v; }
  public void setFee(int v) { this.fee = v; }
}

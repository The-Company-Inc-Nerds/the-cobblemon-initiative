package com.thecompanyinc.cobblemoninitiative.daycare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daycare tuning knobs. Follows the NuzlockeConfig file style: a flat JSON at
 * config/cobblemon-initiative-daycare.json, written with defaults on first run so the
 * showrunner can tune values (pen coords especially) without a code change.
 */
public class DaycareConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-daycare.json");

  private boolean enabled = true;

  /** XP awarded to each boarded Pokémon per drip interval (pre-clamped to the level cap). */
  private int xpPerInterval = 40;
  /** Drip cadence in game ticks. 1200 = once a minute — deliberately slow. */
  private int intervalTicks = 1200;

  /**
   * The Sango daycare pen where stand-ins appear. 0,0,0 = "unset" — stand-ins then spawn
   * at the depositing player's position until the showrunner marks the real pen.
   */
  private int penX = 0;
  private int penY = 0;
  private int penZ = 0;

  /** Pickup fee: feeBase + feePerLevel × levels gained while boarded. */
  private int feeBase = 100;
  private int feePerLevel = 100;

  public DaycareConfig() {}

  public static DaycareConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          DaycareConfig config = GSON.fromJson(reader, DaycareConfig.class);
          if (config != null) {
            return config;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading daycare config, using defaults: {}", e.getMessage());
    }

    DaycareConfig config = new DaycareConfig();
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
      LOGGER.error("Error saving daycare config: {}", e.getMessage());
    }
  }

  public boolean isEnabled() { return enabled; }
  public int getXpPerInterval() { return xpPerInterval; }
  public int getIntervalTicks() { return intervalTicks; }
  public int getPenX() { return penX; }
  public int getPenY() { return penY; }
  public int getPenZ() { return penZ; }
  public int getFeeBase() { return feeBase; }
  public int getFeePerLevel() { return feePerLevel; }

  // Setters — the ModMenu config screen writes these, then calls save() + manager reload.
  public void setEnabled(boolean v) { this.enabled = v; }
  public void setXpPerInterval(int v) { this.xpPerInterval = v; }
  public void setIntervalTicks(int v) { this.intervalTicks = v; }
  public void setPenX(int v) { this.penX = v; }
  public void setPenY(int v) { this.penY = v; }
  public void setPenZ(int v) { this.penZ = v; }
  public void setFeeBase(int v) { this.feeBase = v; }
  public void setFeePerLevel(int v) { this.feePerLevel = v; }

  /** 0,0,0 means the showrunner has not marked the pen yet. */
  public boolean isPenSet() { return penX != 0 || penY != 0 || penZ != 0; }
}

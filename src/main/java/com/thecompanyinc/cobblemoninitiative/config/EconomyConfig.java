package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Company fee tuning (showrunner 2026-08-03: "it needs to hurt every time you have to pay it").
 * One multiplier scales BOTH recurring Company fees — the Pokécenter heal
 * ({@code economy/heal_paid}: 200 + 100×badges + 2×instability) and the flat town utility
 * fee ({@code UtilityFeeManager}: 2000 CD per station use). 100 = the tuned defaults;
 * 200 doubles the pain; 50 halves it. The heal side reads the value from the
 * {@code #cfg_fee_mult cd_const} scoreboard holder, which {@code UtilityFeeManager.tick}
 * re-asserts every ~10s (the config→scoreboard bridge, orc-spoils precedent) — so a ModMenu
 * change reaches the datapack within seconds, no relog. Singleton like {@link DojoConfig}:
 * cached {@link #get()}, {@link #reload()} after a ModMenu save.
 */
public class EconomyConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-economy.json"
  );

  private static EconomyConfig instance;

  /** Percent multiplier on both Company fees (heal + utility). 100 = tuned defaults. */
  private int feeMultiplierPercent = 100;

  public static EconomyConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static EconomyConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          EconomyConfig cfg = GSON.fromJson(reader, EconomyConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Economy] Error loading config, using defaults: {}", e.getMessage());
    }
    EconomyConfig cfg = new EconomyConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      LOGGER.warn("[Economy] Error saving config: {}", e.getMessage());
    }
  }

  public int getFeeMultiplierPercent() {
    return Math.max(10, Math.min(1000, feeMultiplierPercent));
  }

  public void setFeeMultiplierPercent(int v) {
    this.feeMultiplierPercent = Math.max(10, Math.min(1000, v));
  }
}

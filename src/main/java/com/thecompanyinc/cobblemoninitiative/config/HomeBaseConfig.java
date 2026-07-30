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
 * "Build a home and the world notices." Once a player has placed {@link #blockThreshold} blocks of
 * their own (only possible outside town — see the town build-lock), a Latios appears near the home
 * town and Mom calls about it (see {@code HomeBaseManager}). The threshold is ModMenu-tunable
 * (showrunner picked the 50-100 band); disabling it turns the whole reward off. Singleton like
 * {@link DojoConfig}: cached {@link #get()}, {@link #reload()} after a ModMenu save.
 */
public class HomeBaseConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-homebase.json"
  );

  private static HomeBaseConfig instance;

  /** Master switch for the build-a-home Latios reward. */
  private boolean enabled = true;

  /** Blocks the player must place (outside town) before the reward fires. */
  private int blockThreshold = 75;

  public static HomeBaseConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static HomeBaseConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          HomeBaseConfig cfg = GSON.fromJson(reader, HomeBaseConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[HomeBase] Error loading config, using defaults: {}", e.getMessage());
    }
    HomeBaseConfig cfg = new HomeBaseConfig();
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
      LOGGER.error("[HomeBase] Error saving config: {}", e.getMessage());
    }
  }

  public boolean isEnabled() { return enabled; }

  public void setEnabled(boolean v) { this.enabled = v; }

  public int getBlockThreshold() { return Math.max(10, Math.min(1000, blockThreshold)); }

  public void setBlockThreshold(int v) { this.blockThreshold = Math.max(10, Math.min(1000, v)); }
}

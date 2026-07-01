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
 * Runtime-tunable progression knobs, editable in ModMenu.
 *
 * These back values that used to be hardcoded across the level-cap and
 * defeat-reward code. Cached singleton read pattern (per {@link ShrineConfig});
 * refreshed by {@link #reload()} from the ModMenu save runnable.
 */
public class ProgressionConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-progression.json"
  );

  private static ProgressionConfig instance;

  /** Starting level cap before any gym badge is earned. */
  private int baseLevelCap = 20;

  /** Fallback cap reported once every badge/champion milestone is earned. */
  private int championLevelCap = 100;

  /** Default Y offset for a Pokémon spawned on trainer defeat (when the trainer sets none). */
  private int spawnOnDefeatYOffset = 1;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static ProgressionConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static ProgressionConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          ProgressionConfig cfg = GSON.fromJson(reader, ProgressionConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Progression] Error loading config, using defaults: {}", e.getMessage());
    }
    ProgressionConfig cfg = new ProgressionConfig();
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
      LOGGER.error("[Progression] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ───────────────────────────────────────────────────────

  public int getBaseLevelCap() { return baseLevelCap; }
  public int getChampionLevelCap() { return championLevelCap; }
  public int getSpawnOnDefeatYOffset() { return spawnOnDefeatYOffset; }

  public void setBaseLevelCap(int v) { this.baseLevelCap = v; }
  public void setChampionLevelCap(int v) { this.championLevelCap = v; }
  public void setSpawnOnDefeatYOffset(int v) { this.spawnOnDefeatYOffset = v; }
}

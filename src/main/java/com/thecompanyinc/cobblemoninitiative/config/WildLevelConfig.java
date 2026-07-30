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
 * Caps natural wild-Pokémon spawn levels to the player's progression so no route ever spawns
 * absurdly over your badge cap. A wild spawn's NATURAL (biome) level is preserved but CLAMPED into
 * {@code [minLevel, levelCap + maxOffset]} — the level cap is the badge-gated ceiling from
 * LevelCapManager. {@code maxOffset} is cap-relative (default {@code +10} = up to ten over the cap);
 * {@code minLevel} is an ABSOLUTE floor (default {@code 0} = no floor, so anything naturally below
 * the cap spawns unchanged). Only outliers are pulled in-range — a level-5 route spawn stays 5, a
 * level-40 spawn under a cap-22 route is knocked down to 32. Turn it off or retune in ModMenu.
 * Applied in {@code NaturalSpawnGuard} on the same natural-spawn event that enforces the
 * special-spawn blacklist. Singleton like {@link DojoConfig}.
 */
public class WildLevelConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-wildlevel.json"
  );

  private static WildLevelConfig instance;

  /** Master switch — off leaves wild spawns at their natural (biome-configured) level. */
  private boolean enabled = true;

  /** Absolute floor — the LOWEST level a wild spawn may keep (0 = no floor, natural low levels
   *  pass through). NOT cap-relative: a spawn below this is raised to it, otherwise left alone. */
  private int minLevel = 0;

  /** Ceiling, relative to the level cap (+10 = clamp anything above cap+10 down to cap+10). */
  private int maxOffset = 10;

  public static WildLevelConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static WildLevelConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          WildLevelConfig cfg = GSON.fromJson(reader, WildLevelConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[WildLevel] Error loading config, using defaults: {}", e.getMessage());
    }
    WildLevelConfig cfg = new WildLevelConfig();
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
      LOGGER.error("[WildLevel] Error saving config: {}", e.getMessage());
    }
  }

  public boolean isEnabled() { return enabled; }
  /** Absolute floor level (0..100; 0 = no floor). */
  public int getMinLevel() { return Math.max(0, Math.min(100, minLevel)); }
  public int getMaxOffset() { return Math.max(-100, Math.min(100, maxOffset)); }

  public void setEnabled(boolean v) { this.enabled = v; }
  public void setMinLevel(int v) { this.minLevel = Math.max(0, Math.min(100, v)); }
  public void setMaxOffset(int v) { this.maxOffset = Math.max(-100, Math.min(100, v)); }
}

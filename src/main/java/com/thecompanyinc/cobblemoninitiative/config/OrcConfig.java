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
 * Runtime-tunable knobs for the rotating orc encampment, editable in-game via ModMenu.
 *
 * <p>The orc bodies (easy_npc:orc / orc_warrior) carry baked base max_health / attack_damage in
 * their duel-snippet presets. {@code healthMultiplier} / {@code damageMultiplier} scale those bases
 * per body, applied once on load (see {@code DojoDifficultyManager}, which handles the ci_orc tag
 * too). {@code spoilsRolls} is the REWARD knob — how many times the camp-cleared spoils table is
 * rolled; it is pushed to the {@code #cfg_orc_spoils_rolls ci_ambient} scoreboard (server start +
 * config reload) and read by {@code orc/camp_cleared}. Mirrors {@link NobleConfig}/{@link DojoConfig}:
 * cached {@link #get()} singleton, {@link #reload()} after a ModMenu save.
 */
public class OrcConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-orc.json"
  );

  private static OrcConfig instance;

  /** Multiplier on every orc's baked max_health (applied once, at body load). */
  private float healthMultiplier = 1.0f;

  /** Multiplier on every melee orc's baked attack_damage (applied once, at body load). */
  private float damageMultiplier = 1.0f;

  /** How many times the camp-cleared spoils table is rolled (the reward knob). */
  private int spoilsRolls = 1;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static OrcConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static OrcConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          OrcConfig cfg = GSON.fromJson(reader, OrcConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Orc] Error loading config, using defaults: {}", e.getMessage());
    }
    OrcConfig cfg = new OrcConfig();
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
      LOGGER.error("[Orc] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ─────────────────────────────────────────────────────────

  public float getHealthMultiplier() { return clamp(healthMultiplier, 0.25f, 4.0f); }
  public float getDamageMultiplier() { return clamp(damageMultiplier, 0.25f, 4.0f); }
  public int getSpoilsRolls() { return Math.max(0, Math.min(5, spoilsRolls)); }

  public void setHealthMultiplier(float v) { this.healthMultiplier = clamp(v, 0.25f, 4.0f); }
  public void setDamageMultiplier(float v) { this.damageMultiplier = clamp(v, 0.25f, 4.0f); }
  public void setSpoilsRolls(int v) { this.spoilsRolls = Math.max(0, Math.min(5, v)); }

  private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}

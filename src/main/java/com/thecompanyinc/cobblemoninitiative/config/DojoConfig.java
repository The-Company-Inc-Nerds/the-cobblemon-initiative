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
 * Runtime-tunable difficulty knobs for the Deepcore dojo PVP fights, editable in-game via ModMenu.
 *
 * <p>The floor masters / pit apprentices are hostile Easy NPC "duel" bodies whose baked base
 * max_health + attack_damage live in {@code presets/snippets/duel/duel_melee.snbt} (the canonical
 * values). These two multipliers scale those bases per-instance — applied ONCE when each hostile
 * body loads (see {@code DojoDifficultyManager}), so the presets stay the source of truth. Left at
 * 1.0 (neutral) they do nothing. Mirrors {@link NobleConfig}: read via the cached {@link #get()}
 * singleton, {@link #reload()} after a ModMenu save.
 */
public class DojoConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-dojo.json"
  );

  private static DojoConfig instance;

  /** Multiplier on every dojo fighter's baked max_health (applied once, at body load). */
  private float fighterHealthMultiplier = 1.0f;

  /** Multiplier on every melee dojo fighter's baked attack_damage (applied once, at body load). */
  private float fighterDamageMultiplier = 1.0f;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static DojoConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static DojoConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          DojoConfig cfg = GSON.fromJson(reader, DojoConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Dojo] Error loading config, using defaults: {}", e.getMessage());
    }
    DojoConfig cfg = new DojoConfig();
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
      LOGGER.error("[Dojo] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters (clamped 0.25–4.0, matching NobleConfig's boss knobs) ────

  public float getFighterHealthMultiplier() { return clamp(fighterHealthMultiplier, 0.25f, 4.0f); }
  public float getFighterDamageMultiplier() { return clamp(fighterDamageMultiplier, 0.25f, 4.0f); }

  public void setFighterHealthMultiplier(float v) { this.fighterHealthMultiplier = clamp(v, 0.25f, 4.0f); }
  public void setFighterDamageMultiplier(float v) { this.fighterDamageMultiplier = clamp(v, 0.25f, 4.0f); }

  private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}

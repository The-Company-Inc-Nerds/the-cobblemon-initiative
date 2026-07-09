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
 * Runtime-tunable "feel" knobs for noble boss encounters, editable in-game via ModMenu.
 *
 * <p>Per-noble encounter data (species, arena, attack list, rewards) lives in the jar-baked
 * {@code data/cobblemon_initiative/noble_encounters/<id>.json}. The global difficulty/feel
 * scalars a player may want to retune without rebuilding live here, in
 * {@code config/cobblemon-initiative-noble.json}. Mirrors {@link ShrineConfig}: read per-tick
 * via the cached {@link #get()} singleton; refresh with {@link #reload()} after a ModMenu save.
 */
public class NobleConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-noble.json"
  );

  private static NobleConfig instance;

  /** Master switch for the whole subsystem. */
  private boolean noblesEnabled = true;

  /** Global multiplier applied to every noble attack's damage. */
  private float attackDamageMultiplier = 1.0f;

  /** Show the stagger boss bar (accessibility toggle). */
  private boolean bossBarEnabled = true;

  /** How hard the arena boundary shoves a player who crosses it (blocks inside the edge). */
  private float ringPushback = 0.6f;

  /** Volume / pitch for the shared start / stagger / complete cues. */
  private float sfxVolume = 1.0f;
  private float sfxPitch = 1.0f;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static NobleConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static NobleConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          NobleConfig cfg = GSON.fromJson(reader, NobleConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Noble] Error loading config, using defaults: {}", e.getMessage());
    }
    NobleConfig cfg = new NobleConfig();
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
      LOGGER.error("[Noble] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ─────────────────────────────────────────────────────────

  public boolean isNoblesEnabled() { return noblesEnabled; }
  public float getAttackDamageMultiplier() { return attackDamageMultiplier; }
  public boolean isBossBarEnabled() { return bossBarEnabled; }
  public float getRingPushback() { return ringPushback; }
  public float getSfxVolume() { return sfxVolume; }
  public float getSfxPitch() { return clampPitch(sfxPitch); }

  public void setNoblesEnabled(boolean v) { this.noblesEnabled = v; }
  public void setAttackDamageMultiplier(float v) { this.attackDamageMultiplier = Math.max(0f, v); }
  public void setBossBarEnabled(boolean v) { this.bossBarEnabled = v; }
  public void setRingPushback(float v) { this.ringPushback = v; }
  public void setSfxVolume(float v) { this.sfxVolume = v; }
  public void setSfxPitch(float v) { this.sfxPitch = clampPitch(v); }

  private static float clampPitch(float v) { return Math.max(0.5f, Math.min(2.0f, v)); }
}

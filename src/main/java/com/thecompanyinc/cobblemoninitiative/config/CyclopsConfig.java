package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config for the giant mushroom-island cyclops (2026-07-30 request). The body is an Easy NPC
 * humanoid at 3x scale (dialog-src/visuals/cyclops.npc.snbt) with 50 HP + hostile AI baked into
 * the duel/cyclops movement snippet; {@link com.thecompanyinc.cobblemoninitiative.CyclopsManager}
 * spawns copies at {@link #spawnPoints} (mushroom-island coords latched later) and drives the
 * grab-&gt;squeeze-&gt;throw player attack + chase water-avoidance. Cached {@link #get()} singleton with
 * clamped setters, mirroring {@link OrcConfig}/{@link NobleConfig}; edit the JSON or ModMenu, then
 * {@code /cobblemon-initiative cyclops reload}.
 */
public class CyclopsConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-cyclops.json");

  private static CyclopsConfig instance;

  public boolean enabled = true;
  public String dimension = "minecraft:overworld";
  /** Preset import_new'd at each spawn point (baked from dialog-src by content_compile). */
  public String bodyPreset = "easy_npc:preset/humanoid/cyclops.npc.snbt";
  /** Entity tag every cyclops body carries — the manager's find / scale / grab target. */
  public String bodyTag = "ci_cyclops";

  /** Mushroom-island spawn positions (EMPTY by default — latched later; run
   *  {@code /cobblemon-initiative cyclops spawn} once the coords are filled). */
  public List<Pos> spawnPoints = new ArrayList<>();

  // ── combat tuning (ModMenu / JSON) ──────────────────────────────────────────────
  /** Multiplier on the baked 50 max_health (applied once at body load). */
  private float healthMultiplier = 1.0f;
  /** Multiplier on the baked 8 melee attack_damage vs MOBS (applied once at body load). */
  private float damageMultiplier = 1.0f;
  /** How close (blocks) a targeted player must be for the cyclops to grab (generous — 3x reach). */
  private double grabRange = 4.5;
  /** Total ticks a grab holds (squeeze phase) before the throw. */
  private int grabDurationTicks = 60;
  /** Damage per squeeze pulse during the hold. */
  private float squeezeDamage = 2.0f;
  /** Ticks between squeeze pulses. */
  private int squeezeIntervalTicks = 15;
  /** Horizontal throw velocity. */
  private double throwHorizontal = 4.0;
  /** Upward throw velocity. */
  private double throwVertical = 1.6;
  /** Impact damage applied at the moment of the throw (fall damage comes on landing). */
  private float throwImpactDamage = 3.0f;
  /** Ticks the cyclops cannot re-grab after a throw. */
  private int grabCooldownTicks = 50;

  public static class Pos {
    public double x, y, z;
    public Pos() {}
    public Pos(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
  }

  // ── singleton / lifecycle ──────────────────────────────────────────────────────

  public static CyclopsConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() { instance = load(); }

  public static CyclopsConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          CyclopsConfig cfg = GSON.fromJson(reader, CyclopsConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[Cyclops] Error loading config, using defaults: {}", e.getMessage());
    }
    CyclopsConfig cfg = new CyclopsConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) { GSON.toJson(this, writer); }
    } catch (IOException e) {
      LOGGER.error("[Cyclops] Error saving config: {}", e.getMessage());
    }
  }

  // ── getters / setters (clamped) ─────────────────────────────────────────────────

  public float getHealthMultiplier() { return clampF(healthMultiplier, 0.25f, 6.0f); }
  public float getDamageMultiplier() { return clampF(damageMultiplier, 0.0f, 4.0f); }
  public double getGrabRange() { return clampD(grabRange, 2.0, 8.0); }
  public int getGrabDurationTicks() { return Math.max(10, Math.min(200, grabDurationTicks)); }
  public float getSqueezeDamage() { return clampF(squeezeDamage, 0.0f, 20.0f); }
  public int getSqueezeIntervalTicks() { return Math.max(1, Math.min(60, squeezeIntervalTicks)); }
  public double getThrowHorizontal() { return clampD(throwHorizontal, 0.0, 12.0); }
  public double getThrowVertical() { return clampD(throwVertical, 0.0, 6.0); }
  public float getThrowImpactDamage() { return clampF(throwImpactDamage, 0.0f, 20.0f); }
  public int getGrabCooldownTicks() { return Math.max(0, Math.min(400, grabCooldownTicks)); }

  public void setHealthMultiplier(float v) { this.healthMultiplier = clampF(v, 0.25f, 6.0f); }
  public void setDamageMultiplier(float v) { this.damageMultiplier = clampF(v, 0.0f, 4.0f); }
  public void setGrabRange(double v) { this.grabRange = clampD(v, 2.0, 8.0); }
  public void setGrabDurationTicks(int v) { this.grabDurationTicks = Math.max(10, Math.min(200, v)); }
  public void setSqueezeDamage(float v) { this.squeezeDamage = clampF(v, 0.0f, 20.0f); }
  public void setSqueezeIntervalTicks(int v) { this.squeezeIntervalTicks = Math.max(1, Math.min(60, v)); }
  public void setThrowHorizontal(double v) { this.throwHorizontal = clampD(v, 0.0, 12.0); }
  public void setThrowVertical(double v) { this.throwVertical = clampD(v, 0.0, 6.0); }
  public void setThrowImpactDamage(float v) { this.throwImpactDamage = clampF(v, 0.0f, 20.0f); }
  public void setGrabCooldownTicks(int v) { this.grabCooldownTicks = Math.max(0, Math.min(400, v)); }

  private static float clampF(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
  private static double clampD(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}

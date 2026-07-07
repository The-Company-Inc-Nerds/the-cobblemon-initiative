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
 * Runtime-tunable shrine settings, editable in-game via ModMenu (Cloth Config).
 *
 * Per-shrine challenge data (which shrine is an ice-floor trial, which blocks are
 * hazardous, the recorded safe path) lives in the jar-baked challenge JSON, which
 * is not hot-reloadable. The numeric "feel" knobs that a player may want to retune
 * without rebuilding the mod live here instead, in {@code config/cobblemon-initiative-shrine.json}.
 *
 * Read at runtime via the cached {@link #get()} singleton (the ice-floor hazard
 * queries this every tick, so it must not touch disk); refreshed by {@link #reload()}
 * from the ModMenu save runnable. Mirrors the {@code NuzlockeConfig}/{@code NpcSightConfig}
 * pattern.
 */
public class ShrineConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-shrine.json"
  );

  private static ShrineConfig instance;

  /**
   * Master switch for the ice-trial floor hazard. The per-shrine challenge JSON
   * still decides WHICH shrines use the floor (via {@code iceFloorEnabled} there);
   * turning this off disables the hazard for all of them at once.
   */
  private boolean iceFloorEnabled = true;

  /** Damage dealt (half-hearts ×2) when stepping on hazard ice off the safe path. */
  private float iceFloorDamage = 4.0f;

  /** Ticks of immunity after a hazard hit, so a single misstep only fires once. 20 = 1s. */
  private int iceFloorHitCooldownTicks = 15;

  /** Volume / pitch of the ice-crack sound played when the floor hazard hits. */
  private float iceCrackSoundVolume = 1.0f;
  private float iceCrackSoundPitch = 0.8f;

  // ── ground shrine — the buried maze (dark_gauntlet) ───────────────────────────
  /** Fraction of max health the player starts the ground shrine at. */
  private float darkGauntletStartHealthFraction = 0.5f;
  /** Blindness effect duration (ticks) re-applied to keep the maze dark. */
  private int darkGauntletBlindnessDurationTicks = 200;
  /** How often (ticks) blindness is re-applied so it never fades. */
  private int darkGauntletBlindnessRefreshTicks = 100;
  /** Nausea/confusion duration (ticks) applied on each earthquake teleport. 0 disables. */
  private int earthquakeNauseaTicks = 60;
  /** Volume / pitch of the earthquake rumble. Pitch floor is 0.5: the sound engine
   *  clamps below that, and the config UI's 0.5..2.0 bounds flagged the old 0.4
   *  default as invalid (showrunner, 0.5.0-alpha.1). */
  private float earthquakeSoundVolume = 0.6f;
  private float earthquakeSoundPitch = 0.5f;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  /** Cached instance for per-tick runtime reads. Lazy-loads on first access. */
  public static ShrineConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  /** Refresh the cached instance from disk (call after a ModMenu save). */
  public static void reload() {
    instance = load();
  }

  /** Read a fresh instance from disk, creating the file with defaults if absent. */
  public static ShrineConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          ShrineConfig cfg = GSON.fromJson(reader, ShrineConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Shrine] Error loading config, using defaults: {}", e.getMessage());
    }
    ShrineConfig cfg = new ShrineConfig();
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
      LOGGER.error("[Shrine] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ───────────────────────────────────────────────────────

  public boolean isIceFloorEnabled() { return iceFloorEnabled; }
  public float getIceFloorDamage() { return iceFloorDamage; }
  public int getIceFloorHitCooldownTicks() { return iceFloorHitCooldownTicks; }

  public void setIceFloorEnabled(boolean v) { this.iceFloorEnabled = v; }
  public void setIceFloorDamage(float v) { this.iceFloorDamage = v; }
  public void setIceFloorHitCooldownTicks(int v) { this.iceFloorHitCooldownTicks = v; }

  public float getIceCrackSoundVolume() { return iceCrackSoundVolume; }
  public float getIceCrackSoundPitch() { return iceCrackSoundPitch; }
  public float getDarkGauntletStartHealthFraction() { return darkGauntletStartHealthFraction; }
  public int getDarkGauntletBlindnessDurationTicks() { return darkGauntletBlindnessDurationTicks; }
  public int getDarkGauntletBlindnessRefreshTicks() { return darkGauntletBlindnessRefreshTicks; }
  public int getEarthquakeNauseaTicks() { return earthquakeNauseaTicks; }
  public float getEarthquakeSoundVolume() { return earthquakeSoundVolume; }
  // Getter clamps: Gson loads a saved config straight into the field (bypassing the
  // setter), and pre-0.5.0 configs carry the invalid 0.4 default.
  public float getEarthquakeSoundPitch() { return clampPitch(earthquakeSoundPitch); }

  public void setIceCrackSoundVolume(float v) { this.iceCrackSoundVolume = v; }
  public void setIceCrackSoundPitch(float v) { this.iceCrackSoundPitch = v; }
  public void setDarkGauntletStartHealthFraction(float v) { this.darkGauntletStartHealthFraction = v; }
  public void setDarkGauntletBlindnessDurationTicks(int v) { this.darkGauntletBlindnessDurationTicks = v; }
  public void setDarkGauntletBlindnessRefreshTicks(int v) { this.darkGauntletBlindnessRefreshTicks = v; }
  public void setEarthquakeNauseaTicks(int v) { this.earthquakeNauseaTicks = v; }
  public void setEarthquakeSoundVolume(float v) { this.earthquakeSoundVolume = v; }
  public void setEarthquakeSoundPitch(float v) { this.earthquakeSoundPitch = clampPitch(v); }

  private static float clampPitch(float v) { return Math.max(0.5f, Math.min(2.0f, v)); }
}

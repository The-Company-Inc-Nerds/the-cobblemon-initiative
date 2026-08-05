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

  /** If true, dojo fights are non-lethal: a defeated fighter is knocked out (left lying at the
   *  spot) instead of killed, and a player who would die in the dojo is knocked out instead. */
  private boolean knockoutMode = true;

  /** Player health (in half-hearts, HP points) left after a dojo knockout — 1.0 = half a heart. */
  private float knockoutPlayerHealth = 1.0f;

  /** CobbleDollars taken from the player on a dojo knockout. */
  private int knockoutCost = 100;

  /** If true (and knockout mode is on), a player knockout also EJECTS them to the quarry-side
   *  clinic and fully resets the dojo run — latches re-armed, floor/pit defeat tags stripped,
   *  passive bodies back on their posts (Bruno's own badge credit is untouched). */
  private boolean resetOnKnockout = true;

  /** Where a knocked-out player wakes: outside Rilka's quarry-side clinic, facing her post.
   *  JSON-editable; must stay >48 blocks from every dojo post so re-raised fighters cannot
   *  reach the half-heart player. */
  private double dojoEjectX = 1092.5;
  private double dojoEjectY = 114.0;
  private double dojoEjectZ = 3206.5;

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

  public boolean isKnockoutMode() { return knockoutMode; }
  public float getKnockoutPlayerHealth() { return clamp(knockoutPlayerHealth, 0.5f, 20.0f); }
  public int getKnockoutCost() { return Math.max(0, knockoutCost); }

  public boolean isResetOnKnockout() { return resetOnKnockout; }
  public double getDojoEjectX() { return dojoEjectX; }
  public double getDojoEjectY() { return dojoEjectY; }
  public double getDojoEjectZ() { return dojoEjectZ; }

  public void setKnockoutMode(boolean v) { this.knockoutMode = v; }
  public void setKnockoutPlayerHealth(float v) { this.knockoutPlayerHealth = clamp(v, 0.5f, 20.0f); }
  public void setKnockoutCost(int v) { this.knockoutCost = Math.max(0, v); }
  public void setResetOnKnockout(boolean v) { this.resetOnKnockout = v; }

  private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}

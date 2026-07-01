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
 * Runtime settings for the "unplaced chest" loot mechanic, editable in ModMenu.
 *
 * When a player opens a chest they did NOT place themselves (i.e. a chest that
 * ships with the map / was placed via structure or command rather than by the
 * player's own hand), it is stocked in place with a few random items from the
 * {@code badge_reward} loot tables — scaled to how many gym badges the player
 * has earned — and then opened normally so they can take what they want.
 *
 * Cached singleton read pattern (per {@link ShrineConfig}); the interaction
 * handler queries {@link #get()} on every chest open, so it must not touch disk.
 */
public class LootChestConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-lootchest.json"
  );

  private static LootChestConfig instance;

  /** Master switch for the whole unplaced-chest loot mechanic. */
  private boolean enabled = true;

  /** Roll the Minecraft-resource pool (metals / naturals / minerals). */
  private boolean giveMinecraftPool = true;

  /** Roll the Cobblemon-resource pool (apricorns / evo & held items / healing). */
  private boolean giveCobblemonPool = true;

  /**
   * When true, each unplaced chest dispenses loot only once; further opens fall
   * through to the normal (now-empty) chest. When false, every open re-rolls
   * (useful for testing loot tables).
   */
  private boolean oneTimePerChest = true;

  /**
   * Scales how much loot a chest is stocked with, relative to the loot tables'
   * default output. 1.0 = default (stacks + counts as the tables roll), 0 = none,
   * 3.0 = triple. Item counts are multiplied (overflowing into extra stacks);
   * clamped to 0.0..3.0.
   */
  private double lootMultiplier = 1.0;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static LootChestConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static LootChestConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          LootChestConfig cfg = GSON.fromJson(reader, LootChestConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[Loot Chest] Error loading config, using defaults: {}", e.getMessage());
    }
    LootChestConfig cfg = new LootChestConfig();
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
      LOGGER.error("[Loot Chest] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ───────────────────────────────────────────────────────

  public boolean isEnabled() { return enabled; }
  public boolean isGiveMinecraftPool() { return giveMinecraftPool; }
  public boolean isGiveCobblemonPool() { return giveCobblemonPool; }
  public boolean isOneTimePerChest() { return oneTimePerChest; }
  public double getLootMultiplier() { return lootMultiplier; }

  public void setEnabled(boolean v) { this.enabled = v; }
  public void setGiveMinecraftPool(boolean v) { this.giveMinecraftPool = v; }
  public void setGiveCobblemonPool(boolean v) { this.giveCobblemonPool = v; }
  public void setOneTimePerChest(boolean v) { this.oneTimePerChest = v; }
  public void setLootMultiplier(double v) { this.lootMultiplier = Math.max(0.0, Math.min(3.0, v)); }
}

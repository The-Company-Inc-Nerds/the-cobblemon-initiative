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
   * When true, sends the "[Supply Cache]" chat line the first time an unplaced
   * chest is stocked. Off by default to keep stream chat clean.
   */
  private boolean announceUnplacedChests = false;

  /**
   * Scales the NUMBER of item stacks stocked into a chest, relative to what the
   * loot tables roll. 1.0 = the tables' default count of stacks, 0.5 = half,
   * 0 = none, up to 3.0 = triple (extra stacks come from re-rolling the tables).
   * Clamped to 0.0..3.0. Default 1.0 (reads as 1.0× in the config UI). A fixed internal
   * BASE_LOOT_SCALE in rollLoot keeps the real stack count small — about a third of the
   * legacy amount — so 1.0× is the neutral knob while chests stay lean; the min-1 floor
   * guarantees at least one stack. Raise toward 3.0× for fuller chests.
   */
  private double stackMultiplier = 1.0;

  /**
   * Scales the number of ITEMS within each stack, relative to the loot tables'
   * counts. 1.0 = default, 0.5 = half, up to 3.0 — capped at each item's max
   * stack size, so it never spills into extra stacks (independent of the stack
   * multiplier). Clamped to 0.0..3.0. Default 1.0 (reads as 1.0× in the config UI); the
   * shared BASE_LOOT_SCALE in rollLoot keeps the real count small and the min-1 floor
   * keeps each kept stack at about one item by default.
   */
  private double itemMultiplier = 1.0;

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
  public boolean isAnnounceUnplacedChests() { return announceUnplacedChests; }
  // Getters clamp too: Gson loads a hand-edited config straight into the fields
  // (bypassing the setters), so clamping on read keeps the documented 0.0..3.0
  // range — and bounds the per-open loot roll — even for out-of-range edits.
  public double getStackMultiplier() { return clamp(stackMultiplier); }
  public double getItemMultiplier() { return clamp(itemMultiplier); }

  public void setEnabled(boolean v) { this.enabled = v; }
  public void setGiveMinecraftPool(boolean v) { this.giveMinecraftPool = v; }
  public void setGiveCobblemonPool(boolean v) { this.giveCobblemonPool = v; }
  public void setOneTimePerChest(boolean v) { this.oneTimePerChest = v; }
  public void setAnnounceUnplacedChests(boolean v) { this.announceUnplacedChests = v; }
  public void setStackMultiplier(double v) { this.stackMultiplier = clamp(v); }
  public void setItemMultiplier(double v) { this.itemMultiplier = clamp(v); }

  private static double clamp(double v) { return Math.max(0.0, Math.min(3.0, v)); }
}

package com.thecompanyinc.cobblemoninitiative.safari;

import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;

/**
 * Bait → species tables for the Baiting Yards, loaded from the jar resource
 * {@code data/cobblemon_initiative/safari/lure_tables.json}.
 *
 * <p>Every species id is validated at authoring time against the Cobblemon 1.7.3 jar
 * ({@code dev/cobblemon_validation/species_index.json}) — an invalid id would make
 * {@code PokemonProperties.parse} silently produce a substitute, so no unchecked
 * species may ship here.
 *
 * <p>Warm spots: a successful catch at a bait spot raises that spot's warmth (max 2)
 * for the rest of the visit. Warmth biases the roll up-tier by suppressing entries
 * BELOW the warmth rank (common=0, uncommon=1, rare=2) to a quarter of their weight —
 * the table never empties, the odds visibly shift.
 */
public class SafariLureTables {

  private static final String RESOURCE_PATH =
    "data/cobblemon_initiative/safari/lure_tables.json";

  private static final Gson GSON = new Gson();

  /** Weight multiplier applied to entries below the spot's warmth rank. */
  private static final double COLD_TIER_FACTOR = 0.25;

  public static class Entry {

    public String species;
    /** common | uncommon | rare */
    public String rarity = "common";
    public double weight = 1.0;
    /** Optional per-entry level band override (0 = use the table-wide band). */
    public int minLevel = 0;
    public int maxLevel = 0;

    int rarityRank() {
      return switch (rarity == null ? "common" : rarity) {
        case "rare" -> 2;
        case "uncommon" -> 1;
        default -> 0;
      };
    }
  }

  public static class Table {

    public String displayName;
    /** Vanilla item id this bait rides on (renamed via components). */
    public String item;
    public List<Entry> entries = new ArrayList<>();
  }

  /** Root JSON shape. */
  private static class Root {

    int levelMin = 25;
    int levelMax = 35;
    Map<String, Table> tables = new LinkedHashMap<>();
  }

  private Root root = new Root();

  public static SafariLureTables load() {
    SafariLureTables tables = new SafariLureTables();
    try (
      InputStream in = SafariLureTables.class.getClassLoader()
        .getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "Safari lure tables resource missing ({}); baiting disabled.",
          RESOURCE_PATH
        );
        return tables;
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        Root parsed = GSON.fromJson(reader, Root.class);
        if (parsed != null && parsed.tables != null) {
          tables.root = parsed;
        }
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load safari lure tables.", e);
    }
    InitiativeInit.LOGGER.info(
      "Loaded {} safari lure table(s).",
      tables.root.tables.size()
    );
    return tables;
  }

  public boolean hasBait(String baitType) {
    return root.tables.containsKey(baitType);
  }

  public Table getTable(String baitType) {
    return root.tables.get(baitType);
  }

  public java.util.Set<String> baitTypes() {
    return root.tables.keySet();
  }

  /**
   * Roll one species from the bait's table at the given warmth (0..2). Returns a full
   * PokemonProperties string ({@code <species> level=<n>}), or null for an unknown bait
   * or an empty table.
   */
  public String roll(String baitType, int warmth, RandomSource random) {
    Table table = root.tables.get(baitType);
    if (table == null || table.entries.isEmpty()) return null;

    double total = 0;
    double[] weights = new double[table.entries.size()];
    for (int i = 0; i < table.entries.size(); i++) {
      Entry e = table.entries.get(i);
      double w = Math.max(0, e.weight);
      if (e.rarityRank() < warmth) w *= COLD_TIER_FACTOR;
      weights[i] = w;
      total += w;
    }
    if (total <= 0) return null;

    double pick = random.nextDouble() * total;
    Entry chosen = table.entries.get(table.entries.size() - 1);
    for (int i = 0; i < weights.length; i++) {
      pick -= weights[i];
      if (pick <= 0) {
        chosen = table.entries.get(i);
        break;
      }
    }

    int min = chosen.minLevel > 0 ? chosen.minLevel : root.levelMin;
    int max = chosen.maxLevel > 0 ? chosen.maxLevel : root.levelMax;
    if (max < min) max = min;
    int level = min + random.nextInt(max - min + 1);
    return chosen.species + " level=" + level;
  }
}

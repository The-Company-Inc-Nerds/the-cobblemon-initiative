package com.thecompanyinc.cobblemoninitiative.safari;

import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.SpecialSpawnConfig;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.RandomSource;

/**
 * Bait → species tables for the Ridgewatch Preserve, loaded from the jar resource
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
    /**
     * Optional placement rule for the scatter click. {@code null} = anywhere;
     * {@code "tree"} (the only defined value) = the clicked block must be part of a
     * tree (logs or leaves) — SafariManager enforces it in onUseBlock.
     */
    public String placement;
    public List<Entry> entries = new ArrayList<>();
  }

  /** Root JSON shape. Band defaults track the bundled data (gym-6 leg, cap 56). */
  private static class Root {

    int levelMin = 40;
    int levelMax = 50;
    Map<String, Table> tables = new LinkedHashMap<>();
  }

  /** One resolved roll: the full PokemonProperties string + the entry's rarity. */
  public record Roll(String properties, String rarity) {}

  private Root root = new Root();

  /**
   * Every species id carried on any table, lowercased — the safari-exclusive roster
   * NaturalSpawnGuard cancels worldwide. Immutable; rebuilt on every load/reload.
   */
  private Set<String> speciesIds = Set.of();

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
    // One walk over the tables: collect the safari-exclusive roster AND run the
    // authoring-time tripwire — bait spawns ride addFreshEntity and bypass
    // NaturalSpawnGuard by construction, so a SpecialSpawnConfig-blacklisted
    // (curated-only) species on a table would quietly become a repeatable catch.
    Set<String> ids = new LinkedHashSet<>();
    for (Map.Entry<String, Table> tableEntry : tables.root.tables.entrySet()) {
      if (tableEntry.getValue().entries == null) continue;
      for (Entry entry : tableEntry.getValue().entries) {
        if (entry.species == null) continue;
        ids.add(entry.species.toLowerCase(Locale.ROOT));
        if (SpecialSpawnConfig.get().isBlacklisted(entry.species)) {
          InitiativeInit.LOGGER.warn(
            "Safari lure table '{}' carries SpecialSpawnConfig-blacklisted species '{}' — curated-only lines must not ride a repeatable bait table.",
            tableEntry.getKey(), entry.species
          );
        }
      }
    }
    tables.speciesIds = Collections.unmodifiableSet(ids);
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
   * The table's placement rule — {@code null} for an unknown table or a
   * place-anywhere one; {@code "tree"} means the scatter click must land on a tree
   * block (logs/leaves).
   */
  public String placementFor(String baitType) {
    Table table = root.tables.get(baitType);
    return table == null ? null : table.placement;
  }

  /** The safari-exclusive roster: every table species id, lowercased. Immutable. */
  public Set<String> speciesIds() {
    return speciesIds;
  }

  /**
   * Roll one species from the bait's table at the given warmth (0..2). Returns the full
   * PokemonProperties string ({@code <species> level=<n>}) plus the entry's rarity
   * (contest scoring records it on the lure), or null for an unknown bait or an empty
   * table.
   */
  public Roll roll(String baitType, int warmth, RandomSource random) {
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
    return new Roll(
      chosen.species + " level=" + level,
      chosen.rarity == null ? "common" : chosen.rarity
    );
  }
}

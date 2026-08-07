package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The curated "special Pokémon" list: legendaries, mythicals, and noble-exclusive species that
 * should ONLY be obtainable through their scripted encounter (a noble arena, a shrine crystal, the
 * Wisp-Lantern, a story gift) and NEVER appear as a natural wild spawn. {@code NaturalSpawnGuard}
 * reads this and cancels any of these species that Cobblemon's world spawner tries to place.
 *
 * <p>The list ({@link #blacklistedSpecies}) is a hand-editable JSON array of Cobblemon species
 * resource-paths (e.g. {@code "kyogre"}, {@code "marshadow"}, {@code "hooh"}); forms/aspects share
 * the base species id, so blocking {@code "landorus"} blocks every Landorus form. The master
 * {@link #preventNaturalSpawns} switch is exposed as a ModMenu toggle. Mirrors {@link DojoConfig} /
 * {@link NobleConfig}: cached {@link #get()} singleton, {@link #reload()} after a ModMenu save.
 *
 * <p>Scripted spawns are unaffected by design: the noble catch bodies use
 * {@code PokemonProperties.createEntity() + level.addFreshEntity()} and the crystal/wisp use the
 * {@code /spawnpokemon} command — neither routes through Cobblemon's {@code SingleEntitySpawnAction}
 * (the sole emitter of the spawn event the guard listens on), so only the wild spawner is filtered.
 */
public class SpecialSpawnConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-special-spawns.json"
  );

  /**
   * The seed list, grouped by source. Nobles (weather trio + birds + Mew + the pseudo-dragon
   * lines), shrine crystals, the Wisp-Lantern mythical, story gifts, and the boss-ace legendaries
   * (kept out of the wild too, so a villain's ace never turns up as a random encounter).
   */
  private static final List<String> DEFAULT_BLACKLIST = List.of(
    // nobles — legendaries
    "articuno", "zapdos", "moltres", "mew", "groudon", "kyogre", "rayquaza",
    // nobles — the pseudo-dragon lines (delivered by the wandering mini-nobles)
    "dratini", "bagon", "beldum", "gible", "deino", "larvitar", "salamence", "hydreigon",
    // shrine crystals
    "hooh", "landorus", "glastrier", "kyurem", "xerneas",
    // Wisp-Lantern
    "marshadow",
    // story gifts / events
    "victini", "manaphy", "entei", "latios",
    // boss aces (battle-only, but kept out of the wild for good measure)
    "mewtwo", "darkrai", "heatran",
    // starters — gift-only, must never appear as a wild spawn (playtest 2026-08-06: a wild
    // Skiddo leaked). Base-species ids, so this covers the starter form itself. NOTE: the
    // Hisuian Growlithe starter's base id "growlithe" is intentionally NOT here — blocking it
    // would also kill regular Growlithe wild spawns (see wave notes; awaiting a ruling).
    "skiddo", "totodile"
  );

  private static SpecialSpawnConfig instance;

  /** Master switch — when true, the blacklisted species never spawn naturally. */
  private boolean preventNaturalSpawns = true;

  /** Editable list of Cobblemon species resource-paths to keep out of natural spawns. */
  private List<String> blacklistedSpecies = new ArrayList<>(DEFAULT_BLACKLIST);

  /** Lazily-built lowercase lookup set (rebuilt per instance — reload() makes a fresh one). */
  private transient Set<String> lookup;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static SpecialSpawnConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static SpecialSpawnConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          SpecialSpawnConfig cfg = GSON.fromJson(reader, SpecialSpawnConfig.class);
          if (cfg != null) {
            if (cfg.blacklistedSpecies == null) {
              cfg.blacklistedSpecies = new ArrayList<>(DEFAULT_BLACKLIST);
            }
            return cfg;
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[SpecialSpawn] Error loading config, using defaults: {}", e.getMessage());
    }
    SpecialSpawnConfig cfg = new SpecialSpawnConfig();
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
      LOGGER.error("[SpecialSpawn] Error saving config: {}", e.getMessage());
    }
  }

  // ── API ───────────────────────────────────────────────────────────────────────

  public boolean isPreventNaturalSpawns() { return preventNaturalSpawns; }

  public void setPreventNaturalSpawns(boolean v) { this.preventNaturalSpawns = v; }

  public List<String> getBlacklistedSpecies() { return blacklistedSpecies; }

  /** True if {@code speciesPath} (a Cobblemon species resource-path, any case) is on the list. */
  public boolean isBlacklisted(String speciesPath) {
    if (speciesPath == null) return false;
    Set<String> set = lookup;
    if (set == null) {
      set = new HashSet<>();
      if (blacklistedSpecies != null) {
        for (String s : blacklistedSpecies) {
          if (s != null && !s.isBlank()) set.add(s.trim().toLowerCase(Locale.ROOT));
        }
      }
      lookup = set;
    }
    return set.contains(speciesPath.toLowerCase(Locale.ROOT));
  }
}

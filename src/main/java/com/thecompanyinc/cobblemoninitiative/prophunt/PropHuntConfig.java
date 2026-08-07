package com.thecompanyinc.cobblemoninitiative.prophunt;

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
 * Config for the "prop hunt" quest mini-game (0.7.1). One or more {@link Arena}s, each a set of
 * barrel spots the game floats a distinctive falling-block "barrel" at (see
 * {@link com.thecompanyinc.cobblemoninitiative.prophunt.PropHuntManager}). One spot is the real
 * barrel; the rest are decoys. Right-click the real one to win; a wrong pick poofs that barrel,
 * and {@link Arena#wrongAllowed} wrong picks lose the round.
 *
 * <p>Coord-driven like {@link com.thecompanyinc.cobblemoninitiative.gaviota.GaviotaConfig}: a
 * default with PLACEHOLDER spots is written to {@code config/cobblemon-initiative-prophunt.json}
 * on first load. The showrunner fills the arena geometry, then {@code /cobblemon-initiative
 * prophunt reload}. A dialog "Start the hunt" button runs {@code cobblemon-initiative prophunt
 * start <arena>} (ExecAsUser); winning tags the player {@link Arena#winTag} for the quest turn-in.
 */
public class PropHuntConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-prophunt.json");

  private static PropHuntConfig instance;

  public boolean enabled = true;

  /** Every prop-hunt arena. A round runs one arena; the {@code start} command names it (or the
   *  first enabled arena is used when the name is omitted). */
  public List<Arena> arenas = new ArrayList<>(List.of(new Arena()));

  public static class Arena {
    /** Arena id the dialog button passes to {@code prophunt start <id>}. */
    public String id = "sample";
    public String dimension = "minecraft:overworld";
    /** Block state summoned as the floating prop (any full block reads well as a falling block). */
    public String barrelBlock = "minecraft:barrel";
    /** The candidate spots. PLACEHOLDER — replace with real coords, then reload. One is picked at
     *  random each round as the real barrel; the others are decoys. Space spots >= 2 blocks apart. */
    public List<Pos> spots = new ArrayList<>(List.of(
      new Pos(0.5, 64, 0.5), new Pos(3.5, 64, 0.5), new Pos(6.5, 64, 0.5),
      new Pos(0.5, 64, 3.5), new Pos(3.5, 64, 3.5), new Pos(6.5, 64, 3.5)));
    /** How high above each spot the barrel floats (the falling-block render + a small lift make it
     *  read as a game piece, not scenery). */
    public double floatHeight = 0.5;
    /** Wrong picks allowed before the round is lost. 0 = one strike (first wrong pick loses). */
    public int wrongAllowed = 2;
    /** Player tag added on a win — the quest turn-in reads it. Blank = none. */
    public String winTag = "prophunt_sample_won";
    /** Optional server command run on win (e.g. a cutscene / give). {@code @p} etc. not expanded;
     *  runs from the arena's dimension at permission 2. Blank = none. */
    public String winCommand = "";
    /** Optional server command run on loss. Blank = none. */
    public String loseCommand = "";
    public String startMessage = "Somewhere among these barrels, one is the real thing. Find it.";
    public String winMessage = "That's the one! You found the real barrel.";
    public String wrongMessage = "A decoy — it crumbles to dust.";
    public String loseMessage = "The last of the barrels collapses. The trail's gone cold.";

    public boolean isPlaceholder() {
      if (spots == null || spots.isEmpty()) return true;
      Pos p = spots.get(0);
      return p.x == 0.5 && p.y == 64 && p.z == 0.5;
    }
  }

  public static class Pos {
    public double x, y, z;
    public Pos() {}
    public Pos(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
  }

  /** The named arena, or the first one when {@code id} is null/blank, or null if none match. */
  public Arena arena(String id) {
    if (arenas == null || arenas.isEmpty()) return null;
    if (id == null || id.isBlank()) return arenas.get(0);
    for (Arena a : arenas) {
      if (a.id != null && a.id.equalsIgnoreCase(id)) return a;
    }
    return null;
  }

  // ── singleton / lifecycle ──────────────────────────────────────────────────────

  public static PropHuntConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() { instance = load(); }

  public static PropHuntConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader r = new FileReader(CONFIG_FILE)) {
          PropHuntConfig cfg = GSON.fromJson(r, PropHuntConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[PropHunt] Error loading config, using defaults: {}", e.getMessage());
    }
    PropHuntConfig cfg = new PropHuntConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter w = new FileWriter(CONFIG_FILE)) { GSON.toJson(this, w); }
    } catch (IOException e) {
      LOGGER.error("[PropHunt] Error saving config: {}", e.getMessage());
    }
  }
}

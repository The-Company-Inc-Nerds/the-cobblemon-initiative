package com.thecompanyinc.cobblemoninitiative.dittohunt;

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
 * Config for the "Ditto hide-and-seek" quest mini-game (see
 * {@link com.thecompanyinc.cobblemoninitiative.dittohunt.DittoHuntManager}). A {@link Game} places
 * {@code count} distinct Cobblemon-modelled Easy NPC actors (drawn at random from
 * {@link #speciesPool}) at its {@link Game#spots}. Talking to one opens its dialog, which shows its
 * cry as text; one actor is a disguised Ditto whose cry text subtly "slips". Accuse the fake to win.
 *
 * <p>The actors are pre-generated Easy NPC presets (one real + one {@code _fake} per pool species,
 * built by {@code scripts/generate_ditto_actors} into
 * {@code data/easy_npc/preset/cobblemon/ci_ditto_<species>[_fake].npc.snbt}). {@link #speciesPool}
 * MUST list only species that have those presets. Coord-driven like the Gaviota set-pieces: a
 * default with PLACEHOLDER spots is written on first load — fill them, then
 * {@code /cobblemon-initiative ditto reload}.
 */
public class DittoHuntConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-ditto.json");

  private static DittoHuntConfig instance;

  public boolean enabled = true;

  /** Species the round draws from (must have generated {@code ci_ditto_<id>} presets). Keep this in
   *  sync with {@code scripts/generate_ditto_actors}' SPECIES table. */
  public List<String> speciesPool = new ArrayList<>(List.of(
    "pikachu", "eevee", "snorlax", "gengar", "jigglypuff", "meowth", "psyduck", "charmander",
    "bulbasaur", "squirtle", "magikarp", "gyarados", "machop", "growlithe", "vulpix", "oddish",
    "geodude", "zubat", "poliwag", "cubone", "clefairy", "diglett", "abra", "onix", "rattata",
    "pidgey", "ponyta", "slowpoke", "koffing", "staryu"));

  /** Reusable format for a real actor preset resource: {@code %s} = species id. */
  public String presetReal = "easy_npc:preset/cobblemon/ci_ditto_%s.npc.snbt";
  /** The disguised-Ditto variant preset resource (same render, cry text slips). */
  public String presetFake = "easy_npc:preset/cobblemon/ci_ditto_%s_fake.npc.snbt";

  /** Audio nudge: talking to / the "Listen" button plays the REAL canonical cry
   *  {@code cobblemon:pokemon.<species>.cry}. Reals play it clean; the disguised Ditto plays the
   *  SAME cry a touch HIGH — a subtle secondary tell on top of the misremembered text. Kept slight
   *  on purpose (the game is meant to be hard — you read them aloud and puzzle it out). */
  public float cryVolume = 1.0f;
  public float cryPitch = 1.0f;
  /** The fake's cry pitch — a slight step up from 1.0. Subtle by design. */
  public float fakeCryPitch = 1.06f;

  public List<Game> games = new ArrayList<>(List.of(new Game()));

  public static class Game {
    /** Game id the dialog button passes to {@code ditto start <id>}. */
    public String id = "sample";
    public String dimension = "minecraft:overworld";
    /** How many actors to place (one is the fake). Capped at {@link #spots} size. */
    public int count = 5;
    /** Where the actors stand. PLACEHOLDER — replace with real coords (>= count of them), then
     *  reload. Space spots >= 3 blocks apart so "accuse the nearest" is unambiguous. */
    public List<Pos> spots = new ArrayList<>(List.of(
      new Pos(0.5, 64, 0.5), new Pos(4.5, 64, 0.5), new Pos(8.5, 64, 0.5),
      new Pos(0.5, 64, 4.5), new Pos(4.5, 64, 4.5), new Pos(8.5, 64, 4.5)));
    /** Player tag added on a win — the quest turn-in reads it. Blank = none. */
    public String winTag = "ditto_sample_won";
    /** Optional server command run on win / loss (permission 2, from the game's dimension). */
    public String winCommand = "";
    public String loseCommand = "";
    public String startMessage =
      "One of these Pokemon is a Ditto in disguise. Listen to each cry — the fake's will slip. "
        + "When you're sure, call it out.";
    public String winMessage = "Blorp! The disguise melts away — you caught the Ditto out!";
    public String loseMessage = "That one was the real deal. The Ditto giggles and holds its cover.";

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

  /** The named game, or the first one when {@code id} is null/blank, or null if none match. */
  public Game game(String id) {
    if (games == null || games.isEmpty()) return null;
    if (id == null || id.isBlank()) return games.get(0);
    for (Game g : games) {
      if (g.id != null && g.id.equalsIgnoreCase(id)) return g;
    }
    return null;
  }

  // ── singleton / lifecycle ──────────────────────────────────────────────────────

  public static DittoHuntConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() { instance = load(); }

  public static DittoHuntConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader r = new FileReader(CONFIG_FILE)) {
          DittoHuntConfig cfg = GSON.fromJson(r, DittoHuntConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[Ditto] Error loading config, using defaults: {}", e.getMessage());
    }
    DittoHuntConfig cfg = new DittoHuntConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter w = new FileWriter(CONFIG_FILE)) { GSON.toJson(this, w); }
    } catch (IOException e) {
      LOGGER.error("[Ditto] Error saving config: {}", e.getMessage());
    }
  }
}

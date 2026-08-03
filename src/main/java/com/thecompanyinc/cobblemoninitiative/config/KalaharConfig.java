package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config for the Kalahar Reach (gym 6) mirage hunt. When the player nears the gym guide the
 * gym cast is declared "scattered" — heat-shimmer doubles of the six students appear around town
 * (tag {@link #fakeTag}). The REAL students are placement-latched talkable bodies (their existing
 * battle blocks / ladder are untouched); the fakes are summon-only decoys the
 * {@link com.thecompanyinc.cobblemoninitiative.KalaharManager} import_new's at {@link #scatterSpots}.
 * Reaching out to a fake rolls {@link #dopplerChance}: it either poofs (reusing the ci_mirage_popped
 * FX in ambient/tick) or collapses into a hostile low-HP Doppler ({@link #dopplerPreset}) that
 * attacks the player and must be killed.
 *
 * <p>Cached {@link #get()} singleton with clamped setters, mirroring {@link CyclopsConfig}. Edit the
 * JSON or ModMenu, then {@code /cobblemon-initiative kalahar reload}. Coordinates (guide, scatter
 * pool) are showrunner-tunable — the real students sit on verified town ground; a scatter spot that
 * lands off-surface only floats/sinks a harmless decoy, never a required battle.
 */
public class KalaharConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-kalahar.json");

  private static KalaharConfig instance;

  public boolean enabled = true;
  public String dimension = "minecraft:overworld";

  /** Gym-guide position — the hunt auto-starts once a player comes within {@link #guideTriggerRadius}. */
  public Pos guidePos = new Pos(1978.0, 131.0, 4085.0);
  /** Radius (blocks) around {@link #guidePos} that triggers the "on sight" scatter + intro. */
  public double guideTriggerRadius = 12.0;

  /** Total copies (1 real + fakes) per dune trainer — the manager spawns (count - 1) fakes. */
  private int trainerMirageCount = 3;
  /** Total copies (1 real + fakes) per apprentice / jr. apprentice. */
  private int apprenticeMirageCount = 4;
  /** Probability that a reached-out fake collapses into a hostile Doppler (else it just poofs). */
  private double dopplerChance = 0.5;
  /** Doppler max health (absolute, applied once on load) — low so the player one/two-shots it. */
  private float dopplerHealth = 8.0f;
  /** Doppler melee attack damage (absolute) — a real hardcore poke, but not a killer. */
  private float dopplerDamage = 2.0f;

  /** Summon-only hostile Doppler preset (baked from dialog-src). Uses the native easy_npc:doppler
   *  entity type, so it lands under preset/doppler/ (not humanoid/). */
  public String dopplerPreset = "easy_npc:preset/doppler/kalahar_doppler.npc.snbt";
  /** Tag every Doppler body carries (find / scale / bulk-clear key). */
  public String dopplerTag = "ci_mirage_doppler";
  /** Tag every scattered fake mirage carries. */
  public String fakeTag = "ci_mirage_fake";

  /** Town scatter pool the manager draws fake positions from (shuffled per hunt). Showrunner-verify:
   *  a spot off the surface only misplaces a harmless decoy. Needs >= total fakes (~14 by default). */
  public List<Pos> scatterSpots = new ArrayList<>(Arrays.asList(
    new Pos(2004.0, 136.0, 4098.0), new Pos(1992.0, 136.0, 4108.0),
    new Pos(1982.0, 136.0, 4130.0), new Pos(1970.0, 136.0, 4140.0),
    new Pos(2028.0, 127.0, 3980.0), new Pos(2016.0, 127.0, 3968.0),
    new Pos(2143.0, 138.0, 3992.0), new Pos(2155.0, 138.0, 3980.0),
    new Pos(1990.0, 136.0, 4050.0), new Pos(1978.0, 136.0, 4062.0),
    new Pos(2082.0, 121.0, 3954.0), new Pos(2070.0, 121.0, 3942.0),
    new Pos(2058.0, 126.0, 4075.0), new Pos(2088.0, 121.0, 3928.0),
    new Pos(2050.0, 128.0, 4030.0), new Pos(2100.0, 125.0, 4100.0)
  ));

  public static class Pos {
    public double x, y, z;
    public Pos() {}
    public Pos(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
  }

  // ── singleton / lifecycle ──────────────────────────────────────────────────────

  public static KalaharConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() { instance = load(); }

  public static KalaharConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          KalaharConfig cfg = GSON.fromJson(reader, KalaharConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[Kalahar] Error loading config, using defaults: {}", e.getMessage());
    }
    KalaharConfig cfg = new KalaharConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) { GSON.toJson(this, writer); }
    } catch (IOException e) {
      LOGGER.error("[Kalahar] Error saving config: {}", e.getMessage());
    }
  }

  // ── getters / setters (clamped) ─────────────────────────────────────────────────

  public int getTrainerMirageCount() { return Math.max(1, Math.min(8, trainerMirageCount)); }
  public int getApprenticeMirageCount() { return Math.max(1, Math.min(8, apprenticeMirageCount)); }
  public double getDopplerChance() { return clampD(dopplerChance, 0.0, 1.0); }
  public float getDopplerHealth() { return clampF(dopplerHealth, 1.0f, 60.0f); }
  public float getDopplerDamage() { return clampF(dopplerDamage, 0.0f, 20.0f); }

  public void setTrainerMirageCount(int v) { this.trainerMirageCount = Math.max(1, Math.min(8, v)); }
  public void setApprenticeMirageCount(int v) { this.apprenticeMirageCount = Math.max(1, Math.min(8, v)); }
  public void setDopplerChance(double v) { this.dopplerChance = clampD(v, 0.0, 1.0); }
  public void setDopplerHealth(float v) { this.dopplerHealth = clampF(v, 1.0f, 60.0f); }
  public void setDopplerDamage(float v) { this.dopplerDamage = clampF(v, 0.0f, 20.0f); }

  private static float clampF(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
  private static double clampD(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}

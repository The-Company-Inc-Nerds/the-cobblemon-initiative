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

  /** UNUSED since a19 (playtest 2026-08-04 N1): the hunt starts from Tarek Ramessu's dialog
   *  button, not proximity. Fields retained so existing config JSONs keep parsing. */
  public Pos guidePos = new Pos(2031.5, 136.0, 4091.5);
  /** UNUSED since a19 — see {@link #guidePos}. */
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

  /** Scatter pool the manager draws fake positions from (shuffled per hunt) — the 31 in-world
   *  "Gym Mirage Spot" pins from the 2026-08-03 playtest (P5–P35; replace the 16 desk-placed
   *  defaults). Needs >= total fakes (~14 by default). */
  public List<Pos> scatterSpots = new ArrayList<>(Arrays.asList(
    new Pos(2156.1, 126.0, 4199.6), new Pos(2152.1, 126.0, 4228.4),
    new Pos(2221.6, 144.0, 4123.1), new Pos(2205.7, 142.0, 4122.4),
    new Pos(2199.8, 154.0, 4100.1), new Pos(2139.4, 138.0, 4143.0),
    new Pos(2062.3, 134.0, 4141.2), new Pos(2039.7, 139.0, 4098.0),
    new Pos(2039.8, 139.0, 4077.7), new Pos(2012.3, 164.0, 4054.2),
    new Pos(2008.4, 164.0, 4058.4), new Pos(1949.4, 164.0, 4058.3),
    new Pos(1944.0, 164.0, 4053.5), new Pos(1949.5, 164.0, 4116.7),
    new Pos(1943.4, 164.0, 4122.3), new Pos(2007.1, 164.0, 4116.5),
    new Pos(2013.2, 164.0, 4122.4), new Pos(2000.3, 174.0, 4087.5),
    new Pos(1978.7, 174.0, 4065.2), new Pos(1955.6, 174.0, 4087.5),
    new Pos(1978.5, 174.0, 4110.2), new Pos(2019.0, 138.0, 4194.9),
    new Pos(2015.3, 137.0, 4194.3), new Pos(2111.5, 129.0, 4050.3),
    new Pos(2157.2, 129.0, 4051.0), new Pos(2086.0, 136.0, 4014.9),
    new Pos(2042.9, 136.0, 3951.4), new Pos(2210.1, 137.0, 4197.6),
    new Pos(2203.0, 137.0, 4212.4), new Pos(2178.8, 135.0, 4236.5),
    new Pos(2163.2, 135.0, 4244.4)
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

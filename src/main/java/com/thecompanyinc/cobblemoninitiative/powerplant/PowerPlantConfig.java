package com.thecompanyinc.cobblemoninitiative.powerplant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tunables + geometry for the Cyber City power-plant lights puzzle (gym 7 gate) — loaded from the
 * jar resource {@code data/cobblemon_initiative/powerplant/powerplant.json} with a writable
 * override at {@code config/cobblemon-initiative-powerplant.json} (override wins; ModMenu saves
 * there — the SafariConfig shape). Coordinates are showrunner latches filled in later: the engine
 * is INACTIVE until {@link #bulbs} holds exactly {@link #BULB_COUNT} entries.
 *
 * <p>Geometry contract (mirrored in the bundled resource's {@code _comment} keys): exactly 9
 * copper-bulb positions; each switch is a LEVER position plus the two bulb indices (0-8) it
 * toggles. Cover all 9 bulbs via the pairs — 8+ switches; a ring 0-1, 1-2, …, 7-8, 8-0 (9 levers)
 * gives full coverage and a clean circuit fiction. Place levers AWAY from the bulbs so real
 * redstone never fights the engine (the vanilla lever still flips visually; its orientation is
 * cosmetic).
 */
public class PowerPlantConfig {

  /** The puzzle is exactly 9 lights — the pair-toggle math and persistence shape assume it. */
  public static final int BULB_COUNT = 9;

  private static final String RESOURCE_PATH =
    "data/cobblemon_initiative/powerplant/powerplant.json";
  /** Writable override (ModMenu-editable). When present it wins over the bundled resource. */
  private static final File CONFIG_FILE =
    new File("config/cobblemon-initiative-powerplant.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** A copper-bulb block position (any oxidation/waxed variant of the copper-bulb family). */
  public static class Pos {
    public int x, y, z;
    public Pos() {}
    public Pos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
  }

  /** A lever position + the two bulb indices (0-8, distinct) it toggles together. */
  public static class Switch {
    public int x, y, z;
    public int a, b;
    public Switch() {}
    public Switch(int x, int y, int z, int a, int b) {
      this.x = x; this.y = y; this.z = z; this.a = a; this.b = b;
    }
  }

  /** Bulb positions. The engine is INACTIVE unless exactly {@link #BULB_COUNT} are listed. */
  public List<Pos> bulbs = new ArrayList<>();

  /** Lever positions + their bulb pairs. */
  public List<Switch> switches = new ArrayList<>();

  /** Uniform-random switch presses applied to the all-lit state to build a puzzle. */
  public int scrambleMoves = 12;

  /** Re-roll the scramble (bounded) until at least this many bulbs are unlit (always even). */
  public int minUnlit = 4;

  /** Visual re-assert runs only while a player is within this many blocks of any bulb. */
  public int syncRange = 32;

  /** Engine-active gate: the showrunner has latched exactly 9 bulb coords. */
  public boolean isActive() {
    return bulbs != null && bulbs.size() == BULB_COUNT;
  }

  /**
   * Load-time validation (WARN, never fail — the showrunner reads the log while latching):
   * <ul>
   *   <li>Any switch whose two indices are equal or outside 0..8 is INERT (excluded from both the
   *       scramble and the press path — a malformed generator would break the reachability proof)
   *       and warned about.</li>
   *   <li>Union-find over the switch-pair graph on the 9 bulbs: any bulb in no pair, and any
   *       disconnected component, is listed. Coverage gaps mean those bulbs can never change —
   *       they stay LIT from the all-lit scramble start, so the puzzle remains solvable, but the
   *       showrunner should know the wiring is incomplete.</li>
   * </ul>
   *
   * @return warning lines (also logged by the caller); empty = clean.
   */
  public List<String> validate() {
    List<String> warns = new ArrayList<>();
    if (bulbs == null) bulbs = new ArrayList<>();
    if (switches == null) switches = new ArrayList<>();
    if (!bulbs.isEmpty() && bulbs.size() != BULB_COUNT) {
      warns.add("bulbs: " + bulbs.size() + " listed — the engine needs exactly "
        + BULB_COUNT + " (INACTIVE until then).");
    }
    for (int i = 0; i < switches.size(); i++) {
      Switch s = switches.get(i);
      if (s == null || s.a == s.b || s.a < 0 || s.a >= BULB_COUNT || s.b < 0 || s.b >= BULB_COUNT) {
        warns.add("switch " + i + ": invalid pair (a=" + (s == null ? "?" : s.a)
          + ", b=" + (s == null ? "?" : s.b) + ") — must be two DISTINCT indices in 0..8; "
          + "this switch is INERT until fixed.");
      }
    }
    // Union-find the pair graph over the 9 bulbs (valid switches only).
    int[] parent = new int[BULB_COUNT];
    for (int i = 0; i < BULB_COUNT; i++) parent[i] = i;
    boolean[] covered = new boolean[BULB_COUNT];
    for (Switch s : validSwitches()) {
      covered[s.a] = true;
      covered[s.b] = true;
      int ra = find(parent, s.a), rb = find(parent, s.b);
      if (ra != rb) parent[ra] = rb;
    }
    List<Integer> uncovered = new ArrayList<>();
    for (int i = 0; i < BULB_COUNT; i++) if (!covered[i]) uncovered.add(i);
    if (!uncovered.isEmpty()) {
      warns.add("coverage gap: bulb(s) " + uncovered + " appear in NO switch pair — they can "
        + "never change state (they stay lit from the scramble; still solvable, but wire them in).");
    }
    // Components among covered bulbs only (uncovered bulbs are already reported above).
    List<List<Integer>> components = new ArrayList<>();
    for (int root = 0; root < BULB_COUNT; root++) {
      if (!covered[root] || find(parent, root) != root) continue;
      List<Integer> comp = new ArrayList<>();
      for (int i = 0; i < BULB_COUNT; i++) if (covered[i] && find(parent, i) == root) comp.add(i);
      components.add(comp);
    }
    if (components.size() > 1) {
      warns.add("disconnected wiring: the switch-pair graph splits into " + components.size()
        + " components " + components + " — each scrambles/solves independently (intended?).");
    }
    return warns;
  }

  /** The switches that actually participate (distinct in-range pair indices). */
  public List<Switch> validSwitches() {
    List<Switch> out = new ArrayList<>();
    if (switches == null) return out;
    for (Switch s : switches) {
      if (s != null && s.a != s.b
          && s.a >= 0 && s.a < BULB_COUNT && s.b >= 0 && s.b < BULB_COUNT) {
        out.add(s);
      }
    }
    return out;
  }

  private static int find(int[] parent, int i) {
    while (parent[i] != i) {
      parent[i] = parent[parent[i]];
      i = parent[i];
    }
    return i;
  }

  public static PowerPlantConfig load() {
    // Writable ModMenu override wins over the bundled resource default.
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        PowerPlantConfig cfg = GSON.fromJson(reader, PowerPlantConfig.class);
        if (cfg != null) {
          return cfg;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to read {} — falling back to the bundled default.", CONFIG_FILE, e);
      }
    }
    try (
      InputStream in = PowerPlantConfig.class.getClassLoader()
        .getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "Power plant config resource missing ({}); using built-in defaults.",
          RESOURCE_PATH
        );
        return new PowerPlantConfig();
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        PowerPlantConfig cfg = new Gson().fromJson(reader, PowerPlantConfig.class);
        return cfg != null ? cfg : new PowerPlantConfig();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load power plant config — using defaults.", e);
      return new PowerPlantConfig();
    }
  }

  /** Write the current values to the config override (ModMenu save path). */
  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Error saving power plant config: {}", e.getMessage());
    }
  }
}

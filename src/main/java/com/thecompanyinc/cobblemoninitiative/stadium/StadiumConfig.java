package com.thecompanyinc.cobblemoninitiative.stadium;

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
 * Stadium wave schedule, loaded from the jar-baked
 * {@code data/cobblemon_initiative/stadium/waves.json} (same classpath-resource pattern
 * as the shrine challenge configs). One shared wave list serves every bracket — the
 * battle-level lock flattens all combatants to the chosen bracket, so per-bracket team
 * variants would be indistinguishable in play.
 *
 * <p>Purse invariant (ENGINE_FINDINGS §3): every amount here is FIXED and is printed to
 * the player before the wave is fought — never rolled. Repeatable content also never
 * grants item/training packs, so prizes are CobbleDollars only.
 */
public class StadiumConfig {

  private static final Gson GSON = new Gson();
  private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();
  private static final String RESOURCE_PATH =
    "data/cobblemon_initiative/stadium/waves.json";
  /**
   * Writable ModMenu override — holds ONLY the scalar knobs. The wave schedule stays
   * content: it is always read from the bundled resource so a version bump that retunes
   * the wave teams is never frozen by a stale config file.
   */
  private static final File OVERRIDE_FILE = new File("config/cobblemon-initiative-stadium.json");

  /** The scalar tunables that the ModMenu screen persists (waves excluded on purpose). */
  private static class ScalarOverrides {
    int ticksBetweenWaves;
    int completionPurse;
  }

  /** One wave: an rctmod trainer team file + its fixed purse. */
  public static class Wave {
    public String trainerId;
    public String displayName;
    public int purse;
  }

  private int ticksBetweenWaves = 100;
  private int completionPurse = 1500;
  private List<Wave> waves = new ArrayList<>();

  public static StadiumConfig load() {
    try (
      InputStream in = StadiumConfig.class
        .getClassLoader()
        .getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in != null) {
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
          StadiumConfig cfg = GSON.fromJson(reader, StadiumConfig.class);
          if (cfg != null && cfg.waves != null && !cfg.waves.isEmpty()) {
            cfg = applyOverrides(cfg);
            InitiativeInit.LOGGER.info(
              "[Stadium] Loaded {} waves (completion purse {}).",
              cfg.waves.size(),
              cfg.completionPurse
            );
            return cfg;
          }
        }
      }
      InitiativeInit.LOGGER.warn(
        "[Stadium] Wave config missing or empty at {} — stadium runs disabled.",
        RESOURCE_PATH
      );
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[Stadium] Failed to load wave config", e);
    }
    return applyOverrides(new StadiumConfig()); // empty waves — startRun refuses cleanly
  }

  /** Overlay the ModMenu scalar override (if present) onto a resource-loaded config. */
  private static StadiumConfig applyOverrides(StadiumConfig cfg) {
    if (OVERRIDE_FILE.exists()) {
      try (FileReader reader = new FileReader(OVERRIDE_FILE)) {
        ScalarOverrides ov = GSON.fromJson(reader, ScalarOverrides.class);
        if (ov != null) {
          cfg.ticksBetweenWaves = ov.ticksBetweenWaves;
          cfg.completionPurse = ov.completionPurse;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "[Stadium] Failed to read {} — using resource scalars.", OVERRIDE_FILE, e);
      }
    }
    return cfg;
  }

  /** Persist ONLY the scalar knobs to the override file (ModMenu save path). */
  public void save() {
    ScalarOverrides ov = new ScalarOverrides();
    ov.ticksBetweenWaves = ticksBetweenWaves;
    ov.completionPurse = completionPurse;
    try {
      OVERRIDE_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(OVERRIDE_FILE)) {
        PRETTY.toJson(ov, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[Stadium] Error saving config: {}", e.getMessage());
    }
  }

  public int getTicksBetweenWaves() { return Math.max(1, ticksBetweenWaves); }
  public int getCompletionPurse() { return completionPurse; }
  public List<Wave> getWaves() { return waves; }

  public void setTicksBetweenWaves(int v) { this.ticksBetweenWaves = v; }
  public void setCompletionPurse(int v) { this.completionPurse = v; }
}

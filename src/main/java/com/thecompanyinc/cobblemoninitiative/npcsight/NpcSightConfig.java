package com.thecompanyinc.cobblemoninitiative.npcsight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcSightConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-npcsight.json"
  );

  /** Default sight range in blocks for NPCs that don't override it. */
  private int defaultSightRange = 10;

  /** Show colored dust particles along the raycast path. */
  private boolean debugMode = false;

  /** Distance within which canSeePlayer AND proximity triggers the Easy NPC dialog. */
  private double dialogRange = 3.0;

  /**
   * Default Easy NPC dialog identifier used when an NPC has no per-entity dialog set.
   * Blank string disables the fallback entirely.
   */
  private String defaultDialogName = "npc_sight_trigger";

  /** Field-of-view cone (degrees) an NPC can see; 120 = cos(60°). 360 = omnidirectional. */
  private int fovDegrees = 120;

  /** Run the full sight check every N server ticks (5 ≈ 4×/sec at 20 TPS). */
  private int tickInterval = 5;

  /** Debug raycast: block step between particles. */
  private double debugRayStep = 0.5;

  /** Debug raycast: cap on particles drawn per ray. */
  private int debugRayMaxSteps = 512;

  public static NpcSightConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          NpcSightConfig cfg = GSON.fromJson(reader, NpcSightConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[NPC Sight] Error loading config, using defaults: {}", e.getMessage());
    }
    NpcSightConfig cfg = new NpcSightConfig();
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
      LOGGER.error("[NPC Sight] Error saving config: {}", e.getMessage());
    }
  }

  public int getDefaultSightRange() {
    return defaultSightRange;
  }

  public boolean isDebugMode() {
    return debugMode;
  }

  public double getDialogRange() {
    return dialogRange;
  }

  public void setDefaultSightRange(int v) {
    this.defaultSightRange = v;
  }

  public void setDebugMode(boolean v) {
    this.debugMode = v;
  }

  public void setDialogRange(double v) {
    this.dialogRange = v;
  }

  public String getDefaultDialogName() {
    return defaultDialogName;
  }

  public void setDefaultDialogName(String v) {
    this.defaultDialogName = v;
  }

  public int getFovDegrees() { return fovDegrees; }
  public int getTickInterval() { return tickInterval; }
  public double getDebugRayStep() { return debugRayStep; }
  public int getDebugRayMaxSteps() { return debugRayMaxSteps; }

  public void setFovDegrees(int v) { this.fovDegrees = v; }
  public void setTickInterval(int v) { this.tickInterval = v; }
  public void setDebugRayStep(double v) { this.debugRayStep = v; }
  public void setDebugRayMaxSteps(int v) { this.debugRayMaxSteps = v; }
}

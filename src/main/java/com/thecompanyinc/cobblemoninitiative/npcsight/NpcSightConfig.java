package com.thecompanyinc.cobblemoninitiative.npcsight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NpcSightConfig {

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

  public static NpcSightConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          NpcSightConfig cfg = GSON.fromJson(reader, NpcSightConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      System.out.println(
        "[NPC Sight] Error loading config, using defaults: " + e.getMessage()
      );
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
      System.out.println("[NPC Sight] Error saving config: " + e.getMessage());
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
}

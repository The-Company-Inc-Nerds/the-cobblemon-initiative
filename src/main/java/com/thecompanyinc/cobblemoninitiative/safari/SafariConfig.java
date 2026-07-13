package com.thecompanyinc.cobblemoninitiative.safari;

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

/**
 * Tunables for the Biodiversity Asset Preserve ("the Baiting Yards") — loaded from the
 * jar resource {@code data/cobblemon_initiative/safari/safari.json} (noble/quest-track
 * pattern: showrunner numbers live in data, code carries the defaults as a fallback).
 *
 * <p>All fees are FLAT and printed before commitment (randomness invariant: committed
 * amounts are never rolled).
 */
public class SafariConfig {

  private static final String RESOURCE_PATH =
    "data/cobblemon_initiative/safari/safari.json";
  /** Writable override (ModMenu-editable). When present it wins over the bundled resource. */
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-safari.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** Day Permit price in CobbleDollars — the pay-probe amount. */
  public int permitFee = 1500;

  /** Company-issue Safari Balls granted per permit (clawed back at exit). */
  public int balls = 20;

  /** Site clock, in seconds (tick-based — the ESC pause freezes it; say so on stream). */
  public int clockSeconds = 900;

  /** How long lured Pokémon linger before wandering off, in seconds. */
  public int windowSeconds = 75;

  /** Gym badges required before Intake will sell a permit. */
  public int gateBadges = 3;

  /** Suspense window between a scatter and the spawn roll, in seconds (min..max). */
  public int suspenseMinSeconds = 5;

  public int suspenseMaxSeconds = 15;

  /** Spawns rolled per scatter (min..max, inclusive). */
  public int spawnsMin = 1;

  public int spawnsMax = 3;

  /**
   * Eject pad. All-zero = unset → the timer escort returns the player to the exact
   * position where they entered (recorded at permit purchase — verified-safe ground by
   * construction: they stood on it).
   */
  public int ejectX = 0;

  public int ejectY = 0;

  public int ejectZ = 0;

  public boolean hasEjectPad() {
    return ejectX != 0 || ejectY != 0 || ejectZ != 0;
  }

  public static SafariConfig load() {
    // Writable ModMenu override wins over the bundled resource default.
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        SafariConfig cfg = GSON.fromJson(reader, SafariConfig.class);
        if (cfg != null) {
          return cfg;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to read {} — falling back to the bundled default.", CONFIG_FILE, e);
      }
    }
    try (
      InputStream in = SafariConfig.class.getClassLoader()
        .getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "Safari config resource missing ({}); using built-in defaults.",
          RESOURCE_PATH
        );
        return new SafariConfig();
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        SafariConfig cfg = new Gson().fromJson(reader, SafariConfig.class);
        return cfg != null ? cfg : new SafariConfig();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load safari config — using defaults.", e);
      return new SafariConfig();
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
      InitiativeInit.LOGGER.error("Error saving safari config: {}", e.getMessage());
    }
  }
}

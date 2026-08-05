package com.thecompanyinc.cobblemoninitiative.phone;

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
 * Tunables for the PokePhone ring flow — loaded from the jar resource
 * {@code data/cobblemon_initiative/phone/phone.json} with a writable ModMenu override
 * (the SafariConfig pattern: showrunner numbers live in data, code carries fallbacks).
 */
public class PhoneCallConfig {

  private static final String RESOURCE_PATH = "data/cobblemon_initiative/phone/phone.json";
  /** Writable override (ModMenu-editable). When present it wins over the bundled resource. */
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-phone.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** Ring window: how long the incoming-call actionbar flashes before the call is missed. */
  public int ringSeconds = 8;

  /** Owed-call re-ring delay: a missed/declined/aborted story call rings again after this
   *  long — completion is the ONLY consume, so no beat is ever lost. */
  public int reRingSeconds = 90;

  public static PhoneCallConfig load() {
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        PhoneCallConfig cfg = GSON.fromJson(reader, PhoneCallConfig.class);
        if (cfg != null) {
          return cfg;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to read {} — falling back to the bundled default.", CONFIG_FILE, e);
      }
    }
    try (
      InputStream in = PhoneCallConfig.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "Phone config resource missing ({}); using built-in defaults.", RESOURCE_PATH);
        return new PhoneCallConfig();
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        PhoneCallConfig cfg = new Gson().fromJson(reader, PhoneCallConfig.class);
        return cfg != null ? cfg : new PhoneCallConfig();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load phone config — using defaults.", e);
      return new PhoneCallConfig();
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
      InitiativeInit.LOGGER.error("Error saving phone config: {}", e.getMessage());
    }
  }
}

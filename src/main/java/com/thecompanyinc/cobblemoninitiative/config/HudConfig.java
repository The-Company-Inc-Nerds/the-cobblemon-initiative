package com.thecompanyinc.cobblemoninitiative.config;

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
 * Stream HUD tunables — the only-on-change CobbleDollars balance flash and the
 * quest-sidebar auto-hide. Loaded from the jar resource
 * {@code data/cobblemon_initiative/hud/hud.json} with a writable ModMenu override
 * (the SafariConfig bundled+override shape).
 *
 * <p>Unlike the manager-held configs this one is read per frame by two client mixins,
 * so a live static instance is kept ({@link #get}); {@link #reload} swaps it after a
 * ModMenu save (EconomyConfig/SpecialSpawnConfig are the static-reload precedent).
 */
public class HudConfig {

  private static final String RESOURCE_PATH = "data/cobblemon_initiative/hud/hud.json";
  /** Writable override (ModMenu-editable). When present it wins over the bundled resource. */
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-hud.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static volatile HudConfig instance;

  /** Show the CobbleDollars balance HUD only when the balance changes (plus once on join). */
  public boolean cobbledollarsOnChange = true;

  /** How long the balance HUD stays up after a change, in seconds. */
  public int cobbledollarsShowSeconds = 4;

  /** Auto-hide the ci_quest scoreboard sidebar between quest events. */
  public boolean autoHideQuestSidebar = true;

  /** How long the quest sidebar stays up after a trigger, in seconds. */
  public int sidebarShowSeconds = 8;

  /** Cover every world join with the branded hold overlay until the terrain around the camera
   * has compiled (hides Sodium/BSL chunk pop-in; the fresh-install opening cutscene waits on
   * the release). See {@code renderready/RenderReadyClient}. */
  public boolean holdOverlayOnJoin = true;

  /** Hard cap on the join hold, in seconds (5–120): the overlay releases (and reports ready)
   * even if the renderer never settles. */
  public int overlayMaxSeconds = 45;

  /** Radius in chunks (2–16, clamped to render distance) that must be streamed in around the
   * player before the join hold releases — the opening cutscene's near field. */
  public int readyRadiusChunks = 8;

  /** The live instance the render-path mixins read every frame. */
  public static HudConfig get() {
    HudConfig cfg = instance;
    if (cfg == null) {
      cfg = load();
      instance = cfg;
    }
    return cfg;
  }

  /** Re-read into the live static (ModMenu save path). */
  public static void reload() {
    instance = load();
  }

  public static HudConfig load() {
    // Writable ModMenu override wins over the bundled resource default.
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        HudConfig cfg = GSON.fromJson(reader, HudConfig.class);
        if (cfg != null) {
          return cfg;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to read {} — falling back to the bundled default.", CONFIG_FILE, e);
      }
    }
    try (
      InputStream in = HudConfig.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "HUD config resource missing ({}); using built-in defaults.", RESOURCE_PATH);
        return new HudConfig();
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        HudConfig cfg = new Gson().fromJson(reader, HudConfig.class);
        return cfg != null ? cfg : new HudConfig();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load HUD config — using defaults.", e);
      return new HudConfig();
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
      InitiativeInit.LOGGER.error("Error saving HUD config: {}", e.getMessage());
    }
  }
}

package com.thecompanyinc.cobblemoninitiative.graphics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hand-editable definition of the HIGH and LOW graphics presets that {@link GraphicsPresetManager}
 * applies. Mirrors {@code SpecialSpawnConfig} / {@code DojoConfig}: pretty-printed Gson at
 * {@code config/cobblemon-initiative-graphics.json}, cached {@link #get()} singleton,
 * {@link #reload()} after an external edit.
 *
 * <p>The two texture packs are referenced by their {@code PackRepository} id — a local pack in
 * {@code resourcepacks/} is {@code "file/<zip-or-folder-name>"}. The 256x pack ships with the mod;
 * the 32x pack must be dropped into {@code resourcepacks/} and its filename set in {@link #lowPackId}
 * (default assumes {@code Prime's HD Textures [32x].zip}). Shaders are toggled on/off only — Iris's
 * public API cannot pick a pack by name, so {@code iris.properties} keeps BSL selected and we flip
 * {@code enableShaders}. See {@link GraphicsPresetManager}.
 */
public class GraphicsPresetConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-graphics.json");

  /** One graphics tier: which shaders, textures, and video settings it applies. */
  public static class Preset {
    /** Keep BSL on (true — the normal path) or disable shaders entirely (false, fallback). */
    public boolean shaders;
    /**
     * BSL option overrides applied live via Iris when {@link #shaders} is on — same keys BSL's own
     * {@code profile.LOW}/{@code profile.HIGH} set. Edit freely (booleans "true"/"false"; sliders
     * like {@code shadowMapResolution}=int, {@code shadowDistance}=float). Empty = leave BSL as-is.
     */
    public Map<String, String> shaderOptions;
    /** "fast" | "fancy" | "fabulous". */
    public String graphics;
    public int renderDistance;
    public int simulationDistance;
    /** Smooth lighting (ambient occlusion). */
    public boolean ao;
    /** "all" | "decreased" | "minimal". */
    public String particles;
    /** "off" | "fast" | "fancy". */
    public String clouds;
    public double entityDistanceScaling;
    public int biomeBlendRadius;
    public int mipmapLevels;

    Preset(boolean shaders, String graphics, int renderDistance, int simulationDistance,
           boolean ao, String particles, String clouds, double entityDistanceScaling,
           int biomeBlendRadius, int mipmapLevels) {
      this.shaders = shaders;
      this.graphics = graphics;
      this.renderDistance = renderDistance;
      this.simulationDistance = simulationDistance;
      this.ao = ao;
      this.particles = particles;
      this.clouds = clouds;
      this.entityDistanceScaling = entityDistanceScaling;
      this.biomeBlendRadius = biomeBlendRadius;
      this.mipmapLevels = mipmapLevels;
    }
  }

  /**
   * BSL's ULTRA tier (profile.ULTRA, resolved) — used by the HIGH graphics mode: full
   * shadows/lighting like HIGH, but a 3072 shadow map at 512 distance. Only these keys are
   * applied, so any other BSL options you've enabled (e.g. ADVANCED_MATERIALS) are preserved.
   */
  private static Map<String, String> bslUltra() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("SHADOW", "true");
    m.put("SHADOW_COLOR", "true");
    m.put("SHADOW_FILTER", "true");
    m.put("AO", "true");
    m.put("LIGHT_SHAFT", "true");
    m.put("TAA", "true");
    m.put("shadowMapResolution", "3072");
    m.put("shadowDistance", "512.0");
    return m;
  }

  /** BSL's LOW tier (profile.LOW, resolved): shadows kept but 1024, no AO/light-shafts/soft-shadows/TAA. */
  private static Map<String, String> bslLow() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("SHADOW", "true");
    m.put("SHADOW_COLOR", "false");
    m.put("SHADOW_FILTER", "false");
    m.put("AO", "false");
    m.put("LIGHT_SHAFT", "false");
    m.put("TAA", "false");
    m.put("shadowMapResolution", "1024");
    m.put("shadowDistance", "128.0");
    return m;
  }

  private static Preset defaultHigh() {
    Preset p = new Preset(true, "fancy", 16, 12, true, "all", "fancy", 3.0, 5, 4);
    p.shaderOptions = bslUltra();
    return p;
  }

  private static Preset defaultLow() {
    // shaders STAY on — BSL is toned down to its LOW tier rather than disabled.
    Preset p = new Preset(true, "fast", 8, 5, false, "minimal", "off", 0.5, 0, 0);
    p.shaderOptions = bslLow();
    return p;
  }

  /** Last-applied mode, so a fresh session restores the player's choice. "high" | "low". */
  public String mode = "high";

  /** {@code PackRepository} id of the 256x HD pack (ships with the mod). */
  public String hdPackId = "file/Prime's HD Textures [256x].zip";

  /** {@code PackRepository} id of the 32x low pack (Modrinth primes-hd-textures 35.2, parentheses). */
  public String lowPackId = "file/Prime's HD Textures (32x).zip";

  /** Informational: the shaderpack HIGH re-enables. Selection lives in {@code iris.properties}. */
  public String highShaderPack = "BSL_v10.1.3.zip";

  /** When false, the toggle only swaps textures + shaders and leaves video sliders untouched. */
  public boolean applyVideoSettings = true;

  public Preset high = defaultHigh();

  public Preset low = defaultLow();

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  private static GraphicsPresetConfig instance;

  public static GraphicsPresetConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static GraphicsPresetConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          GraphicsPresetConfig cfg = GSON.fromJson(reader, GraphicsPresetConfig.class);
          if (cfg != null) {
            if (cfg.high == null) cfg.high = defaultHigh();
            if (cfg.low == null) cfg.low = defaultLow();
            if (cfg.high.shaderOptions == null) cfg.high.shaderOptions = bslUltra();
            if (cfg.low.shaderOptions == null) cfg.low.shaderOptions = bslLow();
            return cfg;
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[Graphics] Error loading config, using defaults: {}", e.getMessage());
    }
    GraphicsPresetConfig cfg = new GraphicsPresetConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      if (CONFIG_FILE.getParentFile() != null) CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[Graphics] Error saving config: {}", e.getMessage());
    }
  }

  public Preset preset(GraphicsMode m) {
    return m == GraphicsMode.LOW ? low : high;
  }

  public GraphicsMode savedMode() {
    GraphicsMode m = GraphicsMode.fromId(mode);
    return m == null ? GraphicsMode.HIGH : m;
  }
}

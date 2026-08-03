package com.thecompanyinc.cobblemoninitiative.graphics;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Soft bridge to Iris shaders. Iris is a runtime-only mod (dropped into the pack, never a
 * compile-time dependency — same spirit as the JourneyMap bridge), so every call is guarded
 * by {@link #isIrisLoaded()} and reaches Iris through reflection.
 *
 * <p>This pack keeps BSL <em>on</em> in both graphics modes and tones it up/down via BSL's own
 * quality options instead of turning shaders off. The apply path (all bytecode-verified against
 * iris-fabric-1.8.8):
 * <ol>
 *   <li>Ensure {@code enableShaders=true} + {@code shaderPack=BSL} in {@code config/iris.properties}
 *       (persists across restarts; dodges the Iris #2310 empty-pack-but-enabled trap).
 *   <li>{@code net.irisshaders.iris.api.v0.IrisApiConfig#setShadersEnabledAndApply(true)} if shaders
 *       were off.
 *   <li>{@code net.irisshaders.iris.Iris#queueShaderPackOptionsFromProperties(Properties)} with the
 *       mode's BSL option values (e.g. {@code SHADOW}, {@code AO}, {@code LIGHT_SHAFT}, {@code TAA},
 *       {@code shadowMapResolution}) — the same keys BSL's {@code profile.LOW}/{@code profile.HIGH}
 *       set — then {@code Iris#reload()} to apply + persist them to {@code shaderpacks/<pack>.zip.txt}.
 * </ol>
 *
 * <p>Passing {@code shadersEnabled=false} instead just disables shaders (kept as a fallback path).
 * Iris's public API can't select a pack by name, so BSL stays the selected pack via iris.properties.
 */
public final class IrisBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final File IRIS_PROPERTIES = new File("config/iris.properties");

  private IrisBridge() {}

  public static boolean isIrisLoaded() {
    return FabricLoader.getInstance().isModLoaded("iris");
  }

  /**
   * Apply a graphics mode's shader state.
   *
   * @param shadersEnabled  keep BSL on (true — the normal path) or disable shaders (false).
   * @param shaderPack      the shaderpack to pin in iris.properties when enabling (BSL).
   * @param shaderOptions   BSL option overrides to apply live (ignored when disabling / null / empty).
   */
  public static void apply(boolean shadersEnabled, String shaderPack, Map<String, String> shaderOptions) {
    if (!isIrisLoaded()) return;
    writeIrisProperties(shadersEnabled, shaderPack);
    if (!shadersEnabled) {
      setEnabledLive(false);
      return;
    }
    setEnabledLive(true); // no-ops if already on
    if (shaderOptions != null && !shaderOptions.isEmpty()) {
      queueOptionsAndReload(shaderOptions, shaderPack);
    }
  }

  /** The Iris per-pack settings file for a shaderpack: {@code shaderpacks/<pack>.txt}. */
  private static File packSettingsFile(String shaderPack) {
    return new File("shaderpacks/" + shaderPack + ".txt");
  }

  // ── iris.properties (enable flag + pinned pack) ───────────────────────────────

  private static void writeIrisProperties(boolean enabled, String shaderPack) {
    try {
      List<String> lines = IRIS_PROPERTIES.exists()
        ? new ArrayList<>(Files.readAllLines(IRIS_PROPERTIES.toPath(), StandardCharsets.UTF_8))
        : new ArrayList<>();
      boolean sawEnable = false;
      boolean sawPack = false;
      boolean wantPack = shaderPack != null && !shaderPack.isBlank();
      for (int i = 0; i < lines.size(); i++) {
        String trimmed = lines.get(i).trim();
        if (trimmed.startsWith("#")) continue;
        if (trimmed.startsWith("enableShaders")) {
          lines.set(i, "enableShaders=" + enabled);
          sawEnable = true;
        } else if (wantPack && trimmed.startsWith("shaderPack")) {
          lines.set(i, "shaderPack=" + shaderPack);
          sawPack = true;
        }
      }
      if (!sawEnable) lines.add("enableShaders=" + enabled);
      if (wantPack && !sawPack && enabled) lines.add("shaderPack=" + shaderPack);
      if (IRIS_PROPERTIES.getParentFile() != null) IRIS_PROPERTIES.getParentFile().mkdirs();
      Files.write(IRIS_PROPERTIES.toPath(), lines, StandardCharsets.UTF_8);
    } catch (Exception e) {
      LOGGER.warn("[Graphics] Could not rewrite iris.properties: {}", e.getMessage());
    }
  }

  // ── live Iris calls (reflection) ──────────────────────────────────────────────

  /** Flip the live shaders-enabled flag via the public v0 API; skips a redundant recompile. */
  private static void setEnabledLive(boolean enabled) {
    try {
      Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
      Object api = apiClass.getMethod("getInstance").invoke(null);
      Object config = apiClass.getMethod("getConfig").invoke(api);
      Object current = config.getClass().getMethod("areShadersEnabled").invoke(config);
      if (current instanceof Boolean b && b == enabled) return;
      config.getClass().getMethod("setShadersEnabledAndApply", boolean.class).invoke(config, enabled);
    } catch (ClassNotFoundException e) {
      LOGGER.debug("[Graphics] Iris API not found via reflection; relying on iris.properties.");
    } catch (Throwable t) {
      LOGGER.warn("[Graphics] Iris live enable toggle failed: {}", t.toString());
    }
  }

  /**
   * Queue BSL option overrides and reload the pack so they apply live and persist to the pack's
   * settings file — the same mechanism Iris's own shader screen uses.
   *
   * <p>A queued reload resets any option NOT in the queue back toward the pack defaults (that's why
   * a naive queue of only our tier keys dropped the user's {@code ADVANCED_MATERIALS=true}). To keep
   * every option the player has set, we first read the current {@code shaderpacks/<pack>.txt} and
   * seed the queue with all of it, then overlay our tier overrides — so nothing else is disturbed.
   */
  private static void queueOptionsAndReload(Map<String, String> shaderOptions, String shaderPack) {
    try {
      Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
      Properties props = new Properties();
      // Seed with the player's existing per-pack settings so a queued reload doesn't reset them.
      File settings = packSettingsFile(shaderPack);
      if (settings.isFile()) {
        try (var in = Files.newInputStream(settings.toPath())) {
          props.load(in);
        } catch (Exception ex) {
          LOGGER.warn("[Graphics] Could not read {} — preserving nothing extra: {}",
            settings.getName(), ex.getMessage());
        }
      }
      // Overlay this mode's tier overrides on top.
      for (Map.Entry<String, String> e : shaderOptions.entrySet()) {
        if (e.getKey() != null && e.getValue() != null) {
          props.setProperty(e.getKey(), e.getValue());
        }
      }
      iris.getMethod("queueShaderPackOptionsFromProperties", Properties.class).invoke(null, props);
      iris.getMethod("reload").invoke(null);
      LOGGER.info("[Graphics] Applied {} BSL option(s) (incl. preserved) and reloaded shaders.",
        props.size());
    } catch (ClassNotFoundException e) {
      LOGGER.debug("[Graphics] Iris core class not found; BSL options not applied.");
    } catch (Throwable t) {
      // reload() declares IOException; it arrives wrapped — log and move on, iris.properties still set.
      LOGGER.warn("[Graphics] Iris shader-option apply/reload failed: {}", t.toString());
    }
  }
}

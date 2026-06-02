package com.thecompanyinc.cobblemoninitiative.mapfrontiers;

import com.thecompanyinc.cobblemoninitiative.install.InstallZone;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thin bridge between install-time code and the Map Frontiers integration.
 *
 * <p>This class has <b>no Map Frontiers imports</b>, so it is safe to reference from
 * anywhere in the mod regardless of whether Map Frontiers is installed. The actual
 * integration is in {@link MapFrontiersIntegration}, which is only loaded when the
 * mod is present (guarded by a {@code FabricLoader.isModLoaded} check).
 */
public final class MapFrontiersBridge {

  private static Consumer<List<InstallZone>> handler;

  private MapFrontiersBridge() {}

  /** Called by {@link MapFrontiersIntegration} once the plugin is registered. */
  public static void setHandler(Consumer<List<InstallZone>> h) {
    handler = h;
  }

  /**
   * Returns {@code true} if Map Frontiers is loaded and the plugin has been
   * initialized (i.e., the server has started at least once).
   */
  public static boolean isAvailable() {
    return handler != null;
  }

  /** Creates one global frontier per zone. No-op when Map Frontiers is absent. */
  public static void createFrontiers(List<InstallZone> zones) {
    if (handler != null) handler.accept(zones);
  }
}

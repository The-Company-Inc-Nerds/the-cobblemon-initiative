package com.thecompanyinc.cobblemoninitiative.mapfrontiers;

import com.thecompanyinc.cobblemoninitiative.install.InstallZone;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

/**
 * Public entry point for the Map Frontiers integration.
 *
 * <p>This class touches <b>no</b> Map Frontiers types, so it is safe to reference from install
 * code regardless of whether Map Frontiers is present. The actual work is done reflectively in
 * {@link MapFrontiersIntegration} (which also has no Map Frontiers compile-time symbols), so on
 * the 1.21.1 Cobblemon line — where the Map Frontiers plugin API does not exist — frontier
 * regions are still created by calling the mod's internal manager directly. See
 * {@link MapFrontiersIntegration} for why reflection is used here.
 */
public final class MapFrontiersBridge {

  private MapFrontiersBridge() {}

  /** True when Map Frontiers is installed. Frontier creation is a no-op otherwise. */
  public static boolean isAvailable() {
    return FabricLoader.getInstance().isModLoaded("mapfrontiers");
  }

  /**
   * Creates one global frontier per zone. No-op (returns 0) when Map Frontiers is absent,
   * the zone list is empty, or no owner player is available.
   *
   * @param zones zones to draw, from install.json
   * @param owner player to register as creator/owner of the (server-wide) frontiers
   * @return the number of frontiers successfully created
   */
  public static int createFrontiers(List<InstallZone> zones, ServerPlayer owner) {
    if (!isAvailable() || zones == null || zones.isEmpty()) return 0;
    return MapFrontiersIntegration.createGlobalFrontiers(zones, owner);
  }

  /**
   * True when global frontiers already exist in this world — pre-baked into the bundled
   * map by build_mrpack, or created by an earlier install run. Install run checks this
   * BEFORE creating, so baked worlds and manual re-runs never stack duplicate zones.
   * False when Map Frontiers is absent or the check cannot resolve (fail-open: creation
   * would fail identically, so nothing double-creates).
   */
  public static boolean hasExistingFrontiers() {
    return isAvailable() && MapFrontiersIntegration.hasAnyGlobalFrontiers();
  }
}

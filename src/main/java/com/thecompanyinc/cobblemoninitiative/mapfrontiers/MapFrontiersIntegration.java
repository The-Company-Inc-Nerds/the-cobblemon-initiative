package com.thecompanyinc.cobblemoninitiative.mapfrontiers;

import com.thecompanyinc.cobblemoninitiative.install.InstallZone;
import games.alejandrocoria.mapfrontiers.api.MapFrontiersAPI;
import games.alejandrocoria.mapfrontiers.api.model.DimensionId;
import games.alejandrocoria.mapfrontiers.api.model.FrontierCreateRequest;
import games.alejandrocoria.mapfrontiers.api.model.FrontierShape;
import games.alejandrocoria.mapfrontiers.api.model.FrontierVisibilityFlag;
import games.alejandrocoria.mapfrontiers.api.model.Point2i;
import games.alejandrocoria.mapfrontiers.api.model.UserRef;
import games.alejandrocoria.mapfrontiers.api.plugin.IMapFrontiersServerPlugin;
import games.alejandrocoria.mapfrontiers.api.server.IMapFrontiersServerAPI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map Frontiers server plugin — creates labelled frontier regions from the zones
 * defined in {@code install.json} when {@code /cobblemon-initiative install run} is used.
 *
 * <p><b>This class is only loaded when Map Frontiers is installed.</b> It is always
 * accessed through the import-free {@link MapFrontiersBridge} so the rest of the mod
 * never touches Map Frontiers classes directly.
 */
public class MapFrontiersIntegration implements IMapFrontiersServerPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  // Nil UUID used as "server" owner — no real player UUID needed for global frontiers.
  private static final UserRef SERVER_OWNER = new UserRef(
    new UUID(0L, 0L), "server"
  );

  private IMapFrontiersServerAPI pluginApi;

  /**
   * Called once during mod init from inside a {@code FabricLoader.isModLoaded("mapfrontiers")}
   * guard. Registers this instance as a Map Frontiers plugin and wires the bridge so
   * {@link MapFrontiersBridge#createFrontiers} routes here.
   */
  public static void registerAndSetBridge() {
    MapFrontiersIntegration integration = new MapFrontiersIntegration();
    MapFrontiersAPI.registerServerPlugin(integration);
    MapFrontiersBridge.setHandler(integration::createFrontiers);
    LOGGER.info("[CobblemonInitiative] Map Frontiers plugin registered.");
  }

  // ── IMapFrontiersServerPlugin ─────────────────────────────────────────────

  @Override
  public String getModId() {
    return "cobblemon-initiative";
  }

  @Override
  public void initialize(IMapFrontiersServerAPI api) {
    this.pluginApi = api;
    LOGGER.info("[CobblemonInitiative] Map Frontiers API initialized.");
  }

  @Override
  public void shutdown(IMapFrontiersServerAPI api) {
    this.pluginApi = null;
  }

  // ── Frontier creation ─────────────────────────────────────────────────────

  void createFrontiers(List<InstallZone> zones) {
    if (pluginApi == null) {
      LOGGER.warn("[CobblemonInitiative] Map Frontiers API not yet initialized — cannot create frontiers.");
      return;
    }

    var service = pluginApi.frontiers();
    int created = 0;

    for (InstallZone zone : zones) {
      try {
        List<Point2i> vertices = zone.hasVertices()
          ? zone.vertices.stream().map(v -> new Point2i(v.x, v.z)).collect(Collectors.toList())
          : octagonVertices(zone.derivedCenterX(), zone.derivedCenterZ(), zone.derivedRadius());

        var builder = FrontierCreateRequest
          .builder(new DimensionId(zone.dimension), FrontierShape.vertex(vertices))
          .name1(zone.name)
          .color(parseHexColor(zone.color))
          .visibility(Set.of(
            FrontierVisibilityFlag.Frontier,
            FrontierVisibilityFlag.FullscreenName,
            FrontierVisibilityFlag.Minimap,
            FrontierVisibilityFlag.MinimapName
          ));

        if (zone.subtitle != null && !zone.subtitle.isEmpty()) {
          builder = builder.name2(zone.subtitle);
        }

        service.createGlobalFrontier(SERVER_OWNER, builder.build());
        created++;

      } catch (Exception e) {
        LOGGER.warn("[CobblemonInitiative] Failed to create frontier '{}': {}", zone.name, e.getMessage());
      }
    }

    LOGGER.info("[CobblemonInitiative] Created {}/{} Map Frontiers frontier(s).", created, zones.size());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /** 8-point approximation of a circular zone boundary. */
  private static List<Point2i> octagonVertices(int cx, int cz, int radius) {
    List<Point2i> verts = new ArrayList<>(8);
    for (int i = 0; i < 8; i++) {
      double angle = Math.toRadians(i * 45.0);
      verts.add(new Point2i(
        cx + (int)(radius * Math.cos(angle)),
        cz + (int)(radius * Math.sin(angle))
      ));
    }
    return verts;
  }

  private static int parseHexColor(String hex) {
    if (hex == null || !hex.startsWith("#")) return 0x808080;
    try {
      return Integer.parseInt(hex.substring(1), 16);
    } catch (NumberFormatException e) {
      return 0x808080;
    }
  }
}

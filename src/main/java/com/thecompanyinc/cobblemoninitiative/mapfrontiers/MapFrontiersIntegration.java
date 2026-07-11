package com.thecompanyinc.cobblemoninitiative.mapfrontiers;

import com.thecompanyinc.cobblemoninitiative.install.InstallZone;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates labelled Map Frontiers regions from the zones in {@code install.json}, driven by
 * {@code /cobblemon-initiative install run}.
 *
 * <p><b>Why reflection instead of the Map Frontiers plugin API?</b> The public plugin API
 * (the {@code games.alejandrocoria.mapfrontiers.api} package) only ships in Map Frontiers
 * builds for Minecraft 1.21.7+. Cobblemon is hard-locked to 1.21.1, whose newest Map
 * Frontiers release ({@code 2.7.0-beta.18}) contains no {@code api} package at all — so the
 * API can never be present at runtime on this modpack. Its internal {@code FrontiersManager}
 * <i>is</i> reachable, though: it is a public class with a public static {@code instance} and
 * public create/update methods, and only the API <i>bootstrap</i> is guarded by a trusted-caller
 * check. We share the JVM and class loader with Map Frontiers, so we call the manager directly.
 *
 * <p>This class references <b>no</b> Map Frontiers types at compile time — every Map Frontiers
 * symbol is resolved reflectively — so it adds no compile dependency and can never throw
 * {@code NoClassDefFoundError}. It is reached only through the import-free {@link MapFrontiersBridge}.
 *
 * <p><b>Caveat:</b> this couples us to Map Frontiers internals, which is unsupported. The 1.21.1
 * line is frozen at {@code 2.7.0-beta.18} (the author moved new work, including the API, to newer
 * Minecraft versions), so the surface is effectively stable for this modpack. {@link #warnIfUntestedVersion}
 * logs if a different build is ever installed, so a surprise update that breaks reflection is diagnosable.
 */
public final class MapFrontiersIntegration {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private static final String MF_MOD_ID = "mapfrontiers";
  /** The Map Frontiers build this reflection bridge was written and tested against. */
  private static final String TESTED_VERSION = "2.7.0-beta.18";

  private static final String FRONTIERS_MANAGER =
    "games.alejandrocoria.mapfrontiers.common.FrontiersManager";
  private static final String FRONTIER_DATA =
    "games.alejandrocoria.mapfrontiers.common.FrontierData";
  private static final String VISIBILITY_ENUM =
    "games.alejandrocoria.mapfrontiers.common.FrontierData$VisibilityData$Visibility";

  /** Visibility flags forced on so the zone label shows on both the fullscreen map and minimap. */
  private static final String[] VISIBILITY_ON = { "Frontier", "FullscreenName", "MinimapName" };

  private static boolean versionWarned = false;

  private MapFrontiersIntegration() {}

  /**
   * Creates one global frontier per zone via reflection into the Map Frontiers manager.
   * Map Frontiers persists each frontier to {@code <world>/mapfrontiers/frontiers.dat} itself.
   *
   * @param zones zones to draw (from install.json)
   * @param owner player to register as creator/owner (global frontiers are server-wide regardless)
   * @return the number of frontiers successfully created
   */
  static int createGlobalFrontiers(List<InstallZone> zones, ServerPlayer owner) {
    if (owner == null) {
      LOGGER.warn("[CobblemonInitiative] Map Frontiers: no player available as frontier owner — skipping.");
      return 0;
    }
    warnIfUntestedVersion();

    final Object manager;
    final Class<?> visEnum;
    final Method createMethod;
    final Method updateMethod;
    final Method setName1;
    final Method setName2;
    final Method setColor;
    final Method setVisibility;
    try {
      Class<?> fmClass = Class.forName(FRONTIERS_MANAGER);
      Class<?> fdClass = Class.forName(FRONTIER_DATA);
      visEnum = Class.forName(VISIBILITY_ENUM);

      Field instanceField = fmClass.getField("instance");
      manager = instanceField.get(null);
      if (manager == null) {
        LOGGER.warn("[CobblemonInitiative] Map Frontiers manager not initialized yet — skipping frontier creation.");
        return 0;
      }

      // MC param types remap to the same intermediary classes Map Frontiers compiled against,
      // since this mod is remapped too — so reflecting with Mojmap types resolves correctly.
      createMethod = fmClass.getMethod("createNewGlobalFrontier",
        ResourceKey.class, ServerPlayer.class, List.class, List.class);
      updateMethod = fmClass.getMethod("updateGlobalFrontier", fdClass);
      setName1 = fdClass.getMethod("setName1", String.class);
      setName2 = fdClass.getMethod("setName2", String.class);
      setColor = fdClass.getMethod("setColor", int.class);
      setVisibility = fdClass.getMethod("setVisibility", visEnum, boolean.class);
    } catch (ReflectiveOperationException e) {
      LOGGER.warn("[CobblemonInitiative] Map Frontiers internals not found (incompatible build?); "
        + "frontier creation disabled.", e);
      return 0;
    }

    int created = 0;
    for (InstallZone zone : zones) {
      try {
        ResourceKey<Level> dim = ResourceKey.create(
          Registries.DIMENSION, ResourceLocation.parse(zone.dimension));

        // Chunks MUST be null, not an empty list. createNewFrontier sets the frontier to Vertex
        // mode when vertices is non-null, then UNCONDITIONALLY overwrites mode to Chunk when the
        // chunks arg is non-null — even when it is empty. A non-null empty list therefore yields a
        // Chunk-mode frontier with zero chunks, which stores the vertices but renders nothing (the
        // "chunks:0, invisible on the map" symptom). Passing null keeps it in Vertex mode.
        Object frontier = createMethod.invoke(manager, dim, owner, verticesFor(zone), null);
        if (frontier == null) {
          LOGGER.warn("[CobblemonInitiative] Map Frontiers returned no frontier for zone '{}'.", zone.name);
          continue;
        }

        setName1.invoke(frontier, zone.name);
        // Second label line is intentionally left blank: the map shows the zone NAME only
        // (routes read as e.g. "Blossom Path", not "Route 1"). Subtitles are reserved for
        // in-game area announcements, which are off by default (Map Frontiers is the map).
        setName2.invoke(frontier, "");
        setColor.invoke(frontier, parseHexColor(zone.color));
        for (String flag : VISIBILITY_ON) {
          try {
            setVisibility.invoke(frontier, enumConst(visEnum, flag), true);
          } catch (IllegalArgumentException | ReflectiveOperationException ignored) {
            // Unknown flag name on this build — non-fatal; defaults still apply.
          }
        }
        updateMethod.invoke(manager, frontier); // persists name/color/visibility to frontiers.dat
        created++;

      } catch (InvocationTargetException e) {
        LOGGER.warn("[CobblemonInitiative] Failed to create frontier '{}': {}",
          zone.name, e.getCause() != null ? e.getCause() : e);
      } catch (ReflectiveOperationException e) {
        LOGGER.warn("[CobblemonInitiative] Failed to create frontier '{}': {}", zone.name, e);
      }
    }

    LOGGER.info("[CobblemonInitiative] Created {}/{} Map Frontiers frontier(s).", created, zones.size());
    return created;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static Object enumConst(Class<?> enumClass, String name) {
    return Enum.valueOf((Class) enumClass, name);
  }

  /** Builds the frontier outline: the traced polygon if present, else an 8-point circle. */
  private static List<BlockPos> verticesFor(InstallZone zone) {
    List<BlockPos> verts = new ArrayList<>();
    int y = zone.centerY;
    if (zone.hasVertices()) {
      for (InstallZone.Vertex v : zone.vertices) {
        verts.add(new BlockPos(v.x, y, v.z));
      }
    } else {
      int cx = zone.derivedCenterX();
      int cz = zone.derivedCenterZ();
      int r = zone.derivedRadius();
      for (int i = 0; i < 8; i++) {
        double a = Math.toRadians(i * 45.0);
        verts.add(new BlockPos(cx + (int) (r * Math.cos(a)), y, cz + (int) (r * Math.sin(a))));
      }
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

  /**
   * True when ANY global frontier already exists (any dimension) — i.e. the world shipped
   * with pre-baked frontiers (build_mrpack bakes install.json's zones into
   * {@code mapfrontiers/frontiers.dat}) or an earlier install run already created them.
   * Used by install run to skip creation so re-runs / baked worlds never duplicate zones.
   *
   * <p>Must be an in-memory count, never a file-existence test: Map Frontiers writes an
   * EMPTY frontiers.dat on every fresh world's first boot. And never per-name dedup —
   * install.json legitimately repeats zone names (route segments). Fail-open (false) is
   * correct: if this reflection breaks, createGlobalFrontiers breaks identically, so
   * nothing double-creates.
   */
  static boolean hasAnyGlobalFrontiers() {
    try {
      Class<?> fmClass = Class.forName(FRONTIERS_MANAGER);
      Object manager = fmClass.getField("instance").get(null);
      if (manager == null) return false;
      Object byDim = fmClass.getMethod("getAllGlobalFrontiers").invoke(manager);
      if (!(byDim instanceof java.util.Map<?, ?> map)) return false;
      for (Object frontierList : map.values()) {
        if (frontierList instanceof List<?> list && !list.isEmpty()) return true;
      }
    } catch (ReflectiveOperationException | RuntimeException e) {
      LOGGER.debug("[CobblemonInitiative] Map Frontiers existing-frontier check failed", e);
    }
    return false;
  }

  private static void warnIfUntestedVersion() {
    if (versionWarned) return;
    versionWarned = true;
    FabricLoader.getInstance().getModContainer(MF_MOD_ID).ifPresent(c -> {
      String v = c.getMetadata().getVersion().getFriendlyString();
      if (!v.contains(TESTED_VERSION)) {
        LOGGER.warn(
          "[CobblemonInitiative] Map Frontiers {} detected; the reflection bridge was written against {}. "
            + "Frontier creation may misbehave if internals changed.",
          v, TESTED_VERSION
        );
      }
    });
  }
}

package com.thecompanyinc.cobblemoninitiative.install;

import java.util.List;

/**
 * A named zone entry in {@code install.json}.
 *
 * <p>On {@code /cobblemon-initiative install run}, each zone is registered as a
 * {@link com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig.SafeZone} (mob-spawn
 * suppression + area announcements) and written into the Map Frontiers frontier file for
 * visual labelling on the JourneyMap minimap.
 *
 * <p>If {@link #vertices} is provided (traced with the zone-trace dev tool), center and
 * radius are derived automatically. Otherwise {@link #centerX}, {@link #centerZ}, and
 * {@link #radius} are used directly.
 */
public class InstallZone {

  /** Display name shown in-game on entry and on the JourneyMap frontier label. */
  public String name = "";

  /**
   * Optional subtitle shown beneath the zone name when using TITLE announcement style.
   * Good for short flavour lines, e.g. "Gym 1 — Bug Type" or "The Company, Inc. HQ".
   */
  public String subtitle = "";

  /**
   * Zone category — used to pick a sensible default color when none is specified.
   * Recognized values: TOWN, ROUTE, SHRINE, VILLAIN, BATTLE_FRONTIER, LANDMARK.
   */
  public String type = "LANDMARK";

  public int centerX = 0;
  public int centerY = 64;
  public int centerZ = 0;

  /** Radius in blocks. Cylindrical (no Y check) by default. */
  public int radius = 100;

  public String dimension = "minecraft:overworld";

  /** If true, only hostile mob spawning is suppressed; passive mobs still spawn. */
  public boolean hostileOnly = true;

  /**
   * If true, this zone does NOT suppress mob spawning — mobs spawn normally (the zone
   * is still drawn on the map and can announce). {@link #hostileOnly} is ignored when
   * this is true. Use for routes / farms / wilderness markers that keep full spawns.
   * Default false (the zone suppresses spawns per {@link #hostileOnly}).
   */
  public boolean mobsSpawn = false;

  /**
   * If true, zone is a vertical cylinder (ignore Y). Recommended for open-world zones.
   * Set false for underground / enclosed spaces (full sphere check).
   */
  public boolean cylindrical = true;

  /** If true, an area announcement fires when the player enters this zone. */
  public boolean announce = true;

  /** Hex color string for the JourneyMap frontier label, e.g. {@code "#7AAAD0"}. */
  public String color = "#AAAAAA";

  /**
   * Optional polygon vertices traced with the zone-trace dev tool.
   * When present, {@link #centerX}/{@link #centerZ}/{@link #radius} are derived from the
   * polygon centroid and are not required in install.json.
   * Map Frontiers frontiers use the exact polygon; safe zones use the derived circle.
   */
  public List<Vertex> vertices = null;

  public static class Vertex {
    public int x, z;
    public Vertex() {}
    public Vertex(int x, int z) { this.x = x; this.z = z; }
  }

  /**
   * Returns true when this zone has explicit polygon vertex data.
   */
  public boolean hasVertices() {
    return vertices != null && !vertices.isEmpty();
  }

  /**
   * Derives the safe-zone center X from the polygon centroid when vertices are present.
   */
  public int derivedCenterX() {
    if (!hasVertices()) return centerX;
    double sum = 0;
    for (Vertex v : vertices) sum += v.x;
    return (int) Math.round(sum / vertices.size());
  }

  /**
   * Derives the safe-zone center Z from the polygon centroid when vertices are present.
   */
  public int derivedCenterZ() {
    if (!hasVertices()) return centerZ;
    double sum = 0;
    for (Vertex v : vertices) sum += v.z;
    return (int) Math.round(sum / vertices.size());
  }

  /**
   * Derives the safe-zone radius as the max vertex distance from the centroid.
   */
  public int derivedRadius() {
    if (!hasVertices()) return radius;
    int cx = derivedCenterX();
    int cz = derivedCenterZ();
    double max = 0;
    for (Vertex v : vertices) {
      double d = Math.sqrt(Math.pow(v.x - cx, 2) + Math.pow(v.z - cz, 2));
      if (d > max) max = d;
    }
    return (int) Math.ceil(max);
  }
}

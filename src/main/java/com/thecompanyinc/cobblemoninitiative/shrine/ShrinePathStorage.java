package com.thecompanyinc.cobblemoninitiative.shrine;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-world persistence for shrine "safe path" block positions.
 *
 * The ice trial (and any other shrine that opts into a floor hazard) treats
 * every hazard block the player steps on as deadly UNLESS its position was
 * recorded here. Positions are recorded during development by walking the
 * intended route with {@code /cobblemon-initiative shrine <id> path record}.
 *
 * Stored in the world directory ({@code data/shrine_paths.json}) so the route
 * travels with the map save — same load-on-start / save-on-stop pattern as
 * {@link com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightStorage}.
 *
 * Positions are kept in memory as packed {@link BlockPos#asLong()} values for
 * O(1) per-tick membership checks, and serialised as {@code [x, y, z]} arrays
 * for human-readable diffs / hand-editing.
 */
public class ShrinePathStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** shrineId -> set of packed BlockPos that are "safe" to stand on. */
  private final Map<String, Set<Long>> safePositions = new LinkedHashMap<>();
  private File dataFile;

  // ── Lifecycle ─────────────────────────────────────────────────────────────────

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/shrine_paths.json")
      .toFile();

    safePositions.clear();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null) return;

      for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
        String shrineId = entry.getKey();
        if (!entry.getValue().isJsonArray()) continue;

        Set<Long> set = new HashSet<>();
        for (JsonElement el : entry.getValue().getAsJsonArray()) {
          JsonArray xyz = el.getAsJsonArray();
          if (xyz.size() != 3) continue;
          set.add(
            BlockPos.asLong(
              xyz.get(0).getAsInt(),
              xyz.get(1).getAsInt(),
              xyz.get(2).getAsInt()
            )
          );
        }
        safePositions.put(shrineId, set);
        LOGGER.info(
          "[Shrine Path] Loaded {} safe positions for {}",
          set.size(),
          shrineId
        );
      }
    } catch (Exception e) {
      LOGGER.error("[Shrine Path] Error loading data: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      for (Map.Entry<String, Set<Long>> entry : safePositions.entrySet()) {
        root.add(entry.getKey(), toJsonArray(entry.getValue()));
      }
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[Shrine Path] Error saving data: {}", e.getMessage());
    }
  }

  // ── Queries / edits ─────────────────────────────────────────────────────────

  public boolean isSafe(String shrineId, BlockPos pos) {
    Set<Long> set = safePositions.get(shrineId);
    return set != null && set.contains(pos.asLong());
  }

  /** Add a position WITHOUT persisting — caller is responsible for {@link #save()}. */
  public boolean addDeferred(String shrineId, BlockPos pos) {
    return safePositions
      .computeIfAbsent(shrineId, k -> new HashSet<>())
      .add(pos.asLong());
  }

  /** @return true if the position was newly added (false if already present). */
  public boolean add(String shrineId, BlockPos pos) {
    boolean added = addDeferred(shrineId, pos);
    if (added) save();
    return added;
  }

  /** @return number of positions removed (0 or all of them). */
  public int clear(String shrineId) {
    Set<Long> set = safePositions.remove(shrineId);
    int removed = set == null ? 0 : set.size();
    if (removed > 0) save();
    return removed;
  }

  public int count(String shrineId) {
    Set<Long> set = safePositions.get(shrineId);
    return set == null ? 0 : set.size();
  }

  /** Immutable snapshot of the packed positions for a shrine. */
  public Set<Long> get(String shrineId) {
    Set<Long> set = safePositions.get(shrineId);
    return set == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(set));
  }

  /** Renders any set of packed positions as a compact {@code [[x,y,z],...]} JSON string. */
  public static String toJsonString(Set<Long> packed) {
    return GSON.toJson(toJsonArray(packed));
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private static JsonArray toJsonArray(Set<Long> packed) {
    // Sort for stable, diff-friendly output.
    List<Long> sorted = new ArrayList<>(packed);
    Collections.sort(sorted);
    JsonArray array = new JsonArray();
    for (long p : sorted) {
      BlockPos pos = BlockPos.of(p);
      JsonArray xyz = new JsonArray();
      xyz.add(pos.getX());
      xyz.add(pos.getY());
      xyz.add(pos.getZ());
      array.add(xyz);
    }
    return array;
  }
}

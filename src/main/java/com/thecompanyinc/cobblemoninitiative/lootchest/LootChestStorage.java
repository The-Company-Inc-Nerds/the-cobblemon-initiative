package com.thecompanyinc.cobblemoninitiative.lootchest;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-world persistence for the unplaced-chest loot mechanic.
 *
 * Tracks two sets of chest positions (packed {@link BlockPos#asLong()}):
 *   - {@code playerPlaced}: chests a player placed by hand — these open normally.
 *   - {@code claimed}:      unplaced chests already looted once — they no longer
 *                           dispense loot (fall through to the normal chest).
 *
 * Stored in the world directory ({@code data/loot_chests.json}); same
 * load-on-start / save-on-stop pattern as the other subsystem stores.
 */
public class LootChestStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final Set<Long> playerPlaced = new HashSet<>();
  private final Set<Long> claimed = new HashSet<>();
  private File dataFile;

  // ── Lifecycle ─────────────────────────────────────────────────────────────────

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/loot_chests.json")
      .toFile();

    playerPlaced.clear();
    claimed.clear();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null) return;
      readInto(root, "playerPlaced", playerPlaced);
      readInto(root, "claimed", claimed);
      LOGGER.info(
        "[Loot Chest] Loaded {} player-placed, {} claimed chest positions",
        playerPlaced.size(),
        claimed.size()
      );
    } catch (Exception e) {
      LOGGER.error("[Loot Chest] Error loading data: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      root.add("playerPlaced", toJsonArray(playerPlaced));
      root.add("claimed", toJsonArray(claimed));
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[Loot Chest] Error saving data: {}", e.getMessage());
    }
  }

  // ── Queries / edits ─────────────────────────────────────────────────────────

  public boolean isPlayerPlaced(BlockPos pos) {
    return playerPlaced.contains(pos.asLong());
  }

  public void markPlayerPlaced(BlockPos pos) {
    if (playerPlaced.add(pos.asLong())) save();
  }

  public boolean isClaimed(BlockPos pos) {
    return claimed.contains(pos.asLong());
  }

  public void markClaimed(BlockPos pos) {
    if (claimed.add(pos.asLong())) save();
  }

  /** Forget a position entirely (called when the block is broken). */
  public void forget(BlockPos pos) {
    boolean changed = playerPlaced.remove(pos.asLong());
    changed |= claimed.remove(pos.asLong());
    if (changed) save();
  }

  /** Wipe all tracking (admin reset). @return total positions removed. */
  public int clearAll() {
    int total = playerPlaced.size() + claimed.size();
    playerPlaced.clear();
    claimed.clear();
    if (total > 0) save();
    return total;
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private static void readInto(JsonObject root, String key, Set<Long> into) {
    if (!root.has(key) || !root.get(key).isJsonArray()) return;
    for (JsonElement el : root.getAsJsonArray(key)) {
      JsonArray xyz = el.getAsJsonArray();
      if (xyz.size() != 3) continue;
      into.add(
        BlockPos.asLong(xyz.get(0).getAsInt(), xyz.get(1).getAsInt(), xyz.get(2).getAsInt())
      );
    }
  }

  private static JsonArray toJsonArray(Set<Long> packed) {
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

package com.thecompanyinc.cobblemoninitiative.npcsight;

import com.google.gson.*;
import com.thecompanyinc.cobblemoninitiative.util.UuidGsonAdapter;
import java.io.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcSightStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .registerTypeHierarchyAdapter(UUID.class, UuidGsonAdapter.INSTANCE)
    .create();

  private final Map<UUID, NpcSightData> npcDataMap = new LinkedHashMap<>();
  private File dataFile;

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/npcsight.json")
      .toFile();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null || !root.has("npcs")) return;

      for (JsonElement el : root.getAsJsonArray("npcs")) {
        NpcSightData data = GSON.fromJson(el, NpcSightData.class);
        if (data != null && data.uuid != null) {
          npcDataMap.put(data.uuid, data);
        }
      }
    } catch (IOException e) {
      LOGGER.error("[NPC Sight] Error loading data: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (NpcSightData data : npcDataMap.values()) {
        array.add(GSON.toJsonTree(data));
      }
      root.add("npcs", array);
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[NPC Sight] Error saving data: {}", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // CRUD
  // ---------------------------------------------------------------------------

  public void put(NpcSightData data) {
    npcDataMap.put(data.uuid, data);
    save();
  }

  public boolean remove(UUID uuid) {
    boolean removed = npcDataMap.remove(uuid) != null;
    if (removed) save();
    return removed;
  }

  public NpcSightData get(UUID uuid) {
    return npcDataMap.get(uuid);
  }

  public Collection<NpcSightData> getAll() {
    return Collections.unmodifiableCollection(npcDataMap.values());
  }

  public boolean contains(UUID uuid) {
    return npcDataMap.containsKey(uuid);
  }

  public int size() {
    return npcDataMap.size();
  }

}

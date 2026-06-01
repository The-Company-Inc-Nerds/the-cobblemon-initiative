package com.thecompanyinc.cobblemoninitiative.npcmap;

import com.google.gson.*;
import com.thecompanyinc.cobblemoninitiative.util.UuidGsonAdapter;
import java.io.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcMapStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .registerTypeHierarchyAdapter(UUID.class, UuidGsonAdapter.INSTANCE)
    .create();

  private final Map<UUID, NpcMapEntry> entryMap = new LinkedHashMap<>();
  private File dataFile;

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/npc_preset_map.json")
      .toFile();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null || !root.has("mappings")) return;

      for (JsonElement el : root.getAsJsonArray("mappings")) {
        NpcMapEntry entry = GSON.fromJson(el, NpcMapEntry.class);
        if (entry != null && entry.uuid != null) {
          entryMap.put(entry.uuid, entry);
        }
      }
    } catch (IOException e) {
      LOGGER.error("[NPC Map] Error loading data: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (NpcMapEntry entry : entryMap.values()) {
        array.add(GSON.toJsonTree(entry));
      }
      root.add("mappings", array);
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[NPC Map] Error saving data: {}", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // CRUD
  // ---------------------------------------------------------------------------

  public void put(NpcMapEntry entry) {
    entryMap.put(entry.uuid, entry);
    save();
  }

  public boolean remove(UUID uuid) {
    boolean removed = entryMap.remove(uuid) != null;
    if (removed) save();
    return removed;
  }

  public NpcMapEntry get(UUID uuid) {
    return entryMap.get(uuid);
  }

  public Collection<NpcMapEntry> getAll() {
    return Collections.unmodifiableCollection(entryMap.values());
  }

  public boolean contains(UUID uuid) {
    return entryMap.containsKey(uuid);
  }

  public int size() {
    return entryMap.size();
  }

}

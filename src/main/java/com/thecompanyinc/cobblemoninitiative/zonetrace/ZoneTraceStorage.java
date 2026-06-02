package com.thecompanyinc.cobblemoninitiative.zonetrace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Persists completed zone-trace entries to {world}/data/zone_trace.json. */
public class ZoneTraceStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final List<ZoneEntry> zones = new ArrayList<>();
  private File dataFile;

  // ---------------------------------------------------------------------------
  // Model
  // ---------------------------------------------------------------------------

  /** One completed traced zone, matching the InstallZone JSON schema. */
  public static class ZoneEntry {
    public String name       = "";
    public String subtitle   = "";
    public String type       = "TOWN";
    public String dimension  = "minecraft:overworld";
    public boolean announce  = true;
    public String color      = "#AAAAAA";
    public boolean hostileOnly  = true;
    public boolean cylindrical  = true;
    public int centerY          = 64;
    public List<Vertex> vertices = new ArrayList<>();

    public static class Vertex {
      public int x, z;
      public Vertex() {}
      public Vertex(int x, int z) { this.x = x; this.z = z; }
    }
  }

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/zone_trace.json")
      .toFile();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null || !root.has("zones")) return;

      for (JsonElement el : root.getAsJsonArray("zones")) {
        ZoneEntry entry = GSON.fromJson(el, ZoneEntry.class);
        if (entry != null && entry.name != null && !entry.name.isBlank()) {
          zones.add(entry);
        }
      }
    } catch (IOException e) {
      LOGGER.error("[ZoneTrace] Error loading zone_trace.json: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (ZoneEntry entry : zones) {
        array.add(GSON.toJsonTree(entry));
      }
      root.add("zones", array);
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[ZoneTrace] Error saving zone_trace.json: {}", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // CRUD
  // ---------------------------------------------------------------------------

  public void put(ZoneEntry entry) {
    zones.removeIf(e -> e.name.equalsIgnoreCase(entry.name));
    zones.add(entry);
    save();
  }

  public boolean remove(String name) {
    boolean removed = zones.removeIf(e -> e.name.equalsIgnoreCase(name));
    if (removed) save();
    return removed;
  }

  public ZoneEntry get(String name) {
    return zones.stream()
      .filter(e -> e.name.equalsIgnoreCase(name))
      .findFirst().orElse(null);
  }

  public List<ZoneEntry> getAll() {
    return Collections.unmodifiableList(zones);
  }

  public int size() {
    return zones.size();
  }
}

package com.thecompanyinc.cobblemoninitiative.devtools;

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

/**
 * Persists gym-gimmick coordinate marks to {@code {world}/data/gym_marks.json}.
 *
 * <p>Dev-only — used by the {@code gym-mark} authoring command to capture the
 * placeholder positions the gimmick build needs (vine-wall boxes, rift fight origin,
 * crystal pillars, sentinel spots, heat box, ...). The export is handed back to the
 * source tree where the placeholders live. Remove before final release (see the
 * dev-only cleanup checklist — ships with the field-mark tool).
 */
public class GymMarkStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final List<MarkEntry> marks = new ArrayList<>();
  private File dataFile;

  /** One mark: a POINT (x/y/z) or a BOX (start corner x/y/z + stop corner x2/y2/z2). */
  public static class MarkEntry {
    public String key = "";
    /** "point" or "box". */
    public String kind = "point";
    public String dimension = "minecraft:overworld";
    public int x, y, z;
    /** Box stop corner; only meaningful when kind=box AND complete=true. */
    public int x2, y2, z2;
    /** Boxes: false while only the start corner is recorded. Points are always true. */
    public boolean complete = true;

    public MarkEntry() {}
  }

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/gym_marks.json")
      .toFile();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null || !root.has("marks")) return;
      for (JsonElement el : root.getAsJsonArray("marks")) {
        MarkEntry entry = GSON.fromJson(el, MarkEntry.class);
        if (entry != null && entry.key != null && !entry.key.isBlank()) {
          marks.add(entry);
        }
      }
    } catch (IOException e) {
      LOGGER.error("[GymMark] Error loading gym_marks.json: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (MarkEntry entry : marks) {
        array.add(GSON.toJsonTree(entry));
      }
      root.add("marks", array);
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[GymMark] Error saving gym_marks.json: {}", e.getMessage());
    }
  }

  public void put(MarkEntry entry) {
    marks.removeIf(e -> e.key.equalsIgnoreCase(entry.key));
    marks.add(entry);
    save();
  }

  public boolean remove(String key) {
    boolean removed = marks.removeIf(e -> e.key.equalsIgnoreCase(key));
    if (removed) save();
    return removed;
  }

  public MarkEntry get(String key) {
    return marks.stream()
      .filter(e -> e.key.equalsIgnoreCase(key))
      .findFirst().orElse(null);
  }

  public List<MarkEntry> getAll() {
    return Collections.unmodifiableList(marks);
  }

  public int size() {
    return marks.size();
  }

  public File getDataFile() {
    return dataFile;
  }
}

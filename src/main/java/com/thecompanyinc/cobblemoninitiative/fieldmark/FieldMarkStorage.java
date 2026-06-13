package com.thecompanyinc.cobblemoninitiative.fieldmark;

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
 * Persists marked wheat-field locations to {world}/data/field_marks.json.
 *
 * <p>Dev-only — used by the {@code field-mark} authoring command to capture
 * Wheat War liberation field positions in-world. Remove before final release
 * (see the dev-only cleanup checklist).
 */
public class FieldMarkStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private final List<FieldEntry> fields = new ArrayList<>();
  private File dataFile;

  /** One marked wheat field. Mirrors the planned liberation/fields.json schema. */
  public static class FieldEntry {
    public String id = "";
    public String region = "";
    public String dimension = "minecraft:overworld";
    public int centerX;
    public int centerY;
    public int centerZ;
    public int radius = 12;
    /** true = set-piece field (counts toward liberation arc); false = scattered minor (bounty only). */
    public boolean setpiece = true;

    public FieldEntry() {}
  }

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  public void load(MinecraftServer server) {
    dataFile = server
      .getWorldPath(LevelResource.ROOT)
      .resolve("data/field_marks.json")
      .toFile();

    if (!dataFile.exists()) return;

    try (FileReader reader = new FileReader(dataFile)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null || !root.has("fields")) return;

      for (JsonElement el : root.getAsJsonArray("fields")) {
        FieldEntry entry = GSON.fromJson(el, FieldEntry.class);
        if (entry != null && entry.id != null && !entry.id.isBlank()) {
          fields.add(entry);
        }
      }
    } catch (IOException e) {
      LOGGER.error("[FieldMark] Error loading field_marks.json: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (FieldEntry entry : fields) {
        array.add(GSON.toJsonTree(entry));
      }
      root.add("fields", array);
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(root, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[FieldMark] Error saving field_marks.json: {}", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // CRUD
  // ---------------------------------------------------------------------------

  public void put(FieldEntry entry) {
    fields.removeIf(e -> e.id.equalsIgnoreCase(entry.id));
    fields.add(entry);
    save();
  }

  public boolean remove(String id) {
    boolean removed = fields.removeIf(e -> e.id.equalsIgnoreCase(id));
    if (removed) save();
    return removed;
  }

  public FieldEntry get(String id) {
    return fields.stream()
      .filter(e -> e.id.equalsIgnoreCase(id))
      .findFirst().orElse(null);
  }

  public List<FieldEntry> getAll() {
    return Collections.unmodifiableList(fields);
  }

  public int size() {
    return fields.size();
  }
}

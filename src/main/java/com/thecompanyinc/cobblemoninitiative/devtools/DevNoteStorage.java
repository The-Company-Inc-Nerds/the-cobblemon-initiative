package com.thecompanyinc.cobblemoninitiative.devtools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only: persists the in-world NPC review notes (whacked-NPC comments + relocations)
 * to {@code <world>/data/npc_notes.json} so a session survives a relog before the
 * {@code /cobblemon-initiative npcnote log} dump. Remove the entrypoint before release.
 */
public class DevNoteStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-devnote");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** One reviewed NPC. Positions are block coords; new* is null until a move is set. */
  public static class NpcNote {
    public String name;
    public String uuid;
    public List<String> tags = new ArrayList<>();
    public double ox, oy, oz;      // observed (current) position
    public Double nx, ny, nz;      // requested new position (null = keep)
    public String comment = "";
  }

  /** A standalone position marker (from /cobblemon-initiative pos): a coordinate the
   *  player captured, with an optional title and note. Not tied to an entity. */
  public static class PosMark {
    public String title;   // null = untitled
    public String note;    // null = no note
    public double x, y, z;
  }

  /** A smoke-test result the player recorded in-game (status + optional note). */
  public static class SmokeResult {
    public String status;   // PASS / COMMENT / FAIL
    public String note;     // optional
  }

  private final List<NpcNote> notes = new ArrayList<>();
  private final List<PosMark> positions = new ArrayList<>();
  private final java.util.Map<String, SmokeResult> smoke = new java.util.LinkedHashMap<>();
  private File dataFile;

  public java.util.Map<String, SmokeResult> getSmoke() {
    return smoke;
  }

  public List<NpcNote> getNotes() {
    return notes;
  }

  public List<PosMark> getPositions() {
    return positions;
  }

  /** Find the note for an entity uuid, or null. */
  public NpcNote find(String uuid) {
    for (NpcNote n : notes) {
      if (n.uuid.equals(uuid)) return n;
    }
    return null;
  }

  /** On-disk shape holding all three collections. */
  private static class Saved {
    List<NpcNote> notes = new ArrayList<>();
    List<PosMark> positions = new ArrayList<>();
    java.util.Map<String, SmokeResult> smoke = new java.util.LinkedHashMap<>();
  }

  public void load(MinecraftServer server) {
    dataFile = server.getWorldPath(LevelResource.ROOT).resolve("data/npc_notes.json").toFile();
    notes.clear();
    positions.clear();
    smoke.clear();
    if (!dataFile.exists()) return;
    try (FileReader reader = new FileReader(dataFile)) {
      Saved s = GSON.fromJson(reader, Saved.class);
      if (s != null) {
        if (s.notes != null) notes.addAll(s.notes);
        if (s.positions != null) positions.addAll(s.positions);
        if (s.smoke != null) smoke.putAll(s.smoke);
      }
    } catch (Exception e) {
      LOGGER.warn("[DevNote] Could not load npc_notes.json: {}", e.getMessage());
    }
  }

  public void save() {
    if (dataFile == null) return;
    try {
      dataFile.getParentFile().mkdirs();
      Saved s = new Saved();
      s.notes = notes;
      s.positions = positions;
      s.smoke = smoke;
      try (FileWriter writer = new FileWriter(dataFile)) {
        GSON.toJson(s, writer);
      }
    } catch (Exception e) {
      LOGGER.warn("[DevNote] Could not save npc_notes.json: {}", e.getMessage());
    }
  }
}

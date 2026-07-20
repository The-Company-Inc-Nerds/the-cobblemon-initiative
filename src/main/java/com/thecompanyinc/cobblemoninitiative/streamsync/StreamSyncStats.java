package com.thecompanyinc.cobblemoninitiative.streamsync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Per-world stream counters + the save's {@code worldId}, persisted as
 * {@code cobblemon_initiative_streamstats.json} in the world root (load on
 * SERVER_STARTED / save on SERVER_STOPPING, like the sibling storages).
 *
 * <p>The worldId is a UUID minted once on the FIRST load of a save and never
 * changed — it is how the overlay service tells one hardcore attempt from the
 * next (hardcore reset = new world = new worldId; the service banks the old
 * save's counters into campaign totals and starts a new attempt). The mod is
 * the source of truth only for the per-save counters below; campaign totals
 * across attempts live service-side.
 *
 * <p>All access is synchronized because the sacrifice path posts from the
 * CLIENT thread (see {@code NuzlockeInit.sacrificePokemon}) while snapshots
 * read on the server thread.
 */
public class StreamSyncStats {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String STATS_FILE_NAME = "cobblemon_initiative_streamstats.json";

  /** Gson shape — these field names are the on-disk contract. */
  private static class Data {
    String worldId;
    long pokemonLost;
    long whiteouts;
    long sacrifices;
    long duplicateReleases;
    long captures;
  }

  private Data data = new Data();
  private boolean loaded = false;

  private Path getSavePath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(STATS_FILE_NAME);
  }

  public synchronized void load(MinecraftServer server) {
    Path savePath = getSavePath(server);
    data = new Data();
    if (Files.exists(savePath)) {
      try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
        Data read = GSON.fromJson(reader, Data.class);
        if (read != null) data = read;
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("[StreamSync] Failed to load stream stats", e);
      }
    }
    loaded = true;
    if (data.worldId == null || data.worldId.isBlank()) {
      // First load of this save: mint the attempt id and persist it IMMEDIATELY,
      // so a crash before SERVER_STOPPING can never re-mint — the overlay would
      // otherwise count a phantom hardcore attempt.
      data.worldId = UUID.randomUUID().toString();
      save(server);
      InitiativeInit.LOGGER.info("[StreamSync] Minted worldId {} for this save", data.worldId);
    }
  }

  public synchronized void save(MinecraftServer server) {
    Path savePath = getSavePath(server);
    try {
      Files.createDirectories(savePath.getParent());
      try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
        GSON.toJson(data, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[StreamSync] Failed to save stream stats", e);
    }
  }

  public synchronized boolean isLoaded() {
    return loaded;
  }

  /** Drops the in-memory state after a lifecycle save — the next world loads its own file. */
  public synchronized void unload() {
    loaded = false;
    data = new Data();
  }

  public synchronized String getWorldId() {
    return data.worldId;
  }

  /** Records a loss; returns the new all-cause total (the event's {@code deathsTotal}). */
  public synchronized long recordLoss(String cause) {
    data.pokemonLost++;
    switch (cause) {
      case StreamSyncEvents.CAUSE_SACRIFICE -> data.sacrifices++;
      case StreamSyncEvents.CAUSE_DUPLICATE_RELEASE -> data.duplicateReleases++;
      default -> {} // faint has no dedicated counter — it's the total minus the other two
    }
    return data.pokemonLost;
  }

  public synchronized void recordWhiteout() {
    data.whiteouts++;
  }

  public synchronized void recordCapture() {
    data.captures++;
  }

  /** All-cause permanent-loss total for this save — the seed the achievement deaths counter
   *  reconciles against when it is introduced mid-run onto an already-deep save. */
  public synchronized long getPokemonLost() {
    return data.pokemonLost;
  }

  /** The snapshot's {@code deaths} block, built under the same lock the increments use. */
  public synchronized JsonObject toDeathsJson() {
    JsonObject json = new JsonObject();
    json.addProperty("total", data.pokemonLost);
    json.addProperty("whiteouts", data.whiteouts);
    json.addProperty("sacrifices", data.sacrifices);
    json.addProperty("duplicateReleases", data.duplicateReleases);
    return json;
  }
}

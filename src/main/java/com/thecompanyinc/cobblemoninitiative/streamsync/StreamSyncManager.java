package com.thecompanyinc.cobblemoninitiative.streamsync;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import com.thecompanyinc.cobblemoninitiative.config.StreamSyncConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackManager;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Tick-driven snapshot builder. Runs entirely on the SERVER thread — every
 * game-object read (party, progress, cap, quest, zone) happens here; the pusher
 * only ever sees the finished JsonObject.
 *
 * <p>Change detection: the built payload's JSON string is the fingerprint. A
 * push happens when it differs from the last pushed one, when a bus event
 * marked the state dirty (so a toast and its snapshot land together), or when
 * the heartbeat interval lapses — the overlay's staleness watchdog always has a
 * pulse even on an idle pause screen.
 */
public class StreamSyncManager {

  private int tickCounter = 0;
  private String lastFingerprint = null;
  private long lastPushMillis = 0;
  /** Set by the bus (any thread) to force the next cadence check to push. */
  private volatile boolean dirty = false;

  /** END_SERVER_TICK hook. Inert (one static read, zero allocation) while disabled. */
  public void tick(MinecraftServer server) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    StreamSyncConfig cfg = StreamSyncConfig.get();
    if (++tickCounter < Math.max(1, cfg.getSnapshotIntervalTicks())) return;
    tickCounter = 0;
    pushSnapshot(server, pusher, cfg, false);
  }

  /**
   * Builds the snapshot and enqueues it if changed / dirty / heartbeat-due.
   * {@code force} skips those gates (the /streamsync push command).
   *
   * @return true if a snapshot was enqueued
   */
  public boolean pushSnapshot(
    MinecraftServer server, StreamSyncPusher pusher, StreamSyncConfig cfg, boolean force
  ) {
    List<ServerPlayer> players = server.getPlayerList().getPlayers();
    if (players.isEmpty()) return false; // single-player mod — no player, no state to show
    ServerPlayer player = players.get(0);

    JsonObject snapshot = buildSnapshot(server, player, cfg);
    String fingerprint = snapshot.toString();
    long now = System.currentTimeMillis();
    boolean heartbeatDue = now - lastPushMillis >= cfg.getHeartbeatSeconds() * 1000L;
    if (!force && !dirty && !heartbeatDue && fingerprint.equals(lastFingerprint)) return false;

    dirty = false;
    lastFingerprint = fingerprint;
    lastPushMillis = now;
    pusher.enqueueSnapshot(snapshot);
    return true;
  }

  /** Bus events invalidate the fingerprint so their state change ships on the next cadence tick. */
  public void invalidate() {
    dirty = true;
  }

  /** Fresh session (server start / /streamsync on|reload) — first tick always pushes. */
  public void resetSession() {
    tickCounter = 0;
    lastFingerprint = null;
    lastPushMillis = 0;
    dirty = false;
  }

  // ── Snapshot assembly (server thread) ────────────────────────────────────────

  private JsonObject buildSnapshot(MinecraftServer server, ServerPlayer player, StreamSyncConfig cfg) {
    JsonObject json = new JsonObject();
    json.addProperty("v", StreamSyncInit.PROTOCOL_VERSION);
    json.addProperty("type", "snapshot");
    json.addProperty("player", player.getName().getString());
    json.addProperty("worldId", StreamSyncInit.getStats().getWorldId());

    // World clock — the overworld's (UPM 2 lives there).
    ServerLevel overworld = server.overworld();
    JsonObject world = new JsonObject();
    world.addProperty("day", overworld.getDayTime() / 24000L);
    world.addProperty("timeOfDay", overworld.getDayTime() % 24000L);
    world.addProperty("playtimeTicks", overworld.getGameTime());
    json.add("world", world);

    if (cfg.isIncludeLocation()) {
      String zone = zoneName(player);
      if (zone != null) json.addProperty("location", zone);
    }

    json.add("party", buildParty(player, cfg.isIncludeHeldItems()));
    json.add("deaths", StreamSyncInit.getStats().toDeathsJson());
    json.add("progress", buildProgress(player));

    QuestTrackManager.TrackedWaypoint waypoint = QuestTrackManager.current(player);
    if (waypoint != null) {
      JsonObject quest = new JsonObject();
      quest.addProperty("name", waypoint.questName());
      quest.addProperty("stage", waypoint.label());
      json.add("quest", quest);
    }

    return json;
  }

  private JsonArray buildParty(ServerPlayer player, boolean includeHeldItems) {
    JsonArray party = new JsonArray();
    PlayerPartyStore store = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (int slot = 0; slot < 6; slot++) {
      Pokemon pokemon = store.get(slot);
      if (pokemon == null) continue;
      JsonObject entry = describePokemon(pokemon);
      entry.addProperty("slot", slot);
      entry.addProperty("hp", pokemon.getCurrentHealth());
      entry.addProperty("maxHp", pokemon.getMaxHealth());
      entry.addProperty("fainted", pokemon.isFainted());
      if (includeHeldItems) {
        ItemStack held = pokemon.heldItem();
        if (held != null && !held.isEmpty()) {
          entry.addProperty("heldItem", BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
        }
      }
      party.add(entry);
    }
    return party;
  }

  private JsonObject buildProgress(ServerPlayer player) {
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    JsonObject json = new JsonObject();
    json.addProperty("badges", countBadges(progress));
    json.addProperty("levelCap", InitiativeInit.getLevelCapManager().getLevelCap(player));
    json.addProperty("nextLevelCap", InitiativeInit.getLevelCapManager().getNextLevelCap(player));
    json.addProperty("trainersDefeated", progress.getDefeatedTrainers().size());
    return json;
  }

  /** Announced-zone name via the Nuzlocke zone system, or null in the wild. */
  private String zoneName(ServerPlayer player) {
    NuzlockeConfig nuzlocke = NuzlockeInit.getConfig();
    if (nuzlocke == null) return null;
    NuzlockeConfig.SafeZone zone = nuzlocke.getAnnouncedZoneAt(
      player.level().dimension().location().toString(),
      player.getBlockX(),
      player.getBlockY(),
      player.getBlockZ(),
      player.getServer()
    );
    return zone != null ? zone.name : null;
  }

  // ── Shared shapes (also used by StreamSyncEvents on the calling thread) ──────

  /** The wire's pokemon object: ids for sprites (species path + dex) + display fields. */
  static JsonObject describePokemon(Pokemon pokemon) {
    JsonObject json = new JsonObject();
    json.addProperty("uuid", pokemon.getUuid().toString());
    json.addProperty("species", pokemon.getSpecies().getResourceIdentifier().toString());
    json.addProperty("dex", pokemon.getSpecies().getNationalPokedexNumber());
    MutableComponent nickname = pokemon.getNickname();
    json.addProperty(
      "name",
      nickname != null && !nickname.getString().isBlank()
        ? nickname.getString()
        : pokemon.getSpecies().getName()
    );
    json.addProperty("level", pokemon.getLevel());
    json.addProperty("shiny", pokemon.getShiny());
    json.addProperty("gender", pokemon.getGender().name().toLowerCase(Locale.ROOT));
    return json;
  }

  /** Badge count — the same gym-leader derivation the all_badges achievement uses. */
  static int countBadges(ServerPlayer player) {
    return countBadges(InitiativeInit.getProgressManager().getProgress(player));
  }

  private static int countBadges(PlayerProgress progress) {
    int badges = 0;
    for (TrainerConfig trainer : InitiativeInit.getConfigLoader().getTrainersByCategory("gym")) {
      if ("leader".equals(trainer.getTrainerType()) && progress.hasDefeatedTrainer(trainer.getId())) {
        badges++;
      }
    }
    return badges;
  }
}

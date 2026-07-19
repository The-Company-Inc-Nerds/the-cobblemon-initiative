package com.thecompanyinc.cobblemoninitiative.streamsync;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import net.minecraft.server.level.ServerPlayer;

/**
 * The static event bus other subsystems post to — one-liners at their EXISTING
 * decision points. Loss semantics stay owned by the subsystem that gates them
 * (Nuzlocke's stadium/frontier/noble guards, the progress dedup, the cap
 * ladder); re-deriving those rules in an independent subscriber would drift.
 *
 * <p>Contract: every method is a cheap no-op when the subsystem is inactive
 * (one static read + null check — zero allocation), and when active it builds
 * the COMPLETE event JsonObject on the CALLING thread before enqueueing — the
 * pusher thread never touches Pokemon/player/level objects. The sacrifice site
 * calls in from the CLIENT thread; that is fine by design: all reads happen
 * right here, and the only cross-thread hand-off is a finished JsonObject.
 */
public final class StreamSyncEvents {

  // pokemon_lost causes (wire values — the overlay keys cemetery icons on these).
  public static final String CAUSE_FAINT = "faint";
  public static final String CAUSE_SACRIFICE = "sacrifice";
  public static final String CAUSE_DUPLICATE_RELEASE = "duplicate_release";

  // whiteout reasons (wire values).
  public static final String REASON_FAINT = "faint";
  public static final String REASON_FLEE = "flee";
  public static final String REASON_FORFEIT = "forfeit";

  private StreamSyncEvents() {}

  /** A Pokémon permanently left the run (faint removal, sacrifice, duplicate release). */
  public static void pokemonLost(ServerPlayer player, Pokemon pokemon, String cause) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    long deathsTotal = StreamSyncInit.getStats().recordLoss(cause);
    JsonObject json = eventEnvelope("pokemon_lost");
    json.addProperty("cause", cause);
    json.add("pokemon", StreamSyncManager.describePokemon(pokemon));
    json.addProperty("deathsTotal", deathsTotal);
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** A capture stuck (posted by StreamSyncInit's own POKEMON_CAPTURED subscriber). */
  public static void captured(ServerPlayer player, Pokemon pokemon) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    StreamSyncInit.getStats().recordCapture();
    JsonObject json = eventEnvelope("capture");
    json.add("pokemon", StreamSyncManager.describePokemon(pokemon));
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** The run-ending latch tripped (about to {@code player.kill()}). */
  public static void whiteout(ServerPlayer player, String reason) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    StreamSyncInit.getStats().recordWhiteout();
    JsonObject json = eventEnvelope("whiteout");
    json.addProperty("reason", reason);
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** A gym badge advancement was granted. {@code badgeId} is the short id, e.g. "badge_grass". */
  public static void badgeEarned(ServerPlayer player, String badgeId) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    JsonObject json = eventEnvelope("badge");
    json.addProperty("badgeId", badgeId);
    json.addProperty("badges", StreamSyncManager.countBadges(player));
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** A trainer defeat was recorded for the first time (post-dedup in PlayerProgressManager). */
  public static void trainerDefeated(ServerPlayer player, String trainerId) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    TrainerConfig trainer = InitiativeInit.getConfigLoader() != null
      ? InitiativeInit.getConfigLoader().getTrainer(trainerId)
      : null;
    JsonObject json = eventEnvelope("trainer_defeated");
    json.addProperty("trainerId", trainerId);
    json.addProperty("trainerName", trainer != null ? trainer.getDisplayName() : trainerId);
    if (trainer != null && trainer.getCategory() != null) {
      json.addProperty("category", trainer.getCategory());
    }
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** The level cap moved (LevelCapManager's newCap != current branch). */
  public static void levelCapChanged(ServerPlayer player, int cap) {
    StreamSyncPusher pusher = StreamSyncInit.getPusher();
    if (pusher == null) return;
    JsonObject json = eventEnvelope("level_cap");
    json.addProperty("cap", cap);
    pusher.enqueueEvent(json);
    StreamSyncInit.getManager().invalidate();
  }

  /** Shared event envelope; session/seq/t are stamped by the pusher at enqueue. */
  static JsonObject eventEnvelope(String event) {
    JsonObject json = new JsonObject();
    json.addProperty("v", StreamSyncInit.PROTOCOL_VERSION);
    json.addProperty("type", "event");
    json.addProperty("event", event);
    return json;
  }
}

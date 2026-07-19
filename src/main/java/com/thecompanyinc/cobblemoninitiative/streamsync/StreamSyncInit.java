package com.thecompanyinc.cobblemoninitiative.streamsync;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.StreamSyncConfig;
import java.util.UUID;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * Entrypoint for the stream-sync subsystem: pushes live run state (party bar,
 * death counter, badges / level cap, event toasts) as JSON to the broadcast
 * host's overlay service, which OBS renders via browser sources. Push-only over
 * HTTP — the gaming box never opens an inbound port.
 *
 * <p>Default-DISABLED ({@link StreamSyncConfig}): in a bare install this
 * entrypoint registers hooks that no-op on a static null check, starts no
 * thread, and creates no network object. The mrpack override flips it on.
 *
 * <p>Threading: snapshots are built on the server tick, event JSON at each bus
 * call site (whatever thread that is); {@link StreamSyncPusher} is the only
 * other thread and only ever sees finished JsonObjects.
 */
public class StreamSyncInit implements ModInitializer {

  /** Wire protocol version — bump only with the overlay service in lockstep. */
  public static final int PROTOCOL_VERSION = 1;

  private static StreamSyncManager manager;
  private static StreamSyncStats stats;
  /** Non-null exactly while a session is live — the bus + tick gate (volatile: client-thread posts). */
  private static volatile StreamSyncPusher pusher;

  @Override
  public void onInitialize() {
    StreamSyncConfig.get(); // load (and write defaults) up front like the sibling configs
    manager = new StreamSyncManager();
    stats = new StreamSyncStats();

    registerCommands();

    ServerTickEvents.END_SERVER_TICK.register(server -> manager.tick(server));
    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      if (StreamSyncConfig.get().isEnabled()) startSession(server);
    });
    ServerLifecycleEvents.SERVER_STOPPING.register(StreamSyncInit::stopSession);

    // Capture toasts ride Cobblemon's own event, at LOWEST so Nuzlocke's NORMAL
    // handler (duplicate release / send-to-PC / zero-HP) has already settled the
    // outcome — a released duplicate emits capture AND pokemon_lost, in order.
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, event -> {
      StreamSyncEvents.captured(event.getPlayer(), event.getPokemon());
      return Unit.INSTANCE;
    });

    InitiativeInit.LOGGER.info(
      "Stream sync initialized ({}).",
      StreamSyncConfig.get().isEnabled() ? "enabled" : "disabled"
    );
  }

  // ── Static accessors (the bus + manager read these on every post/tick) ───────

  /** The live pusher, or null while disabled — the whole subsystem gates on this. */
  public static StreamSyncPusher getPusher() {
    return pusher;
  }

  public static StreamSyncManager getManager() {
    return manager;
  }

  public static StreamSyncStats getStats() {
    return stats;
  }

  // ── Session lifecycle (server thread) ────────────────────────────────────────

  private static synchronized void startSession(MinecraftServer server) {
    if (pusher != null) return;
    StreamSyncConfig cfg = StreamSyncConfig.get();
    if (!stats.isLoaded()) stats.load(server); // mints + persists worldId on a fresh save
    manager.resetSession();
    StreamSyncPusher started = new StreamSyncPusher(UUID.randomUUID().toString(), cfg);
    sendSessionStart(started);
    pusher = started; // publish last — the bus only sees a fully wired session
    InitiativeInit.LOGGER.info(
      "[StreamSync] Session {} started → {}", started.getSessionId(), cfg.getEndpointUrl()
    );
  }

  private static synchronized void stopSession(MinecraftServer server) {
    StreamSyncPusher stopping = pusher;
    if (stopping != null) {
      pusher = null; // close the gate first — late bus posts no-op instead of racing shutdown
      stopping.enqueueEvent(StreamSyncEvents.eventEnvelope("session_stop"));
      stopping.shutdown(2000); // hard ceiling — world save is never delayed by a flush
      InitiativeInit.LOGGER.info("[StreamSync] Session {} stopped.", stopping.getSessionId());
    }
    if (stats.isLoaded()) {
      stats.save(server);
      // Forget this save's data: the singleplayer client keeps this static across
      // integrated-server restarts, so without the unload the NEXT world (the
      // hardcore-reset flow) would inherit this world's worldId + counters and
      // stopSession would write them into the wrong save's file. startSession
      // re-loads from the current world's file.
      stats.unload();
    }
  }

  private static void sendSessionStart(StreamSyncPusher target) {
    JsonObject json = StreamSyncEvents.eventEnvelope("session_start");
    json.addProperty(
      "modVersion",
      FabricLoader.getInstance().getModContainer(InitiativeInit.MOD_ID)
        .map(c -> c.getMetadata().getVersion().getFriendlyString())
        .orElse("unknown")
    );
    json.addProperty("protocol", PROTOCOL_VERSION);
    target.enqueueEvent(json);
  }

  // ── Commands ────────────────────────────────────────────────────────────────

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      dispatcher.register(
        Commands.literal("streamsync")
          .requires(source -> source.hasPermission(2))
          .then(Commands.literal("status").executes(context -> {
            StreamSyncConfig cfg = StreamSyncConfig.get();
            StreamSyncPusher p = pusher;
            // Stable, greppable one-liner (key=value; "-" while no session is live).
            String line = "[streamsync] enabled=" + cfg.isEnabled()
              + " session=" + (p != null ? p.getSessionId() : "-")
              + " link=" + (p != null ? (p.isLinkUp() ? "up" : "down") : "-")
              + " queued=" + (p != null ? p.queuedCount() : 0)
              + " seq=" + (p != null ? p.lastSeq() : 0)
              + " endpoint=" + cfg.getEndpointUrl();
            context.getSource().sendSuccess(() -> Component.literal(line), false);
            return 1;
          }))
          .then(Commands.literal("on").executes(context -> {
            StreamSyncConfig cfg = StreamSyncConfig.get();
            cfg.setEnabled(true);
            cfg.save();
            startSession(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.literal("[streamsync] enabled"), true);
            return 1;
          }))
          .then(Commands.literal("off").executes(context -> {
            StreamSyncConfig cfg = StreamSyncConfig.get();
            cfg.setEnabled(false);
            cfg.save();
            stopSession(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.literal("[streamsync] disabled"), true);
            return 1;
          }))
          .then(Commands.literal("push").executes(context -> {
            StreamSyncPusher p = pusher;
            if (p == null) {
              context.getSource().sendFailure(
                Component.literal("[streamsync] push skipped: disabled")
              );
              return 0;
            }
            boolean sent = manager.pushSnapshot(
              context.getSource().getServer(), p, StreamSyncConfig.get(), true
            );
            if (sent) {
              context.getSource().sendSuccess(
                () -> Component.literal("[streamsync] snapshot pushed"), false
              );
            } else {
              context.getSource().sendFailure(
                Component.literal("[streamsync] push skipped: no player online")
              );
            }
            return sent ? 1 : 0;
          }))
          .then(Commands.literal("reload").executes(context -> {
            StreamSyncConfig.reload();
            StreamSyncConfig cfg = StreamSyncConfig.get();
            MinecraftServer server = context.getSource().getServer();
            // A session restart also picks up a changed endpoint / token / queue size.
            stopSession(server);
            if (cfg.isEnabled()) startSession(server);
            context.getSource().sendSuccess(
              () -> Component.literal("[streamsync] config reloaded enabled=" + cfg.isEnabled()),
              true
            );
            return 1;
          }))
      );
    });
  }
}

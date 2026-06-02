package com.thecompanyinc.cobblemoninitiative.zonetrace;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only: zone boundary tracing tool.
 * Registers the Zone Tracer wand listener and zone-trace commands.
 * Remove this entrypoint from fabric.mod.json before final release.
 */
public class ZoneTraceInit implements ModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-zonetrace");

  private static ZoneTraceStorage storage;

  @Override
  public void onInitialize() {
    storage = new ZoneTraceStorage();

    // Register block right-click listener for the Zone Tracer wand.
    ZoneTraceCommand.registerItemListener();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      ZoneTraceCommand.register(dispatcher, registryAccess, storage)
    );

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      storage.load(server);
      LOGGER.info("Zone Trace loaded {} saved zone(s).", storage.size());
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      storage.save();
    });

    LOGGER.info("Zone Trace dev tool initialized.");
  }

  public static ZoneTraceStorage getStorage() {
    return storage;
  }
}

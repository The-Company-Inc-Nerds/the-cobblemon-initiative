package com.thecompanyinc.cobblemoninitiative.npcmap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcMapInit implements ModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-npcmap");

  private static NpcMapStorage storage;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing NPC Map...");

    storage = new NpcMapStorage();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      NpcMapCommand.register(dispatcher, registryAccess, storage)
    );

    // Per-NPC preset refresh: imports shipped presets onto mapped NPCs as their chunks
    // load (a one-shot bulk import can only reach loaded NPCs — see the manager's javadoc).
    NpcPresetRefreshManager.init();

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      storage.load(server);
      LOGGER.info("NPC Map loaded {} mapping(s).", storage.size());
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      storage.save();
      LOGGER.info("NPC Map saved data.");
    });

    LOGGER.info("NPC Map initialized.");
  }

  public static NpcMapStorage getStorage() {
    return storage;
  }
}

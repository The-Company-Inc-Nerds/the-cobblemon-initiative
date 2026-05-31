package com.thecompanyinc.cobblemoninitiative.npcsight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcSightInit implements ModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-npcsight");

  private static NpcSightConfig config;
  private static NpcSightStorage storage;
  private static NpcSightManager manager;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing NPC Sight...");

    config = NpcSightConfig.load();
    storage = new NpcSightStorage();
    manager = new NpcSightManager(storage, config);

    // Tick processing
    ServerTickEvents.END_SERVER_TICK.register(manager::tick);

    // Command registration
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      NpcSightCommand.register(dispatcher, registryAccess, manager, storage)
    );

    // World data load / save
    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      storage.load(server);
      LOGGER.info("NPC Sight loaded {} registered NPC(s).", storage.size());
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      storage.save();
      LOGGER.info("NPC Sight saved data.");
    });

    LOGGER.info("NPC Sight initialized.");
  }

  // ---------------------------------------------------------------------------
  // Static accessors (used by config screen)
  // ---------------------------------------------------------------------------

  public static NpcSightConfig getConfig() {
    return config;
  }

  public static NpcSightStorage getStorage() {
    return storage;
  }

  public static NpcSightManager getManager() {
    return manager;
  }

  public static void reloadConfig() {
    config = NpcSightConfig.load();
    if (manager != null) manager.reloadConfig(config);
  }
}

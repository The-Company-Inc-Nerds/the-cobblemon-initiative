package com.thecompanyinc.cobblemoninitiative.npcmap;

import net.fabricmc.api.ModInitializer;
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

    // The /npc-map command was pruned 2026-07-30 (unused in playtest). The storage below is
    // still SHIPPING infra — InstallCommand reads it to apply presets to UUIDs on install.

    // NpcPresetRefreshManager.init() moved to InitiativeInit (2026-07-11): the refresh is
    // SHIPPING code and must survive this dev entrypoint's 1.0.0 strip (TODO §2).

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

package com.thecompanyinc.cobblemoninitiative.cutscene;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Entrypoint for the reusable cutscene rig. Owns the {@link CutsceneManager} singleton and
 * wires the tick loop, the disconnect hook (restore game mode before a mid-scene logout can
 * persist SPECTATOR into the save), the server-stop teardown, and the commands.
 */
public class CutsceneInit implements ModInitializer {

  private static CutsceneManager manager;

  @Override
  public void onInitialize() {
    manager = new CutsceneManager();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      CutsceneCommands.register(dispatcher));

    ServerTickEvents.END_SERVER_TICK.register(server -> manager.tick(server));
    ServerLifecycleEvents.SERVER_STOPPING.register(manager::onServerStopping);
    // Restore game mode the instant a player disconnects mid-scene (before it saves SPECTATOR).
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> manager.onDisconnect(handler.player));

    InitiativeInit.LOGGER.info("Cutscene rig initialized.");
  }

  public static CutsceneManager getManager() {
    return manager;
  }
}

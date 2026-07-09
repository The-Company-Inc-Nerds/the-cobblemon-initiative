package com.thecompanyinc.cobblemoninitiative.noble;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Entrypoint for the noble boss-fight subsystem. Owns the manager singleton and wires the
 * tick loop, lifecycle hooks, commands, and its own Cobblemon battle/capture subscribers
 * (Cobblemon's event bus is multi-subscriber — InitiativeInit and NuzlockeInit already both
 * subscribe to BATTLE_VICTORY, so a third self-contained handler here is idiomatic).
 */
public class NobleEncounterInit implements ModInitializer {

  private static NobleEncounterManager manager;

  @Override
  public void onInitialize() {
    manager = new NobleEncounterManager();
    manager.loadNobles();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      NobleCommands.register(dispatcher);
      // /noble-abort — no OP required; lets players withdraw.
      dispatcher.register(
        Commands.literal("noble-abort").executes(ctx -> {
          ServerPlayer player = ctx.getSource().getPlayer();
          if (player != null) manager.abort(player);
          return 1;
        })
      );
    });

    ServerTickEvents.END_SERVER_TICK.register(server -> manager.tick(server));
    ServerLifecycleEvents.SERVER_STARTED.register(manager::onServerStarted);
    ServerLifecycleEvents.SERVER_STOPPING.register(manager::onServerStopping);

    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
      manager.onBattleVictory(event);
      return Unit.INSTANCE;
    });
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
      manager.onPokemonCaptured(event);
      return Unit.INSTANCE;
    });

    InitiativeInit.LOGGER.info("Noble encounters initialized ({} defined).", manager.getNobleIds().length);
  }

  public static NobleEncounterManager getManager() {
    return manager;
  }
}

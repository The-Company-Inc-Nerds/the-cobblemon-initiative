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
  private static RiftDragonManager riftDragon;
  private static AmbientNobleManager ambientNobles;

  @Override
  public void onInitialize() {
    manager = new NobleEncounterManager();
    manager.loadNobles();
    riftDragon = new RiftDragonManager();
    riftDragon.loadConfig();
    ambientNobles = new AmbientNobleManager();
    ambientNobles.load();

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
      // /riftdragon start|stop — the Ryujin rift boss (perm 2; the leader's dialog
      // button reaches it through function cobblemon_initiative:gym/rift_start).
      dispatcher.register(
        Commands.literal("riftdragon")
          .requires(source -> source.hasPermission(2))
          .then(Commands.literal("start").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            return (player != null && riftDragon.start(player)) ? 1 : 0;
          }))
          .then(Commands.literal("stop").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player != null) riftDragon.abort(player);
            return 1;
          }))
      );
    });

    ServerTickEvents.END_SERVER_TICK.register(server -> {
      manager.tick(server);
      riftDragon.tick(server);
      ambientNobles.tick(server);
    });
    ServerLifecycleEvents.SERVER_STARTED.register(manager::onServerStarted);
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      manager.onServerStopping(server);
      riftDragon.onServerStopping(server);
    });

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

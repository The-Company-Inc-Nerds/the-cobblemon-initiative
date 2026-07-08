package com.thecompanyinc.cobblemoninitiative;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.thecompanyinc.cobblemoninitiative.command.CobblemonInitiativeCommands;
import com.thecompanyinc.cobblemoninitiative.compat.EasyNpcSecurityConfig;
import com.thecompanyinc.cobblemoninitiative.dex.DexScoreManager;
import com.thecompanyinc.cobblemoninitiative.install.AutoInstall;
import com.thecompanyinc.cobblemoninitiative.install.InstallCommand;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgressManager;
import com.thecompanyinc.cobblemoninitiative.items.ModItems;
import com.thecompanyinc.cobblemoninitiative.levelcap.LevelCapManager;
import com.thecompanyinc.cobblemoninitiative.docprop.DocPropManager;
import com.thecompanyinc.cobblemoninitiative.lootchest.LootChestManager;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackManager;
import com.thecompanyinc.cobblemoninitiative.shrine.ShrineChallengeManager;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitiativeInit implements ModInitializer {

  public static final String MOD_ID = "cobblemon-initiative";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static ConfigLoader configLoader;
  private static PlayerProgressManager progressManager;
  private static LevelCapManager levelCapManager;
  private static ShrineChallengeManager shrineChallengeManager;
  private static LootChestManager lootChestManager;
  private static DocPropManager docPropManager;
  private static QuestTrackManager questTrackManager;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing The Cobblemon Initiative...");

    // Standalone guarantee (no mrpack required): Easy NPC blocks every dialog-button
    // command unless its root is allowlisted in security.cfg. Easy NPC reads that file
    // lazily (first button press), so patching here reliably precedes the first read.
    EasyNpcSecurityConfig.ensureAllowlist();

    ModItems.register();

    FabricLoader.getInstance()
      .getModContainer(MOD_ID)
      .ifPresent(container -> {
        ResourceManagerHelper.registerBuiltinResourcePack(
          ResourceLocation.fromNamespaceAndPath(MOD_ID, "trainer_textures"),
          container,
          Component.literal("Trainer Textures"),
          ResourcePackActivationType.DEFAULT_ENABLED
        );
      });

    configLoader = new ConfigLoader();
    configLoader.loadAllConfigs();

    progressManager = new PlayerProgressManager();
    levelCapManager = new LevelCapManager(configLoader);

    shrineChallengeManager = new ShrineChallengeManager();
    shrineChallengeManager.loadChallenges();

    lootChestManager = new LootChestManager();
    docPropManager = new DocPropManager();

    questTrackManager = new QuestTrackManager();
    questTrackManager.loadQuests();

    // Map Frontiers integration is applied lazily at /cobblemon-initiative install run
    // (see MapFrontiersBridge); no init-time registration is needed.

    registerBattleEvents();

    // Server tick — drives parkour timers and ground-gauntlet effects
    ServerTickEvents.END_SERVER_TICK.register(server ->
      shrineChallengeManager.tick(server)
    );

    // Quest tracking — 5-tick waypoint resolution + sidebar ▶ highlight, and the
    // 10-tick particle beam fallback when JourneyMap is absent.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      questTrackManager.tick(server)
    );

    // THE INCOMPLETE FILE props: click the ledger barrel / portrait chest to "find" the
    // document. Registered BEFORE LootChest so it wins at the portrait chest during the
    // pickup window (a non-PASS result short-circuits later UseBlock handlers).
    UseBlockCallback.EVENT.register(docPropManager::onUse);

    // Unplaced-chest loot: intercept chest opens; track hand-placed chests.
    UseBlockCallback.EVENT.register(lootChestManager::onChestUse);
    PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
      lootChestManager.onBlockBroken(state, pos)
    );

    // Pack-only first-join auto-install: runs `install run` once per fresh world when
    // the mrpack's marker config is present; inert on bare-mod installs.
    AutoInstall.init();

    // Mirror Pokédex caught-count into the dex_caught scoreboard (starter unlock gates).
    DexScoreManager.init();

    CommandRegistrationCallback.EVENT.register(
      (dispatcher, registryAccess, environment) -> {
        CobblemonInitiativeCommands.register(dispatcher);
        InstallCommand.register(dispatcher);

        // /shrine-abort — no OP permission required; lets players abort themselves
        dispatcher.register(
          Commands.literal("shrine-abort").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player != null) {
              shrineChallengeManager.stopChallenge(player);
            }
            return 1;
          })
        );
      }
    );

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      progressManager.loadProgress(server);
      shrineChallengeManager.loadPaths(server);
      lootChestManager.load(server);
      questTrackManager.load(server);
      // Standalone guarantee: force rctmod's allowOverLeveling + our series into its live
      // config cache on ANY world (bundled map bakes it; fresh/bare worlds get defaults
      // that would re-enable rctmod's clamp and fight our badge ladder). SERVER_STARTED is
      // after the per-world serverconfig loads, before any player joins.
      com.thecompanyinc.cobblemoninitiative.compat.RctmodServerConfig.healServerConfig(server);
      LOGGER.info("Loaded player progress data.");
    });

    // Migrate players saved under the wrong/empty rctmod series (new players are placed by
    // the healed initialSeries; this only fixes pre-existing stat.dat).
    net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
      (handler, sender, server) -> {
        com.thecompanyinc.cobblemoninitiative.compat.RctmodServerConfig.ensurePlayerSeries(handler.player);
        snapFirstJoinToSpawn(handler.player, server);
      });

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      progressManager.saveProgress(server);
      shrineChallengeManager.savePaths();
      lootChestManager.save();
      questTrackManager.save(server); // also hands the ▶-highlighted lines back
      LOGGER.info("Saved player progress data.");
    });

    LOGGER.info("The Cobblemon Initiative initialized successfully!");
  }

  // FRESH-WORLD SPAWN SNAP (belt-and-braces; mirrors the RctmodServerConfig self-heal):
  // vanilla 1.21.1 IGNORES SpawnY for a brand-new player — ServerPlayer's constructor
  // calls adjustSpawnLocation → PlayerRespawnLogic.getOverworldRespawnPos, which scans
  // DOWN from the MOTION_BLOCKING heightmap top of the spawn column, so on UPM 2 a
  // first join lands on the spawn house ROOF (y=122 instead of 109). The mrpack build
  // bakes a sanitized Data.Player tag that places the bundled-map host correctly; this
  // covers dev/bare-mod worlds the bake never touches. JOIN fires while the client is
  // still on the terrain-loading screen, so no roof frame ever renders. The
  // ci_spawn_snapped player tag (persisted in player NBT, same mechanism as
  // chose_starter) makes this strictly once per player — set unconditionally after the
  // first check so it can never re-fire on a player who has since walked away.
  private static void snapFirstJoinToSpawn(ServerPlayer player, net.minecraft.server.MinecraftServer server) {
    if (!player.addTag("ci_spawn_snapped")) {
      return; // tag already present — not a first join
    }
    net.minecraft.server.level.ServerLevel overworld = server.overworld();
    if (player.serverLevel() != overworld) {
      return; // only the overworld initial spawn — never pull a player across dimensions
    }
    net.minecraft.core.BlockPos spawn = overworld.getSharedSpawnPos();
    double x = spawn.getX() + 0.5;
    double y = spawn.getY();
    double z = spawn.getZ() + 0.5;
    if (player.distanceToSqr(x, y, z) <= 4.0) {
      return; // already where the map intends (e.g. the mrpack-baked host tag placed them)
    }
    player.teleportTo(overworld, x, y, z, overworld.getSharedSpawnAngle(), 0.0f);
    LOGGER.info(
      "First join: snapped {} to the shared spawn {} {} {} (vanilla heightmap placement ignores SpawnY).",
      player.getName().getString(),
      spawn.getX(),
      spawn.getY(),
      spawn.getZ()
    );
  }

  private void registerBattleEvents() {
    // Showrunner design (2026-07-04): the vanilla Cobblemon starter flow is CLOSED —
    // starters come from the three stand-in NPCs Acacia spawns (givepokemonother +
    // chose_starter, atomic in one dialog click).
    // BYTECODE-VERIFIED ORDER BUG (round 9): Cobblemon syncs ClientGeneralPlayerData
    // during SYNC_DATA_PACK_CONTENTS — BEFORE Fabric's ENTITY_LOAD fires — and nothing
    // re-syncs, so a hook there mutates the cached data AFTER the client already heard
    // starterSelected=false ("You have not yet selected a starter…" on party open).
    // DATA_SYNCHRONIZED at Priority.HIGHEST runs before Cobblemon's own NORMAL-priority
    // subscriber, whose syncAllToPlayer then ships the corrected flags — no extra packet.
    CobblemonEvents.DATA_SYNCHRONIZED.subscribe(Priority.HIGHEST, joining -> {
      try {
        var data = com.cobblemon.mod.common.Cobblemon.playerDataManager.getGenericData(joining);
        data.setStarterPrompted(true);
        data.setStarterSelected(true);
        com.cobblemon.mod.common.Cobblemon.playerDataManager.saveSingle(
          data,
          com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes.INSTANCE.getGENERAL());
      } catch (Exception e) {
        LOGGER.warn("Could not close the vanilla starter flow for {}", joining.getName().getString(), e);
      }
      return Unit.INSTANCE;
    });

    // Belt-and-braces: if the vanilla starter path ever fires anyway (config drift,
    // admin command), keep the chain in sync.
    CobblemonEvents.STARTER_CHOSEN.subscribe(Priority.NORMAL, event -> {
      ServerPlayer starterPlayer = event.getPlayer();
      if (starterPlayer != null) {
        starterPlayer.addTag("chose_starter");
      }
      return Unit.INSTANCE;
    });

    // LEVEL CAP ENFORCEMENT (round 9, bytecode-verified): nothing enforced our badge
    // ladder before — rctmod's own clamp (the only real one) derives its cap from its
    // SERIES graph, and with initialSeries="empty" that is frozen at initialLevelCap
    // (15) forever; rctmod has NO command or API to set a per-player cap. So the pack
    // config flips rctmod's kill switch (allowOverLeveling=true) and WE clamp with the
    // exact semantics rctmod uses: cap the gain at experience-to-cap, never cancel
    // (candies are auto-refunded by Cobblemon when the applied gain is 0).
    CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.NORMAL, ev -> {
      var owner = ev.getPokemon().getOwnerPlayer();
      if (owner instanceof ServerPlayer sp && levelCapManager != null) {
        int cap = levelCapManager.getLevelCap(sp);
        int maxGain = Math.max(0, ev.getPokemon().getExperienceToLevel(cap));
        if (ev.getExperience() > maxGain) {
          ev.setExperience(maxGain);
          sp.displayClientMessage(
            Component.literal("§6Level cap §e" + cap + "§6 — the next badge raises it."), true);
        }
      }
      return Unit.INSTANCE;
    });

    // Belt-and-braces for paths that adjust the level directly on level-up.
    CobblemonEvents.LEVEL_UP_EVENT.subscribe(Priority.NORMAL, ev -> {
      var owner = ev.getPokemon().getOwnerPlayer();
      if (owner instanceof ServerPlayer sp && levelCapManager != null) {
        int cap = levelCapManager.getLevelCap(sp);
        if (ev.getNewLevel() > cap) {
          ev.setNewLevel(cap);
        }
      }
      return Unit.INSTANCE;
    });

    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
      PokemonBattle battle = event.getBattle();

      for (BattleActor winner : event.getWinners()) {
        if (winner instanceof PlayerBattleActor playerActor) {
          ServerPlayer player = (ServerPlayer) playerActor.getEntity();
          if (player == null) continue;

          for (BattleActor loser : event.getLosers()) {
            String trainerName = loser.getName().getString();

            LOGGER.debug(
              "Player {} won battle against: {}",
              player.getName().getString(),
              trainerName
            );

            for (TrainerConfig trainer : configLoader.getAllTrainers()) {
              if (
                trainer.getDisplayName().equalsIgnoreCase(trainerName) ||
                trainer.getName().equalsIgnoreCase(trainerName) ||
                trainerName.contains(trainer.getDisplayName())
              ) {
                LOGGER.info(
                  "Player {} defeated trainer {} ({})",
                  player.getName().getString(),
                  trainer.getDisplayName(),
                  trainer.getId()
                );
                progressManager.onTrainerDefeated(player, trainer.getId());

                // Keep rctmod's native series graph in step (round 10): players enter
                // the cobblemon-initiative series via initialSeries, but tbcs battles
                // bypass rctmod entirely — its defeat memory never advances on its own.
                // Only gym apprentices + leaders are graph nodes (mobs/trainers/single).
                // Enforcement stays OURS (EXPERIENCE clamp above; allowOverLeveling on):
                // rctmod's next-key-trainer cap model can't express the badge ladder.
                String type = trainer.getTrainerType();
                if ("apprentice".equals(type) || "leader".equals(type)) {
                  var server = player.getServer();
                  if (server != null) {
                    // Grammar (bytecode, round 10b): targets go BEFORE the after
                    // literal — `player add progress <targets> after <trainerId>`.
                    // `after` marks the id + all its graph prerequisites.
                    server.getCommands().performPrefixedCommand(
                      server.createCommandSourceStack().withSuppressedOutput(),
                      "rctmod player add progress " + player.getGameProfile().getName()
                        + " after " + trainer.getId());
                  }
                }
                break;
              }
            }
          }
        }
      }

      return Unit.INSTANCE;
    });

    LOGGER.info("Registered battle victory event listener.");
  }

  public static ConfigLoader getConfigLoader() {
    return configLoader;
  }

  public static PlayerProgressManager getProgressManager() {
    return progressManager;
  }

  public static LevelCapManager getLevelCapManager() {
    return levelCapManager;
  }

  public static ShrineChallengeManager getShrineChallengeManager() {
    return shrineChallengeManager;
  }

  public static LootChestManager getLootChestManager() {
    return lootChestManager;
  }

  public static QuestTrackManager getQuestTrackManager() {
    return questTrackManager;
  }
}

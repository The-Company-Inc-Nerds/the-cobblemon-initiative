package com.thecompanyinc.cobblemoninitiative;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.thecompanyinc.cobblemoninitiative.achievement.AchievementManager;
import com.thecompanyinc.cobblemoninitiative.command.CobblemonInitiativeCommands;
import com.thecompanyinc.cobblemoninitiative.compat.EasyNpcSecurityConfig;
import com.thecompanyinc.cobblemoninitiative.dex.DexScoreManager;
import com.thecompanyinc.cobblemoninitiative.install.AutoInstall;
import com.thecompanyinc.cobblemoninitiative.install.InstallCommand;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgressManager;
import com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager;
import com.thecompanyinc.cobblemoninitiative.config.MinecraftFlavorConfig;
import com.thecompanyinc.cobblemoninitiative.homestead.HomesteadManager;
import com.thecompanyinc.cobblemoninitiative.momcare.MomCareManager;
import com.thecompanyinc.cobblemoninitiative.items.ModItems;
import com.thecompanyinc.cobblemoninitiative.levelcap.LevelCapManager;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterManager;
import com.thecompanyinc.cobblemoninitiative.docprop.DocPropManager;
import com.thecompanyinc.cobblemoninitiative.lootchest.LootChestManager;
import com.thecompanyinc.cobblemoninitiative.questtrack.QuestTrackManager;
import com.thecompanyinc.cobblemoninitiative.safari.SafariManager;
import com.thecompanyinc.cobblemoninitiative.shrine.ShrineChallengeManager;
import com.thecompanyinc.cobblemoninitiative.stadium.StadiumManager;
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
  private static SafariManager safariManager;
  private static com.thecompanyinc.cobblemoninitiative.frontier.FrontierManager frontierManager;
  private static QuestTrackManager questTrackManager;
  private static DaycareManager daycareManager;
  private static MinecraftFlavorConfig flavorConfig;
  private static HomesteadManager homesteadManager;
  private static MomCareManager momCareManager;
  private static AchievementManager achievementManager;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing The Cobblemon Initiative...");

    // Standalone guarantee (no mrpack required): Easy NPC blocks every dialog-button
    // command unless its root is allowlisted in security.cfg. Easy NPC reads that file
    // lazily (first button press), so patching here reliably precedes the first read.
    EasyNpcSecurityConfig.ensureAllowlist();

    // Per-NPC preset refresh: imports shipped presets onto mapped NPCs as their chunks
    // load. SHIPPING system — lives in npcmap/ but is init'd here so the npcmap dev
    // tooling (NpcMapInit + commands) can be stripped at 1.0.0 without touching it
    // (TODO §2). Idempotent guard inside init() protects against double registration.
    com.thecompanyinc.cobblemoninitiative.npcmap.NpcPresetRefreshManager.init();

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

    daycareManager = new DaycareManager();

    // "Make it Minecraft" flavor systems (docs/MINECRAFT_FLAVOR.md + PHONE_AND_CARE.md):
    // datapack toggles (mirrored to the ci_flavor scoreboard on start), the homestead-beacon
    // income loop, and Mom's friendship care. Configs load now; managers load world data on start.
    flavorConfig = MinecraftFlavorConfig.load();
    homesteadManager = new HomesteadManager();
    momCareManager = new MomCareManager();

    // Global/derived-state achievements: silent mid-run backfill on join + live toasts during
    // play (see AchievementManager). No world data of its own beyond a per-world batch latch;
    // the earned state is mirrored into PlayerProgress, which loads/saves with the rest.
    achievementManager = new AchievementManager();

    // Safari Zone — the Baiting Yards (badge-3 paid catch-only preserve). Owns its own
    // Cobblemon subscriptions (battle-cancel guard + session-gated capture ledger).
    safariManager = new SafariManager();
    safariManager.load();
    safariManager.registerEvents();

    // Battle Frontier hall mechanics — rentals, the wheel, castle points, priced
    // opponents, crew battles, the no-heal gauntlet, the streak climb (2026-07-19).
    frontierManager = new com.thecompanyinc.cobblemoninitiative.frontier.FrontierManager();
    frontierManager.registerEvents();

    // Map Frontiers integration is applied lazily at /cobblemon-initiative install run
    // (see MapFrontiersBridge); no init-time registration is needed.

    registerBattleEvents();

    // Stadium exhibition circuit: level-locked, attrition-free wave battles.
    // NuzlockeInit's faint/flee/forfeit handlers guard on StadiumManager.isStadiumActive
    // — init BEFORE NuzlockeInit's entrypoint runs so its event handlers exist first
    // (StadiumManager's own outcome handlers subscribe at Priority.LOWEST regardless).
    StadiumManager.init();

    // Server tick — drives parkour timers and ground-gauntlet effects
    ServerTickEvents.END_SERVER_TICK.register(server ->
      shrineChallengeManager.tick(server)
    );

    // Showdown wedge trap toast — a contained engine fault deserves one visible line
    // (the battle it hit may need /stopbattle; every detail is already in the log).
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (com.thecompanyinc.cobblemoninitiative.compat.ShowdownWedgeTrap.consumePendingNotify()) {
        for (net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
          p.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§c[Initiative] §7A battle-engine fault was contained. If the current battle "
              + "hangs, §f/stopbattle§7 recovers it — details are in the log."));
        }
      }
    });

    // Stadium wave loop — countdowns, battle-id capture, stale-run sweep (like shrine).
    ServerTickEvents.END_SERVER_TICK.register(StadiumManager::tick);

    // Quest tracking — 5-tick waypoint resolution + sidebar ▶ highlight, and the
    // 10-tick particle beam fallback when JourneyMap is absent.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      questTrackManager.tick(server)
    );

    // Daycare — slow cap-clamped XP drip + lazy pen stand-in reconcile.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      daycareManager.tick(server)
    );

    // Homestead beacons — day-latch harvest payout + deferred beacon purchase.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      homesteadManager.tick(server)
    );

    // Mom's friendship care — day-latch friendship drip + deferred (optional) fee.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      momCareManager.tick(server)
    );

    // Safari sessions — site clock, suspense windows, lure lifecycles, ejects.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      safariManager.tick(server)
    );

    // Frontier hall mechanics — fee resolution, chain delays, capture watchdogs.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      frontierManager.tick(server)
    );

    // Achievements — periodic live re-evaluation (every 2s) catches scoreboard/Pokédex-derived
    // milestone crossings (dex count, fields liberated, frontier halls) that have no event site.
    ServerTickEvents.END_SERVER_TICK.register(server ->
      achievementManager.tick(server)
    );

    // THE INCOMPLETE FILE props: click the ledger barrel / portrait chest to "find" the
    // document. Registered BEFORE LootChest so it wins at the portrait chest during the
    // pickup window (a non-PASS result short-circuits later UseBlock handlers).
    UseBlockCallback.EVENT.register(docPropManager::onUse);

    // Unplaced-chest loot: intercept chest opens; track hand-placed chests.
    UseBlockCallback.EVENT.register(lootChestManager::onChestUse);

    // Safari bait scatter: right-click ground with a ci_bait-marked item. Fast PASS
    // for anything else, so ordering after docprop/lootchest is safe.
    UseBlockCallback.EVENT.register(safariManager::onUseBlock);

    // Homestead: feed a nether star to a claimed beacon to unlock its top tier. Fast PASS
    // unless the held item is a nether star AND the block is a registered homestead beacon.
    UseBlockCallback.EVENT.register((player, level, hand, hit) ->
      homesteadManager.onUseBlock(player, level, hand, hit)
    );
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
      safariManager.onServerStarted(server); // lifetime stats + stray-lure sweep
      frontierManager.onServerStarted(server); // factory custody load
      daycareManager.load(server);
      homesteadManager.load(server);
      momCareManager.load(server);
      flavorConfig.pushToScoreboard(server); // publish datapack toggles to ci_flavor
      flavorConfig.applyGymGateTags(server); // open/close the gym MC gate per the toggle
      // Standalone guarantee: force rctmod's allowOverLeveling + our series into its live
      // config cache on ANY world (bundled map bakes it; fresh/bare worlds get defaults
      // that would re-enable rctmod's clamp and fight our badge ladder). SERVER_STARTED is
      // after the per-world serverconfig loads, before any player joins.
      com.thecompanyinc.cobblemoninitiative.compat.RctmodServerConfig.healServerConfig(server);
      LOGGER.info("Loaded player progress data.");
    });

    // S2C/C2S payloads (party-picker flow) — types must register before any join.
    com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads.register();

    // Nickname ritual: capture-side hook (LOWEST — after the dupes clause settles).
    // The gift/trade side rides the giveProperties choke point in the command class.
    com.thecompanyinc.cobblemoninitiative.nickname.NicknameManager.registerEvents();

    // Migrate players saved under the wrong/empty rctmod series (new players are placed by
    // the healed initialSeries; this only fixes pre-existing stat.dat).
    net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
      (handler, sender, server) -> {
        com.thecompanyinc.cobblemoninitiative.compat.RctmodServerConfig.ensurePlayerSeries(handler.player);
        snapFirstJoinToSpawn(handler.player, server);
        flavorConfig.applyGymGateTag(handler.player); // gym MC gate on/off for the joiner
        // Post-Board endgame: keep the Founder mirror tracking the live party across
        // sessions (silent; the dialog-button refresh covers the fight itself).
        if (progressManager.getProgress(handler.player).hasAchievement("board_cleared")) {
          com.thecompanyinc.cobblemoninitiative.founder.FounderMirrorManager.refresh(handler.player);
        }
        frontierManager.onPlayerJoin(handler.player); // parked-party reminder
        resendOwedVictoryWatchers(handler.player); // self-heal a watcher lost to a mid-window relog
        // Silently backfill every achievement this (possibly deep) save already qualifies for,
        // then arm the live-toast path. Runs after progress is loaded (SERVER_STARTED precedes
        // any join) and before the player acts, so nothing retroactive toasts.
        achievementManager.onPlayerJoin(handler.player, server);
      });

    net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
      (handler, server) ->
        achievementManager.onPlayerDisconnect(handler.player.getUUID()));

    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      progressManager.saveProgress(server);
      shrineChallengeManager.savePaths();
      lootChestManager.save();
      safariManager.onServerStopping(server); // forfeit live sessions + save stats
      frontierManager.onServerStopping(server); // factory custody save
      questTrackManager.save(server); // also hands the ▶-highlighted lines back
      daycareManager.save(); // belt-and-braces — custody already write-through saves
      homesteadManager.save();
      momCareManager.save();
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

  /** Gym-leader id → the post-win "watcher" cutscene that pans up to the shadow figure. */
  private static final java.util.Map<String, String> LEADER_VICTORY_WATCHERS = java.util.Map.of(
    "takehara_leader", "takehara_victory_watcher");

  /** The scenes we may hand to a client — validated inside {@link #playVictoryWatcherConfirmed}
   * so a stale/forged C2S bounce can never run an arbitrary cutscene. */
  private static final java.util.Set<String> VICTORY_WATCHER_SCENES =
    java.util.Set.copyOf(LEADER_VICTORY_WATCHERS.values());

  private static String victoryWatcherSeenTag(String scene) { return "ci_seen_" + scene; }

  /**
   * After a gym leader falls, pan up to the all-black figure watching from outside the glass
   * (note 20). One-shot per leader.
   *
   * <p>The scene is NOT played here: BATTLE_VICTORY fires while Cobblemon's battle GUI still
   * owns the client camera, so a spectator cutscene started now climbs UNSEEN (the reveal read
   * as "it doesn't climb after the battle"). Instead we hand the scene to the client, which
   * holds it until its battle GUI has closed ({@code getBattle()==null} — the same gate the
   * nickname prompt uses) and then bounces a C2S back so the server plays it, exactly like the
   * {@code /cutscene play} command path.
   *
   * <p>The one-shot latch ({@code ci_seen_<scene>}) is committed only when the scene ACTUALLY
   * plays ({@link #playVictoryWatcherConfirmed}) — never here at send time — so a disconnect in
   * the send→play window doesn't silently burn the beat; {@link #resendOwedVictoryWatchers}
   * re-offers it on the next join.
   */
  private static void maybePlayVictoryWatcher(ServerPlayer player, String leaderId) {
    String scene = LEADER_VICTORY_WATCHERS.get(leaderId);
    if (scene == null) return;
    if (player.getTags().contains(victoryWatcherSeenTag(scene))) return; // already played
    com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads.sendVictoryWatcher(player, scene);
  }

  /**
   * JOIN re-arm: re-offer any victory-watcher the player is OWED (leader defeated) but hasn't
   * SEEN — self-heals a scene lost when a disconnect interrupted the send→play round-trip. The
   * leader's TBCS onwin sets {@code defeated_<leaderId>} (persistent), so "owed" survives relog.
   */
  static void resendOwedVictoryWatchers(ServerPlayer player) {
    for (java.util.Map.Entry<String, String> e : LEADER_VICTORY_WATCHERS.entrySet()) {
      if (player.getTags().contains("defeated_" + e.getKey())
          && !player.getTags().contains(victoryWatcherSeenTag(e.getValue()))) {
        com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads.sendVictoryWatcher(player, e.getValue());
      }
    }
  }

  /**
   * Play a victory-watcher scene the client has confirmed is safe to run (its battle GUI is
   * gone). Validates the id against {@link #VICTORY_WATCHER_SCENES}, then latches the one-shot
   * tag ONLY on a successful start — a refused play (dead player, missing manager) stays owed
   * and re-arms next join. Idempotent: a second bounce for an already-seen scene is a no-op.
   */
  public static void playVictoryWatcherConfirmed(ServerPlayer player, String scene) {
    if (player == null || scene == null || !VICTORY_WATCHER_SCENES.contains(scene)) {
      if (player != null) {
        LOGGER.warn("[VictoryWatcher] Rejected unknown scene '{}' from {}.",
          scene, player.getName().getString());
      }
      return;
    }
    String seenTag = victoryWatcherSeenTag(scene);
    if (player.getTags().contains(seenTag)) return; // already played
    var manager = com.thecompanyinc.cobblemoninitiative.cutscene.CutsceneInit.getManager();
    if (manager != null && manager.play(player, scene)) {
      player.addTag(seenTag); // latch on CONFIRM (successful start), not on send
    }
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
        // Fires before Cobblemon's party add, but the prompt only sends a uuid —
        // the C2S reply resolves against the store later, by which time it's in.
        com.thecompanyinc.cobblemoninitiative.nickname.NicknameManager.promptFor(
          starterPlayer, event.getPokemon());
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
          // State the ACTUAL next requirement (badge, Champion, or Board) instead of
          // always "the next badge" — the latter is wrong in the Champion (cap 80→85)
          // and Board (cap 85→100) windows where no badge is left to earn.
          String next = levelCapManager.getNextLevelCapRequirement(sp);
          String suffix = "Max level reached!".equals(next)
            ? "§6 — max level." : "§6 — next: §e" + next + "§6.";
          sp.displayClientMessage(
            Component.literal("§6Level cap §e" + cap + suffix), true);
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

    // Capture clamp: a caught Pokemon that comes in ABOVE the current level cap is lowered to
    // the cap; a below-cap catch is left as-is (the cap is a CEILING, not a floor). Nobles are
    // EXEMPT — a boss catch (e.g. a lv70 Ho-Oh) keeps its own level (flagged in
    // NobleEncounterManager.swapBody). Gifts never reach here: givemon/giveProperties does not
    // fire POKEMON_CAPTURED. LOWEST so Nuzlocke's NORMAL handler (duplicate release / send-to-PC)
    // has already settled the outcome before we touch the level.
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, event -> {
      var mon = event.getPokemon();
      // Null store coordinates ⇒ Nuzlocke's NORMAL handler already RELEASED this catch as a
      // duplicate this tick — do not touch a released mon (mirrors NicknameManager's guard).
      if (mon.getStoreCoordinates().get() == null) {
        return Unit.INSTANCE;
      }
      if (NobleEncounterManager.isNoblePokemon(mon.getUuid())) {
        return Unit.INSTANCE;
      }
      ServerPlayer sp = event.getPlayer();
      if (sp != null && levelCapManager != null) {
        int cap = levelCapManager.getLevelCap(sp);
        if (mon.getLevel() > cap) {
          mon.setLevel(cap);
          sp.displayClientMessage(
            Component.literal("§6Your catch settled to the level cap §e" + cap + "§6."), true);
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

            // Resolve the config trainer for this loser. EXACT match (displayName or
            // name) wins over any substring match, and among substring candidates the
            // LONGEST displayName wins — so a title-prefix name ("Senior Agent",
            // "Field Agent") can never shadow the full name that contains it
            // ("Senior Agent Osamu"), and a short §k board nameplate ("M§kaaa") never
            // prefix-shadows a longer sibling ("M§kaaaaaaaaa"). Without this, board
            // defeats mis-credited (cap 100 never unlocked) and grunt/kalahar rewards
            // landed on the wrong id.
            TrainerConfig matched = null;
            for (TrainerConfig trainer : configLoader.getAllTrainers()) {
              if (
                trainer.getDisplayName().equalsIgnoreCase(trainerName) ||
                trainer.getName().equalsIgnoreCase(trainerName)
              ) {
                matched = trainer;
                break;
              }
            }
            if (matched == null) {
              for (TrainerConfig trainer : configLoader.getAllTrainers()) {
                String dn = trainer.getDisplayName();
                if (
                  dn != null && !dn.isEmpty() && trainerName.contains(dn) &&
                  (matched == null || dn.length() > matched.getDisplayName().length())
                ) {
                  matched = trainer;
                }
              }
            }
            if (matched != null) {
              LOGGER.info(
                "Player {} defeated trainer {} ({})",
                player.getName().getString(),
                matched.getDisplayName(),
                matched.getId()
              );
              progressManager.onTrainerDefeated(player, matched.getId());

              // Note 20: after a gym leader falls, pan up to the shadow figure watching
              // from outside the glass. One-shot per leader.
              maybePlayVictoryWatcher(player, matched.getId());

              // Keep rctmod's native series graph in step (round 10): players enter
              // the cobblemon-initiative series via initialSeries, but tbcs battles
              // bypass rctmod entirely — its defeat memory never advances on its own.
              // Only gym apprentices + leaders are graph nodes (mobs/trainers/single).
              // Enforcement stays OURS (EXPERIENCE clamp above; allowOverLeveling on):
              // rctmod's next-key-trainer cap model can't express the badge ladder.
              String type = matched.getTrainerType();
              if ("apprentice".equals(type) || "leader".equals(type)) {
                var server = player.getServer();
                if (server != null) {
                  // Grammar (bytecode, round 10b): targets go BEFORE the after
                  // literal — `player add progress <targets> after <trainerId>`.
                  // `after` marks the id + all its graph prerequisites.
                  server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(),
                    "rctmod player add progress " + player.getGameProfile().getName()
                      + " after " + matched.getId());
                }
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

  public static DaycareManager getDaycareManager() {
    return daycareManager;
  }

  public static com.thecompanyinc.cobblemoninitiative.frontier.FrontierManager getFrontierManager() {
    return frontierManager;
  }

  public static SafariManager getSafariManager() {
    return safariManager;
  }

  public static MinecraftFlavorConfig getFlavorConfig() {
    return flavorConfig;
  }

  /** Re-read the flavor config from disk (the /reload command) and re-publish its toggles. */
  public static void reloadFlavorConfig(net.minecraft.server.MinecraftServer server) {
    flavorConfig = MinecraftFlavorConfig.load();
    if (server != null) {
      flavorConfig.pushToScoreboard(server);
      flavorConfig.applyGymGateTags(server);
    }
  }

  public static HomesteadManager getHomesteadManager() {
    return homesteadManager;
  }

  public static MomCareManager getMomCareManager() {
    return momCareManager;
  }

  public static AchievementManager getAchievementManager() {
    return achievementManager;
  }
}

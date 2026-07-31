package com.thecompanyinc.cobblemoninitiative;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.stadium.StadiumManager;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncEvents;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncInit;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuzlockeInit implements ModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiativeInit.MOD_ID);

  /**
   * Player scoreboard tag marking a safe-exhibition zone where Nuzlocke attrition is
   * suspended (no faint damage, no party removal, no whiteout). Maintained per-tick by the
   * {@code cobblemon_initiative:frontier/region_tick} datapack function, which adds the tag
   * inside the Battle Frontier AABB and removes it everywhere else — so it self-corrects and
   * can never leak to disable Nuzlocke off the plateau. Mirrors {@link StadiumManager}'s
   * clone-party guard for the Frontier's opt-in above-cap grind.
   */
  private static final String FRONTIER_ACTIVE_TAG = "frontier_active";

  /**
   * alpha.26 flee cooldown: per-player scoreboard armed to {@link #FLEE_COOLDOWN_TICKS} when
   * the player FLEES a trainer battle. The {@code cobblemon_initiative:combat/flee_cooldown}
   * datapack tick decrements it and maintains the inverse {@code no_recent_flee} PLAYER_TAG
   * that compiled {@code engage:touch} presets gate their forced ON_DISTANCE_VERY_CLOSE
   * battle (and CLOSE hail) on — without the gate the trainer re-forced the fight on the very
   * next band tick while the fleeing player was still inside the 4-block band.
   * {@code NpcSightManager} also reads this objective to stand a PURSUE-mode chaser down.
   */
  private static final String FLEE_COOLDOWN_OBJ = "ci_flee_cd";

  /** 5 minutes at 20 tps — playtest ruling (0.7.0-alpha.3): fleeing buys a genuinely long
   *  reprieve before the corridor trainers re-force the fight (the old 15 s expired while
   *  the player was still fumbling out of the 4-block band). Voluntary re-engage via the
   *  dialog battle button stays available throughout — only the forced path waits. */
  private static final int FLEE_COOLDOWN_TICKS = 6000;

  private static NuzlockeConfig config;
  private static boolean pendingWhiteoutDeath = false;
  private static boolean pendingSacrifice = false;
  /** Per-player post-revive grace: while the player's world gameTime is below the stored value,
   *  a Nuzlocke whiteout will not fire {@code player.kill()} on them. Opened by the Dishonorable
   *  Respawn path so a queued same-battle re-entry can't re-kill the revived player. */
  private static final Map<UUID, Long> whiteoutGraceUntil = new ConcurrentHashMap<>();
  /** Players whose imminent {@code player.kill()} is a Nuzlocke whiteout (marked by
   *  {@link #whiteoutKill}, consumed by the AFTER_DEATH hook) — so the blackout is not
   *  ALSO reported as a natural {@code player_death} (which would double the grave). */
  private static final java.util.Set<UUID> pendingWhiteoutDeaths = ConcurrentHashMap.newKeySet();
  private static final Map<UUID, String> playerZones = new ConcurrentHashMap<>();
  private static int announceTick = 0;
  private static final Random URGE_RANDOM = new Random();
  private static final Map<UUID, Long> lastUrgeTick = new ConcurrentHashMap<>();

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Nuzlocke mechanics...");

    config = NuzlockeConfig.load();
    config.save();

    CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, NuzlockeInit::handleBattleFainted);
    CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, NuzlockeInit::handleBattleFled);
    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, NuzlockeInit::handleBattleVictory);
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, NuzlockeInit::handlePokemonCaptured);

    // A genuinely NEW battle ends the post-revive grace immediately — this is what makes a
    // long grace window safe: every battle event arriving during grace can only be frozen
    // fallout of the battle the player already died in, never a fresh fight's outcome.
    CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, event -> {
      for (ServerPlayer battlePlayer : event.getBattle().getPlayers()) {
        if (whiteoutGraceUntil.remove(battlePlayer.getUUID()) != null) {
          LOGGER.info(
            "Post-revive grace ended for {} — new battle started",
            battlePlayer.getName().getString());
        }
      }
      return Unit.INSTANCE;
    });

    // Record the TRAINER's own hardcore deaths for the stream overlay's graveyard. A
    // whiteout is already reported (richer) by its own event just before player.kill(),
    // so it is filtered here; everything else (fall, lava, mob, drowning, …) ships as a
    // natural player_death carrying the DamageSource's cause + message + killer.
    ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
      if (entity instanceof ServerPlayer sp) {
        if (pendingWhiteoutDeaths.remove(sp.getUUID())) return;
        StreamSyncEvents.playerDeath(sp, source);
      }
    });

    ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (++announceTick % Math.max(1, config.getZoneCheckCadenceTicks()) != 0) return;
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
        checkZoneTransition(player);
        suppressSafeZonePhantoms(player);
      }
    });

    // Post-respawn takeover: the tick AFTER a respawn command runs — once the player has actually
    // spawned in — apply the mod's game-mode change (see queueRespawnTakeover). This is what lets the
    // Dishonorable Respawn go through the exact Die-with-Honor spawn-in and only THEN claw the player
    // back to survival, instead of racing the respawn packet inside the command.
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (pendingRespawnTakeover.isEmpty()) return;
      var it = pendingRespawnTakeover.entrySet().iterator();
      while (it.hasNext()) {
        var e = it.next();
        ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
        if (p == null) { it.remove(); continue; } // disconnected before takeover — drop it
        RespawnTakeover t = e.getValue();
        p.setGameMode(t.mode);
        if (t.restore) {
          p.setHealth(p.getMaxHealth());
          p.getFoodData().setFoodLevel(20);
          p.level().playSound(null, p.blockPosition(),
            net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
            net.minecraft.sounds.SoundSource.MASTER, 0.4f, 0.6f);
        }
        it.remove();
      }
    });

    registerCommands();

    LOGGER.info("Nuzlocke mechanics initialized!");
  }

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register(
      (dispatcher, registryAccess, environment) -> {
        dispatcher.register(
          Commands.literal("nuzlocke")
            .then(
              // OP-only test hook: both of these end a hardcore run outright, so they
              // must never be reachable at permission 0 (a one-keystroke run-ender in
              // single-player). Kept for the dev harness; strip with the dev tooling.
              Commands.literal("deathscreen")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                  var player = context.getSource().getPlayerOrException();
                  player.sendSystemMessage(Component.literal("§7[dev] Forcing whiteout."));
                  // Route through whiteoutKill so the forced whiteout is a real one — it fires the
                  // whiteout event + stat + AFTER_DEATH marker, so the stream overlay records a
                  // whiteout stone, not a mislabeled genericKill "natural death".
                  whiteoutKill(player, StreamSyncEvents.REASON_FAINT);
                  return 1;
                })
            )
            .then(
              Commands.literal("sacrifice")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                  var player = context.getSource().getPlayerOrException();
                  PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
                  int partyCount = countPartySize(party);

                  if (partyCount <= 1) {
                    player.sendSystemMessage(
                      Component.literal("§7[dev] One Pokémon left — forcing whiteout instead.")
                    );
                    // Same as /nuzlocke deathscreen: a real whiteout (event + stat + marker), not
                    // a bare kill that the overlay would mislabel as a natural death.
                    whiteoutKill(player, StreamSyncEvents.REASON_FAINT);
                  } else {
                    player.sendSystemMessage(Component.literal("§7[dev] Opening sacrifice selection."));
                    pendingSacrifice = true;
                  }
                  return 1;
                })
            )
            .then(
              Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                  reloadConfig();
                  context.getSource().sendSuccess(() -> Component.literal("§aConfig reloaded!"), true);
                  return 1;
                })
            )
        );

        dispatcher.register(
          Commands.literal("safezone")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.literal("add").then(
                Commands.argument("name", StringArgumentType.word()).then(
                  Commands.argument("radius", IntegerArgumentType.integer(1, 500)).then(
                    Commands.argument("hostileOnly", BoolArgumentType.bool()).then(
                      Commands.argument("cylindrical", BoolArgumentType.bool()).executes(context -> {
                        var source = context.getSource();
                        var player = source.getPlayerOrException();
                        String name = StringArgumentType.getString(context, "name");
                        int radius = IntegerArgumentType.getInteger(context, "radius");
                        boolean hostileOnly = BoolArgumentType.getBool(context, "hostileOnly");
                        boolean isCylindrical = BoolArgumentType.getBool(context, "cylindrical");

                        String dimension = player.level().dimension().location().toString();
                        int x = player.getBlockX();
                        int y = player.getBlockY();
                        int z = player.getBlockZ();

                        NuzlockeConfig.SafeZone zone = new NuzlockeConfig.SafeZone(
                          name, dimension, x, y, z, radius, hostileOnly, isCylindrical
                        );
                        config.addSafeZone(zone);

                        source.sendSuccess(
                          () -> Component.literal(
                            "§aCreated safe zone '" + name + "' at " + x + ", " + y + ", " + z
                            + " with radius " + radius
                            + " (hostile only: " + hostileOnly
                            + ") (cylindrical: " + isCylindrical + ")"
                          ),
                          true
                        );
                        return 1;
                      })
                    )
                  )
                )
              )
            )
            .then(
              Commands.literal("remove").then(
                Commands.argument("name", StringArgumentType.word()).executes(context -> {
                  String name = StringArgumentType.getString(context, "name");
                  boolean removed = config.removeSafeZone(name);
                  if (removed) {
                    context.getSource().sendSuccess(
                      () -> Component.literal("§aRemoved safe zone '" + name + "'"), true
                    );
                  } else {
                    context.getSource().sendFailure(
                      Component.literal("§cSafe zone '" + name + "' not found")
                    );
                  }
                  return removed ? 1 : 0;
                })
              )
            )
            .then(
              Commands.literal("list").executes(context -> {
                var zones = config.getSafeZones();
                if (zones.isEmpty()) {
                  context.getSource().sendSuccess(
                    () -> Component.literal("§7No safe zones defined"), false
                  );
                } else {
                  context.getSource().sendSuccess(
                    () -> Component.literal("§6Safe Zones (" + zones.size() + "):"), false
                  );
                  for (var zone : zones) {
                    context.getSource().sendSuccess(
                      () -> Component.literal(
                        "§7- " + zone.name + ": " + zone.centerX + ", " + zone.centerY + ", "
                        + zone.centerZ + " r=" + zone.radius + " (" + zone.dimension + ")"
                        + (zone.preventHostileOnly ? " [hostile only]" : " [all mobs]")
                      ),
                      false
                    );
                  }
                }
                return zones.size();
              })
            )
        );
      }
    );
  }

  // ---------------------------------------------------------------------------
  // Cobblemon event handlers
  // ---------------------------------------------------------------------------

  private static Unit handlePokemonCaptured(PokemonCapturedEvent event) {
    Pokemon pokemon = event.getPokemon();
    ServerPlayer player = event.getPlayer();
    String speciesName = pokemon.getSpecies().getName();

    NuzlockeConfig.DuplicateHandling handling = config.getDuplicateHandling();

    if (handling != NuzlockeConfig.DuplicateHandling.OFF) {
      boolean shouldRelease = false;

      if (handling == NuzlockeConfig.DuplicateHandling.RELEASE_IF_OWNED) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon partyPokemon : party) {
          if (
            partyPokemon != null &&
            partyPokemon.getSpecies().getName().equalsIgnoreCase(speciesName) &&
            partyPokemon != pokemon
          ) {
            shouldRelease = true;
            break;
          }
        }
        if (!shouldRelease) {
          PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
          for (Pokemon pcPokemon : pc) {
            // Self-exclusion matters here too: a catch with a FULL party overflows
            // straight into the PC before this event fires, so without it the mon
            // matches itself and every full-party capture reads as a duplicate.
            if (
              pcPokemon != null &&
              pcPokemon.getSpecies().getName().equalsIgnoreCase(speciesName) &&
              pcPokemon != pokemon
            ) {
              shouldRelease = true;
              break;
            }
          }
        }
      } else if (handling == NuzlockeConfig.DuplicateHandling.RELEASE_IF_EVER_CAUGHT) {
        if (config.hasEverCaught(speciesName)) {
          shouldRelease = true;
        }
      }

      if (shouldRelease) {
        // Release from whichever store actually holds the mon — a full-party catch
        // lives in the overflow PC, where party.remove() silently no-ops (wrong
        // store) and the mon would stay owned while chat announces the release.
        // Removing via the coordinates also nulls them, which is the exact premise
        // the nickname prompt's released-dupe guard reads.
        var coords = pokemon.getStoreCoordinates().get();
        if (coords != null) coords.remove();
        StreamSyncEvents.pokemonLost(player, pokemon, StreamSyncEvents.CAUSE_DUPLICATE_RELEASE);
        player.sendSystemMessage(
          Component.literal("§e" + speciesName + " was automatically released (duplicate species).")
        );
        LOGGER.info("Auto-released duplicate {} for player {}", speciesName, player.getName().getString());
        return Unit.INSTANCE;
      }
    }

    config.addCaughtSpecies(speciesName);

    if (config.isSetCaughtToZeroHP()) {
      pokemon.setCurrentHealth(0);
      player.sendSystemMessage(Component.literal("§7" + speciesName + " arrived fainted..."));
    }

    if (config.isSendCaughtToPC()) {
      PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
      PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);

      if (party.remove(pokemon)) {
        var pcPosition = pc.getFirstAvailablePosition();
        if (pcPosition != null) {
          pc.set(pcPosition, pokemon);
          player.sendSystemMessage(Component.literal("§a" + speciesName + " was sent to your PC."));
        } else {
          party.add(pokemon);
          player.sendSystemMessage(
            Component.literal("§cPC is full! " + speciesName + " was added to your party.")
          );
        }
      } else {
        // A full-party catch never entered the party — Cobblemon overflowed it to
        // the PC already, so there is nothing to re-file, only to announce.
        player.sendSystemMessage(Component.literal("§a" + speciesName + " was sent to your PC."));
      }
    }

    LOGGER.info("Player {} captured {}", player.getName().getString(), speciesName);
    return Unit.INSTANCE;
  }

  private static Unit handleBattleFled(BattleFledEvent event) {
    PlayerBattleActor playerActor = event.getPlayer();
    ServerPlayer player = playerActor.getEntity();
    if (player == null) return Unit.INSTANCE;

    // alpha.26 flee cooldown — arm BEFORE any sacrifice-config early return: the grace
    // window is an anti-re-engage mechanic, not a Nuzlocke one. Trainer battles only
    // (NPC actor present — same detection handleBattleVictory uses); fleeing a wild
    // encounter needs no grace.
    boolean wasTrainerBattle = false;
    for (BattleActor actor : event.getBattle().getActors()) {
      if (actor.getType() == ActorType.NPC) {
        wasTrainerBattle = true;
        break;
      }
    }
    if (wasTrainerBattle) startFleeCooldown(player);

    // alpha.2 leak fix: BATTLE_FLED fires neither onwin branch, so a fled trigger-forced
    // battle left in_trainer_battle on the player forever — permanently standing down
    // every pursue trainer on the map. Clear it here; victory/loss keep their onwin clears.
    player.removeTag("in_trainer_battle");

    // Post-revive grace: this fled event is frozen fallout of the battle the player already
    // died in (the death screen pauses the SP server; a new battle revokes the grace at
    // start) — no sacrifice, no run-ender. The cooldown/tag maintenance above still applies.
    if (inWhiteoutGrace(player)) {
      LOGGER.info(
        "Suppressed stale flee fallout for {} — inside post-revive grace",
        player.getName().getString());
      return Unit.INSTANCE;
    }

    if (!config.isSacrificeOnFlee()) return Unit.INSTANCE;

    // Stadium exhibition runs are attrition-free: battles use CLONED parties and the
    // StadiumManager (subscribed at Priority.LOWEST, i.e. after this) owns the outcome.
    // The Battle Frontier is a safe exhibition too: the frontier_active tag (maintained by
    // the frontier/region_tick datapack function while the player stands on the plateau)
    // marks the "nothing you love dies on our floor" zone.
    if (StadiumManager.isStadiumActive(player.getUUID())
        || player.getTags().contains(FRONTIER_ACTIVE_TAG) || isNobleActive(player)) return Unit.INSTANCE;

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int partyCount = countPartySize(party);

    if (partyCount <= 1) {
      player.sendSystemMessage(
        Component.literal("§4You fled with only one Pokémon! There is no escape...")
      );
      whiteoutKill(player, StreamSyncEvents.REASON_FLEE);
      LOGGER.info("Player {} fled with only one Pokemon - killed", player.getName().getString());
      return Unit.INSTANCE;
    }

    player.sendSystemMessage(
      Component.literal("§cYou fled from battle! You must sacrifice a Pokémon...")
    );
    pendingSacrifice = true;
    LOGGER.info("Player {} fled from battle, sacrifice required", player.getName().getString());
    return Unit.INSTANCE;
  }

  /**
   * Set {@code ci_flee_cd} = {@link #FLEE_COOLDOWN_TICKS} on the fleeing player. The
   * objective is created defensively here (mirrors {@code NpcSightManager.updateScoreboard});
   * the {@code combat/load} datapack function also registers it on world load. The one-shot
   * actionbar explains the grace window — the tick function cannot know the cooldown just
   * STARTED, so the start beat lives here.
   */
  private static void startFleeCooldown(ServerPlayer player) {
    try {
      if (player.getServer() == null) return;
      Scoreboard sb = player.getServer().getScoreboard();
      Objective obj = sb.getObjective(FLEE_COOLDOWN_OBJ);
      if (obj == null) {
        obj = sb.addObjective(
          FLEE_COOLDOWN_OBJ,
          ObjectiveCriteria.DUMMY,
          Component.literal(FLEE_COOLDOWN_OBJ),
          ObjectiveCriteria.RenderType.INTEGER,
          false,
          null
        );
      }
      sb.getOrCreatePlayerScore(player, obj).set(FLEE_COOLDOWN_TICKS);
      player.connection.send(new ClientboundSetActionBarTextPacket(
        Component.literal("§7They lost track of you — move.")
      ));
      LOGGER.info(
        "Player {} fled a trainer battle — flee cooldown armed ({} ticks)",
        player.getName().getString(), FLEE_COOLDOWN_TICKS
      );
    } catch (Exception e) {
      LOGGER.warn("Failed to arm flee cooldown: {}", e.getMessage());
    }
  }

  private static Unit handleBattleVictory(BattleVictoryEvent event) {
    if (!config.isSacrificeOnFlee()) return Unit.INSTANCE;
    if (event.getWasWildCapture()) return Unit.INSTANCE;

    for (BattleActor loser : event.getLosers()) {
      if (loser.getType() != ActorType.PLAYER) continue;
      if (!(loser instanceof PlayerBattleActor playerActor)) continue;

      ServerPlayer player = playerActor.getEntity();
      if (player == null) continue;

      // Post-revive grace: a loss delivered during grace is the already-died-in battle
      // resolving after the death-screen pause — never a fresh forfeit.
      if (inWhiteoutGrace(player)) {
        LOGGER.info(
          "Suppressed stale battle-loss fallout for {} — inside post-revive grace",
          player.getName().getString());
        continue;
      }

      // Stadium runs: losing an exhibition wave is not a forfeit — the battle party is
      // a clone (real party often healthy), so this branch would whiteout-kill a player
      // who lost nothing. StadiumManager ends the run after this handler returns.
      // The Battle Frontier (frontier_active tag) is likewise a no-forfeit safe exhibition.
      if (StadiumManager.isStadiumActive(player.getUUID())
          || player.getTags().contains(FRONTIER_ACTIVE_TAG) || isNobleActive(player)) continue;

      boolean wasTrainerBattle = false;
      for (BattleActor actor : event.getBattle().getActors()) {
        if (actor.getType() == ActorType.NPC) {
          wasTrainerBattle = true;
          break;
        }
      }
      if (!wasTrainerBattle) continue;

      PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
      int remainingPokemon = 0;
      for (Pokemon pokemon : party) {
        if (pokemon != null && !pokemon.isFainted()) remainingPokemon++;
      }

      if (remainingPokemon > 0) {
        LOGGER.info(
          "Player {} forfeited trainer battle with {} Pokemon remaining",
          player.getName().getString(), remainingPokemon
        );

        if (remainingPokemon == 1) {
          player.sendSystemMessage(
            Component.literal("§4You forfeited with only one Pokémon! There is no escape...")
          );
          whiteoutKill(player, StreamSyncEvents.REASON_FORFEIT);
        } else {
          player.sendSystemMessage(
            Component.literal("§cYou forfeited the battle! You must sacrifice a Pokémon...")
          );
          NuzlockeClientInit.triggerSacrificeSelection();
        }
      }
    }

    return Unit.INSTANCE;
  }

  private static Unit handleBattleFainted(BattleFaintedEvent event) {
    var battle = event.getBattle();
    var faintedPokemon = event.getKilled();

    BattleActor ownerActor = null;
    for (BattleActor actor : battle.getActors()) {
      for (var bp : actor.getPokemonList()) {
        if (bp.getUuid().equals(faintedPokemon.getUuid())) {
          ownerActor = actor;
          break;
        }
      }
      if (ownerActor != null) break;
    }

    if (ownerActor == null || ownerActor.getType() != ActorType.PLAYER) return Unit.INSTANCE;

    boolean hasWildOpponent = false;
    boolean hasNpcOpponent = false;
    for (BattleActor actor : battle.getActors()) {
      if (actor.getType() == ActorType.WILD) hasWildOpponent = true;
      if (actor.getType() == ActorType.NPC) hasNpcOpponent = true;
    }

    if (hasWildOpponent && !config.isApplyInWildBattles()) return Unit.INSTANCE;
    if (hasNpcOpponent && !config.isApplyInTrainerBattles()) return Unit.INSTANCE;

    if (!(ownerActor instanceof PlayerBattleActor playerActor)) return Unit.INSTANCE;

    ServerPlayer player = playerActor.getEntity();
    if (player == null) return Unit.INSTANCE;

    // Post-revive grace: faint fallout arriving during grace belongs to the battle the
    // player already whiteouted in — the death screen pauses the SP server and Cobblemon
    // paces the frozen messages out over several seconds after revive. Without this gate
    // the queued faints re-apply hurt() damage (which the whiteout-only grace check never
    // covered) and can flat-out kill the freshly-revived player.
    if (inWhiteoutGrace(player)) {
      LOGGER.info(
        "Suppressed stale faint fallout for {} — inside post-revive grace",
        player.getName().getString());
      return Unit.INSTANCE;
    }

    // Stadium exhibition faints are clone faints — no damage, no removal, no whiteout.
    // Battle Frontier faints (frontier_active tag) are exhibition faints too: the safe
    // exhibition suppresses damage/removal/whiteout so an optional above-cap grind can
    // never gut a hardcore-Nuzlocke box.
    if (StadiumManager.isStadiumActive(player.getUUID())
        || player.getTags().contains(FRONTIER_ACTIVE_TAG) || isNobleActive(player)) return Unit.INSTANCE;

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int totalPartySize = countPartySize(party);
    int remainingAfterThis = countRemainingPokemon(party, faintedPokemon.getEffectedPokemon());

    float damageAmount = calculateDamage(player, totalPartySize, remainingAfterThis);
    String pokemonName = faintedPokemon.getEffectedPokemon().getSpecies().getName();

    if (config.isRemoveFaintedPokemon()) {
      Pokemon faintedPokemonObj = faintedPokemon.getEffectedPokemon();
      party.remove(faintedPokemonObj);
      // Resolve the KO'er only while streaming — keeps the disabled path zero-alloc.
      JsonObject killer = StreamSyncInit.getPusher() != null
        ? StreamSyncManager.describeKiller(event) : null;
      StreamSyncEvents.pokemonLost(player, faintedPokemonObj, StreamSyncEvents.CAUSE_FAINT, killer);
      LOGGER.info("Removed {} from {}'s party", pokemonName, player.getName().getString());
    }

    applyDamageToPlayer(player, damageAmount, pokemonName, remainingAfterThis == 0);
    // Signal a real Nuzlocke loss for the PokéPhone — Mom's worried call fires once on the first one.
    player.addTag("nuzlocke_lost_one");
    maybeFireDarkUrgeWhisper(player);
    return Unit.INSTANCE;
  }

  // ---------------------------------------------------------------------------
  // Dark Urge whispers
  // ---------------------------------------------------------------------------

  /**
   * On a Pokémon faint outside a safe zone, occasionally surfaces an intrusive
   * "shadow self" whisper. Pure flavour — never touches the faint/damage rules.
   * Chance- and cooldown-gated; tier escalates with the player's level cap.
   */
  private static void maybeFireDarkUrgeWhisper(ServerPlayer player) {
    if (config == null || !config.isEnableDarkUrgeWhispers()) return;

    // Never intrude inside a safe zone (towns/shrines) — the run is "at rest" there.
    String dim = player.level().dimension().location().toString();
    if (config.isInSafeZone(dim, player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getServer())) return;

    // Per-player cooldown (in-memory; harmlessly resets on relog).
    long now = player.level().getGameTime();
    Long last = lastUrgeTick.get(player.getUUID());
    if (last != null && now - last < config.getDarkUrgeCooldownTicks()) return;

    // Guarantee the FIRST whisper of the session (no prior fire) so the shadow-self mechanic
    // always introduces itself on stream; every subsequent faint rolls the chance normally.
    boolean firstEver = (last == null);
    if (!firstEver && URGE_RANDOM.nextFloat() >= config.getDarkUrgeChance()) return;

    List<List<String>> pool = config.getDarkUrgeMessages();
    if (pool == null || pool.isEmpty()) return;
    int tier = darkUrgeTier(player);
    if (tier < 0 || tier >= pool.size()) return;
    List<String> lines = pool.get(tier);
    if (lines == null || lines.isEmpty()) return;

    String line = lines.get(URGE_RANDOM.nextInt(lines.size()));
    lastUrgeTick.put(player.getUUID(), now);

    player.sendSystemMessage(
      Component.literal(line).setStyle(
        Style.EMPTY.withColor(TextColor.fromRgb(0x8B0000)).withItalic(true)
      )
    );
    player.level().playSound(
      null, player.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.MASTER, 0.6f, 0.5f
    );
  }

  /** Whisper escalation tier from the player's current level cap. */
  private static int darkUrgeTier(ServerPlayer player) {
    int cap = ProgressionConfig.get().getBaseLevelCap();
    try {
      cap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    } catch (Exception ignored) {
      // Initiative subsystem not ready / no progress yet — treat as the starting cap.
    }
    if (cap >= config.getDarkUrgeTier3LevelCap()) return 3; // gym 8+ — only after the gym-7 "charter" fragment
    if (cap >= config.getDarkUrgeTier2LevelCap()) return 2; // gyms 4-7
    if (cap >= config.getDarkUrgeTier1LevelCap()) return 1; // gyms 1-3
    return 0;                                               // pre-first-badge
  }

  // ---------------------------------------------------------------------------
  // Sacrifice
  // ---------------------------------------------------------------------------

  public static void sacrificePokemon(UUID playerUuid, UUID pokemonUuid) {
    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
    if (mc.getSingleplayerServer() == null) return;

    ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerUuid);
    if (player == null) return;

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (Pokemon pokemon : party) {
      if (pokemon != null && pokemon.getUuid().equals(pokemonUuid)) {
        String name = pokemon.getSpecies().getName();
        party.remove(pokemon);
        // CLIENT thread — the bus reads the pokemon here and only enqueues a finished JsonObject.
        StreamSyncEvents.pokemonLost(player, pokemon, StreamSyncEvents.CAUSE_SACRIFICE);
        player.sendSystemMessage(Component.literal("§c" + name + " was sacrificed for your escape!"));
        LOGGER.info("Sacrificed {} for player {}", name, player.getName().getString());
        break;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static int countPartySize(PlayerPartyStore party) {
    int count = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null) count++;
    }
    return Math.max(count, 1);
  }

  /**
   * True only when a noble fight is active AND knockout mode is on (lethal nobles off). In that
   * case the Nuzlocke death paths are suspended — the noble engine ends the encounter as a retreat
   * instead. In the default LETHAL mode this returns false, so losing a noble whites you out
   * normally. Mirrors the stadium / frontier exhibition guards.
   */
  private static boolean isNobleActive(net.minecraft.server.level.ServerPlayer player) {
    if (com.thecompanyinc.cobblemoninitiative.config.NobleConfig.get().isLethalNobleFights()) {
      return false;
    }
    var mgr = com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterInit.getManager();
    return mgr != null && mgr.hasActive(player.getUUID());
  }

  private static int countRemainingPokemon(PlayerPartyStore party, Pokemon justFainted) {
    int remaining = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null && pokemon != justFainted && !pokemon.isFainted()) remaining++;
    }
    return remaining;
  }

  private static float calculateDamage(ServerPlayer player, int totalPartySize, int remainingPokemon) {
    if (remainingPokemon == 0) return 0.0f; // unused — whiteout forces death via player.kill()

    float healthBase = config.isUseMaxHealth() ? player.getMaxHealth() : player.getHealth();

    float damage;
    if (config.isScaleDamageByPartySize() && totalPartySize > 0) {
      damage = healthBase / totalPartySize;
    } else {
      damage = player.getMaxHealth();
    }

    float minimumDamage = player.getMaxHealth() * config.getMinimumDamagePercent();
    return Math.max(damage, minimumDamage);
  }

  private static void applyDamageToPlayer(
    ServerPlayer player, float damage, String pokemonName, boolean isWhiteOut
  ) {
    String message;
    if (isWhiteOut) {
      String releaseText = config.isRemoveFaintedPokemon() ? " and was released" : "";
      message = "§4" + pokemonName + " fainted" + releaseText + "! You have no Pokémon left!";
      // Layer the shadow's ledger voice over the mechanical line — the whiteout is the
      // run's most-replayed moment and should sound like the Company, not vanilla.
      String[] whiteoutVoice = {
        "§8The ledger closes.",
        "§8The books close in red.",
        "§8Every name you spent brought you one step closer to your own.",
        "§8The Company thanks you for your service."
      };
      int wt = Math.max(0, Math.min(darkUrgeTier(player), whiteoutVoice.length - 1));
      player.sendSystemMessage(Component.literal(whiteoutVoice[wt]));
    } else {
      if (config.isRemoveFaintedPokemon()) {
        message = "§c" + pokemonName + " fainted and was released! You take damage!";
      } else {
        message = config.getDamageMessage().replace("%pokemon%", pokemonName);
      }
    }
    player.sendSystemMessage(Component.literal(message));
    if (isWhiteOut) {
      // Guaranteed, unblockable death — bypasses armor / absorption / Resistance. Routed through
      // whiteoutKill so a post-Dishonorable-Respawn grace can suppress a same-battle re-entry: a
      // same-turn multi-KO queues more than one whiteout kill, the first pops the death screen,
      // and the rest would land on the revived player a few seconds after they claw back.
      whiteoutKill(player, StreamSyncEvents.REASON_FAINT);
    } else {
      player.hurt(player.damageSources().generic(), damage);
    }
  }

  // ---------------------------------------------------------------------------
  // Whiteout kill + post-revive grace
  // ---------------------------------------------------------------------------

  /**
   * Fire the Nuzlocke run-ender on {@code player}, unless they are inside a post-revive grace
   * window (see {@link #grantWhiteoutGrace}). The death-screen flag and the StreamSync whiteout
   * event are set only when the kill actually goes through, so a suppressed re-entry leaves no
   * ghost death screen or double-counted whiteout behind.
   */
  private static void whiteoutKill(ServerPlayer player, String reason) {
    if (inWhiteoutGrace(player)) {
      LOGGER.info(
        "Whiteout kill for {} suppressed — inside post-revive grace",
        player.getName().getString());
      return;
    }
    pendingWhiteoutDeath = true;
    // Tag the resulting AFTER_DEATH as a whiteout so it is not re-reported as a natural
    // player_death — the whiteout event below is the run-ender's authoritative record.
    pendingWhiteoutDeaths.add(player.getUUID());
    StreamSyncEvents.whiteout(player, reason);
    player.kill();
  }

  /**
   * Open a window during which ALL Nuzlocke battle fallout (whiteout kill, faint damage +
   * party removal, flee/forfeit sacrifice) is suppressed for this player. Called from the
   * Dishonorable Respawn command: the death screen pauses the single-player server, freezing
   * the dying battle's remaining showdown messages — Cobblemon then paces them out over
   * several seconds after revive, where they would re-kill the clawed-back player (queued
   * whiteout kills, or plain faint {@code hurt()} damage that the old whiteout-only check
   * never covered). The window can be generous because {@code BATTLE_STARTED_POST} revokes
   * it the instant a NEW battle begins — a legitimate whiteout is never eaten, and
   * environmental damage still kills throughout.
   */
  public static void grantWhiteoutGrace(ServerPlayer player, int ticks) {
    whiteoutGraceUntil.put(player.getUUID(), player.level().getGameTime() + ticks);
  }

  // ---------------------------------------------------------------------------
  // Deferred post-respawn game-mode takeover
  // ---------------------------------------------------------------------------

  /** Player -> the game mode (and optional refill/brand) the mod applies one tick AFTER a respawn. */
  private static final java.util.Map<java.util.UUID, RespawnTakeover> pendingRespawnTakeover =
    new java.util.concurrent.ConcurrentHashMap<>();

  private static final class RespawnTakeover {
    final net.minecraft.world.level.GameType mode;
    final boolean restore; // dishonorable claw-back: refill health/food + play the takeover sting
    RespawnTakeover(net.minecraft.world.level.GameType mode, boolean restore) {
      this.mode = mode;
      this.restore = restore;
    }
  }

  /**
   * Queue the mod's post-spawn takeover for a just-respawned player. The mode switch (and, for the
   * dishonorable claw-back, the health/food refill + sting) is applied on the NEXT server tick — once
   * the respawn packet has been flushed and the player has spawned in — rather than synchronously
   * inside the respawn command. Deferring past the respawn tick is what makes Dishonorable Respawn go
   * through the exact same spawn-in as Die with Honor, and only THEN have the mod take over.
   */
  public static void queueRespawnTakeover(
      ServerPlayer player, net.minecraft.world.level.GameType mode, boolean restore) {
    pendingRespawnTakeover.put(player.getUUID(), new RespawnTakeover(mode, restore));
  }

  /** Drop a queued sacrifice prompt. Called on Dishonorable Respawn: a flee/forfeit event
   *  frozen behind the death screen may have flagged a sacrifice for a party that is now
   *  wiped — popping that screen on the revived player is meaningless and soft-locks UX. */
  public static void clearPendingSacrifice() {
    pendingSacrifice = false;
  }

  private static boolean inWhiteoutGrace(ServerPlayer player) {
    Long until = whiteoutGraceUntil.get(player.getUUID());
    return until != null && player.level().getGameTime() < until;
  }

  // ---------------------------------------------------------------------------
  // State consumers (polled by client tick / mixin)
  // ---------------------------------------------------------------------------

  public static boolean consumePendingWhiteoutDeath() {
    if (pendingWhiteoutDeath) {
      pendingWhiteoutDeath = false;
      return true;
    }
    return false;
  }

  public static boolean consumePendingSacrifice() {
    if (pendingSacrifice) {
      pendingSacrifice = false;
      return true;
    }
    return false;
  }

  public static void reloadConfig() {
    config = NuzlockeConfig.load();
    playerZones.clear();
    LOGGER.info("Nuzlocke config reloaded.");
  }

  public static NuzlockeConfig getConfig() {
    return config;
  }

  // ---------------------------------------------------------------------------
  // Zone entry / exit announcements
  // ---------------------------------------------------------------------------

  private static void checkZoneTransition(ServerPlayer player) {
    if (!config.isEnableAreaAnnouncements()) return;

    String dim = player.level().dimension().location().toString();
    int x = player.getBlockX();
    int y = player.getBlockY();
    int z = player.getBlockZ();

    NuzlockeConfig.SafeZone zone = config.getAnnouncedZoneAt(dim, x, y, z, player.getServer());
    String zoneName = zone != null ? zone.name : null;
    String prevName = playerZones.get(player.getUUID());

    if (Objects.equals(zoneName, prevName)) return;

    if (zoneName != null) {
      playerZones.put(player.getUUID(), zoneName);
      sendZoneEntry(player, zone);
      maybePlayShrineReveal(player, zoneName);
    } else {
      playerZones.remove(player.getUUID());
      // Only meaningful as a transition *out of* a named zone; prevName == null means the
      // player spawned/relogged in the wild, which we don't announce (avoids login spam).
      if (prevName != null) {
        if (config.isAnnounceWilderness()) {
          sendWilderness(player);
        } else if (config.isAnnounceOnExit()) {
          sendZoneExit(player, prevName);
        }
      }
    }
  }

  /**
   * Towns and shrines suppress natural hostile spawns, but MobSpawnMixin only guards
   * NaturalSpawner.spawnCategoryForPosition — the overworld's separate PhantomSpawner
   * bypasses it entirely, so with doInsomnia on, a sleepless player gets phantoms swooping
   * even inside a safe zone (the "after some time I just randomly was killed" playtest
   * report from Sango). While a player stands in a suppressing zone (mobsSpawn=false) we
   * reset their insomnia timer so nothing new targets them, and discard any phantom that
   * already spawned above town before it can land a hardcore-fatal swoop.
   */
  private static void suppressSafeZonePhantoms(ServerPlayer player) {
    if (config == null) return;
    String dim = player.level().dimension().location().toString();
    NuzlockeConfig.SafeZone zone = config.getSafeZoneAt(
      dim, player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getServer());
    if (zone == null || zone.mobsSpawn) return; // only zones that suppress spawns

    // Reset TIME_SINCE_REST — exactly what a night's sleep resets, and what the vanilla
    // PhantomSpawner gates the 72000-tick threshold on — so no new phantom spawns while
    // the player lingers in the safe zone.
    player.getStats().setValue(
      player,
      net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST),
      0);

    // Remove any phantom that already spawned over the safe zone (spawns ~20b up, then
    // swoops) — the stat reset only stops NEW spawns.
    java.util.List<net.minecraft.world.entity.monster.Phantom> phantoms =
      player.serverLevel().getEntitiesOfClass(
        net.minecraft.world.entity.monster.Phantom.class,
        player.getBoundingBox().inflate(64.0));
    for (net.minecraft.world.entity.monster.Phantom phantom : phantoms) {
      phantom.discard();
    }
  }

  /** Shrine zone name (install.json) → its reveal cutscene id. Playing the reveal on first entry
   * into the shrine area (rather than only via a keeper dialog button most players never click)
   * is the 2026-07-20 showrunner call. Fires exactly once ever, latched by a persistent player tag. */
  private static final Map<String, String> SHRINE_REVEAL_SCENES = Map.of(
    "Fire Shrine", "fire_shrine_reveal",
    "Ice Shrine", "ice_shrine_reveal",
    "Ground Shrine", "ground_shrine_reveal",
    "Fairy Shrine", "fairy_shrine_reveal",
    "Dragon Shrine", "dragon_shrine_reveal");

  /** Play a shrine's reveal cutscene the first time the player crosses into its zone. */
  private static void maybePlayShrineReveal(ServerPlayer player, String zoneName) {
    String scene = SHRINE_REVEAL_SCENES.get(zoneName);
    if (scene == null) return;
    String seenTag = "ci_seen_" + scene; // player tags persist across relog → true one-shot
    if (player.getTags().contains(seenTag)) return;
    net.minecraft.server.MinecraftServer server = player.getServer();
    if (server == null) return;
    player.addTag(seenTag); // latch BEFORE dispatch so a mid-scene relog can't replay it
    net.minecraft.commands.CommandSourceStack src =
      player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "cutscene play " + scene);
  }

  private static void sendZoneEntry(ServerPlayer player, NuzlockeConfig.SafeZone zone) {
    NuzlockeConfig.AnnouncementStyle style = config.getAnnouncementStyle();
    // The stored subtitle is the MAP-facing state (Map Frontiers renders it on the label
    // from install run — e.g. a farm's "Corporate owned."). A liberation-gated zone only
    // announces once its latch has tripped, so the announce always shows the freed state.
    String subtitle =
      (zone.activeWhenObjective != null && !zone.activeWhenObjective.isEmpty())
        ? "Liberated."
        : zone.subtitle;

    // Content mode decides which parts show. `titleText` is the big slot, `subText` the
    // small one (null = none). AUTO keeps the smart per-type default: a ROUTE's name is
    // already on the Map Frontiers label, so its toast promotes the flavor line to the
    // title and drops the subtitle; everything else shows name + subtitle.
    boolean hasSub = subtitle != null && !subtitle.isEmpty();
    NuzlockeConfig.AnnouncementContent content = config.getAnnouncementContent();
    String titleText;
    String subText;
    switch (content) {
      case TITLE_ONLY -> { titleText = zone.name; subText = null; }
      case SUBTITLE_ONLY -> { titleText = hasSub ? subtitle : zone.name; subText = null; }
      case TITLE_AND_SUBTITLE -> { titleText = zone.name; subText = hasSub ? subtitle : null; }
      default -> { // AUTO
        boolean routeFlavorOnly = "ROUTE".equalsIgnoreCase(zone.type) && hasSub;
        titleText = routeFlavorOnly ? subtitle : zone.name;
        subText = routeFlavorOnly ? null : (hasSub ? subtitle : null);
      }
    }
    Component title = buildTitleComponent(titleText, zone.color);

    if (style == NuzlockeConfig.AnnouncementStyle.TITLE) {
      player.connection.send(new ClientboundSetTitlesAnimationPacket(
        config.getAnnouncementFadeIn(),
        config.getAnnouncementStay(),
        config.getAnnouncementFadeOut()
      ));
      player.connection.send(new ClientboundSetTitleTextPacket(title));
      if (subText != null) {
        player.connection.send(new ClientboundSetSubtitleTextPacket(
          Component.literal("§7" + subText)
        ));
      }
    } else if (style == NuzlockeConfig.AnnouncementStyle.ACTIONBAR) {
      player.connection.send(new ClientboundSetActionBarTextPacket(
        Component.literal("§e▶ ").append(title).append(Component.literal(
          subText != null ? " §8— §7" + subText : ""
        ))
      ));
    } else {
      player.sendSystemMessage(
        Component.literal("§6[Area] §eEntering: ").append(title)
      );
    }
  }

  /**
   * Announces undefined territory ("Wilderness") using the same styling as zone entry.
   * Builds a transient {@link NuzlockeConfig.SafeZone} so the global announcement style
   * (TITLE / ACTIONBAR / CHAT) and timing are honoured without duplicating that logic.
   */
  private static void sendWilderness(ServerPlayer player) {
    NuzlockeConfig.SafeZone wild = new NuzlockeConfig.SafeZone();
    wild.name = config.getWildernessName();
    wild.subtitle = config.getWildernessSubtitle();
    wild.color = config.getWildernessColor();
    sendZoneEntry(player, wild);
  }

  private static void sendZoneExit(ServerPlayer player, String zoneName) {
    NuzlockeConfig.AnnouncementStyle style = config.getAnnouncementStyle();
    if (style == NuzlockeConfig.AnnouncementStyle.ACTIONBAR) {
      player.connection.send(new ClientboundSetActionBarTextPacket(
        Component.literal("§8◀ Left: §7" + zoneName)
      ));
    } else if (style == NuzlockeConfig.AnnouncementStyle.CHAT) {
      player.sendSystemMessage(Component.literal("§7[Area] Left: " + zoneName));
    }
  }

  private static Component buildTitleComponent(String name, String hexColor) {
    if (hexColor != null && hexColor.startsWith("#")) {
      try {
        int rgb = Integer.parseInt(hexColor.substring(1), 16);
        return Component.literal(name).withStyle(
          Style.EMPTY.withColor(TextColor.fromRgb(rgb))
        );
      } catch (NumberFormatException ignored) {}
    }
    return Component.literal("§e" + name);
  }
}

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
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.stadium.StadiumManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

  private static NuzlockeConfig config;
  private static boolean pendingWhiteoutDeath = false;
  private static boolean pendingSacrifice = false;
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

    ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (++announceTick % Math.max(1, config.getZoneCheckCadenceTicks()) != 0) return;
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
        checkZoneTransition(player);
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
                  pendingWhiteoutDeath = true;
                  player.kill();
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
                    pendingWhiteoutDeath = true;
                    player.kill();
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
            if (pcPokemon != null && pcPokemon.getSpecies().getName().equalsIgnoreCase(speciesName)) {
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
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        party.remove(pokemon);
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
      party.remove(pokemon);

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
    }

    LOGGER.info("Player {} captured {}", player.getName().getString(), speciesName);
    return Unit.INSTANCE;
  }

  private static Unit handleBattleFled(BattleFledEvent event) {
    if (!config.isSacrificeOnFlee()) return Unit.INSTANCE;

    PlayerBattleActor playerActor = event.getPlayer();
    ServerPlayer player = playerActor.getEntity();
    if (player == null) return Unit.INSTANCE;

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
      pendingWhiteoutDeath = true;
      player.kill();
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

  private static Unit handleBattleVictory(BattleVictoryEvent event) {
    if (!config.isSacrificeOnFlee()) return Unit.INSTANCE;
    if (event.getWasWildCapture()) return Unit.INSTANCE;

    for (BattleActor loser : event.getLosers()) {
      if (loser.getType() != ActorType.PLAYER) continue;
      if (!(loser instanceof PlayerBattleActor playerActor)) continue;

      ServerPlayer player = playerActor.getEntity();
      if (player == null) continue;

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
          pendingWhiteoutDeath = true;
          player.kill();
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
      LOGGER.info("Removed {} from {}'s party", pokemonName, player.getName().getString());
    }

    applyDamageToPlayer(player, damageAmount, pokemonName, remainingAfterThis == 0);
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
      pendingWhiteoutDeath = true;
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
      // Guaranteed, unblockable death — bypasses armor / absorption / Resistance.
      player.kill();
    } else {
      player.hurt(player.damageSources().generic(), damage);
    }
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

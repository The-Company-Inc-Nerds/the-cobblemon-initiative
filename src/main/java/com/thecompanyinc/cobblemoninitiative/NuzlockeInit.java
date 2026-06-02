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
import java.util.Map;
import java.util.Objects;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuzlockeInit implements ModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiativeInit.MOD_ID);

  private static NuzlockeConfig config;
  private static boolean pendingWhiteoutDeath = false;
  private static boolean pendingSacrifice = false;
  private static final Map<UUID, String> playerZones = new ConcurrentHashMap<>();
  private static int announceTick = 0;

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
      if (++announceTick % 20 != 0) return;
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
              Commands.literal("deathscreen").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                player.sendSystemMessage(Component.literal("§4Triggering death screen..."));
                pendingWhiteoutDeath = true;
                player.hurt(player.damageSources().generic(), 20.0f);
                return 1;
              })
            )
            .then(
              Commands.literal("sacrifice").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
                int partyCount = countPartySize(party);

                if (partyCount <= 1) {
                  player.sendSystemMessage(
                    Component.literal("§4You only have one Pokémon! Triggering death instead...")
                  );
                  pendingWhiteoutDeath = true;
                  player.hurt(player.damageSources().generic(), 20.0f);
                } else {
                  player.sendSystemMessage(Component.literal("§cTriggering sacrifice selection..."));
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

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int partyCount = countPartySize(party);

    if (partyCount <= 1) {
      player.sendSystemMessage(
        Component.literal("§4You fled with only one Pokémon! There is no escape...")
      );
      pendingWhiteoutDeath = true;
      player.hurt(player.damageSources().generic(), 20.0f);
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
          player.hurt(player.damageSources().generic(), 20.0f);
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
    return Unit.INSTANCE;
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

  private static int countRemainingPokemon(PlayerPartyStore party, Pokemon justFainted) {
    int remaining = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null && pokemon != justFainted && !pokemon.isFainted()) remaining++;
    }
    return remaining;
  }

  private static float calculateDamage(ServerPlayer player, int totalPartySize, int remainingPokemon) {
    if (remainingPokemon == 0) return 20.0f;

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
    } else {
      if (config.isRemoveFaintedPokemon()) {
        message = "§c" + pokemonName + " fainted and was released! You take damage!";
      } else {
        message = config.getDamageMessage().replace("%pokemon%", pokemonName);
      }
    }
    player.sendSystemMessage(Component.literal(message));
    player.hurt(player.damageSources().generic(), damage);
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

    NuzlockeConfig.SafeZone zone = config.getAnnouncedZoneAt(dim, x, y, z);
    String zoneName = zone != null ? zone.name : null;
    String prevName = playerZones.get(player.getUUID());

    if (Objects.equals(zoneName, prevName)) return;

    if (zoneName != null) {
      playerZones.put(player.getUUID(), zoneName);
      sendZoneEntry(player, zone);
    } else {
      playerZones.remove(player.getUUID());
      if (config.isAnnounceOnExit() && prevName != null) {
        sendZoneExit(player, prevName);
      }
    }
  }

  private static void sendZoneEntry(ServerPlayer player, NuzlockeConfig.SafeZone zone) {
    NuzlockeConfig.AnnouncementStyle style = config.getAnnouncementStyle();
    Component title = buildTitleComponent(zone.name, zone.color);

    if (style == NuzlockeConfig.AnnouncementStyle.TITLE) {
      player.connection.send(new ClientboundSetTitlesAnimationPacket(
        config.getAnnouncementFadeIn(),
        config.getAnnouncementStay(),
        config.getAnnouncementFadeOut()
      ));
      player.connection.send(new ClientboundSetTitleTextPacket(title));
      if (zone.subtitle != null && !zone.subtitle.isEmpty()) {
        player.connection.send(new ClientboundSetSubtitleTextPacket(
          Component.literal("§7" + zone.subtitle)
        ));
      }
    } else if (style == NuzlockeConfig.AnnouncementStyle.ACTIONBAR) {
      String label = zone.name
        + (zone.subtitle != null && !zone.subtitle.isEmpty() ? " §8— §7" + zone.subtitle : "");
      player.connection.send(new ClientboundSetActionBarTextPacket(
        Component.literal("§e▶ ").append(title).append(Component.literal(
          zone.subtitle != null && !zone.subtitle.isEmpty() ? " §8— §7" + zone.subtitle : ""
        ))
      ));
    } else {
      player.sendSystemMessage(
        Component.literal("§6[Area] §eEntering: ").append(title)
      );
    }
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

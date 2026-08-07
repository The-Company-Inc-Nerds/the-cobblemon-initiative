package com.thecompanyinc.cobblemoninitiative.dittohunt;

import com.thecompanyinc.cobblemoninitiative.noble.NobleFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Runs the "Ditto hide-and-seek" quest mini-game (see {@link DittoHuntConfig}). On {@code start},
 * places {@code count} distinct Cobblemon-modelled Easy NPC actors (random species from the pool)
 * at the game's spots; one slot spawns the {@code _fake} preset — a disguised Ditto whose baked
 * dialog cry text subtly slips. Talking to an actor opens its dialog (the cry); an "accuse" dialog
 * button runs {@code cobblemon-initiative ditto accuse} as the player, and {@link #accuse} judges
 * the actor they're standing next to: if it carries {@link #FAKE_TAG} they win (tag + reward),
 * otherwise the round is lost and the real fake is revealed.
 *
 * <p>Static manager in the {@code GaviotaManager}/{@code KalaharManager} mould. Actors carry
 * {@link #ACTOR_TAG} and are cleaned with a tag {@code kill} at every round end (and on disconnect).
 */
public final class DittoHuntManager {

  private DittoHuntManager() {}

  /** Tag every actor carries (baked into the presets) — nearest-actor resolution + cleanup. */
  public static final String ACTOR_TAG = "ci_ditto_actor";
  /** Extra tag only the disguised-Ditto ({@code _fake}) preset carries — the win condition. */
  public static final String FAKE_TAG = "ci_ditto_fake";

  private static final Map<UUID, Active> games = new HashMap<>();
  private static boolean initialized;

  private static final class Active {
    String gameId;
    String dimension;
    String winTag, winCommand, loseCommand, winMessage, loseMessage;
    String fakeSpeciesName; // revealed on a loss
    /** actor entity uuid -> (species, isFake), captured at spawn — drives the "Listen" audio. */
    final Map<UUID, ActorInfo> actors = new HashMap<>();
  }

  private static final class ActorInfo {
    final String species;
    final boolean fake;
    ActorInfo(String species, boolean fake) { this.species = species; this.fake = fake; }
  }

  public static void init() {
    if (initialized) return;
    initialized = true;
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
      ServerPlayer p = handler.player;
      if (p != null && games.remove(p.getUUID()) != null) clearAll(server);
    });
  }

  // ── round lifecycle ────────────────────────────────────────────────────────────

  /** {@code ditto start [game]} — place the actors (dialog button; perm 0). Returns the actor
   *  count on success, 0 on failure. */
  public static int start(ServerPlayer player, String gameId) {
    DittoHuntConfig cfg = DittoHuntConfig.get();
    if (!cfg.enabled) {
      player.displayClientMessage(Component.literal("§7The Ditto hunt is disabled."), false);
      return 0;
    }
    DittoHuntConfig.Game game = cfg.game(gameId);
    if (game == null) {
      player.displayClientMessage(Component.literal("§cNo Ditto game '" + gameId + "'."), false);
      return 0;
    }
    int count = Math.min(game.count, game.spots == null ? 0 : game.spots.size());
    if (count < 2) {
      player.displayClientMessage(Component.literal("§cThat game needs at least 2 spots."), false);
      return 0;
    }
    if (cfg.speciesPool == null || cfg.speciesPool.size() < count) {
      player.displayClientMessage(Component.literal("§cThe species pool is smaller than the round size."), false);
      return 0;
    }
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    ServerLevel level = levelFor(server, game.dimension);

    clearAll(server);
    games.remove(player.getUUID());

    RandomSource rand = level.getRandom();
    List<String> chosen = pickDistinct(cfg.speciesPool, count, rand);
    int fakeIdx = rand.nextInt(count);

    Active active = new Active();
    active.gameId = game.id;
    active.dimension = game.dimension;
    active.winTag = game.winTag;
    active.winCommand = game.winCommand;
    active.loseCommand = game.loseCommand;
    active.winMessage = game.winMessage;
    active.loseMessage = game.loseMessage;
    active.fakeSpeciesName = titleCase(chosen.get(fakeIdx));

    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    for (int i = 0; i < count; i++) {
      DittoHuntConfig.Pos p = game.spots.get(i);
      String species = chosen.get(i);
      String preset = String.format(i == fakeIdx ? cfg.presetFake : cfg.presetReal, species);
      server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
        "easy_npc preset import_new data %s %.3f %.3f %.3f", preset, p.x, p.y, p.z));
    }

    // Capture each spawned actor's uuid -> species/fake (for the Listen audio), matching the
    // nearest freshly-imported ci_ditto_actor to each spot. import_new is synchronous, so the
    // bodies are queryable this tick (same idiom as PropHuntManager's barrel capture).
    Set<UUID> used = new HashSet<>();
    for (int i = 0; i < count; i++) {
      DittoHuntConfig.Pos p = game.spots.get(i);
      Entity e = nearestActorAt(level, p.x, p.y, p.z, used);
      if (e != null) {
        active.actors.put(e.getUUID(), new ActorInfo(chosen.get(i), i == fakeIdx));
        used.add(e.getUUID());
      }
    }

    games.put(player.getUUID(), active);

    if (game.startMessage != null && !game.startMessage.isBlank()) {
      player.displayClientMessage(Component.literal("§d" + game.startMessage), false);
    }
    return count;
  }

  /** {@code ditto accuse} — call out the actor the player is standing next to (dialog button; perm 0). */
  public static int accuse(ServerPlayer player) {
    Active active = games.get(player.getUUID());
    if (active == null) {
      player.displayClientMessage(Component.literal("§7No Ditto hunt is active."), false);
      return 0;
    }
    Entity target = nearestActor(player, 6.0);
    if (target == null) {
      player.displayClientMessage(Component.literal("§cStand right next to a Pokemon to call it out."), true);
      return 0;
    }
    boolean isFake = target.getTags().contains(FAKE_TAG);
    games.remove(player.getUUID());
    ServerLevel level = player.serverLevel();
    clearAll(player.getServer());

    if (isFake) {
      if (active.winTag != null && !active.winTag.isBlank()) player.addTag(active.winTag);
      level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
        30, 0.6, 0.8, 0.6, 0.1);
      level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
      if (active.winMessage != null && !active.winMessage.isBlank()) {
        player.displayClientMessage(Component.literal("§a" + active.winMessage), false);
      }
      runCmd(player.getServer(), active.dimension, active.winCommand);
    } else {
      level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9f, 1.0f);
      String reveal = active.fakeSpeciesName != null
        ? " §7(The fake was the " + active.fakeSpeciesName + ".)" : "";
      String msg = active.loseMessage != null && !active.loseMessage.isBlank()
        ? active.loseMessage : "Wrong — that one was real.";
      player.displayClientMessage(Component.literal("§c" + msg + reveal), false);
      runCmd(player.getServer(), active.dimension, active.loseCommand);
    }
    return 1;
  }

  /** {@code ditto listen} — play the nearest actor's real cry (dialog button + auto on talk).
   *  Reals play {@code cobblemon:pokemon.<species>.cry} clean; the disguised Ditto plays the SAME
   *  cry a touch HIGH ({@link DittoHuntConfig#fakeCryPitch}) — a subtle audible nudge. */
  public static int listen(ServerPlayer player) {
    Active active = games.get(player.getUUID());
    if (active == null) return 0;
    Entity target = nearestActor(player, 6.0);
    if (target == null) return 0;
    DittoHuntConfig cfg = DittoHuntConfig.get();
    ServerLevel level = player.serverLevel();
    double x = target.getX(), y = target.getY() + 0.5, z = target.getZ();

    ActorInfo info = active.actors.get(target.getUUID());
    boolean fake = info != null ? info.fake : target.getTags().contains(FAKE_TAG);
    String species = info != null ? info.species : null;

    if (species != null) {
      String cry = "cobblemon:pokemon." + species + ".cry";
      NobleFx.playSoundId(level, x, y, z, cry, cfg.cryVolume, fake ? cfg.fakeCryPitch : cfg.cryPitch);
    }
    return 1;
  }

  /** {@code ditto stop} — abandon the current game (dialog button; perm 0). */
  public static int stop(ServerPlayer player) {
    boolean had = games.remove(player.getUUID()) != null;
    MinecraftServer server = player.getServer();
    if (server != null) clearAll(server);
    if (had) player.displayClientMessage(Component.literal("§7Ditto hunt ended."), true);
    return 1;
  }

  /** {@code ditto clear} — remove every actor (dev). Runs per-dimension because entity selectors
   *  are scoped to the command source's level. */
  public static int clearAll(MinecraftServer server) {
    for (ServerLevel level : server.getAllLevels()) {
      var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
      server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + ACTOR_TAG + "]");
    }
    return 1;
  }

  // ── helpers ────────────────────────────────────────────────────────────────────

  private static Entity nearestActor(ServerPlayer player, double range) {
    List<Entity> list = player.serverLevel().getEntitiesOfClass(Entity.class,
      player.getBoundingBox().inflate(range), e -> e.getTags().contains(ACTOR_TAG));
    Entity best = null;
    double bd = Double.MAX_VALUE;
    for (Entity e : list) {
      double d = e.distanceToSqr(player);
      if (d < bd) { bd = d; best = e; }
    }
    return best;
  }

  /** The nearest just-imported actor to a spot (within ~2 blocks), skipping ones already claimed. */
  private static Entity nearestActorAt(ServerLevel level, double x, double y, double z, Set<UUID> used) {
    AABB box = new AABB(x - 2.0, y - 2.0, z - 2.0, x + 2.0, y + 2.0, z + 2.0);
    List<Entity> list = level.getEntitiesOfClass(Entity.class, box,
      e -> e.getTags().contains(ACTOR_TAG) && !used.contains(e.getUUID()));
    Entity best = null;
    double bd = Double.MAX_VALUE;
    for (Entity e : list) {
      double d = e.distanceToSqr(x, y, z);
      if (d < bd) { bd = d; best = e; }
    }
    return best;
  }

  /** Partial Fisher-Yates: {@code count} distinct entries from {@code pool} using the world RNG. */
  private static List<String> pickDistinct(List<String> pool, int count, RandomSource rand) {
    List<String> copy = new ArrayList<>(pool);
    for (int i = 0; i < count; i++) {
      int j = i + rand.nextInt(copy.size() - i);
      String tmp = copy.get(i);
      copy.set(i, copy.get(j));
      copy.set(j, tmp);
    }
    return new ArrayList<>(copy.subList(0, count));
  }

  private static String titleCase(String id) {
    if (id == null || id.isEmpty()) return id;
    return Character.toUpperCase(id.charAt(0)) + id.substring(1);
  }

  private static void runCmd(MinecraftServer server, String dimension, String cmd) {
    if (server == null || cmd == null || cmd.isBlank()) return;
    ServerLevel level = levelFor(server, dimension);
    var src = server.createCommandSourceStack().withLevel(level).withPermission(2).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, cmd);
  }

  private static ServerLevel levelFor(MinecraftServer server, String dimension) {
    for (ServerLevel l : server.getAllLevels()) {
      if (l.dimension().location().toString().equals(dimension)) return l;
    }
    return server.overworld();
  }
}

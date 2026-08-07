package com.thecompanyinc.cobblemoninitiative.prophunt;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.RandomSource;

/**
 * Runs the "prop hunt" quest mini-game (see {@link PropHuntConfig}). On {@code start}, floats a
 * distinctive falling-block "barrel" at every arena spot — one is the real barrel (chosen at
 * random), the rest are decoys. The barrels are summoned {@code NoGravity} so they hang in place
 * and render in the falling-block style; {@code FallingBlockPropMixin} freezes their tick so they
 * never drop/despawn. Right-clicking a barrel is caught by {@link #onUseEntity}: the real one wins
 * (tags the player + optional reward command); a decoy poofs into smoke, and once the wrong picks
 * exceed {@link PropHuntConfig.Arena#wrongAllowed} the round is lost.
 *
 * <p>Static manager in the {@link com.thecompanyinc.cobblemoninitiative.CyclopsManager} /
 * {@code GaviotaManager} mould. Single-player, so at most one round is live; barrels carry the
 * {@link #PROP_TAG} and are cleaned with a tag {@code kill} at every round end (and on disconnect).
 */
public final class PropHuntManager {

  private PropHuntManager() {}

  /** Entity tag every barrel prop carries (round cleanup + interaction gate). */
  public static final String PROP_TAG = "ci_prophunt_prop";

  /** Live rounds, keyed by player (single-player, so effectively one). */
  private static final Map<UUID, Round> rounds = new HashMap<>();

  private static boolean initialized;

  private static final class Round {
    String arenaId;
    String dimension;
    UUID correctUuid;      // the real barrel's entity id (primary match)
    double cx, cy, cz;     // real barrel position (fallback match if the uuid capture missed)
    int wrong;
    int wrongAllowed;
    String winTag, winCommand, loseCommand, winMessage, wrongMessage, loseMessage;
  }

  public static void init() {
    if (initialized) return;
    initialized = true;
    UseEntityCallback.EVENT.register(PropHuntManager::onUseEntity);
    // Don't leave floating barrels behind if the player logs out mid-round.
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
      ServerPlayer p = handler.player;
      if (p != null && rounds.remove(p.getUUID()) != null) clearAll(server);
    });
  }

  // ── round lifecycle ────────────────────────────────────────────────────────────

  /** {@code prophunt start [arena]} — begin a round (dialog button; perm 0). */
  public static int start(ServerPlayer player, String arenaId) {
    PropHuntConfig cfg = PropHuntConfig.get();
    if (!cfg.enabled) {
      player.displayClientMessage(Component.literal("§7Prop hunt is disabled."), false);
      return 0;
    }
    PropHuntConfig.Arena arena = cfg.arena(arenaId);
    if (arena == null) {
      player.displayClientMessage(Component.literal("§cNo prop-hunt arena '" + arenaId + "'."), false);
      return 0;
    }
    if (arena.spots == null || arena.spots.size() < 2) {
      player.displayClientMessage(Component.literal("§cThat arena needs at least 2 barrel spots."), false);
      return 0;
    }
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    ServerLevel level = levelFor(server, arena.dimension);

    // Fresh round: wipe any prior barrels + state first.
    clearAll(server);
    rounds.remove(player.getUUID());

    RandomSource rand = level.getRandom();
    int correctIdx = rand.nextInt(arena.spots.size());

    Round round = new Round();
    round.arenaId = arena.id;
    round.dimension = arena.dimension;
    round.wrongAllowed = Math.max(0, arena.wrongAllowed);
    round.winTag = arena.winTag;
    round.winCommand = arena.winCommand;
    round.loseCommand = arena.loseCommand;
    round.winMessage = arena.winMessage;
    round.wrongMessage = arena.wrongMessage;
    round.loseMessage = arena.loseMessage;

    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    for (int i = 0; i < arena.spots.size(); i++) {
      PropHuntConfig.Pos p = arena.spots.get(i);
      double bx = p.x, by = p.y + arena.floatHeight, bz = p.z;
      server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
        "summon minecraft:falling_block %.3f %.3f %.3f "
          + "{BlockState:{Name:\"%s\"},Time:1,NoGravity:1b,Motion:[0.0d,0.0d,0.0d],Tags:[\"%s\"]}",
        bx, by, bz, arena.barrelBlock, PROP_TAG));
      if (i == correctIdx) {
        round.cx = bx; round.cy = by; round.cz = bz;
        FallingBlockEntity real = findPropAt(level, bx, by, bz);
        if (real != null) round.correctUuid = real.getUUID();
      }
    }

    rounds.put(player.getUUID(), round);
    if (arena.startMessage != null && !arena.startMessage.isBlank()) {
      player.displayClientMessage(Component.literal("§e" + arena.startMessage), false);
    }
    return 1;
  }

  /** {@code prophunt stop} — abandon the current round (dialog button; perm 0). */
  public static int stop(ServerPlayer player) {
    boolean had = rounds.remove(player.getUUID()) != null;
    MinecraftServer server = player.getServer();
    if (server != null) clearAll(server);
    if (had) player.displayClientMessage(Component.literal("§7Prop hunt ended."), true);
    return 1;
  }

  /** {@code prophunt clear} — remove every barrel prop (dev). Runs per-dimension because entity
   *  selectors are scoped to the command source's level. */
  public static int clearAll(MinecraftServer server) {
    for (ServerLevel level : server.getAllLevels()) {
      var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
      server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + PROP_TAG + "]");
    }
    return 1;
  }

  // ── interaction ────────────────────────────────────────────────────────────────

  public static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand,
                                              Entity entity, EntityHitResult hit) {
    if (level.isClientSide()) return InteractionResult.PASS;
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(entity instanceof FallingBlockEntity fb)) return InteractionResult.PASS;
    if (!fb.getTags().contains(PROP_TAG)) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

    Round round = rounds.get(sp.getUUID());
    if (round == null) {
      // Stray barrel from an already-finished round — swallow the click and tidy it up.
      fb.discard();
      return InteractionResult.SUCCESS;
    }

    boolean correct = round.correctUuid != null
      ? fb.getUUID().equals(round.correctUuid)
      : fb.distanceToSqr(round.cx, round.cy, round.cz) < 0.75;

    ServerLevel slevel = sp.serverLevel();
    if (correct) {
      win(sp, round, slevel);
    } else {
      wrong(sp, round, fb, slevel);
    }
    return InteractionResult.SUCCESS;
  }

  private static void win(ServerPlayer sp, Round round, ServerLevel level) {
    rounds.remove(sp.getUUID());
    clearAll(sp.getServer());
    if (round.winTag != null && !round.winTag.isBlank()) sp.addTag(round.winTag);
    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, sp.getX(), sp.getY() + 1.0, sp.getZ(),
      30, 0.6, 0.8, 0.6, 0.1);
    level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
    if (round.winMessage != null && !round.winMessage.isBlank()) {
      sp.displayClientMessage(Component.literal("§a" + round.winMessage), false);
    }
    runCmd(sp.getServer(), round.dimension, round.winCommand);
  }

  private static void wrong(ServerPlayer sp, Round round, FallingBlockEntity fb, ServerLevel level) {
    double x = fb.getX(), y = fb.getY() + 0.4, z = fb.getZ();
    fb.discard();
    level.sendParticles(ParticleTypes.SMOKE, x, y, z, 25, 0.3, 0.3, 0.3, 0.02);
    level.sendParticles(ParticleTypes.POOF, x, y, z, 12, 0.25, 0.25, 0.25, 0.02);
    level.playSound(null, sp.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.9f, 0.8f);

    round.wrong++;
    if (round.wrong > round.wrongAllowed) {
      lose(sp, round, level);
      return;
    }
    int remaining = round.wrongAllowed - round.wrong + 1; // wrong picks still tolerated before loss
    String msg = round.wrongMessage != null && !round.wrongMessage.isBlank()
      ? round.wrongMessage : "A decoy — it crumbles to dust.";
    sp.displayClientMessage(
      Component.literal("§c" + msg + " §7(" + remaining + " wrong pick" + (remaining == 1 ? "" : "s") + " left)"),
      true);
  }

  private static void lose(ServerPlayer sp, Round round, ServerLevel level) {
    rounds.remove(sp.getUUID());
    clearAll(sp.getServer());
    level.playSound(null, sp.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.9f, 0.7f);
    if (round.loseMessage != null && !round.loseMessage.isBlank()) {
      sp.displayClientMessage(Component.literal("§c" + round.loseMessage), false);
    }
    runCmd(sp.getServer(), round.dimension, round.loseCommand);
  }

  // ── helpers ────────────────────────────────────────────────────────────────────

  private static FallingBlockEntity findPropAt(ServerLevel level, double x, double y, double z) {
    AABB box = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
    List<FallingBlockEntity> list = level.getEntitiesOfClass(FallingBlockEntity.class, box,
      e -> e.getTags().contains(PROP_TAG));
    FallingBlockEntity best = null;
    double bd = Double.MAX_VALUE;
    for (FallingBlockEntity e : list) {
      double d = e.distanceToSqr(x, y, z);
      if (d < bd) { bd = d; best = e; }
    }
    return best;
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

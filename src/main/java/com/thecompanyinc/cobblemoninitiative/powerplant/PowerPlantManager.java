package com.thecompanyinc.cobblemoninitiative.powerplant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * The Cyber City POWER PLANT lights puzzle — the gym-7 leader gate (0.7.0-alpha.20). Nine COPPER
 * BULB lights start in a random lit/unlit pattern; showrunner-placed levers each toggle exactly
 * TWO of the bulbs (on&harr;off). Restore all nine (all lit) to bring the grid back online and
 * unlock Leader Volt (player tag {@code cyber_power_restored}, gated in
 * {@code dialog-src/dialog/gym_leader_cyber.json}). Geometry is config-latched later
 * ({@link PowerPlantConfig} — the GaviotaManager/GaviotaConfig house precedent).
 *
 * <h2>The math (the correctness core — why this can NEVER soft/hard-lock)</h2>
 *
 * <p>State = {@code bool[9]}. Each switch toggles a fixed pair (a,b). A pair-toggle flips exactly
 * two bits, so it preserves the PARITY of the unlit count — which means an arbitrary random
 * pattern is often UNSOLVABLE (any odd-unlit pattern, and possibly more depending on the pair
 * graph), so we never generate one directly. Instead we SCRAMBLE: start from all-lit and apply
 * {@code scrambleMoves} (config, default 12) uniformly-random switch presses; re-roll the whole
 * scramble (bounded, ~{@value #SCRAMBLE_ATTEMPTS} attempts, keeping the last regardless) until
 * {@code unlitCount >= minUnlit} (config, default 4; the unlit count is always EVEN from an
 * all-lit start).
 *
 * <p>Every scrambled state is reachable-from-solved by construction, hence solvable: each switch
 * is its own inverse (a toggle), so replaying a subset of the scramble's switches walks the state
 * back to all-lit. And because the player's presses are the SAME generators the scramble used,
 * every state the player can ever reach stays inside the group-action coset that contains
 * all-lit — no sequence of moves, in any order, can ever leave it or make the puzzle unsolvable.
 * That is the whole no-softlock proof: solvability is an invariant of the coset, not a property
 * we have to re-check per state.
 *
 * <p>Corollary (validated with a WARN at load, see {@link PowerPlantConfig#validate()}): a bulb in
 * no switch pair can never change — it simply stays lit from the all-lit start (still solvable),
 * but the showrunner should know the wiring has a gap.
 *
 * <h2>Wiring</h2>
 * <ul>
 *   <li>LEVERS: {@code UseBlockCallback} (registered in InitiativeInit) — fast PASS unless the
 *       clicked pos is a registered switch pos; the toggle then PASSES THROUGH so the vanilla
 *       lever still flips visually (its orientation is cosmetic, the engine state is truth).</li>
 *   <li>BULBS: any block of the copper-bulb FAMILY (any oxidation/waxed variant — they all extend
 *       {@link CopperBulbBlock}); only the LIT blockstate property is flipped, everything else
 *       (POWERED, waxing, oxidation) preserved. Loaded chunks only; a 40t visual re-assert
 *       self-heals drift and covers chunks that were unloaded at scramble time.</li>
 *   <li>GATE: every 20t the {@code cyber_power_restored} player tag is maintained on ALL players
 *       when solved OR the engine is inactive, removed otherwise (see {@link #tick}); solved is
 *       also mirrored to scoreboard {@code ci_powerplant} holder {@code #restored} for datapacks
 *       (scoreboard-as-IPC, house philosophy).</li>
 *   <li>PERSISTENCE: {@code <world>/cobblemon_initiative_powerplant.json}, load on
 *       SERVER_STARTED, write-through on every change + SERVER_STOPPING (safari pattern).
 *       Auto-scramble at SERVER_STARTED when active and never scrambled — "random every run"
 *       means every fresh world; a re-run mid-world keeps its state.</li>
 *   <li>GUARDS: bulb-index contract idx 0-2 = generator 1 trio, 3-5 = generator 2 trio, 6-8 =
 *       console (documented in the bundled JSON {@code _comment} keys). A PLAYER press that
 *       knocks a full trio all-dark (edge-detected in {@link #applySwitch}) dispatches one
 *       hostile "Generator Security" iron golem per generator ({@code ci_pp_guard} tag) at the
 *       config spawn pad, angry at the presser. Guards discard on solve/scramble; UUIDs are
 *       session-memory only, so an orphan sweep runs at SERVER_STARTED and inside the 20t
 *       maintenance (CyclopsManager liveness-sweep precedent).</li>
 * </ul>
 */
public class PowerPlantManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String STATE_FILE_NAME = "cobblemon_initiative_powerplant.json";

  /** Gym-7 leader gate tag. FULL string literal on purpose — dialog_lint scans Java literals
   *  (KalaharManager STUDENTS.foundTag precedent); never build this by concatenation. */
  private static final String GATE_TAG = "cyber_power_restored";

  /** Datapack-visible mirror: objective {@code ci_powerplant}, holder {@code #restored} (1=solved). */
  private static final String OBJ = "ci_powerplant";
  private static final String H_RESTORED = "#restored";

  /** Entity tag on every dispatched security golem. FULL string literal (dialog_lint / typed-kill
   *  house law); the constant is the only spawn/sweep key — never build it by concatenation. */
  private static final String GUARD_TAG = "ci_pp_guard";

  /** Bulb-index CONTRACT (mirrored in the bundled JSON {@code _comment} keys): idx
   *  {@code g*3 .. g*3+2} = generator {@code g}'s trio (0-2 gen 1, 3-5 gen 2); 6-8 = console
   *  (never guarded). Reordering the bulbs list rewires the security response. */
  private static final int GENERATOR_COUNT = 2;
  private static final int TRIO_SIZE = 3;

  /** Persistent-anger window granted on dispatch/re-target (~30s; NeutralMob ticks it down). */
  private static final int GUARD_ANGER_TICKS = 600;

  /** The 20t re-assert only adopts a new target within this many blocks of the guard. */
  private static final double GUARD_RETARGET_RANGE = 24.0;

  /** Bounded scramble re-rolls before keeping the last result regardless (see class javadoc). */
  private static final int SCRAMBLE_ATTEMPTS = 20;

  /** Ambient particles at UNLIT bulbs only run with a player this close (blocks). */
  private static final double AMBIENT_RANGE = 24.0;

  // ── persisted state (world dir, write-through) ─────────────────────────────────

  private static class StateFile {
    int version = 1;
    boolean scrambled = false;
    boolean solved = false;
    boolean[] bulbs = new boolean[PowerPlantConfig.BULB_COUNT];
  }

  private PowerPlantConfig config = new PowerPlantConfig();
  /** bulbs[i] = lit. Meaningful only once {@link StateFile#scrambled}; defaults all-unlit on disk
   *  but the auto-scramble runs before the first lever can ever be pressed. */
  private boolean[] bulbs = new boolean[PowerPlantConfig.BULB_COUNT];
  private boolean scrambled = false;
  private boolean solved = false;

  private MinecraftServer server;
  private final Random random = new Random();
  /** One-per-session wrong/absent-bulb-block warns (cleared on reload — re-warn after a fix attempt). */
  private final Set<Integer> warnedBulbs = new HashSet<>();
  /** Solve-beat sound cascade: −1 idle, else the next bulb index to chime (2t apart, rising pitch). */
  private int cascadeStep = -1;
  private long nextCascadeTick = 0;
  /** Live security golem per generator (session-memory only, never persisted — restart orphans
   *  are reaped by tag sweep). null = no guard; a dead slot re-arms that generator's dispatch. */
  private final UUID[] guardUuids = new UUID[GENERATOR_COUNT];

  // ── wiring ─────────────────────────────────────────────────────────────────────

  /** Config load + validation-warn pass. Called at init and from ModMenu/`powerplant reload`. */
  public void load() {
    config = PowerPlantConfig.load();
    warnedBulbs.clear();
    for (String warn : config.validate()) {
      InitiativeInit.LOGGER.warn("[PowerPlant] {}", warn);
    }
  }

  public void onServerStarted(MinecraftServer server) {
    this.server = server;
    loadState(server);
    // Orphan sweep: guard UUIDs are session-memory only, so any tagged golem from a previous
    // session is a stray. Loaded chunks only — the 20t maintenance sweep reaps late loaders.
    sweepOrphanGuards(server.overworld());
    // Auto-scramble ONCE per world when the geometry is latched: "random every run" = every fresh
    // world rolls its own pattern; a re-run mid-world keeps its saved state (scrambled latch).
    if (config.isActive() && !scrambled && !solved) {
      scramble(server);
      InitiativeInit.LOGGER.info("[PowerPlant] Fresh world — scrambled the grid ({} unlit).",
        unlitCount());
    }
  }

  public void onServerStopping(MinecraftServer server) {
    saveState(server); // belt-and-braces — every change already write-through saves
    // Drop the reference: ModMenu's reloadConfig can fire from the TITLE screen after a world
    // closes — the guard-discard path must never touch a stopped server's levels.
    this.server = null;
  }

  public PowerPlantConfig getConfig() {
    return config;
  }

  // ── the scramble (see class javadoc — this is the correctness core) ────────────

  /**
   * Build a fresh puzzle: from all-lit, apply {@code scrambleMoves} uniformly-random switch
   * presses; re-roll the whole scramble up to {@value #SCRAMBLE_ATTEMPTS} times until
   * {@code unlitCount >= minUnlit}, keeping the LAST roll regardless (a pathological pair graph
   * must never hang the server — a too-easy puzzle is still a valid, solvable puzzle).
   * Clears {@code solved}; pushes the new states onto loaded bulb blocks.
   */
  public int scramble(MinecraftServer server) {
    List<PowerPlantConfig.Switch> switches = config.validSwitches();
    if (!config.isActive() || switches.isEmpty()) {
      InitiativeInit.LOGGER.warn(
        "[PowerPlant] scramble: engine inactive ({} bulbs, {} valid switches) — latch the config first.",
        config.bulbs == null ? 0 : config.bulbs.size(), switches.size());
      return 0;
    }
    int moves = Math.max(1, config.scrambleMoves);
    int minUnlit = Math.max(0, config.minUnlit);
    boolean[] candidate = null;
    for (int attempt = 0; attempt < SCRAMBLE_ATTEMPTS; attempt++) {
      candidate = new boolean[PowerPlantConfig.BULB_COUNT];
      java.util.Arrays.fill(candidate, true); // start all-lit: the reachability anchor
      for (int m = 0; m < moves; m++) {
        PowerPlantConfig.Switch s = switches.get(random.nextInt(switches.size()));
        candidate[s.a] = !candidate[s.a];
        candidate[s.b] = !candidate[s.b];
      }
      int unlit = 0;
      for (boolean lit : candidate) if (!lit) unlit++;
      if (unlit >= minUnlit) break; // good roll; otherwise keep the LAST regardless
    }
    bulbs = candidate;
    scrambled = true;
    solved = false;
    cascadeStep = -1;
    discardGuards(server); // a fresh board never inherits an angry golem
    saveState(server);
    pushAllBulbStates(server);
    return 1;
  }

  // ── lever handling ─────────────────────────────────────────────────────────────

  /**
   * UseBlockCallback (registered in InitiativeInit): fast PASS unless the clicked pos is a
   * registered switch pos. Active + scrambled + not solved → apply the pair toggle, then PASS so
   * the VANILLA lever still flips visually (lever orientation is cosmetic — the engine's bool[9]
   * is the truth). Solved → "humming" actionbar flavor, PASS. Inactive → plain PASS.
   */
  public InteractionResult onUseBlock(Player player, Level level, InteractionHand hand,
      BlockHitResult hit) {
    if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    List<PowerPlantConfig.Switch> switches = config.validSwitches();
    if (switches.isEmpty()) return InteractionResult.PASS;
    BlockPos pos = hit.getBlockPos();
    int idx = -1;
    for (int i = 0; i < switches.size(); i++) {
      PowerPlantConfig.Switch s = switches.get(i);
      if (s.x == pos.getX() && s.y == pos.getY() && s.z == pos.getZ()) { idx = i; break; }
    }
    if (idx < 0) return InteractionResult.PASS;
    // Mirror vanilla: sneaking with an item in hand places against the lever instead of using it —
    // skip the toggle so the engine never fires when the lever itself would not flip.
    if (player.isSecondaryUseActive() && !player.getMainHandItem().isEmpty()) {
      return InteractionResult.PASS;
    }
    if (!config.isActive() || !scrambled) return InteractionResult.PASS;
    if (solved) {
      if (player instanceof ServerPlayer sp) {
        sp.displayClientMessage(Component.literal(
          "§bThe generators are humming — the grid holds steady."), true);
      }
      return InteractionResult.PASS;
    }
    applySwitch(level.getServer(), switches.get(idx), player instanceof ServerPlayer sp ? sp : null);
    return InteractionResult.PASS; // vanilla lever still flips (cosmetic)
  }

  /** Shared toggle path (lever click AND the dev {@code flip} command drive this). */
  private void applySwitch(MinecraftServer server, PowerPlantConfig.Switch s, ServerPlayer who) {
    if (server == null) return;
    // Snapshot the generator trios BEFORE the flip: guard dispatch is EDGE-triggered (a trio
    // must GO all-dark on this press), so pressing near an already-dark trio never stacks.
    boolean[] trioDarkBefore = new boolean[GENERATOR_COUNT];
    for (int g = 0; g < GENERATOR_COUNT; g++) trioDarkBefore[g] = isGeneratorDark(g);
    bulbs[s.a] = !bulbs[s.a];
    bulbs[s.b] = !bulbs[s.b];
    saveState(server); // write-through on every change (safari pattern)
    ServerLevel level = server.overworld();
    for (int i : new int[] {s.a, s.b}) {
      setBulbBlock(level, i, bulbs[i]);
      PowerPlantConfig.Pos p = config.bulbs.get(i);
      level.playSound(null, new BlockPos(p.x, p.y, p.z),
        bulbs[i] ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF,
        SoundSource.BLOCKS, 1.0f, 1.0f);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
        p.x + 0.5, p.y + 0.5, p.z + 0.5, 8, 0.3, 0.3, 0.3, 0.05);
    }
    // Security dispatch: PLAYER presses only (who is null from the console dev `flip`).
    if (who != null) {
      for (int g = 0; g < GENERATOR_COUNT; g++) {
        if (!trioDarkBefore[g] && isGeneratorDark(g)) {
          spawnGuard(server, g, who);
        }
      }
    }
    if (unlitCount() == 0) {
      onSolved(server, who);
    } else if (who != null) {
      who.displayClientMessage(Component.literal(
        "§eThe breaker thunks — §f" + (PowerPlantConfig.BULB_COUNT - unlitCount()) + "/"
          + PowerPlantConfig.BULB_COUNT + "§e lights on the board."), true);
    }
  }

  /** True when every bulb of generator {@code g}'s trio (index contract, see GENERATOR_COUNT)
   *  is unlit. */
  private boolean isGeneratorDark(int g) {
    for (int i = g * TRIO_SIZE; i < (g + 1) * TRIO_SIZE; i++) {
      if (i >= bulbs.length || bulbs[i]) return false;
    }
    return true;
  }

  /** All nine lit — the one-shot restoration beat. Persists solved=true; the 20t gate sweep
   *  grants {@code cyber_power_restored} within a second. */
  private void onSolved(MinecraftServer server, ServerPlayer who) {
    solved = true;
    saveState(server);
    discardGuards(server); // grid online — site security stands down
    ServerLevel level = server.overworld();
    // Title beat, corporate-cyber voice (amnesiac era — the grid knows the Company; so might you).
    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
      p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
      p.connection.send(new ClientboundSetTitleTextPacket(
        Component.literal("§b§lGRID ONLINE")));
      p.connection.send(new ClientboundSetSubtitleTextPacket(
        Component.literal("§7Municipal supply restored. §8Usage will be billed accordingly.")));
    }
    level.playSound(null, nearestBulbPos(who), SoundEvents.BEACON_ACTIVATE,
      SoundSource.BLOCKS, 1.0f, 1.2f);
    // Rising sound cascade + per-bulb sparks, one bulb per 2 ticks (driven from tick()).
    cascadeStep = 0;
    nextCascadeTick = 0;
    InitiativeInit.LOGGER.info("[PowerPlant] Grid restored{} — gym 7 gate open.",
      who != null ? " by " + who.getName().getString() : "");
  }

  private BlockPos nearestBulbPos(ServerPlayer who) {
    if (config.bulbs == null || config.bulbs.isEmpty()) {
      return who != null ? who.blockPosition() : BlockPos.ZERO;
    }
    PowerPlantConfig.Pos p = config.bulbs.get(0);
    if (who != null) {
      double best = Double.MAX_VALUE;
      for (PowerPlantConfig.Pos c : config.bulbs) {
        double d = who.distanceToSqr(c.x + 0.5, c.y + 0.5, c.z + 0.5);
        if (d < best) { best = d; p = c; }
      }
    }
    return new BlockPos(p.x, p.y, p.z);
  }

  // ── tick: gate tag (20t), visual sync + ambience (40t), solve cascade ──────────

  public void tick(MinecraftServer server) {
    this.server = server;
    long now = server.overworld().getGameTime();

    // (1) Gate-tag maintenance every 20t. THE INACTIVE=OPEN PAIRING IS LOAD-BEARING: until the
    // showrunner latches the 9 bulb coords the plant does not physically exist, so the leader
    // gate MUST NOT brick gym 7 — an inactive engine grants cyber_power_restored to everyone
    // (Volt's power_out entry never shows). The moment the config goes active on an unsolved
    // world the tag is swept off and the gate closes. Tag literal in GATE_TAG (dialog_lint).
    if (now % 20 == 0) {
      boolean open = solved || !config.isActive();
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        if (open) {
          p.addTag(GATE_TAG);
        } else {
          p.removeTag(GATE_TAG);
        }
      }
      setRestoredScore(server, solved ? 1 : 0);
    }

    if (!config.isActive()) return;
    ServerLevel level = server.overworld();

    // (1b) Guard maintenance every 20t: reap dead/orphaned golems, re-assert lost targets.
    if (now % 20 == 0) {
      maintainGuards(level);
    }

    // (2) Solve cascade: chime each bulb in order at rising pitch, 2t apart.
    if (cascadeStep >= 0 && now >= nextCascadeTick) {
      if (cascadeStep < config.bulbs.size()) {
        PowerPlantConfig.Pos p = config.bulbs.get(cascadeStep);
        BlockPos bp = new BlockPos(p.x, p.y, p.z);
        level.playSound(null, bp, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS,
          1.0f, 0.7f + 0.12f * cascadeStep);
        level.playSound(null, bp, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
          1.0f, 0.6f + 0.15f * cascadeStep);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
          p.x + 0.5, p.y + 0.5, p.z + 0.5, 20, 0.4, 0.4, 0.4, 0.15);
        cascadeStep++;
        nextCascadeTick = now + 2;
      } else {
        cascadeStep = -1;
      }
    }

    if (!scrambled || now % 40 != 0) return;

    // (3) Visual sync every 40t: re-assert the engine's lit states onto loaded bulb blocks while
    // any player is near — self-heals drift (pistons, /setblock, stray redstone) and paints
    // chunks that were unloaded at scramble time as players arrive.
    boolean playerNear = false;
    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
      if (distToNearestBulbSqr(p) <= (double) config.syncRange * config.syncRange) {
        playerNear = true;
        break;
      }
    }
    if (playerNear) {
      pushAllBulbStates(server);
    }

    // (4) Ambience: dead sections of the grid smoke and spit sparks while a player is close.
    if (!solved) {
      for (int i = 0; i < config.bulbs.size(); i++) {
        if (bulbs[i]) continue;
        PowerPlantConfig.Pos p = config.bulbs.get(i);
        boolean near = false;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
          if (sp.distanceToSqr(p.x + 0.5, p.y + 0.5, p.z + 0.5) <= AMBIENT_RANGE * AMBIENT_RANGE) {
            near = true;
            break;
          }
        }
        if (!near) continue;
        level.sendParticles(ParticleTypes.SMOKE,
          p.x + 0.5, p.y + 0.8, p.z + 0.5, 3, 0.2, 0.2, 0.2, 0.01);
        if (random.nextInt(3) == 0) {
          level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
            p.x + 0.5, p.y + 0.5, p.z + 0.5, 2, 0.25, 0.25, 0.25, 0.05);
        }
      }
    }
  }

  private double distToNearestBulbSqr(ServerPlayer p) {
    double best = Double.MAX_VALUE;
    for (PowerPlantConfig.Pos c : config.bulbs) {
      best = Math.min(best, p.distanceToSqr(c.x + 0.5, c.y + 0.5, c.z + 0.5));
    }
    return best;
  }

  // ── bulb blocks ────────────────────────────────────────────────────────────────

  /** Re-assert every bulb's engine state onto its block (loaded chunks only). */
  private void pushAllBulbStates(MinecraftServer server) {
    ServerLevel level = server.overworld();
    for (int i = 0; i < config.bulbs.size() && i < bulbs.length; i++) {
      setBulbBlock(level, i, bulbs[i]);
    }
  }

  /**
   * Flip ONLY the LIT blockstate property at bulb {@code i}, preserving everything else — the
   * position may hold ANY member of the copper-bulb family (all oxidation/waxed variants extend
   * {@link CopperBulbBlock}). Wrong/absent block or unloaded chunk = skip (one warn per session
   * per bulb for the wrong-block case; cleared on reload).
   */
  private void setBulbBlock(ServerLevel level, int i, boolean lit) {
    PowerPlantConfig.Pos p = config.bulbs.get(i);
    BlockPos pos = new BlockPos(p.x, p.y, p.z);
    if (!level.isLoaded(pos)) return; // never touch unloaded chunks — the 40t sync catches up
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof CopperBulbBlock)
        || !state.hasProperty(BlockStateProperties.LIT)) {
      if (warnedBulbs.add(i)) {
        InitiativeInit.LOGGER.warn(
          "[PowerPlant] bulb {} at {} {} {} is not a copper bulb ({}) — place any copper-bulb "
            + "variant there (skipping; warn once per session).",
          i, p.x, p.y, p.z, state.getBlock().getName().getString());
      }
      return;
    }
    if (state.getValue(BlockStateProperties.LIT) != lit) {
      level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), Block.UPDATE_ALL);
    }
  }

  // ── generator security guards (trio-dark dispatch, see class javadoc) ──────────

  /**
   * Dispatch generator {@code g}'s security golem at {@code guardSpawns[min(g, size-1)]}
   * (fewer pads than generators clamps to the last — the bundled default is ONE shared pad),
   * hostile toward {@code who} via persistent anger (NeutralMob). Deduped: a live guard on the
   * slot skips the dispatch; a dead one re-arms it. NobleEncounterManager Phase-2 spawn recipe.
   */
  private boolean spawnGuard(MinecraftServer server, int g, ServerPlayer who) {
    if (server == null || !config.generatorGuards) return false;
    List<PowerPlantConfig.Pos> spawns = config.guardSpawns;
    if (spawns == null || spawns.isEmpty()) return false;
    ServerLevel level = server.overworld();
    if (guardUuids[g] != null) {
      Entity live = level.getEntity(guardUuids[g]);
      if (live != null && live.isAlive()) return false; // one guard per generator
      guardUuids[g] = null;
    }
    PowerPlantConfig.Pos pad = spawns.get(Math.min(g, spawns.size() - 1));
    IronGolem golem = EntityType.IRON_GOLEM.create(level);
    if (golem == null) return false;
    double x = pad.x + 0.5, y = pad.y, z = pad.z + 0.5;
    float yaw = who != null
      ? (float) (Math.toDegrees(Math.atan2(who.getZ() - z, who.getX() - x)) - 90.0)
      : 0f;
    golem.moveTo(x, y, z, yaw, 0f);
    golem.setCustomName(Component.literal("§4Generator Security"));
    golem.addTag(GUARD_TAG);
    golem.setPersistenceRequired();
    if (who != null) {
      golem.setTarget(who);
      golem.setPersistentAngerTarget(who.getUUID());
      golem.setRemainingPersistentAngerTime(GUARD_ANGER_TICKS);
    }
    level.addFreshEntity(golem);
    guardUuids[g] = golem.getUUID();
    level.playSound(null, golem.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK,
      SoundSource.HOSTILE, 1.0f, 0.8f);
    if (who != null) {
      level.playSound(null, who.blockPosition(), SoundEvents.BELL_BLOCK,
        SoundSource.BLOCKS, 1.0f, 0.5f);
      who.displayClientMessage(Component.literal(
        "§4⚠ GENERATOR OFFLINE §7— site security dispatched."), true);
    }
    InitiativeInit.LOGGER.info("[PowerPlant] Generator {} went dark — security golem dispatched{}.",
      g + 1, who != null ? " on " + who.getName().getString() : "");
    return true;
  }

  /**
   * 20t maintenance: reap dead guards (slot re-arms), re-assert a lost/dead target to the
   * nearest survival-mode player within {@value #GUARD_RETARGET_RANGE} blocks (else leave the
   * golem idling), and sweep tagged orphans that late-loaded after the SERVER_STARTED pass.
   */
  private void maintainGuards(ServerLevel level) {
    for (int g = 0; g < GENERATOR_COUNT; g++) {
      if (guardUuids[g] == null) continue;
      Entity e = level.getEntity(guardUuids[g]);
      if (!(e instanceof IronGolem golem) || !golem.isAlive()) {
        guardUuids[g] = null; // felled — the trio going dark again re-dispatches
        continue;
      }
      LivingEntity target = golem.getTarget();
      if (target != null && target.isAlive()
          && !(target instanceof ServerPlayer tp && tp.isSpectator())) {
        continue;
      }
      ServerPlayer nearest = null;
      double best = GUARD_RETARGET_RANGE * GUARD_RETARGET_RANGE;
      for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
        if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;
        double d = golem.distanceToSqr(p);
        if (d <= best) { best = d; nearest = p; }
      }
      if (nearest != null) {
        golem.setTarget(nearest);
        golem.setPersistentAngerTarget(nearest.getUUID());
        golem.setRemainingPersistentAngerTime(GUARD_ANGER_TICKS);
      }
    }
    sweepOrphanGuards(level);
  }

  /** Discard any tagged golem not held by a live slot (typed scan — house kill-selector law;
   *  loaded chunks only, callers re-run this so late-loading strays still get reaped). */
  private void sweepOrphanGuards(ServerLevel level) {
    for (IronGolem stray : level.getEntities(EntityType.IRON_GOLEM,
        e -> e.getTags().contains(GUARD_TAG))) {
      boolean tracked = false;
      for (UUID u : guardUuids) {
        if (stray.getUUID().equals(u)) { tracked = true; break; }
      }
      if (!tracked) stray.discard();
    }
  }

  /** Stand every guard down (solve, fresh scramble, guard-toggle reload, dev {@code guard clear}). */
  private void discardGuards(MinecraftServer server) {
    if (server == null) return;
    ServerLevel level = server.overworld();
    if (level == null) return;
    for (int g = 0; g < GENERATOR_COUNT; g++) {
      if (guardUuids[g] == null) continue;
      Entity e = level.getEntity(guardUuids[g]);
      if (e != null) e.discard();
      guardUuids[g] = null;
    }
    sweepOrphanGuards(level); // belt-and-braces: tagged strays in loaded chunks
  }

  // ── dev/showrunner commands (see CobblemonInitiativeCommands `powerplant`) ─────

  /** {@code powerplant solve} — dev force: all-lit through the SAME solve path (title beat + gate). */
  public int forceSolve(MinecraftServer server) {
    if (!config.isActive()) return 0;
    java.util.Arrays.fill(bulbs, true);
    scrambled = true;
    pushAllBulbStates(server);
    onSolved(server, null);
    return 1;
  }

  /**
   * {@code powerplant flip <switchIndex>} — dev hook driving the SAME toggle path as a lever
   * click. Exists because Carpet bots cannot click levers (the safari `scatter` rationale):
   * headless verification presses switches by index.
   */
  public int flip(MinecraftServer server, int switchIndex, ServerPlayer who) {
    List<PowerPlantConfig.Switch> switches = config.validSwitches();
    if (!config.isActive() || !scrambled || solved) return 0;
    if (switchIndex < 0 || switchIndex >= switches.size()) return 0;
    applySwitch(server, switches.get(switchIndex), who);
    return 1;
  }

  /**
   * {@code powerplant guard spawn <generator>} — dev hook driving the SAME dispatch path as a
   * trio going dark (dedupe and pad clamp included); the command-source player, when present,
   * becomes the anger target. Ignores the trio state on purpose — it tests the golem, not the
   * puzzle.
   */
  public int devGuardSpawn(MinecraftServer server, int generator, ServerPlayer who) {
    if (generator < 0 || generator >= GENERATOR_COUNT) return 0;
    return spawnGuard(server, generator, who) ? 1 : 0;
  }

  /** {@code powerplant guard clear} — dev stand-down: discard live guards + tagged strays. */
  public int devGuardClear(MinecraftServer server) {
    discardGuards(server);
    return 1;
  }

  /** {@code powerplant status} — bits, unlit count, flags, switch count + live coverage warns. */
  public String statusReport() {
    StringBuilder sb = new StringBuilder();
    sb.append("§b[PowerPlant]§7 active=").append(config.isActive())
      .append(" scrambled=").append(scrambled)
      .append(" solved=").append(solved)
      .append(" bulbs=").append(config.bulbs == null ? 0 : config.bulbs.size())
      .append(" switches=").append(config.validSwitches().size())
      .append(config.switches != null && config.switches.size() != config.validSwitches().size()
        ? " §c(" + (config.switches.size() - config.validSwitches().size()) + " invalid)"
        : "");
    int liveGuards = 0;
    for (UUID u : guardUuids) if (u != null) liveGuards++;
    sb.append(" guards=").append(config.generatorGuards
      ? liveGuards + "/" + GENERATOR_COUNT : "§8off§7");
    sb.append("\n§7board: ");
    for (int i = 0; i < bulbs.length; i++) {
      sb.append(bulbs[i] ? "§e" : "§8").append(i).append(bulbs[i] ? "●" : "○").append(' ');
    }
    sb.append("§7(").append(unlitCount()).append(" unlit)");
    for (String warn : config.validate()) {
      sb.append("\n§6warn: §7").append(warn);
    }
    return sb.toString();
  }

  /** {@code powerplant reload} — config hot reload + revalidate (puzzle state untouched).
   *  Guards discard when the reload turns them off or de-latches the engine, so a ModMenu
   *  toggle never strands a live golem. */
  public void reloadConfig() {
    load();
    if (server != null && (!config.generatorGuards || !config.isActive())) {
      discardGuards(server);
    }
  }

  public int unlitCount() {
    int n = 0;
    for (boolean lit : bulbs) if (!lit) n++;
    return n;
  }

  public boolean isSolved() {
    return solved;
  }

  // ── scoreboard mirror (datapack-visible IPC) ───────────────────────────────────

  private static void setRestoredScore(MinecraftServer server, int value) {
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(OBJ);
    if (obj == null) {
      obj = sb.addObjective(OBJ, ObjectiveCriteria.DUMMY,
        Component.literal("Power Plant"), ObjectiveCriteria.RenderType.INTEGER, false, null);
    }
    sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(H_RESTORED), obj).set(value);
  }

  // ── persistence (world dir, Gson, write-through — safari pattern) ──────────────

  private Path stateFile(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(STATE_FILE_NAME);
  }

  private void loadState(MinecraftServer server) {
    Path file = stateFile(server);
    scrambled = false;
    solved = false;
    bulbs = new boolean[PowerPlantConfig.BULB_COUNT];
    if (!Files.exists(file)) return;
    try (Reader reader = Files.newBufferedReader(file)) {
      StateFile data = GSON.fromJson(reader, StateFile.class);
      if (data != null && data.bulbs != null && data.bulbs.length == PowerPlantConfig.BULB_COUNT) {
        scrambled = data.scrambled;
        solved = data.solved;
        bulbs = data.bulbs;
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[PowerPlant] Failed to load {} — starting fresh.", file, e);
    }
  }

  private void saveState(MinecraftServer server) {
    if (server == null) return;
    Path file = stateFile(server);
    StateFile data = new StateFile();
    data.scrambled = scrambled;
    data.solved = solved;
    data.bulbs = bulbs;
    try (Writer writer = Files.newBufferedWriter(file)) {
      GSON.toJson(data, writer);
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[PowerPlant] Failed to save {}.", file, e);
    }
  }
}

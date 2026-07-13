package com.thecompanyinc.cobblemoninitiative.stadium;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Unit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * The Company Exhibition Circuit — the Stadium battle facility.
 *
 * <p>A player picks a level bracket (25/50/75/100) and fights the wave ladder from
 * {@link StadiumConfig}; every combatant is brought to the bracket level via Cobblemon's
 * battle-level lock ({@code BattleFormat.adjustLevel} + rctapi battle rules). With
 * {@code adjustLevel > 0} Cobblemon CLONES the player's party for the battle (clones are
 * flagged uncatchable), so stadium runs are attrition-free: the real party is never
 * touched, and while a run is active {@code NuzlockeInit}'s faint/flee/forfeit handlers
 * early-return on {@link #isStadiumActive} — no faint damage, no whiteout, no sacrifice.
 *
 * <p>HARDCORE INVARIANT: a leaked active flag would disable Nuzlocke everywhere, so
 * every endRun path resets {@code adjustLevel} to 0 and removes the run; the tick also
 * sweeps runs whose player has logged out (belt-and-braces).
 *
 * <p>Wave battles are dispatched through the tbcs command (registry ids are
 * namespace-prefixed: {@code rctmod:<id>} — ENGINE_FINDINGS) and resolved against the
 * run's OWN battle id captured from BattleRegistry right after dispatch. Stadium teams
 * deliberately stay OUT of the TrainerConfig database ({@code data/cobblemon_initiative/
 * trainers/}) so gym-progress name matching can never fire on an exhibition win.
 *
 * <p>Prizes: fixed CobbleDollar purses, printed before each wave is fought (committed
 * amounts are never rolled — ENGINE_FINDINGS §3), and never item/training packs
 * (repeatable content). A full run also increments the {@code stadium_challenged}
 * scoreboard objective — the future Cyber-gym gate counter (Volt's tease; the gate
 * itself is NOT flipped yet, this only maintains the count).
 */
public final class StadiumManager {

  public static final String OBJECTIVE = "stadium_challenged";
  public static final int[] BRACKETS = { 25, 50, 75, 100 };

  /** Ticks before wave 1 after `stadium start` (breathing room to read the schedule). */
  private static final int FIRST_WAVE_DELAY_TICKS = 60;
  /** AWAITING_BATTLE watchdog: give a dispatched battle 5s to appear in the registry. */
  private static final int BATTLE_CAPTURE_TIMEOUT_TICKS = 100;

  private static final Map<UUID, StadiumRunState> activeRuns = new ConcurrentHashMap<>();
  private static StadiumConfig config = new StadiumConfig();

  private StadiumManager() {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────────

  /** Re-read the wave schedule + scalar overrides (the ModMenu save path calls this). */
  public static void reloadConfig() {
    config = StadiumConfig.load();
  }

  public static void init() {
    config = StadiumConfig.load();

    // Priority.LOWEST is load-bearing: NuzlockeInit subscribes these events at NORMAL,
    // and its guards read isStadiumActive. Our handlers END runs (removing the flag) —
    // if they ran first on a stadium loss, Nuzlocke's forfeit branch would see an
    // inactive flag and fire the whiteout kill on a player whose REAL party is healthy
    // (the battle used clones). LOWEST guarantees Nuzlocke's guarded handlers have
    // already run before we clear anything.
    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.LOWEST, StadiumManager::onBattleVictory);
    CobblemonEvents.BATTLE_FLED.subscribe(Priority.LOWEST, StadiumManager::onBattleFled);

    // Server stop mid-run: clear flags + reset adjustLevel. No messages — clients are
    // already disconnecting, and battles die without further events.
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      if (!activeRuns.isEmpty()) {
        InitiativeInit.LOGGER.info(
          "[Stadium] Server stopping — clearing {} active run(s).", activeRuns.size());
        activeRuns.clear();
        resetAdjustLevel();
      }
    });
  }

  /** The NuzlockeInit guard: true while this player has an active stadium run. */
  public static boolean isStadiumActive(UUID playerId) {
    return playerId != null && activeRuns.containsKey(playerId);
  }

  // ── Run control (command entry points) ────────────────────────────────────────

  /** /cobblemon-initiative stadium start <bracket> */
  public static void startRun(ServerPlayer player, int bracket) {
    if (!isValidBracket(bracket)) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] Sanctioned brackets: 25, 50, 75, 100."));
      return;
    }
    if (config.getWaves().isEmpty()) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] The exhibition schedule failed to load. See the server log."));
      return;
    }
    if (activeRuns.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] You are already on the circuit. §7(/cobblemon-initiative stadium abort)"));
      return;
    }
    // Never stack the exhibition onto a live battle — the level lock rides a shared
    // format singleton and run resolution matches battle ids.
    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] Finish your current battle first."));
      return;
    }
    if (!hasBattleReadyPokemon(player)) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] You need at least one healthy Pokémon to register."));
      return;
    }

    activeRuns.put(
      player.getUUID(),
      new StadiumRunState(player.getUUID(), bracket, FIRST_WAVE_DELAY_TICKS));

    // Full purse schedule up front — committed amounts are always printed before the
    // player fights for them (never rolled).
    List<StadiumConfig.Wave> waves = config.getWaves();
    StringBuilder purses = new StringBuilder();
    for (int i = 0; i < waves.size(); i++) {
      if (i > 0) purses.append("§7/");
      purses.append("§e").append(waves.get(i).purse);
    }
    player.sendSystemMessage(Component.literal(
      "§6§l[Stadium]§r §eThe Company Exhibition Circuit. §7Bracket §b" + bracket
        + "§7 — all combatants adjusted to level §b" + bracket + "§7."));
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §7" + waves.size() + " sponsored teams. Wave purses: " + purses
        + " §7CD. Completion bonus: §e" + config.getCompletionPurse() + " §7CD."));
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §8Exhibition rules: your Pokémon fight as insured copies. "
        + "No injuries occur on Company property."));

    InitiativeInit.LOGGER.info(
      "[Stadium] {} started a bracket-{} run.", player.getName().getString(), bracket);
  }

  /** /cobblemon-initiative stadium abort */
  public static void abortRun(ServerPlayer player) {
    StadiumRunState run = activeRuns.get(player.getUUID());
    if (run == null) {
      player.sendSystemMessage(Component.literal(
        "§7[Stadium] You are not on the circuit."));
      return;
    }
    // Refuse while a wave battle is live (or materializing): ending the run mid-battle
    // would drop the Nuzlocke guard while cloned-party faints are still resolving.
    // Fleeing the battle ends the run cleanly with the guard intact.
    if (run.getPhase() != StadiumRunState.Phase.COUNTDOWN) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] The bout is live — flee or finish it, and the run ends with it."));
      return;
    }
    endRun(player, run,
      "§6[Stadium] §7You withdrew from the exhibition. The Company thanks you for your participation.");
  }

  /** /cobblemon-initiative stadium status */
  public static void sendStatus(ServerPlayer player) {
    StadiumRunState run = activeRuns.get(player.getUUID());
    if (run == null) {
      player.sendSystemMessage(Component.literal(
        "§7[Stadium] No active run. §8/cobblemon-initiative stadium start <25|50|75|100>"));
      return;
    }
    int total = config.getWaves().size();
    String phase = switch (run.getPhase()) {
      case COUNTDOWN -> "next wave in " + (Math.max(0, run.getTicksToNextWave()) / 20 + 1) + "s";
      case AWAITING_BATTLE -> "wave forming up";
      case IN_BATTLE -> "wave in progress";
    };
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §7Bracket §b" + run.getBracket() + "§7 — wave §e"
        + (run.getWaveIndex() + 1) + "§7/§e" + total + "§7 (" + phase + ")."));
  }

  // ── Tick (registered from InitiativeInit, like the shrine manager) ────────────

  public static void tick(MinecraftServer server) {
    if (activeRuns.isEmpty()) return;

    Iterator<Map.Entry<UUID, StadiumRunState>> it = activeRuns.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<UUID, StadiumRunState> entry = it.next();
      StadiumRunState run = entry.getValue();
      ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

      // Stale-flag belt: the player logged out mid-run. A lingering entry would keep
      // Nuzlocke disabled for them on rejoin — clear it and reset the format singleton.
      if (player == null) {
        it.remove();
        resetAdjustLevel();
        InitiativeInit.LOGGER.info(
          "[Stadium] Cleared run for offline player {}.", entry.getKey());
        continue;
      }

      switch (run.getPhase()) {
        case COUNTDOWN -> {
          if (run.decrementTicksToNextWave() <= 0) {
            dispatchWave(server, player, run);
          }
        }
        case AWAITING_BATTLE -> {
          PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
          if (battle != null && hasNpcActor(battle)) {
            // The deferred battle creation has read adjustLevel — safe to reset now
            // (see dispatchWave: resetting right after dispatch defeated the lock).
            resetAdjustLevel();
            run.setBattleId(battle.getBattleId());
            run.setPhase(StadiumRunState.Phase.IN_BATTLE);
          } else if (run.incrementCaptureTicks() > BATTLE_CAPTURE_TIMEOUT_TICKS) {
            // Dispatch never produced a battle (unregistered trainer id, refused start).
            InitiativeInit.LOGGER.warn(
              "[Stadium] Wave {} battle never started for {} — ending run.",
              run.getWaveIndex() + 1, player.getName().getString());
            endRun(player, run,
              "§c[Stadium] The exhibition team failed to take the field. The run has been voided.");
          }
        }
        case IN_BATTLE -> {
          // Normally event-driven — but a battle can end WITHOUT firing VICTORY/FLED
          // (admin /stopbattle, runtime-found 2026-07-12). If the player's battle is
          // gone from the registry and no event resolved the wave, the run would hang
          // in IN_BATTLE with the Nuzlocke guard stuck ON. Liveness-check the registry.
          if (BattleRegistry.getBattleByParticipatingPlayer(player) == null) {
            InitiativeInit.LOGGER.warn(
              "[Stadium] Wave {} battle for {} vanished without an event (stopbattle?) — ending run.",
              run.getWaveIndex() + 1, player.getName().getString());
            endRun(player, run,
              "§c[Stadium] The bout was called off. The run has been voided.");
          }
        }
      }
    }
  }

  // ── Wave dispatch ─────────────────────────────────────────────────────────────

  private static void dispatchWave(MinecraftServer server, ServerPlayer player, StadiumRunState run) {
    StadiumConfig.Wave wave = config.getWaves().get(run.getWaveIndex());
    int total = config.getWaves().size();

    // Purse printed BEFORE the fight — the committed amount is fixed, never rolled.
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §7Wave §e" + (run.getWaveIndex() + 1) + "§7/§e" + total + "§7 — §f"
        + wave.displayName + "§7 — purse §e" + wave.purse + " §7CobbleDollars."));

    // TBCS refuses "vs rctmod:<id>" unless the trainer is ATTACHED to a live world
    // entity ("X is not attached to an entity" — runtime-found 2026-07-12). Waves have
    // no NPC bodies, so summon an invisible armor-stand anchor at the player, attach
    // the wave trainer to it, and sweep it again on every endRun path.
    var src = server.createCommandSourceStack().withSuppressedOutput();
    String anchorTag = "ci_stadium_anchor_" + player.getUUID();
    server.getCommands().performPrefixedCommand(src,
      "kill @e[tag=" + anchorTag + "]");
    server.getCommands().performPrefixedCommand(src,
      "execute at " + player.getGameProfile().getName()
        + " run summon minecraft:armor_stand ~2 ~ ~ {Invisible:1b,NoGravity:1b,Tags:[\""
        + anchorTag + "\"]}");
    server.getCommands().performPrefixedCommand(src,
      "tbcs attach rctmod:" + wave.trainerId + " @e[tag=" + anchorTag + ",limit=1]");

    // Level lock (bytecode-verified mechanism): the GEN_9_SINGLES format singleton has a
    // mutable adjustLevel; with it set, Cobblemon clones + flattens the player's party
    // for this battle. The rctapi battle rules flatten the NPC side and heal on entry.
    // IMPORTANT: tbcs creates the battle a tick or two AFTER the dispatch returns (the
    // AWAITING_BATTLE watchdog exists for exactly that reason), so resetting in a
    // finally block here silently disabled the lock (runtime-found 2026-07-12: Furret
    // came out at its authored 50 in a bracket-25 run). The reset instead happens at
    // battle capture (the AWAITING_BATTLE tick) and on every endRun path. Single-player
    // means no concurrent battle can catch the value during that short window.
    // Dispatch AS the player with @s: TBCS resolves a bare name through its trainer
    // NAME registry (misses Carpet bots — "No such trainer is registered"), but @s
    // resolves the entity directly. Player-source dispatch also matches the daycare
    // fee-probe pattern.
    // BOOLEANS MUST BE QUOTED STRINGS (jar-traced 2026-07-12): TBCS's rules argument
    // parses SNBT (TagParser) then re-serializes to string for Gson — a bare `true`
    // becomes ByteTag `1b`, and Gson's Boolean.parseBoolean("1b") yields FALSE, so the
    // adjust flags silently died. A quoted "true" survives the SNBT→Gson round-trip.
    String cmd = "tbcs battle GEN_9_SINGLES @s vs rctmod:" + wave.trainerId
      + " rules {adjustPlayerLevels:\"true\",adjustNPCLevels:\"true\",healPlayers:\"true\"}";
    BattleFormat.Companion.getGEN_9_SINGLES().setAdjustLevel(run.getBracket());
    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withSuppressedOutput().withPermission(2), cmd);

    // Dispatch is synchronous — the battle is normally registered already; the
    // AWAITING_BATTLE tick captures its id (and watchdogs a silent refusal).
    run.setBattleId(null);
    run.resetCaptureTicks();
    run.setPhase(StadiumRunState.Phase.AWAITING_BATTLE);
  }

  // ── Cobblemon event hooks (subscribed in init, Priority.LOWEST) ───────────────

  private static Unit onBattleVictory(BattleVictoryEvent event) {
    UUID battleId = event.getBattle().getBattleId();
    if (battleId == null || activeRuns.isEmpty()) return Unit.INSTANCE;

    List<BattleActor> everyone = new ArrayList<>();
    everyone.addAll(event.getWinners());
    everyone.addAll(event.getLosers());

    for (BattleActor actor : everyone) {
      if (!(actor instanceof PlayerBattleActor playerActor)) continue;
      ServerPlayer player = playerActor.getEntity();
      if (player == null) continue;

      StadiumRunState run = activeRuns.get(player.getUUID());
      if (run == null || !battleId.equals(run.getBattleId())) continue;

      boolean playerWon = event.getWinners().stream().anyMatch(
        w -> w instanceof PlayerBattleActor p && p.getEntity() == player);
      if (playerWon) {
        onWaveWon(player.getServer(), player, run);
      } else {
        endRun(player, run,
          "§6[Stadium] §cSwept. §7The exhibition ends here — but no injuries occur on "
            + "Company property. Your Pokémon are untouched.");
      }
      return Unit.INSTANCE;
    }
    return Unit.INSTANCE;
  }

  private static Unit onBattleFled(BattleFledEvent event) {
    PlayerBattleActor playerActor = event.getPlayer();
    ServerPlayer player = playerActor.getEntity();
    if (player == null) return Unit.INSTANCE;

    StadiumRunState run = activeRuns.get(player.getUUID());
    UUID battleId = event.getBattle().getBattleId();
    if (run == null || battleId == null || !battleId.equals(run.getBattleId())) {
      return Unit.INSTANCE;
    }

    endRun(player, run,
      "§6[Stadium] §7You withdrew mid-bout. The run is closed — no purse, no penalty.");
    return Unit.INSTANCE;
  }

  // ── Outcomes ──────────────────────────────────────────────────────────────────

  private static void onWaveWon(MinecraftServer server, ServerPlayer player, StadiumRunState run) {
    StadiumConfig.Wave wave = config.getWaves().get(run.getWaveIndex());
    payPurse(server, player, wave.purse);
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §aWave " + (run.getWaveIndex() + 1) + " cleared. §e" + wave.purse
        + " §aCobbleDollars credited."));

    int nextWave = run.getWaveIndex() + 1;
    if (nextWave >= config.getWaves().size()) {
      completeRun(server, player, run);
      return;
    }

    run.setWaveIndex(nextWave);
    run.setBattleId(null);
    run.setTicksToNextWave(config.getTicksBetweenWaves());
    run.setPhase(StadiumRunState.Phase.COUNTDOWN);
  }

  private static void completeRun(MinecraftServer server, ServerPlayer player, StadiumRunState run) {
    payPurse(server, player, config.getCompletionPurse());
    int record = incrementStadiumChallenged(server, player);
    player.sendSystemMessage(Component.literal(
      "§6§l[Stadium] §a§lCircuit complete! §r§eCompletion bonus " + config.getCompletionPurse()
        + " §eCobbleDollars credited."));
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §7The Company notes your performance. Exhibition record: §b" + record + "§7."));
    InitiativeInit.LOGGER.info(
      "[Stadium] {} completed a bracket-{} run (record {}).",
      player.getName().getString(), run.getBracket(), record);

    activeRuns.remove(run.getPlayerId());
    resetAdjustLevel();
  }

  private static void endRun(ServerPlayer player, StadiumRunState run, String message) {
    // ALWAYS: clear the flag and reset the format singleton — a leaked flag would
    // disable Nuzlocke everywhere; a leaked adjustLevel would flatten gym battles.
    activeRuns.remove(run.getPlayerId());
    resetAdjustLevel();
    // Sweep this run's battle anchor (see dispatchWave) — harmless if none exists.
    MinecraftServer server = player != null ? player.getServer() : null;
    if (server != null) {
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withSuppressedOutput(),
        "kill @e[tag=ci_stadium_anchor_" + run.getPlayerId() + "]");
    }
    if (player != null && message != null) {
      player.sendSystemMessage(Component.literal(message));
    }
    InitiativeInit.LOGGER.info(
      "[Stadium] Run ended for {} at wave {} (bracket {}).",
      player != null ? player.getName().getString() : run.getPlayerId(),
      run.getWaveIndex() + 1, run.getBracket());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private static boolean isValidBracket(int bracket) {
    for (int b : BRACKETS) {
      if (b == bracket) return true;
    }
    return false;
  }

  private static boolean hasBattleReadyPokemon(ServerPlayer player) {
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (Pokemon pokemon : party) {
      if (pokemon != null && !pokemon.isFainted()) return true;
    }
    return false;
  }

  private static boolean hasNpcActor(PokemonBattle battle) {
    for (BattleActor actor : battle.getActors()) {
      if (actor.getType() == ActorType.NPC) return true;
    }
    return false;
  }

  /**
   * Fixed CobbleDollar purse via the CobbleDollars command (grammar bytecode-verified:
   * {@code give <targets> <amount>}, selector/name-first; there is no {@code add}).
   */
  private static void payPurse(MinecraftServer server, ServerPlayer player, int amount) {
    if (amount <= 0) return;
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withSuppressedOutput(),
      "cobbledollars give " + player.getGameProfile().getName() + " " + amount);
  }

  /**
   * Increments the player's {@code stadium_challenged} score (mirrors DexScoreManager's
   * ensure-then-write pattern) and returns the new value. This is the counter the Cyber
   * gym gate will read once the Stadium ships for real — do NOT flip that gate here.
   */
  private static int incrementStadiumChallenged(MinecraftServer server, ServerPlayer player) {
    Objective objective = server.getScoreboard().getObjective(OBJECTIVE);
    if (objective == null) {
      objective = server.getScoreboard().addObjective(
        OBJECTIVE,
        ObjectiveCriteria.DUMMY,
        Component.literal("Stadium Circuits"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }
    return server.getScoreboard().getOrCreatePlayerScore(player, objective).add(1);
  }

  /**
   * Belt-and-braces reset of the shared format singleton. The only writer is
   * {@link #dispatchWave}'s try/finally, so this is normally a no-op — but the
   * singleton backs every gym battle, so endRun paths re-assert it anyway.
   */
  private static void resetAdjustLevel() {
    BattleFormat.Companion.getGEN_9_SINGLES().setAdjustLevel(0);
  }
}

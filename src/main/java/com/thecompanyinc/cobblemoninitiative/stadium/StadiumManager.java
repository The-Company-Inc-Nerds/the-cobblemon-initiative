package com.thecompanyinc.cobblemoninitiative.stadium;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
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

  /**
   * The exhibition is a three-Pokémon format: registration needs at least this many, and
   * the player's FIRST three party slots represent them (party order = the selection —
   * reorder with {@code stadium team}). See {@link #selectTeam} and the NOTE-1 design flag.
   */
  public static final int STADIUM_TEAM_SIZE = 3;

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
    // NOTE 1 (playtest + follow-up): the exhibition is a THREE-Pokémon format. Neither TBCS nor
    // Cobblemon exposes a team-size cap to the command layer, so the SELECTION is "bring exactly
    // three" — registration is REFUSED unless the party holds exactly three; the player picks
    // which three at a PC. occupied() = filterNotNull().size() (jar-verified), gaps excluded.
    int partyCount = partyCount(player);
    if (partyCount != STADIUM_TEAM_SIZE) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] The exhibition is a three-Pokémon format. You have §e" + partyCount
          + "§c — go to a PC and set your party to exactly §e" + STADIUM_TEAM_SIZE
          + "§c (the three you want to field), then come back."));
      return;
    }

    StadiumRunState run =
      new StadiumRunState(player.getUUID(), bracket, FIRST_WAVE_DELAY_TICKS);
    // FIXED ARENA: remember where the player registered so the arena tp is undone when the
    // run ends (captured whether or not an arena is configured — cheap, and future-proof).
    run.setReturn(
      player.getX(), player.getY(), player.getZ(),
      player.getYRot(), player.getXRot());
    activeRuns.put(player.getUUID(), run);

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

    // NOTE 1: your first three party Pokémon represent you. Print them and how to reorder,
    // while there is still time before the first wave dispatches (COUNTDOWN only).
    sendTeamPreview(player);

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

  /**
   * /cobblemon-initiative stadium team &lt;a&gt; &lt;b&gt; &lt;c&gt; — pick which three Pokémon
   * represent you by moving those party slots (1-based) into positions 1-3. The battle
   * fields your party in order, so this IS the selection (see the NOTE-1 design flag: a
   * hard three-only cap / in-battle picker needs TBCS or a Cobblemon battle rule and is
   * NOT implemented here — the exhibition currently fields your whole party led by these
   * three). Only permitted before the first wave dispatches.
   */
  public static void selectTeam(ServerPlayer player, int a, int b, int c) {
    StadiumRunState run = activeRuns.get(player.getUUID());
    if (run == null) {
      player.sendSystemMessage(Component.literal(
        "§7[Stadium] No active run. §8Start one, then pick your three."));
      return;
    }
    if (run.getPhase() != StadiumRunState.Phase.COUNTDOWN || run.getWaveIndex() > 0
        || run.hasMovedToArena()) {
      player.sendSystemMessage(Component.literal(
        "§c[Stadium] Team is locked once the circuit is underway. Set it before wave 1."));
      return;
    }
    int[] picks = { a, b, c };
    // Validate: 1-based, in range, distinct, and each slot actually holds a Pokémon.
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    java.util.Set<Integer> seen = new java.util.HashSet<>();
    for (int p : picks) {
      if (p < 1 || p > 6 || !seen.add(p)) {
        player.sendSystemMessage(Component.literal(
          "§c[Stadium] Pick three DISTINCT party slots (1-6), e.g. §e/cobblemon-initiative "
            + "stadium team 1 2 3§c."));
        return;
      }
      if (party.get(p - 1) == null) {
        player.sendSystemMessage(Component.literal(
          "§c[Stadium] Party slot §e" + p + "§c is empty."));
        return;
      }
    }
    // Move each pick into positions 1-3 by swapping. Do it left-to-right: after swapping
    // pick i into slot i, later picks that referenced slot i now live where pick i was —
    // so re-resolve each pick's CURRENT slot by identity before swapping.
    Pokemon[] chosen = { party.get(picks[0] - 1), party.get(picks[1] - 1), party.get(picks[2] - 1) };
    for (int target = 0; target < chosen.length; target++) {
      int current = currentSlotOf(party, chosen[target]);
      if (current < 0 || current == target) continue;
      party.swap(new PartyPosition(target), new PartyPosition(current));
    }
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §aTeam set. §7These lead your exhibition roster:"));
    sendTeamPreview(player);
  }

  /** Current 0-based slot of a specific Pokémon instance in the party, or -1. */
  private static int currentSlotOf(PlayerPartyStore party, Pokemon target) {
    if (target == null) return -1;
    for (int i = 0; i < 6; i++) {
      if (party.get(i) == target) return i;
    }
    return -1;
  }

  /** Print the party's first {@link #STADIUM_TEAM_SIZE} Pokémon + the reorder hint. */
  private static void sendTeamPreview(ServerPlayer player) {
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    StringBuilder line = new StringBuilder("§6[Stadium] §7Your three: ");
    for (int i = 0; i < STADIUM_TEAM_SIZE; i++) {
      Pokemon p = party.get(i);
      if (i > 0) line.append("§7, ");
      line.append("§b").append(i + 1).append(".§f")
        .append(p != null ? p.getSpecies().getName() : "—");
    }
    player.sendSystemMessage(Component.literal(line.toString()));
    player.sendSystemMessage(Component.literal(
      "§6[Stadium] §8Reorder before wave 1 with §7/cobblemon-initiative stadium team <a> <b> <c>§8 "
        + "(party slots 1-6)."));
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
        sweepBody(server, entry.getKey());
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

    // FIXED ARENA (2026-08-06): when both spots are configured, teleport the player to the
    // player battle spot (P1) facing the arena and spawn the opponent body at the npc spot
    // (P2). Otherwise keep the legacy fight-where-you-stand placement. The
    // player is moved before the FIRST wave only (subsequent waves keep them at P1); the
    // pre-run position is restored on every run-end path (endRun / completeRun).
    StadiumConfig.Spot playerSpot = config.getPlayerSpot();
    StadiumConfig.Spot npcSpot = config.getNpcSpot();
    boolean fixedArena = config.hasFixedArena();
    if (fixedArena && !run.hasMovedToArena()) {
      float pyaw = playerSpot.yaw != null ? playerSpot.yaw : player.getYRot();
      server.getCommands().performPrefixedCommand(
        player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
        "tp @s " + playerSpot.x + " " + playerSpot.y + " " + playerSpot.z
          + " " + pyaw + " 0");
      run.setMovedToArena(true);
    }

    // TBCS refuses "vs rctmod:<id>" unless the trainer is ATTACHED to a live world
    // entity ("X is not attached to an entity" — runtime-found 2026-07-12). Waves had no
    // NPC bodies, so early builds summoned an INVISIBLE armor stand — which showed nothing
    // (playtest: "Stadium easy npc not appearing"). Now spawn a VISIBLE, themed Easy NPC
    // opponent body via `import_new` (a real easy_npc:humanoid, TBCS-attachable exactly
    // like the dojo duel bodies — ENGINE_FINDINGS: tbcs attach = TrainerNPC.setEntity), and
    // sweep it on every endRun path. The preset ships a shared finder Tag
    // (ci_stadium_opponent) and carries NO dialog, so a right-click can never launch a
    // rogue battle — the wave battle is dispatched from Java below.
    var src = server.createCommandSourceStack().withSuppressedOutput();
    String bodyTag = "ci_stadium_body_" + player.getUUID();
    // Kill any body left from the previous wave (per-player tag) before spawning the next.
    sweepBody(server, player.getUUID());

    String preset = config.bodyPresetForWave(run.getWaveIndex());
    double bx, by, bz;
    float byaw;
    if (fixedArena) {
      bx = npcSpot.x; by = npcSpot.y; bz = npcSpot.z;
      byaw = npcSpot.yaw != null ? npcSpot.yaw : 0.0f;
    } else {
      // Legacy fight-where-you-stand: 2 blocks in front of the player, facing them.
      double rad = Math.toRadians(player.getYRot());
      bx = player.getX() - Math.sin(rad) * 2.0;
      by = player.getY();
      bz = player.getZ() + Math.cos(rad) * 2.0;
      byaw = player.getYRot() + 180.0f; // face back toward the player
    }

    // import_new spawns from the FULL preset NBT — including the baked finder Tag
    // (ENGINE_FINDINGS: only import_new gets vanilla Tags; the uuid-import path drops
    // them). Anchor the source at the arena level+pos so the body never lands in the
    // wrong dimension (mirrors NobleEncounterManager.spawnBody).
    var spawnSrc = server.createCommandSourceStack().withPermission(4).withSuppressedOutput()
      .withLevel(player.serverLevel())
      .withPosition(new net.minecraft.world.phys.Vec3(bx, by, bz));
    server.getCommands().performPrefixedCommand(spawnSrc, String.format(java.util.Locale.ROOT,
      "easy_npc preset import_new data %s %.2f %.2f %.2f", preset, bx, by, bz));

    // Tag the just-spawned body with this run's per-player kill tag, face it, and freeze
    // its exact position (the import can land it a fraction off on uneven ground).
    // import_new is synchronous server-side (ENGINE_FINDINGS: addFreshEntity before the
    // command returns), so the body is in getAllEntities() this tick.
    net.minecraft.world.entity.Entity body =
      findBodyByTag(player.serverLevel(), "ci_stadium_opponent", bodyTag);
    if (body == null) {
      // Spawn failed — void the wave cleanly rather than dispatch a battle that TBCS will
      // refuse ("not attached to an entity"). endRun sweeps any partial body + resets state.
      InitiativeInit.LOGGER.warn(
        "[Stadium] Wave {} opponent body ({}) failed to spawn for {} — voiding run.",
        run.getWaveIndex() + 1, preset, player.getName().getString());
      endRun(player, run,
        "§c[Stadium] The exhibition team failed to take the field. The run has been voided.");
      return;
    }
    body.addTag(bodyTag);
    body.moveTo(bx, by, bz, byaw, 0.0f);
    body.setYHeadRot(byaw);
    server.getCommands().performPrefixedCommand(src,
      "tbcs attach rctmod:" + wave.trainerId + " @e[tag=" + bodyTag + ",limit=1]");

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

    // Sweep the beaten body now so it doesn't linger through the between-wave countdown;
    // the next dispatchWave spawns a fresh, correctly-themed body.
    sweepBody(server, run.getPlayerId());

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
    // Sweep the last wave's opponent body — a circuit completes on a win, so a body is
    // always live at this point (see dispatchWave / onWaveWon).
    if (server != null) sweepBody(server, run.getPlayerId());
    restoreReturnPosition(player, run);
  }

  private static void endRun(ServerPlayer player, StadiumRunState run, String message) {
    // ALWAYS: clear the flag and reset the format singleton — a leaked flag would
    // disable Nuzlocke everywhere; a leaked adjustLevel would flatten gym battles.
    activeRuns.remove(run.getPlayerId());
    resetAdjustLevel();
    // Sweep this run's opponent body (see dispatchWave) — harmless if none exists.
    MinecraftServer server = player != null ? player.getServer() : null;
    if (server != null) {
      sweepBody(server, run.getPlayerId());
    }
    if (player != null && message != null) {
      player.sendSystemMessage(Component.literal(message));
    }
    restoreReturnPosition(player, run);
    InitiativeInit.LOGGER.info(
      "[Stadium] Run ended for {} at wave {} (bracket {}).",
      player != null ? player.getName().getString() : run.getPlayerId(),
      run.getWaveIndex() + 1, run.getBracket());
  }

  /**
   * FIXED ARENA: put the player back where they registered, if they were ever tp'd into
   * the arena this run. No-op for legacy fight-where-you-stand runs (movedToArena never
   * set) and for an offline player (their next battle simply resolves in place). Uses the
   * command tp for parity with the rest of the class; the player stays in the overworld.
   */
  private static void restoreReturnPosition(ServerPlayer player, StadiumRunState run) {
    if (player == null || !run.hasMovedToArena() || run.getReturnPos() == null) return;
    double[] pos = run.getReturnPos();
    MinecraftServer server = player.getServer();
    if (server == null) return;
    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
      "tp @s " + pos[0] + " " + pos[1] + " " + pos[2]
        + " " + run.getReturnYaw() + " " + run.getReturnPitch());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private static boolean isValidBracket(int bracket) {
    for (int b : BRACKETS) {
      if (b == bracket) return true;
    }
    return false;
  }

  /** Number of real Pokémon in the player's party (gaps excluded — occupied()). */
  private static int partyCount(ServerPlayer player) {
    return Cobblemon.INSTANCE.getStorage().getParty(player).occupied();
  }

  private static boolean hasNpcActor(PokemonBattle battle) {
    for (BattleActor actor : battle.getActors()) {
      if (actor.getType() == ActorType.NPC) return true;
    }
    return false;
  }

  /**
   * Find a freshly-spawned opponent body: the FIRST live entity carrying the shared
   * finder tag {@code sharedTag} that has NOT yet been claimed by a per-player kill tag
   * {@code claimedTag}. Single-player + one body-per-player means this is unambiguous
   * (mirrors NobleEncounterManager.findBodyByTag).
   */
  private static net.minecraft.world.entity.Entity findBodyByTag(
      net.minecraft.server.level.ServerLevel level, String sharedTag, String claimedTag) {
    if (level == null) return null;
    for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
      if (e.isAlive() && e.getTags().contains(sharedTag) && !e.getTags().contains(claimedTag)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Remove a player's stadium opponent body. Uses Easy NPC's clean deregister first, then
   * a hard kill as belt-and-braces — a lingering body would spoof a beaten opponent still
   * standing in the arena. Safe to call when no body exists.
   */
  private static void sweepBody(MinecraftServer server, UUID playerId) {
    if (server == null) return;
    var src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    String bodyTag = "ci_stadium_body_" + playerId;
    // easy_npc delete takes an EasyNPC entity-selector argument (getEntitiesWithAccess,
    // jar-verified) — a tag selector resolves cleanly and "Nothing to delete!" no-ops when
    // absent. The hard kill is belt-and-braces in case the body was already deregistered.
    server.getCommands().performPrefixedCommand(src, "easy_npc delete @e[tag=" + bodyTag + "]");
    server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + bodyTag + "]");
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

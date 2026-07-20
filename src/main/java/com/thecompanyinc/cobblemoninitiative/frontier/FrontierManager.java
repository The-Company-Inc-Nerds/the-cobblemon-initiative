package com.thecompanyinc.cobblemoninitiative.frontier;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * REAL gameplay mechanics for the seven Battle Frontier halls (showrunner directive
 * 2026-07-19 — "I want this stuff to be actual gameplay mechanics"; forks ruled the
 * same day). Every hall's fiction now cashes mechanically:
 *
 * <ul>
 *   <li><b>Factory</b> — rental teams: the player's party is PARKED in hall custody
 *       (daycare persistence pattern, write-through) and three Company-issue loaners are
 *       granted; {@code factory return} claws the loaners and restores the party.</li>
 *   <li><b>Arcade</b> — the wheel: {@code arcade spin} rolls a battle condition
 *       (level lock / doubles flip / purse multiplier — conditions only, never the
 *       wallet, per ruling); the next {@code arcade fight} dispatches under it.</li>
 *   <li><b>Castle</b> — castle points: a 60-point budget per run; healing costs 20,
 *       scouting the next team costs 15; leftover points pay out ×15 CD after the
 *       Castle Lord falls.</li>
 *   <li><b>Market</b> — priced opponents: buy the fight off the stall (bargain /
 *       fair / premium listings — fee via the deferred pay-probe rail).</li>
 *   <li><b>Port</b> — crew battles: GEN_9_MULTI 2v2, the player + an AI deckhand
 *       vs the rival pair (the Fighting-gym MULTI emitter finally earning its keep).</li>
 *   <li><b>Pyramid</b> — the ancients' gauntlet: three sequential fights with NO
 *       heals between (the hydra dispatcher pattern, healing off).</li>
 *   <li><b>Tower</b> — the streak climb: nine floors including the main floor —
 *       eight fights at escalating level locks, the Tycoon at the top at his authored
 *       strength; a loss resets the climb.</li>
 * </ul>
 *
 * <p>Shared machinery (all bytecode/runtime-proven elsewhere in this mod): anchored
 * tbcs dispatch (Stadium — TBCS refuses unattached trainers), the AWAITING-capture
 * adjustLevel reset timing (Stadium: resetting right after dispatch silently disables
 * the lock; level-locked fights ride Cobblemon's clone-and-flatten, so they are also
 * attrition-free), Java-side purse payment + defeat-tag grants on the victory event
 * (so the wheel's multipliers and the climb's per-floor purses stay in one place),
 * and brain wins still firing the shipped {@code frontier/hall_cleared} chain.
 * Everything happens on the exhibition floor — {@code frontier_active} keeps the
 * Nuzlocke guards suspended, so hardcore stays safe throughout.
 */
public class FrontierManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String CUSTODY_FILE_NAME = "cobblemon_initiative_factory.json";

  // ── Tunables (showrunner retune here; tables not JSON for now — one place) ──────

  /** Arcade base purses (challenger / brain) before the wheel multiplier. */
  private static final int ARCADE_PURSE_CHALLENGER = 500;
  private static final int ARCADE_PURSE_BRAIN = 1500;

  /** Castle: budget per run + costs + the leftover-point exchange rate. */
  private static final int CASTLE_BUDGET = 60;
  private static final int CASTLE_HEAL_COST = 20;
  private static final int CASTLE_SCOUT_COST = 15;
  private static final int CASTLE_POINT_RATE_CD = 15;
  private static final String CASTLE_PTS_OBJECTIVE = "frontier_castle_pts";

  /** Market listings: fee / level lock (0 = authored) / purse. */
  private static final Map<String, int[]> MARKET_LISTINGS = Map.of(
    "bargain", new int[] {200, 40, 400},
    "fair", new int[] {500, 70, 1200},
    "premium", new int[] {1000, 0, 3000}
  );

  /** Port crew battle: the AI deckhand partner + purse. */
  private static final String PORT_PARTNER = "port_deckhand";
  private static final int PORT_CREW_PURSE = 1500;

  /** Pyramid gauntlet stages (no heals between) — the ancients, then the giant.
   *  Purses are the authored prizes from the retired dialog battle blocks. */
  private static final String[] PYRAMID_STAGES = {
    "pyramid_challenger_1", "pyramid_challenger_2", "frontier_brain_pyramid"
  };
  private static final int[] PYRAMID_PURSES = {800, 800, 3000};

  /** Tower floors 2..9: trainer / level lock (0 = authored) / purse. Floor 9 is the
   *  Tycoon at full authored strength — the climb's fights are level-locked clones
   *  (attrition-free); the summit is real. */
  private static final String[] TOWER_TRAINERS = {
    "tower_challenger_1", "tower_challenger_2", "tower_challenger_1",
    "tower_challenger_2", "tower_challenger_1", "tower_challenger_2",
    "tower_challenger_1", "frontier_brain_tower"
  };
  private static final int[] TOWER_LOCKS = {40, 48, 56, 64, 72, 80, 88, 0};
  private static final int[] TOWER_PURSES = {300, 400, 500, 600, 700, 800, 900, 1200};
  private static final String TOWER_FLOOR_OBJECTIVE = "frontier_tower_floor";

  /** Factory loaner team — Company-issue, deterministic (givemon property strings). */
  private static final String[] FACTORY_LOANERS = {
    "metagross level=70 ability=clearbody moves=meteormash,zenheadbutt,bulletpunch,agility",
    "arcanine level=70 ability=intimidate moves=flareblitz,extremespeed,crunch,willowisp",
    "gyarados level=70 ability=intimidate moves=waterfall,icefang,dragondance,earthquake"
  };

  /** The arcade wheel's slices (conditions only — never the wallet, per ruling). */
  private enum WheelSlice {
    LOCK_50("§bLEVEL 50§7 — both sides flattened to 50"),
    LOCK_75("§bLEVEL 75§7 — both sides flattened to 75"),
    LOCK_100("§bLEVEL 100§7 — both sides raised to 100"),
    DOUBLES("§dDOUBLES§7 — the fight runs 2v2"),
    PURSE_X2("§6PURSE ×2§7 — winnings doubled"),
    PURSE_X3("§6PURSE ×3§7 — winnings tripled");

    final String announce;
    WheelSlice(String announce) { this.announce = announce; }
  }

  // ── State ─────────────────────────────────────────────────────────────────────

  /** One manager-dispatched fight, keyed by battle id once captured. */
  private static final class Dispatch {
    final UUID playerId;
    final String hall;
    final String trainerId;
    final String format;   // GEN_9_SINGLES / GEN_9_DOUBLES / GEN_9_MULTI
    final int lock;        // 0 = no level lock
    final int purse;
    UUID battleId;
    int captureTicks;
    boolean captured;
    int goneTicks;

    Dispatch(UUID playerId, String hall, String trainerId, String format, int lock, int purse) {
      this.playerId = playerId;
      this.hall = hall;
      this.trainerId = trainerId;
      this.format = format;
      this.lock = lock;
      this.purse = purse;
    }
  }

  private static final int CAPTURE_TIMEOUT_TICKS = 100;
  /** Captured dispatch whose battle has been GONE this long = dead entry (see tick). */
  private static final int BATTLE_GONE_TICKS = 40;

  /** Per-player live dispatch (one fight at a time per player). */
  private final Map<UUID, Dispatch> dispatches = new ConcurrentHashMap<>();
  /** Per-player scheduled next fight (pyramid/tower chains), ticks until dispatch. */
  private final Map<UUID, Integer> pendingChain = new ConcurrentHashMap<>();
  /** Per-player pyramid stage index (transient — a relog resets the gauntlet). */
  private final Map<UUID, Integer> pyramidStage = new ConcurrentHashMap<>();
  /** Per-player armed wheel condition (consumed by the next arcade fight). */
  private final Map<UUID, WheelSlice> wheelArmed = new ConcurrentHashMap<>();
  /** Players whose market fee probe is dispatched — resolve #fr_ok next tick. */
  private final Map<UUID, String> pendingMarket = new ConcurrentHashMap<>();

  /** Factory custody: parked party (serialized) + granted loaner uuids. */
  public static class ParkedParty {
    List<JsonObject> partyJsons = new ArrayList<>();
    List<String> loanerUuids = new ArrayList<>();
  }

  private final Map<UUID, ParkedParty> custody = new HashMap<>();
  /** Anchor-stand tags spawned per player — swept by exact tag on every fight end. */
  private final Map<UUID, List<String>> anchorTags = new ConcurrentHashMap<>();
  private MinecraftServer server;

  // ── Wiring ────────────────────────────────────────────────────────────────────

  public void registerEvents() {
    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.LOWEST, event -> {
      UUID battleId = event.getBattle().getBattleId();
      for (var actor : event.getBattle().getActors()) {
        if (!(actor instanceof PlayerBattleActor playerActor)) continue;
        ServerPlayer player = playerActor.getEntity();
        if (player == null) continue;
        Dispatch d = dispatches.get(player.getUUID());
        if (d == null || !battleId.equals(d.battleId)) continue;
        boolean won = event.getWinners().stream().anyMatch(
          w -> w instanceof PlayerBattleActor p && p.getEntity() == player);
        dispatches.remove(player.getUUID());
        resetLock(d.format);
        sweepAnchors(player.getUUID());
        if (won) {
          onFightWon(player, d);
        } else {
          onFightLost(player, d);
        }
      }
      return Unit.INSTANCE;
    });

    CobblemonEvents.BATTLE_FLED.subscribe(Priority.LOWEST, event -> {
      // Fleeing a hall fight = a loss for chain purposes (free leave still holds —
      // no fee, no Nuzlocke fallout on the exhibition floor).
      for (var player : event.getBattle().getPlayers()) {
        Dispatch d = dispatches.get(player.getUUID());
        if (d != null && event.getBattle().getBattleId().equals(d.battleId)) {
          dispatches.remove(player.getUUID());
          resetLock(d.format);
          sweepAnchors(player.getUUID());
          onFightLost(player, d);
        }
      }
      return Unit.INSTANCE;
    });
  }

  public void onServerStarted(MinecraftServer server) {
    this.server = server;
    loadCustody(server);
  }

  public void onServerStopping(MinecraftServer server) {
    saveCustody(server);
  }

  /** Join reminder: a parked party survives relogs — nudge the player back. */
  public void onPlayerJoin(ServerPlayer player) {
    if (custody.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal(
        "§6[Factory] §7Your own team is still in hall custody. Return to the Foreman "
          + "and run §f/cobblemon-initiative frontier factory return§7 when done."));
    }
  }

  public void tick(MinecraftServer server) {
    this.server = server;

    // Deferred market fee resolution (safari pay-probe contract: read next tick).
    if (!pendingMarket.isEmpty()) {
      List<Map.Entry<UUID, String>> pend = new ArrayList<>(pendingMarket.entrySet());
      pendingMarket.clear();
      for (var e : pend) {
        ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
        if (player != null) resolveMarketFee(player, e.getValue());
      }
    }

    // Chain delays (pyramid stages / tower floors).
    Iterator<Map.Entry<UUID, Integer>> it = pendingChain.entrySet().iterator();
    while (it.hasNext()) {
      var e = it.next();
      ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
      if (player == null) { it.remove(); continue; }
      int left = e.getValue() - 1;
      if (left > 0) { e.setValue(left); continue; }
      it.remove();
      chainNext(player);
    }

    // Capture watchdogs — same contract as Stadium's AWAITING_BATTLE.
    for (Dispatch d : dispatches.values()) {
      ServerPlayer player = server.getPlayerList().getPlayer(d.playerId);
      if (player == null) { dispatches.remove(d.playerId); resetLock(d.format); continue; }
      var battle = BattleRegistry.getBattleByParticipatingPlayer(player);
      if (!d.captured) {
        if (battle != null) {
          d.battleId = battle.getBattleId();
          d.captured = true;
          resetLock(d.format); // deferred reset — the battle has read adjustLevel now
        } else if (++d.captureTicks > CAPTURE_TIMEOUT_TICKS) {
          dispatches.remove(d.playerId);
          resetLock(d.format);
          sweepAnchors(d.playerId);
          player.sendSystemMessage(Component.literal(
            "§c[Frontier] §7The fight failed to take the floor. Try again."));
        }
        continue;
      }
      // Self-heal (live-caught 2026-07-19): a battle that vanishes WITHOUT a victory/
      // flee event — stopbattle fires no event (jar-traced), showdown wedge, harness
      // abort — left the captured dispatch behind forever, and the one-fight guards
      // then locked the player out of every hall. Two seconds with no matching live
      // battle means the entry is dead; a pyramid mid-gauntlet keeps its stage and
      // resumes via pyramid start.
      if (battle != null && battle.getBattleId().equals(d.battleId)) {
        d.goneTicks = 0;
      } else if (++d.goneTicks > BATTLE_GONE_TICKS) {
        dispatches.remove(d.playerId);
        sweepAnchors(d.playerId);
        InitiativeInit.LOGGER.info(
          "[Frontier] Cleared a dead dispatch ({} vs {}) — battle ended without an event.",
          player.getGameProfile().getName(), d.trainerId);
      }
    }
  }

  // ── Hall commands ─────────────────────────────────────────────────────────────

  /** frontier arcade spin — arm a wheel condition for the next arcade fight. */
  public int arcadeSpin(ServerPlayer player) {
    WheelSlice[] slices = WheelSlice.values();
    WheelSlice hit = slices[player.serverLevel().random.nextInt(slices.length)];
    wheelArmed.put(player.getUUID(), hit);
    player.sendSystemMessage(Component.literal("§6§l[The Wheel] §r§7clatters… slows… lands on:"));
    player.sendSystemMessage(Component.literal("§l  ➤ " + hit.announce));
    player.sendSystemMessage(Component.literal(
      "§7The condition rides your NEXT arcade fight. House rules: the wheel spins once per fight."));
    return 1;
  }

  /** frontier arcade fight <1|2|brain> — dispatch under the armed condition. */
  public int arcadeFight(ServerPlayer player, String slot) {
    WheelSlice slice = wheelArmed.remove(player.getUUID());
    if (slice == null) {
      player.sendSystemMessage(Component.literal(
        "§c[Arcade] §7No stake on the table — spin the wheel first."));
      return 0;
    }
    String trainerId = switch (slot) {
      case "1" -> "arcade_challenger_1";
      case "2" -> "arcade_challenger_2";
      case "brain" -> "frontier_brain_arcade";
      default -> null;
    };
    if (trainerId == null || alreadyBeaten(player, trainerId)) {
      player.sendSystemMessage(Component.literal("§c[Arcade] §7That table is closed."));
      return 0;
    }
    int base = "brain".equals(slot) ? ARCADE_PURSE_BRAIN : ARCADE_PURSE_CHALLENGER;
    String format = slice == WheelSlice.DOUBLES ? "GEN_9_DOUBLES" : "GEN_9_SINGLES";
    int lock = switch (slice) {
      case LOCK_50 -> 50; case LOCK_75 -> 75; case LOCK_100 -> 100; default -> 0;
    };
    int purse = switch (slice) {
      case PURSE_X2 -> base * 2; case PURSE_X3 -> base * 3; default -> base;
    };
    dispatch(player, new Dispatch(player.getUUID(), "arcade", trainerId, format, lock, purse));
    return 1;
  }

  /** frontier castle enter|heal|scout|claim — the points economy. */
  public int castle(ServerPlayer player, String action) {
    Scoreboard sb = player.getServer().getScoreboard();
    Objective obj = ensureObjective(sb, CASTLE_PTS_OBJECTIVE);
    var score = sb.getOrCreatePlayerScore(player, obj);
    switch (action) {
      case "enter" -> {
        // Once the Lord has yielded, the ledger never reopens — an enter→claim loop
        // would otherwise be an infinite 60-point (900 CD) faucet.
        if (player.getTags().contains("defeated_frontier_brain_castle")) {
          player.sendSystemMessage(Component.literal(
            "§c[Castle] §7The house does not extend credit to its conqueror. The ledger is closed."));
          return 0;
        }
        score.set(CASTLE_BUDGET);
        player.sendSystemMessage(Component.literal(
          "§6§l[Castle] §r§7Old money, older rules. Your ledger opens at §e" + CASTLE_BUDGET
            + " points§7. Healing costs §e" + CASTLE_HEAL_COST + "§7, scouting §e"
            + CASTLE_SCOUT_COST + "§7. Points left after the Lord falls pay §e"
            + CASTLE_POINT_RATE_CD + " CD§7 each."));
      }
      case "heal" -> {
        if (score.get() < CASTLE_HEAL_COST) {
          player.sendSystemMessage(Component.literal("§c[Castle] §7The ledger does not cover it."));
          return 0;
        }
        score.set(score.get() - CASTLE_HEAL_COST);
        runAsConsole("healpokemon " + player.getGameProfile().getName());
        player.sendSystemMessage(Component.literal(
          "§6[Castle] §7The house physician attends. §e-" + CASTLE_HEAL_COST
            + "§7 (balance §e" + score.get() + "§7)."));
      }
      case "scout" -> {
        if (score.get() < CASTLE_SCOUT_COST) {
          player.sendSystemMessage(Component.literal("§c[Castle] §7The ledger does not cover it."));
          return 0;
        }
        String next = !player.getTags().contains("defeated_castle_challenger_1")
          ? "castle_challenger_1"
          : !player.getTags().contains("defeated_castle_challenger_2")
            ? "castle_challenger_2" : "frontier_brain_castle";
        List<String> species = readTeamSpecies(next);
        score.set(score.get() - CASTLE_SCOUT_COST);
        player.sendSystemMessage(Component.literal(
          "§6[Castle] §7Intel, discreetly: §f" + String.join("§7, §f", species)
            + "§7. §e-" + CASTLE_SCOUT_COST + "§7 (balance §e" + score.get() + "§7)."));
      }
      case "claim" -> {
        if (!player.getTags().contains("defeated_frontier_brain_castle")) {
          player.sendSystemMessage(Component.literal(
            "§c[Castle] §7The ledger settles when the Castle Lord has yielded."));
          return 0;
        }
        int pts = Math.max(0, score.get());
        int cd = pts * CASTLE_POINT_RATE_CD;
        score.set(0);
        if (cd > 0) runAsConsole("cobbledollars give " + player.getGameProfile().getName() + " " + cd);
        player.sendSystemMessage(Component.literal(
          "§6§l[Castle] §r§7Ledger closed: §e" + pts + "§7 points banked → §e" + cd + " CD§7."));
      }
      default -> { return 0; }
    }
    return 1;
  }

  /** frontier market buy <listing> — priced opponents via the deferred fee probe. */
  public int marketBuy(ServerPlayer player, String listing) {
    int[] spec = MARKET_LISTINGS.get(listing);
    if (spec == null) return 0;
    if (dispatches.containsKey(player.getUUID()) || pendingMarket.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal("§c[Market] §7One purchase at a time."));
      return 0;
    }
    // The ladder is 1 → 2 → brain, so a defeated brain means the stall is empty.
    // Refuse BEFORE the fee probe — otherwise the fee is charged and the dispatch
    // then refuses on alreadyBeaten, eating the player's CD for nothing.
    if (player.getTags().contains("defeated_frontier_brain_market")) {
      player.sendSystemMessage(Component.literal(
        "§c[Market] §7Sold out — you bought every fight the stall had."));
      return 0;
    }
    dispatchFeeProbe(player, spec[0]);
    pendingMarket.put(player.getUUID(), listing);
    return 1;
  }

  private void resolveMarketFee(ServerPlayer player, String listing) {
    int[] spec = MARKET_LISTINGS.get(listing);
    if (!readFeeProbe()) {
      player.sendSystemMessage(Component.literal(
        "§c[Market] §7Declined — the stall does not run tabs. (§e" + spec[0] + " CD§7)"));
      return;
    }
    String trainerId = !player.getTags().contains("defeated_market_challenger_1")
      ? "market_challenger_1"
      : !player.getTags().contains("defeated_market_challenger_2")
        ? "market_challenger_2" : "frontier_brain_market";
    player.sendSystemMessage(Component.literal(
      "§6[Market] §7Sold. The §f" + listing + "§7 listing takes the floor — purse §e"
        + spec[2] + " CD§7."));
    dispatch(player, new Dispatch(player.getUUID(), "market", trainerId,
      "GEN_9_SINGLES", spec[1], spec[2]));
  }

  /** frontier port crew — the 2v2 MULTI: player + deckhand vs the rival pair. */
  public int portCrew(ServerPlayer player) {
    // Refuses only when BOTH tags are set: a legacy half-pair save (one tag from the
    // old singles wiring) can still muster and complete the pair — the win grants both.
    if (player.getTags().contains("defeated_port_challenger_1")
      && player.getTags().contains("defeated_port_challenger_2")) {
      player.sendSystemMessage(Component.literal(
        "§c[Port] §7The rival crew already struck their colors — the Admiral holds the pier head."));
      return 0;
    }
    if (dispatches.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal("§c[Frontier] §7One fight at a time."));
      return 0;
    }
    Dispatch d = new Dispatch(player.getUUID(), "port_crew",
      "port_challenger_1", "GEN_9_MULTI", 0, PORT_CREW_PURSE);
    // MULTI needs THREE attached trainers: the partner + both opponents.
    String a1 = anchor(player, PORT_PARTNER, 1.5, 0);
    String a2 = anchor(player, "port_challenger_1", -2, 2);
    String a3 = anchor(player, "port_challenger_2", 2, 2);
    runAsConsole("tbcs attach rctmod:" + PORT_PARTNER + " @e[tag=" + a1 + ",limit=1]");
    runAsConsole("tbcs attach rctmod:port_challenger_1 @e[tag=" + a2 + ",limit=1]");
    runAsConsole("tbcs attach rctmod:port_challenger_2 @e[tag=" + a3 + ",limit=1]");
    player.sendSystemMessage(Component.literal(
      "§6§l[Port] §r§7A crew sails together. Deckhand Maru takes your flank — the rival pair takes the dock."));
    runAsPlayer(player, "tbcs battle GEN_9_MULTI @s rctmod:" + PORT_PARTNER
      + " vs rctmod:port_challenger_1 rctmod:port_challenger_2");
    dispatches.put(player.getUUID(), d);
    return 1;
  }

  /** frontier pyramid start — the no-heal gauntlet (heals ONCE at the door). */
  public int pyramidStart(ServerPlayer player) {
    if (player.getTags().contains("defeated_frontier_brain_pyramid")) {
      player.sendSystemMessage(Component.literal("§c[Pyramid] §7The ancients already knelt."));
      return 0;
    }
    if (pyramidStage.containsKey(player.getUUID())) {
      // Mid-gauntlet re-entry: never a fresh start — a re-run here would hand out a
      // free door-heal (defeating the no-heal rule) and desync the stage counter.
      if (dispatches.containsKey(player.getUUID())
        || pendingChain.containsKey(player.getUUID())) {
        player.sendSystemMessage(Component.literal(
          "§c[Pyramid] §7The gauntlet is already underway. There is no second door."));
        return 0;
      }
      // Nothing in flight but a stage is held (relog / failed dispatch): resume the
      // chain where it stopped — no heal; the dark remembers what you carried in.
      player.sendSystemMessage(Component.literal(
        "§6[Pyramid] §7The dark takes you back where it left you. §cNo respite.§7"));
      scheduleChain(player, 40);
      return 1;
    }
    // Fresh gauntlet: strip stage defeat tags from a prior broken run — dispatch()
    // exempts pyramid from alreadyBeaten, but dialogs read these for beaten lanes.
    String name = player.getGameProfile().getName();
    runAsConsole("tag " + name + " remove defeated_pyramid_challenger_1");
    runAsConsole("tag " + name + " remove defeated_pyramid_challenger_2");
    pyramidStage.put(player.getUUID(), 0);
    runAsConsole("healpokemon " + name);
    player.sendSystemMessage(Component.literal(
      "§6§l[Pyramid] §r§7Three ancients, then the giant. §cNo healing between fights§7 — "
        + "what you carry out of each is what you carry into the next."));
    scheduleChain(player, 40);
    return 1;
  }

  /** frontier pyramid abandon — walk out of a held gauntlet (next start is fresh).
   *  Production: relog/wedge recovery without waiting on the resume path. Harness:
   *  the ONLY way to reset a held stage between runs (the stage map is in-memory,
   *  keyed by uuid — it survives everything short of a server restart). */
  public int pyramidAbandon(ServerPlayer player) {
    if (dispatches.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal(
        "§c[Pyramid] §7The dark does not release mid-fight. Finish or fall."));
      return 0;
    }
    pendingChain.remove(player.getUUID());
    if (pyramidStage.remove(player.getUUID()) != null) {
      player.sendSystemMessage(Component.literal(
        "§6[Pyramid] §7You walk back into the light. The gauntlet forgets you — start again at the door."));
    } else {
      player.sendSystemMessage(Component.literal("§7[Pyramid] §7Nothing holds you here."));
    }
    return 1;
  }

  /** frontier tower climb — dispatch the current floor's fight. */
  public int towerClimb(ServerPlayer player) {
    if (player.getTags().contains("defeated_frontier_brain_tower")) {
      player.sendSystemMessage(Component.literal("§c[Tower] §7There is no promotion above the one you hold."));
      return 0;
    }
    Scoreboard sb = player.getServer().getScoreboard();
    Objective obj = ensureObjective(sb, TOWER_FLOOR_OBJECTIVE);
    int floor = sb.getOrCreatePlayerScore(player, obj).get(); // 0-based fight index
    if (floor >= TOWER_TRAINERS.length) floor = TOWER_TRAINERS.length - 1;
    player.sendSystemMessage(Component.literal(
      "§6§l[Tower] §r§7Floor §e" + (floor + 2) + "§7 of 9 — "
        + (TOWER_LOCKS[floor] > 0
            ? "both sides flattened to §e" + TOWER_LOCKS[floor] + "§7"
            : "§cthe Tycoon fights at full strength§7")
        + ". Purse §e" + TOWER_PURSES[floor] + " CD§7. A loss resets the climb."));
    dispatch(player, new Dispatch(player.getUUID(), "tower", TOWER_TRAINERS[floor],
      "GEN_9_SINGLES", TOWER_LOCKS[floor], TOWER_PURSES[floor]));
    return 1;
  }

  /** frontier factory start — park the party, hand over the loaners. */
  public int factoryStart(ServerPlayer player) {
    if (custody.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal("§c[Factory] §7Your team is already in custody."));
      return 0;
    }
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<Pokemon> own = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      Pokemon p = party.get(i);
      if (p != null) own.add(p);
    }
    if (own.isEmpty()) {
      player.sendSystemMessage(Component.literal("§c[Factory] §7Nothing to park."));
      return 0;
    }
    ParkedParty parked = new ParkedParty();
    for (Pokemon p : own) {
      parked.partyJsons.add(p.saveToJSON(player.getServer().registryAccess(), new JsonObject()));
    }
    for (Pokemon p : own) party.remove(p);
    for (String props : FACTORY_LOANERS) {
      try {
        Pokemon loaner = PokemonProperties.Companion.parse(props).create();
        loaner.heal();
        parked.loanerUuids.add(loaner.getUuid().toString());
        party.add(loaner);
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("[Factory] loaner grant failed: {}", props, e);
      }
    }
    custody.put(player.getUUID(), parked);
    saveCustody(player.getServer());
    player.sendSystemMessage(Component.literal(
      "§6§l[Factory] §r§7Your team is logged into custody. Three Company-issue units on "
        + "loan — borrowed steel wins or it does not. Run "
        + "§f/cobblemon-initiative frontier factory return§7 to swap back."));
    return 1;
  }

  /** frontier factory return — claw the loaners, restore the parked party. */
  public int factoryReturn(ServerPlayer player) {
    ParkedParty parked = custody.get(player.getUUID());
    if (parked == null) {
      player.sendSystemMessage(Component.literal("§c[Factory] §7Nothing of yours is in custody."));
      return 0;
    }
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (int i = 5; i >= 0; i--) {
      Pokemon p = party.get(i);
      if (p != null && parked.loanerUuids.contains(p.getUuid().toString())) {
        party.remove(p);
      }
    }
    int restored = 0;
    for (JsonObject json : parked.partyJsons) {
      try {
        Pokemon p = Pokemon.Companion.loadFromJSON(player.getServer().registryAccess(), json);
        party.add(p); // built-in PC fallback if slots are somehow full
        restored++;
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("[Factory] restore failed for one mon", e);
      }
    }
    custody.remove(player.getUUID());
    saveCustody(player.getServer());
    player.sendSystemMessage(Component.literal(
      "§6§l[Factory] §r§7Loan closed. §e" + restored + "§7 of your own returned to your belt."));
    return 1;
  }

  // ── Chain + outcome logic ─────────────────────────────────────────────────────

  private void scheduleChain(ServerPlayer player, int delayTicks) {
    pendingChain.put(player.getUUID(), delayTicks);
  }

  private void chainNext(ServerPlayer player) {
    Integer stage = pyramidStage.get(player.getUUID());
    if (stage == null || stage >= PYRAMID_STAGES.length) return;
    String trainerId = PYRAMID_STAGES[stage];
    player.sendSystemMessage(Component.literal(
      "§6[Pyramid] §7" + (stage < 2 ? "An ancient rises." : "§lThe giant wakes.")));
    dispatch(player, new Dispatch(player.getUUID(), "pyramid", trainerId,
      "GEN_9_SINGLES", 0, PYRAMID_PURSES[stage]));
  }

  private void onFightWon(ServerPlayer player, Dispatch d) {
    String name = player.getGameProfile().getName();
    // Defeat tags keep every existing dialog gate working; brains still fire the
    // shipped hall_cleared chain (counter + warden stir).
    runAsConsole("tag " + name + " add defeated_" + d.trainerId);
    if ("port_crew".equals(d.hall)) {
      runAsConsole("tag " + name + " add defeated_port_challenger_2");
    }
    if (d.trainerId.startsWith("frontier_brain_")) {
      String hall = d.trainerId.substring("frontier_brain_".length());
      runAsConsole("tag " + name + " add frontier_" + hall + "_cleared");
      runAsConsole("execute as " + name + " run function cobblemon_initiative:frontier/hall_cleared");
    }
    if (d.purse > 0) {
      runAsConsole("cobbledollars give " + name + " " + d.purse);
      player.sendSystemMessage(Component.literal("§6[Frontier] §7Purse paid: §e" + d.purse + " CD§7."));
    }
    switch (d.hall) {
      case "pyramid" -> {
        int next = pyramidStage.merge(player.getUUID(), 1, Integer::sum);
        if (next < PYRAMID_STAGES.length) {
          player.sendSystemMessage(Component.literal(
            "§6[Pyramid] §7It kneels. §cNo respite§7 — the next stirs."));
          scheduleChain(player, 60);
        } else {
          pyramidStage.remove(player.getUUID());
          player.sendSystemMessage(Component.literal(
            "§6§l[Pyramid] §r§7The three ancients kneel and the giant with them."));
        }
      }
      case "tower" -> {
        Scoreboard sb = player.getServer().getScoreboard();
        var score = sb.getOrCreatePlayerScore(player, ensureObjective(sb, TOWER_FLOOR_OBJECTIVE));
        int floor = score.get() + 1;
        if (floor < TOWER_TRAINERS.length) {
          score.set(floor);
          player.sendSystemMessage(Component.literal(
            "§6[Tower] §7The lift hums. Floor §e" + (floor + 2)
              + "§7 is open — §f/ca frontier tower climb§7 when ready."));
        } else {
          score.set(0);
          player.sendSystemMessage(Component.literal(
            "§6§l[Tower] §r§7Top floor. There is no promotion above this one."));
        }
      }
      default -> { /* arcade / market / port: single fights, nothing to chain */ }
    }
  }

  private void onFightLost(ServerPlayer player, Dispatch d) {
    switch (d.hall) {
      case "pyramid" -> {
        pyramidStage.remove(player.getUUID());
        player.sendSystemMessage(Component.literal(
          "§c[Pyramid] §7The dark keeps what it catches. The gauntlet resets — start again at the door."));
      }
      case "tower" -> {
        Scoreboard sb = player.getServer().getScoreboard();
        sb.getOrCreatePlayerScore(player, ensureObjective(sb, TOWER_FLOOR_OBJECTIVE)).set(0);
        player.sendSystemMessage(Component.literal(
          "§c[Tower] §7Held. The climb resets to the main floor — the roster remembers."));
      }
      default -> player.sendSystemMessage(Component.literal(
        "§7[Frontier] §7The floor holds no grudge. Try again when ready."));
    }
  }

  // ── Dispatch core (Stadium idioms) ────────────────────────────────────────────

  private void dispatch(ServerPlayer player, Dispatch d) {
    if (dispatches.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal("§c[Frontier] §7One fight at a time."));
      return;
    }
    // Tower + pyramid re-fight their roster by design (the climb repeats trainers;
    // the gauntlet restarts after a loss with stale stage tags possible) — their own
    // entry points hold the real latches (brain defeat tags).
    if (alreadyBeaten(player, d.trainerId)
      && !"tower".equals(d.hall) && !"pyramid".equals(d.hall)) {
      player.sendSystemMessage(Component.literal("§c[Frontier] §7That opponent already yielded."));
      return;
    }
    String tag = anchor(player, d.trainerId, 2, 0);
    runAsConsole("tbcs attach rctmod:" + d.trainerId + " @e[tag=" + tag + ",limit=1]");
    if (d.lock > 0) setLock(d.format, d.lock);
    // BOOLEANS AS QUOTED STRINGS (TBCS SNBT→Gson round-trip — jar-traced 2026-07-12).
    String rules = d.lock > 0
      ? " rules {adjustPlayerLevels:\"true\",adjustNPCLevels:\"true\",healPlayers:\"true\"}"
      : "";
    runAsPlayer(player, "tbcs battle " + d.format + " @s vs rctmod:" + d.trainerId + rules);
    dispatches.put(player.getUUID(), d);
  }

  /** Summon a per-player, per-trainer anchor stand near the player; returns its tag. */
  private String anchor(ServerPlayer player, String trainerId, double dx, double dz) {
    String tag = "ci_frontier_anchor_" + player.getUUID() + "_" + trainerId;
    runAsConsole("kill @e[tag=" + tag + "]");
    runAsConsole("execute at " + player.getGameProfile().getName()
      + " run summon minecraft:armor_stand ~" + dx + " ~ ~" + dz
      + " {Invisible:1b,NoGravity:1b,Tags:[\"" + tag + "\"]}");
    anchorTags.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(tag);
    return tag;
  }

  private void sweepAnchors(UUID playerId) {
    List<String> tags = anchorTags.remove(playerId);
    if (tags == null || server == null) return;
    for (String tag : tags) {
      runAsConsole("kill @e[tag=" + tag + "]");
    }
  }

  private void setLock(String format, int level) {
    switch (format) {
      case "GEN_9_DOUBLES" -> BattleFormat.Companion.getGEN_9_DOUBLES().setAdjustLevel(level);
      case "GEN_9_MULTI" -> BattleFormat.Companion.getGEN_9_MULTI().setAdjustLevel(level);
      default -> BattleFormat.Companion.getGEN_9_SINGLES().setAdjustLevel(level);
    }
  }

  private void resetLock(String format) {
    switch (format) {
      case "GEN_9_DOUBLES" -> BattleFormat.Companion.getGEN_9_DOUBLES().setAdjustLevel(0);
      case "GEN_9_MULTI" -> BattleFormat.Companion.getGEN_9_MULTI().setAdjustLevel(0);
      default -> BattleFormat.Companion.getGEN_9_SINGLES().setAdjustLevel(0);
    }
  }

  private boolean alreadyBeaten(ServerPlayer player, String trainerId) {
    return player.getTags().contains("defeated_" + trainerId);
  }

  // ── Fee probe (safari deferred contract, own #fr_ok holder) ───────────────────

  private void dispatchFeeProbe(ServerPlayer player, int fee) {
    Scoreboard sb = player.getServer().getScoreboard();
    ensureObjective(sb, "cd_calc");
    sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#fr_ok"),
      sb.getObjective("cd_calc")).set(0);
    runAsPlayer(player, "function cobblemon_initiative:frontier/fee {fee:" + fee + "}");
  }

  private boolean readFeeProbe() {
    Scoreboard sb = server.getScoreboard();
    Objective calc = sb.getObjective("cd_calc");
    if (calc == null) return false;
    return sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#fr_ok"), calc).get() >= 1;
  }

  // ── Plumbing ──────────────────────────────────────────────────────────────────

  private Objective ensureObjective(Scoreboard sb, String name) {
    Objective obj = sb.getObjective(name);
    if (obj == null) {
      obj = sb.addObjective(name, ObjectiveCriteria.DUMMY,
        Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, true, null);
    }
    return obj;
  }

  private void runAsConsole(String cmd) {
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withSuppressedOutput(), cmd);
  }

  private void runAsPlayer(ServerPlayer player, String cmd) {
    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withSuppressedOutput().withPermission(2), cmd);
  }

  /** Species list from a bundled rctmod team (the castle scout's intel). */
  private List<String> readTeamSpecies(String trainerId) {
    List<String> out = new ArrayList<>();
    String path = "data/rctmod/trainers/" + trainerId + ".json";
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
      if (in == null) return out;
      JsonObject o = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
      for (var el : o.getAsJsonArray("team")) {
        String s = el.getAsJsonObject().get("species").getAsString();
        out.add(s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1));
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.warn("[Castle] scout read failed for {}", trainerId, e);
    }
    return out;
  }

  // ── Custody persistence (daycare write-through pattern) ───────────────────────

  private Path custodyPath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(CUSTODY_FILE_NAME);
  }

  private void loadCustody(MinecraftServer server) {
    custody.clear();
    Path path = custodyPath(server);
    if (!Files.exists(path)) return;
    try (Reader r = Files.newBufferedReader(path)) {
      Type type = new TypeToken<Map<UUID, ParkedParty>>() {}.getType();
      Map<UUID, ParkedParty> loaded = GSON.fromJson(r, type);
      if (loaded != null) custody.putAll(loaded);
      InitiativeInit.LOGGER.info("[Factory] Loaded {} parked part{}.",
        custody.size(), custody.size() == 1 ? "y" : "ies");
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[Factory] custody load failed", e);
    }
  }

  private void saveCustody(MinecraftServer server) {
    try (Writer w = Files.newBufferedWriter(custodyPath(server))) {
      GSON.toJson(custody, w);
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("[Factory] custody save failed", e);
    }
  }
}

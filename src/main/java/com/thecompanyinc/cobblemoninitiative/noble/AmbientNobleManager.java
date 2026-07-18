package com.thecompanyinc.cobblemoninitiative.noble;

import com.cobblemon.mod.common.battles.BattleRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.NobleConfig;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Ambient / wandering noble spawns (showrunner 2026-07-17: "a few wandering random-spawning
 * ones"). A once-per-in-game-day roll over a config list ({@code ambient_nobles.json}): for
 * each online player, if they carry the gate tag, are inside the roll's area, are past the
 * per-player cooldown, and are NOT already in a noble/Cobblemon battle, roll the chance and —
 * on a hit — call the SAME {@link NobleEncounterManager#start} the dialogs use. This unifies
 * two things from one system:
 * <ul>
 *   <li>the post-HQ-raid <b>bird ambushes</b> (Moltres/Articuno/Zapdos) — the noble's own JSON
 *       carries a FIXED arena at its town, so it spawns there when the player is nearby;</li>
 *   <li><b>wandering mini-nobles</b> — the noble's JSON omits {@code arena.center}, so
 *       {@code start()} spawns it at the player's feet (they "wander into" you on a route).</li>
 * </ul>
 *
 * <p>Per-player cooldown is a plain scoreboard ({@code noble_roam_<id>} = last fire day), so it
 * survives relog with no custom storage file. Mirrors the tick/day-clock patterns of
 * NuzlockeInit + HomesteadManager. No new Fabric hooks — wired into NobleEncounterInit's
 * existing END_SERVER_TICK.
 */
public class AmbientNobleManager {

  private static final Gson GSON = new GsonBuilder().create();
  private static final String CONFIG_PATH = "data/cobblemon_initiative/ambient_nobles.json";

  private List<Roll> rolls = new ArrayList<>();
  private long lastRollDay = Long.MIN_VALUE;
  private int tickCounter = 0;
  /** Proximity re-arm: player uuid -> nobleId -> game-time of the last spawn. */
  private final java.util.Map<UUID, java.util.Map<String, Long>> lastProxSpawn = new java.util.HashMap<>();

  private static final int PROXIMITY_CADENCE = 40; // check proximity rolls every 2s

  // ── Config records (Gson-populated) ──────────────────────────────────────────

  public static final class Config {
    List<Roll> rolls;
  }

  public static final class Roll {
    String nobleId;            // must be a NobleEncounterManager.NOBLE_IDS entry
    Area area;                 // where the PLAYER must be (not where it spawns)
    String gateTag;            // required player tag (e.g. "defeated_villain_boss"); null = none
    /**
     * "daily" (default) — one random roll per in-game day (wandering minis).
     * "proximity" — a RELIABLE recurring town event (the birds): while the player is in the
     * area, not resolved (no {@code defeated_noble_<id>} score) and past the re-arm window,
     * the noble attacks. No chance; it is "always attacking the town" until you catch/defeat
     * it. Re-arm keeps a flee/loss from re-triggering instantly.
     */
    String trigger = "daily";
    int rearmMinutes = 4;       // proximity: minutes before it can re-attack after a spawn
    double dailyChance = 0.3;   // daily: per-eligible-day probability
    int cooldownDays = 2;       // daily: days before it can re-roll for the same player
    boolean oncePerRun = false; // daily: fires at most once ever per player
  }

  public static final class Area {
    String dim = "minecraft:overworld";
    int[] center;              // [x,y,z]
    int radius = 128;          // horizontal (2D column, any Y)
  }

  // ── Lifecycle ────────────────────────────────────────────────────────────────

  public void load() {
    rolls = new ArrayList<>();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH)) {
      if (in == null) {
        InitiativeInit.LOGGER.info("No ambient_nobles.json — ambient noble rolls disabled.");
        return;
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        Config cfg = GSON.fromJson(reader, Config.class);
        if (cfg != null && cfg.rolls != null) {
          for (Roll r : cfg.rolls) {
            if (r != null && r.nobleId != null && r.area != null && r.area.center != null
                && r.area.center.length == 3) {
              rolls.add(r);
            }
          }
        }
        InitiativeInit.LOGGER.info("Loaded {} ambient noble roll(s).", rolls.size());
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load ambient_nobles.json", e);
    }
  }

  // ── Tick ─────────────────────────────────────────────────────────────────────

  public void tick(MinecraftServer server) {
    if (rolls.isEmpty()) return;
    NobleConfig nc = NobleConfig.get();
    if (!nc.isNoblesEnabled()) return;
    if (!nc.isWanderingMinisEnabled() && !nc.isTownBirdAttacksEnabled()) return;

    long day = server.overworld().getDayTime() / 24000L;
    boolean newDay = day != lastRollDay;
    if (newDay) lastRollDay = day;
    boolean proximityTick = (++tickCounter % PROXIMITY_CADENCE == 0);
    if (!newDay && !proximityTick) return; // nothing scheduled this tick

    NobleEncounterManager nobles = NobleEncounterInit.getManager();
    if (nobles == null) return;
    RandomSource rng = server.overworld().getRandom();
    Scoreboard board = server.getScoreboard();
    long gameTime = server.overworld().getGameTime();

    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      // Never interrupt an active noble or any Cobblemon battle.
      if (nobles.hasActive(player.getUUID())) continue;
      if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) continue;

      for (Roll roll : rolls) {
        boolean proximity = "proximity".equals(roll.trigger);
        if (proximity ? !proximityTick : !newDay) continue; // wrong schedule for this roll
        if (roll.gateTag != null && !roll.gateTag.isEmpty()
            && !player.getTags().contains(roll.gateTag)) {
          continue;
        }
        if (!inArea(player, roll.area)) continue;

        if (proximity) {
          if (!nc.isTownBirdAttacksEnabled()) continue;
          // "Always attacking the town" — reliable, until you resolve it. Stop once the
          // noble is caught/defeated (its storyFlag score is set).
          if (scoreOf(board, player, "defeated_noble_" + roll.nobleId) >= 1) continue;
          long last = lastProxSpawn
            .getOrDefault(player.getUUID(), java.util.Collections.emptyMap())
            .getOrDefault(roll.nobleId, Long.MIN_VALUE);
          // Cooldown is the ModMenu global (falls back to the per-roll JSON if unset/0).
          int cdMin = nc.getTownBirdCooldownMinutes() > 0 ? nc.getTownBirdCooldownMinutes() : roll.rearmMinutes;
          if (gameTime - last < cdMin * 1200L) continue; // 1200 ticks/min
          if (nobles.start(player, roll.nobleId)) {
            lastProxSpawn.computeIfAbsent(player.getUUID(), k -> new java.util.HashMap<>())
              .put(roll.nobleId, gameTime);
            break; // one ambient spawn per player per pass
          }
        } else {
          if (!nc.isWanderingMinisEnabled()) continue;
          // Daily wandering roll (mini-nobles).
          Objective obj = latchObjective(board, roll.nobleId);
          ScoreAccess score = board.getOrCreatePlayerScore(player, obj);
          int lastDay = score.get();
          if (roll.oncePerRun && lastDay > 0) continue;
          if (lastDay > 0 && (day - lastDay) < roll.cooldownDays) continue;
          if (rng.nextDouble() > roll.dailyChance * nc.getAmbientChanceMultiplier()) continue;
          if (nobles.start(player, roll.nobleId)) {
            score.set((int) Math.max(1L, day));
            break;
          }
        }
      }
    }
  }

  /** Read a player's scoreboard value in an objective (0 if the objective is absent). */
  private static int scoreOf(Scoreboard board, ServerPlayer player, String objective) {
    Objective obj = board.getObjective(objective);
    if (obj == null) return 0;
    return board.getOrCreatePlayerScore(player, obj).get();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private static Objective latchObjective(Scoreboard board, String nobleId) {
    String name = "noble_roam_" + nobleId;
    Objective obj = board.getObjective(name);
    if (obj == null) {
      obj = board.addObjective(
        name, ObjectiveCriteria.DUMMY,
        net.minecraft.network.chat.Component.literal(name),
        ObjectiveCriteria.RenderType.INTEGER, false, null);
    }
    return obj;
  }

  private static boolean inArea(ServerPlayer player, Area area) {
    ServerLevel level = player.serverLevel();
    if (!level.dimension().location().toString().equals(area.dim)) return false;
    double dx = player.getX() - (area.center[0] + 0.5);
    double dz = player.getZ() - (area.center[2] + 0.5);
    return (dx * dx + dz * dz) <= ((double) area.radius * area.radius);
  }
}

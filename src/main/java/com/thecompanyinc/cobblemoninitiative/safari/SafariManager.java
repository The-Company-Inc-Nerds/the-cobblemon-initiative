package com.thecompanyinc.cobblemoninitiative.safari;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kotlin.Unit;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * The Ridgewatch Preserve — post-liberation safari ROUNDS (2026-08-04 rework of the old
 * "Day Permit" design). A paid, timed, CATCH-ONLY custody game: the round fee buys a
 * fixed kit (marked Safari Balls, bait for every standard table, snowballs) while the
 * player's ENTIRE party and inventory sit in Preserve custody. Bait lures wild Pokémon
 * to the spot; stealth (crouch in grass/leaves cover) keeps them from spooking;
 * snowballs weaken; a bait offering befriends. CAPTURE rounds keep the catches;
 * CONTEST rounds appraise them for rarity points and release them.
 *
 * <p>Pure CobbleDollars sink: the round fee rides the shipped pay-probe idiom
 * ({@code safari/permit_fee.mcfunction}, gate on {@code store result} — CobbleDollars
 * {@code pay} soft-fails). No payouts inside, ever.
 *
 * <p>Hardcore invariants (the load-bearing safety items):
 * <ul>
 *   <li>BATTLE_STARTED_PRE is cancelled at {@link Priority#HIGHEST} while any player
 *       actor holds an active session — no battle can start, so no faint, flee-kill,
 *       sacrifice, or whiteout path is reachable inside a round.</li>
 *   <li>CUSTODY IS CRASH-SAFE BY ORDERING: the custody record (full party JSON +
 *       per-slot inventory SNBT) is written through to
 *       {@code cobblemon_initiative_safari_custody.json} (atomic temp-file move; a
 *       failed write ABORTS round entry with a fee refund) BEFORE the player is
 *       mutated. Death/logout leave the record pending; join/respawn handlers sweep
 *       any marked round gear and restore it. A completed restore only MARKS the
 *       record {@code restored} — Cobblemon persists party/PC stores on a ~30s
 *       interval, so the record survives until a graceful stop (or the next round's
 *       write-through) as the recovery artifact for a crash inside that window.
 *       Nothing the round does can lose a mon or an item.</li>
 *   <li>No damage path exists here: session ends are teleports only, and the clock
 *       escort's destination is the recorded ENTRY position (ground the player
 *       verifiably stood on) unless the config pins an eject pad.</li>
 * </ul>
 *
 * <p>Rounds are keyed on the "Safari Zone" SafeZone POLYGON (exact since the a-series
 * polygon containment landed in NuzlockeConfig): entry requires standing inside it,
 * leaving it mid-round starts a return countdown, and natural spawns are suppressed
 * inside it (NaturalSpawnGuard) so every catch comes off a bait table.
 */
public class SafariManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final String STATS_FILE_NAME = "cobblemon_initiative_safari.json";
  private static final String CUSTODY_FILE_NAME = "cobblemon_initiative_safari_custody.json";

  /** Exact SafeZone name the round geometry keys on (absent on bare-mod worlds). */
  private static final String SAFARI_ZONE_NAME = "Safari Zone";

  /** Entity tag on every manager-spawned lure — swept on session end + server start. */
  public static final String LURE_TAG = "ci_safari_lure";

  /** Scoreboard objective mirroring LIFETIME safari catches (datapack-visible). */
  public static final String CATCH_OBJECTIVE = "ci_safari_catches";

  /** Custom-data key marking round-issue gear (balls + snowballs; gate clawback target). */
  private static final String BALL_MARKER = "ci_safari_issue";

  /** Custom-data key carrying a bait item's table id. */
  private static final String BAIT_MARKER = "ci_bait";

  private static final String SAFARI_BALL_ID = "cobblemon:safari_ball";

  /**
   * The kiosk-only premium table — never in the round kit. The kit itself is
   * DATA-DRIVEN: baitPerTable of every OTHER loaded lure table (see resolvePendingPermit),
   * so the bundled data's 3 standard tables issue 6 kit bait without a hardcoded list.
   */
  private static final String KIOSK_ONLY_TABLE = "executive_blend";

  /** Max blocks a lure spawn position walks DOWN from the scatter spot to find ground. */
  private static final int GROUND_SNAP_RANGE = 8;

  /** Grace extension when a lure's window ends mid-capture (ball shaking). */
  private static final int BUSY_GRACE_TICKS = 40;

  /** Wrap-up countdown once the last marked ball is spent. */
  private static final int BALLS_GRACE_TICKS = 100;

  /** During active rounds, re-run the hostile discard sweep this often. */
  private static final int HOSTILE_SWEEP_INTERVAL_TICKS = 100;

  private static final int MILESTONE_FIRST = 10;
  private static final int MILESTONE_SECOND = 25;

  private SafariConfig config = new SafariConfig();
  private SafariLureTables lureTables = new SafariLureTables();

  private final Map<UUID, SafariSession> sessions = new HashMap<>();

  // ── Lifetime persistence (world dir, PlayerProgressManager pattern, write-through) ──

  private static class LifetimeStats {

    int lifetimeCatches = 0;
    boolean milestone10 = false;
    boolean milestone25 = false;
    int bestContestScore = 0;
  }

  private final Map<UUID, LifetimeStats> lifetime = new HashMap<>();

  // ── Custody (world dir, DaycareManager pattern: JSON is truth, write-through) ──────

  /** One item in custody: exact slot in its container list, serialized as SNBT. */
  public static class CustodyItem {

    int slot;
    /** main | armor | offhand */
    String container;
    String snbt;
  }

  /**
   * One player's round custody. Written to disk BEFORE the party/inventory are taken;
   * a completed restore MARKS it {@link #restored} rather than deleting it (the
   * restored stores are only durable after Cobblemon's next interval save — until a
   * graceful stop, this record is the crash-recovery artifact for that window).
   * {@link #livePokemon} is the same-process cache of the removed party (endRound
   * never re-deserializes); after a crash the JSON is the only copy and the restore
   * loads from it.
   */
  public static class CustodyRecord {

    String mode;
    String dimension;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;
    List<CustodyItem> items = new ArrayList<>();
    List<JsonObject> party = new ArrayList<>();

    /** Restore completed in-memory; awaiting store durability. Join re-heals idempotently. */
    boolean restored;

    transient List<Pokemon> livePokemon;
  }

  /** Custody file root — versioned so a future shape change can migrate instead of drop. */
  private static class CustodyFile {

    int version = 1;
    Map<String, CustodyRecord> records = new HashMap<>();
  }

  private final Map<UUID, CustodyRecord> custody = new HashMap<>();

  /** A round fee dispatched but unresolved — read #sf_ok next tick (mode rides along). */
  private final Map<UUID, SafariSession.Mode> pendingPermits = new HashMap<>();

  /** A kiosk bait order whose fee is dispatched but unresolved — read #sfb_ok next tick. */
  private record PendingBait(String type, int count, int fee) {}

  private final Map<UUID, PendingBait> pendingBaits = new HashMap<>();
  private MinecraftServer server;

  // ── Wiring ────────────────────────────────────────────────────────────────────

  public void load() {
    config = SafariConfig.load();
    lureTables = SafariLureTables.load();
  }

  /**
   * Event subscriptions — called once from InitiativeInit.onInitialize().
   * POKEMON_CAPTURED already has two subscribers (NuzlockeInit, NobleEncounterInit);
   * a third self-contained one is idiomatic. The battle guard subscribes HIGHEST so it
   * cancels before any other handler sees the battle. The join/respawn handlers are the
   * custody self-heal: a record with no live session means the round ended by death,
   * logout, or crash — sweep the marked kit and hand everything back.
   */
  public void registerEvents() {
    CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST, event -> {
      for (ServerPlayer player : event.getBattle().getPlayers()) {
        if (sessions.containsKey(player.getUUID())) {
          event.cancel();
          event.setReason(
            Component.literal("Preserve rule: no battles on the grounds.")
          );
          player.displayClientMessage(
            Component.literal("§c[Preserve] §7Helga's rule: no battles on the grounds — balls and bait only."),
            true
          );
          InitiativeInit.LOGGER.info(
            "Cancelled battle start inside safari round for {}",
            player.getName().getString()
          );
        }
      }
      return Unit.INSTANCE;
    });

    // Session-gated catch ledger. LOWEST so Nuzlocke's NORMAL-priority handler
    // (duplicate handling / PC routing) has already resolved deterministically.
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, event -> {
      ServerPlayer player = event.getPlayer();
      SafariSession session = sessions.get(player.getUUID());
      if (session != null) {
        onSafariCapture(player, session, event.getPokemon());
      }
      return Unit.INSTANCE;
    });

    // Custody self-heal — a pending record with no session restores on the way back in.
    ServerPlayConnectionEvents.JOIN.register((handler, sender, joinServer) -> {
      this.server = joinServer;
      restorePendingCustody(handler.player);
    });
    ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
      restorePendingCustody(newPlayer)
    );
  }

  public void onServerStarted(MinecraftServer server) {
    this.server = server;
    loadStats(server);
    loadCustody(server);
    // Sweep stray lures from a previous run (non-persistent entities usually despawn
    // on their own, but a save mid-window can strand one in the hardcore save).
    int swept = sweepStrayLures(server);
    if (swept > 0) {
      InitiativeInit.LOGGER.info("Swept {} stray safari lure(s) at server start.", swept);
    }
  }

  public void onServerStopping(MinecraftServer server) {
    // Live sessions restore custody SYNCHRONOUSLY (catches are kept — a shutdown must
    // never destroy a mon). An offline holder's record stays pending for the join heal.
    List<SafariSession> live = new ArrayList<>(sessions.values());
    sessions.clear();
    for (SafariSession session : live) {
      teardown(server, session);
      ServerPlayer player = server.getPlayerList().getPlayer(session.getPlayerId());
      if (player != null) {
        restorePendingCustody(player);
      }
    }
    // Restored records exist only to survive a crash inside Cobblemon's interval-save
    // window; a graceful stop persists every dirty store, so purge them here.
    custody.values().removeIf(record -> record.restored);
    saveStats(server);
    saveCustody(server);
  }

  public boolean hasSession(UUID playerId) {
    return sessions.containsKey(playerId);
  }

  public java.util.Set<String> getBaitTypes() {
    return lureTables.baitTypes();
  }

  public SafariConfig getConfig() {
    return config;
  }

  /** The safari-exclusive roster (every lure-table species id, lowercased) — see NaturalSpawnGuard. */
  public java.util.Set<String> getLureSpeciesIds() {
    return lureTables.speciesIds();
  }

  // ── Round entry ───────────────────────────────────────────────────────────────

  /** /cobblemon-initiative safari enter <capture|contest> — gates → pay-probe → round. */
  public boolean enter(ServerPlayer player, String modeWord) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    this.server = server;

    SafariSession.Mode mode;
    if ("capture".equalsIgnoreCase(modeWord)) {
      mode = SafariSession.Mode.CAPTURE;
    } else if ("contest".equalsIgnoreCase(modeWord)) {
      mode = SafariSession.Mode.CONTEST;
    } else {
      player.sendSystemMessage(
        Component.literal("§c[Preserve] §7Choose a round: §ecapture§7 or §econtest§7.")
      );
      return false;
    }

    if (sessions.containsKey(player.getUUID())) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7Your round is already running.")
      );
      return false;
    }
    if (pendingPermits.containsKey(player.getUUID())) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7Helga is still ringing up your last round.")
      );
      return false;
    }
    CustodyRecord pendingRecord = custody.get(player.getUUID());
    if (pendingRecord != null && !pendingRecord.restored) {
      // A pending (unrestored) record means an unfinished restore — never stack a
      // round on top of it. A RESTORED record is only awaiting store durability and
      // is superseded by this round's own write-through.
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7Helga is still sorting your effects from last time — one moment.")
      );
      return false;
    }
    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      // Custody must never remove a party that is mid-battle.
      player.sendSystemMessage(
        Component.literal("§c[Preserve] §7Finish your battle first.")
      );
      return false;
    }

    int badges = badgeCount(player);
    if (badges < config.gateBadges) {
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7Helga asks for §e" + config.gateBadges +
          "§7 gym badges before she'll run you a round (you hold §e" + badges + "§7)."
        )
      );
      return false;
    }

    // Story gate: rounds open only after the clear-out quest (Nova's turn-in is the
    // single safari_liberated writer). PAIRED with the geometry gate's STANDALONE
    // RULE below: no installed "Safari Zone" means a bare-mod world without the
    // quest content — skip this gate exactly like the polygon check.
    NuzlockeConfig.SafeZone zone = findSafariZone();
    if (zone != null && !player.getTags().contains("safari_liberated")) {
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7The clipboards still hold the gate — find the ranger by the east fence."
        )
      );
      return false;
    }

    // Round geometry gate: the player must stand INSIDE the "Safari Zone" polygon.
    // STANDALONE RULE: on a bare-mod world the zone was never installed — skip the
    // check entirely so the round engine still works without the bundled map.
    if (zone != null && !zone.contains(
      player.serverLevel().dimension().location().toString(),
      player.getBlockX(), player.getBlockY(), player.getBlockZ()
    )) {
      player.sendSystemMessage(
        Component.literal("§c[Preserve] §7Rounds start on Preserve grounds — head inside the fence first.")
      );
      return false;
    }

    // Dispatch the pay-probe now but read #sf_ok NEXT TICK: the function's effects are
    // not reliably visible immediately after performPrefixedCommand returns (runtime-
    // found 2026-07-12 — the fee left the balance while the same-tick read saw 0 and
    // the session never started). tick() resolves pendingPermits one tick later.
    dispatchPermitFee(server, player);
    pendingPermits.put(player.getUUID(), mode);
    return true;
  }

  /** Deferred half of enter(): #sf_ok is readable one tick after the fee dispatch. */
  private void resolvePendingPermit(ServerPlayer player, SafariSession.Mode mode) {
    if (!readPermitProbe(server)) {
      // The mcfunction already printed the branded actionbar decline; chat gets the receipt.
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7Helga shakes her head — the fee didn't clear. (§e" +
          config.permitFee + " CD§7 required)"
        )
      );
      return;
    }

    // The fee has cleared — every bail below must send it back. Re-verify the cheap
    // gates: the one-tick probe gap is real time, and the BATTLE_STARTED_PRE guard
    // only covers players who already HOLD a session — a battle (or death, or a
    // zone exit) landing in the gap must never have its party pulled into custody.
    NuzlockeConfig.SafeZone gateZone = findSafariZone();
    boolean outsideZone = gateZone != null && !gateZone.contains(
      player.serverLevel().dimension().location().toString(),
      player.getBlockX(), player.getBlockY(), player.getBlockZ()
    );
    if (player.isDeadOrDying()
      || BattleRegistry.getBattleByParticipatingPlayer(player) != null
      || outsideZone) {
      refundFee(server, player, config.permitFee);
      player.sendSystemMessage(
        Component.literal(
          "§e[Preserve] §7Helga waves the round off — the moment passed. Your fee comes straight back."
        )
      );
      return;
    }

    // ── CUSTODY, crash-safe ordering ──────────────────────────────────────────────
    // (a) Serialize party + inventory and WRITE THE RECORD TO DISK before touching
    //     the player — a crash anywhere past this line restores on the next join.
    CustodyRecord record = new CustodyRecord();
    record.mode = mode == SafariSession.Mode.CONTEST ? "contest" : "capture";
    record.dimension = player.serverLevel().dimension().location().toString();
    record.x = player.getX();
    record.y = player.getY();
    record.z = player.getZ();
    record.yaw = player.getYRot();
    record.pitch = player.getXRot();

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<Pokemon> partyMons = new ArrayList<>();
    for (Pokemon p : party) {
      if (p != null) partyMons.add(p);
    }
    for (Pokemon p : partyMons) {
      // DaycareManager idiom EXACTLY — saveToJSON with the live registry access.
      record.party.add(p.saveToJSON(server.registryAccess(), new JsonObject()));
    }
    record.livePokemon = partyMons;
    snapshotInventory(player, record);

    custody.put(player.getUUID(), record);
    if (!saveCustody(server)) {
      // Write-through BEFORE any mutation is the whole point: with no durable
      // record, taking the party would be exactly the crash-unsafe state the
      // design forbids — abort the round untouched and return the fee.
      custody.remove(player.getUUID());
      refundFee(server, player, config.permitFee);
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7Helga cannot file your paperwork — no round runs without the ledger. Your fee comes straight back."
        )
      );
      return;
    }

    // (b) Take the party into custody + clear the inventory.
    for (Pokemon p : partyMons) {
      party.remove(p);
    }
    player.getInventory().clearContent();

    // (c) The round kit — every piece markered for the gate sweep. Bait rides the
    //     LOADED tables (baitPerTable of each), so a lure-table retune reshapes the
    //     kit with no code change; only the kiosk premium stays out of it.
    issueBalls(player);
    for (String table : lureTables.baitTypes()) {
      if (KIOSK_ONLY_TABLE.equals(table)) continue;
      giveBait(player, table, Math.max(0, config.baitPerTable));
    }
    issueSnowballs(player);

    // (d) The clock.
    SafariSession session = new SafariSession(
      player.getUUID(),
      mode,
      record.dimension,
      record.x,
      record.y,
      record.z,
      record.yaw,
      record.pitch,
      config.roundSeconds * 20
    );
    ServerBossEvent bar = new ServerBossEvent(
      barName(config.roundSeconds * 20),
      BossEvent.BossBarColor.YELLOW,
      BossEvent.BossBarOverlay.PROGRESS
    );
    bar.setProgress(1.0f);
    bar.addPlayer(player);
    session.setBossBar(bar);
    sessions.put(player.getUUID(), session);
    mirrorCatchScore(player);

    // (e) One-shot hostile sweep so the grounds open clean.
    int sweptHostiles = hostileSweep(server);

    player.sendSystemMessage(
      Component.literal(
        "§6§l[Preserve] §r§aRound fee settled — §e" + config.permitFee +
        " CD§a. §e" + config.balls + "§a Preserve Safari Balls, bait for every table, §e" +
        config.snowballs + "§a snowballs, §e" + formatTime(config.roundSeconds * 20) +
        "§a on the clock."
      )
    );
    player.sendSystemMessage(
      Component.literal(
        "§7Your party and packs wait safely at the gate. Crouch in cover — spooked ones bolt. " +
        "A gentle bait offering makes a friend."
      )
    );
    if (mode == SafariSession.Mode.CONTEST) {
      player.sendSystemMessage(
        Component.literal("§7Contest round: Helga appraises your catches at the bell, then releases them home.")
      );
    }
    InitiativeInit.LOGGER.info(
      "Safari round ({}) started for {} ({} CD, {} balls, {}s, {} hostile(s) swept).",
      record.mode, player.getName().getString(), config.permitFee, config.balls,
      config.roundSeconds, sweptHostiles
    );
  }

  /**
   * Dispatch the round-fee pay-probe ({@code safari/permit_fee}) AS THE PLAYER (the
   * function's {@code @s} is the payer). The #sf_ok result is read one tick later by
   * {@link #readPermitProbe} — never immediately (see enter()).
   */
  private void dispatchPermitFee(MinecraftServer server, ServerPlayer player) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective calc = scoreboard.getObjective("cd_calc");
    if (calc == null) {
      // economy/load normally owns cd_calc; create it so the probe works standalone.
      calc = scoreboard.addObjective(
        "cd_calc",
        ObjectiveCriteria.DUMMY,
        Component.literal("cd_calc"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }
    ScoreHolder probe = ScoreHolder.forNameOnly("#sf_ok");
    scoreboard.getOrCreatePlayerScore(probe, calc).set(0);

    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
      "function cobblemon_initiative:safari/permit_fee {fee:" + config.permitFee + "}"
    );
  }

  /**
   * Return an already-cleared fee. The pay-probe mcfunction removed it via
   * CobbleDollars, so the refund is the inverse dispatch (console source — it needs
   * no player context and must work even while the player is dead).
   */
  private void refundFee(MinecraftServer server, ServerPlayer player, int fee) {
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withSuppressedOutput(),
      "cobbledollars give " + player.getGameProfile().getName() + " " + fee
    );
    InitiativeInit.LOGGER.info(
      "Refunded {} CD safari fee to {}.", fee, player.getName().getString()
    );
  }

  /** One tick after {@link #dispatchPermitFee}: 0 = broke/declined, amount = paid. */
  private boolean readPermitProbe(MinecraftServer server) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective calc = scoreboard.getObjective("cd_calc");
    if (calc == null) return false;
    return scoreboard
      .getOrCreatePlayerScore(ScoreHolder.forNameOnly("#sf_ok"), calc)
      .get() >= 1;
  }

  // ── Bait kiosk purchase (permit-fee idiom, own #sfb_ok probe) ─────────────────

  /** Per-unit kiosk price for a bait table — executive_blend is the premium tier. */
  private int baitFeeFor(String type) {
    return KIOSK_ONLY_TABLE.equals(type) ? config.baitFeeExecutive : config.baitFee;
  }

  /**
   * /cobblemon-initiative safari bait — charge the kiosk price, then issue. Fee
   * dispatch + deferred read mirror {@link #enter}; a zero fee (config) falls back
   * to the original free give. The bait itself is issued only after #sfb_ok
   * confirms payment next tick.
   */
  public boolean buyBait(ServerPlayer player, String type, int count) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    this.server = server;

    if (!sessions.containsKey(player.getUUID())) {
      // Bait is unusable outside rounds and a pre-round purchase would only sit in
      // custody forever — the kiosk sells nothing it cannot deliver.
      player.sendSystemMessage(
        Component.literal("§c[Preserve] §7The kiosk serves running rounds — start one at the gate first.")
      );
      return false;
    }

    SafariLureTables.Table table = lureTables.getTable(type);
    if (table == null) {
      player.sendSystemMessage(
        Component.literal(
          "§cUnknown bait type: " + type + " §7(" + String.join(", ", getBaitTypes()) + ")")
      );
      return false;
    }

    // Validate the issue BEFORE the fee dispatch — a bad table.item id must never charge.
    String itemId = table.item != null ? table.item : "minecraft:bone_meal";
    ResourceLocation itemKey = ResourceLocation.tryParse(itemId);
    if (itemKey == null || BuiltInRegistries.ITEM.getOptional(itemKey).isEmpty()) {
      player.sendSystemMessage(
        Component.literal("§cBait item not registered: " + itemId)
      );
      return false;
    }

    int units = Math.max(1, count);
    int fee = baitFeeFor(type) * units;
    if (fee <= 0) {
      return giveBait(player, type, units);
    }

    if (pendingBaits.containsKey(player.getUUID())) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7The kiosk is still processing your last order.")
      );
      return false;
    }

    dispatchBaitFee(server, player, fee);
    pendingBaits.put(player.getUUID(), new PendingBait(type, units, fee));
    return true;
  }

  /** Deferred half of buyBait(): #sfb_ok is readable one tick after the fee dispatch. */
  private void resolvePendingBait(ServerPlayer player, PendingBait order) {
    if (!readBaitProbe(server)) {
      // The mcfunction already printed the branded actionbar decline; chat gets the receipt.
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7The kiosk order didn't clear. (§e" + order.fee() + " CD§7 required)"
        )
      );
      return;
    }
    if (giveBait(player, order.type(), order.count())) {
      player.sendSystemMessage(
        Component.literal("§6[Preserve] §7Bait charged to your account — §e" + order.fee() + " CD§7.")
      );
    }
  }

  /** Dispatch the bait pay-probe ({@code safari/bait_fee}) AS the buying player. */
  private void dispatchBaitFee(MinecraftServer server, ServerPlayer player, int fee) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective calc = scoreboard.getObjective("cd_calc");
    if (calc == null) {
      // economy/load normally owns cd_calc; create it so the probe works standalone.
      calc = scoreboard.addObjective(
        "cd_calc",
        ObjectiveCriteria.DUMMY,
        Component.literal("cd_calc"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }
    ScoreHolder probe = ScoreHolder.forNameOnly("#sfb_ok");
    scoreboard.getOrCreatePlayerScore(probe, calc).set(0);

    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
      "function cobblemon_initiative:safari/bait_fee {fee:" + fee + "}"
    );
  }

  /** One tick after {@link #dispatchBaitFee}: 0 = broke/declined, amount = paid. */
  private boolean readBaitProbe(MinecraftServer server) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective calc = scoreboard.getObjective("cd_calc");
    if (calc == null) return false;
    return scoreboard
      .getOrCreatePlayerScore(ScoreHolder.forNameOnly("#sfb_ok"), calc)
      .get() >= 1;
  }

  /** Gym badges earned — levelcaps achievements starting with "badge_" (LootChest idiom). */
  private int badgeCount(ServerPlayer player) {
    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
    int badges = 0;
    for (LevelCapConfig cap : InitiativeInit.getConfigLoader().getLevelCaps()) {
      String id = cap.getAchievementId();
      if (id != null && id.startsWith("badge_") && progress.hasAchievement(id)) {
        badges++;
      }
    }
    return badges;
  }

  // ── Round end paths ───────────────────────────────────────────────────────────

  /** /cobblemon-initiative safari exit — voluntary end in place. */
  public boolean exitVoluntary(ServerPlayer player) {
    SafariSession session = sessions.get(player.getUUID());
    if (session == null) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7No round running.")
      );
      return false;
    }
    player.sendSystemMessage(
      Component.literal("§6§l[Preserve] §r§eRound closed at the gate.")
    );
    endRound(player.getServer(), player, session, false, "voluntary exit");
    return true;
  }

  /**
   * Shared end-of-round. STRICT ORDERING (the custody contract): discard scatters+lures
   * → sweep every marked kit item → resolve CATCHES off the round's CatchRecord ledger
   * (by Pokémon UUID, in the party OR the PC — Nuzlocke's higher-priority
   * POKEMON_CAPTURED handler routes catches to the PC under the live sendCaughtToPC
   * config, so party occupancy proves nothing) → mode split (CONTEST scores from the
   * records and releases every located catch from whichever store holds it; CAPTURE
   * leaves catches where Nuzlocke filed them) → set party-resident catches aside →
   * restore the original party → restore the inventory → capture re-adds the
   * set-aside catches → ledger → mark custody restored.
   */
  private void endRound(
    MinecraftServer server,
    ServerPlayer player,
    SafariSession session,
    boolean escort,
    String reason
  ) {
    sessions.remove(player.getUUID());
    teardown(server, session);
    if (server != null) sweepStrayLures(server);

    int sweptItems = sweepIssuedItems(player);

    // Resolve the ledger: each catch sits wherever Nuzlocke routed it (party or PC);
    // one in neither store was released upstream (e.g. duplicate handling) — contest
    // still scores its record, and there is nothing to move.
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
    List<Pokemon> partyCatches = new ArrayList<>();
    List<Pokemon> pcCatches = new ArrayList<>();
    for (SafariSession.CatchRecord catchRecord : session.getCatches()) {
      Pokemon inParty = party.get(catchRecord.pokemonUuid);
      if (inParty != null) {
        partyCatches.add(inParty);
        continue;
      }
      Pokemon inPc = pc.get(catchRecord.pokemonUuid);
      if (inPc != null) pcCatches.add(inPc);
    }

    // Set every party-resident catch aside so the original party restores into its
    // saved slots (CONTEST never re-adds them; CAPTURE re-adds after the restore).
    for (Pokemon caught : partyCatches) {
      party.remove(caught);
    }

    // Contest appraisal rides the catch-time records BEFORE the restore so the
    // printout can share the same message block as the ledger.
    int contestScore = 0;
    int bestBefore = 0;
    if (session.getMode() == SafariSession.Mode.CONTEST) {
      for (SafariSession.CatchRecord catchRecord : session.getCatches()) {
        contestScore += pointsFor(catchRecord);
      }
      LifetimeStats stats = lifetime.computeIfAbsent(player.getUUID(), id -> new LifetimeStats());
      bestBefore = stats.bestContestScore;
      if (contestScore > stats.bestContestScore) {
        stats.bestContestScore = contestScore;
      }
      // Appraise-and-release: PC-routed catches leave their boxes too.
      for (Pokemon caught : pcCatches) {
        pc.remove(caught);
      }
    }

    CustodyRecord record = custody.get(player.getUUID());
    if (record != null) {
      restoreParty(player, record);
      restoreInventory(player, record);
    } else {
      // Should be impossible (custody writes before the session exists) — never
      // silently eat a party: log loud and keep the catches at least.
      InitiativeInit.LOGGER.error(
        "Safari round ended for {} with NO custody record — party/inventory restore skipped.",
        player.getName().getString()
      );
    }

    if (session.getMode() == SafariSession.Mode.CAPTURE) {
      // Re-add the set-aside party catches AFTER the original party is back so their
      // slots append (PC-routed catches never moved). On a full party, route to the
      // PC — and never leave a mon referenced by two stores (add() only links on
      // success, so the PC fallback is dupe-safe).
      for (Pokemon caught : partyCatches) {
        if (!party.add(caught)) {
          boolean stored = Cobblemon.INSTANCE.getStorage().getPC(player).add(caught);
          if (!stored) {
            InitiativeInit.LOGGER.error(
              "Safari: party AND PC full — could not store {} for {}.",
              caught.getSpecies().getName(), player.getName().getString()
            );
          } else {
            player.sendSystemMessage(
              Component.literal(
                "§7Your party is full — §e" + caught.getSpecies().getName() + "§7 was sent to the PC."
              )
            );
          }
        }
      }
    }
    // CONTEST: the released catches are simply not re-added anywhere.

    printLedger(player, session, sweptItems);
    if (session.getMode() == SafariSession.Mode.CONTEST) {
      LifetimeStats stats = lifetime.computeIfAbsent(player.getUUID(), id -> new LifetimeStats());
      String bestNote = contestScore > bestBefore
        ? " §6— a new personal best!"
        : " §7(personal best §e" + stats.bestContestScore + "§7)";
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Preserve] §r§eAppraisal: §6" + contestScore + "§e point" +
          (contestScore == 1 ? "" : "s") + bestNote
        )
      );
      if (!session.getCatches().isEmpty()) {
        player.sendSystemMessage(
          Component.literal("§7Every catch was appraised and released back to the Preserve.")
        );
      }
    }

    if (escort) {
      // Teleport ONLY — never a damage path. Destination: the configured eject pad,
      // or the exact spot the player entered from (ground they verifiably stood on).
      ServerLevel level = resolveLevel(server, session.getDimension());
      if (level == null && server != null) level = server.overworld();
      if (level != null) {
        double x;
        double y;
        double z;
        if (config.hasEjectPad()) {
          x = config.ejectX + 0.5;
          y = config.ejectY;
          z = config.ejectZ + 0.5;
        } else {
          x = session.getEntryX();
          y = session.getEntryY();
          z = session.getEntryZ();
        }
        player.teleportTo(level, x, y, z, session.getEntryYaw(), session.getEntryPitch());
      }
    }

    // The record stays (marked restored) until the restored stores are durable:
    // Cobblemon saves party/PC on an interval, and deleting now would leave a crash
    // in that window with an empty on-disk party and no recovery artifact.
    if (record != null) {
      record.restored = true;
    }
    if (server != null) {
      saveCustody(server);
      saveStats(server);
    }

    InitiativeInit.LOGGER.info(
      "Safari round ended for {} ({}; {} catch(es), {} item(s) swept, escort={}).",
      player.getName().getString(), reason, session.getCatches().size(), sweptItems, escort
    );
  }

  /** Contest points for one catch, from its catch-time ledger record. */
  private int pointsFor(SafariSession.CatchRecord record) {
    int points = switch (record.rarity == null ? "common" : record.rarity) {
      case "rare" -> config.pointsRare;
      case "uncommon" -> config.pointsUncommon;
      default -> config.pointsCommon;
    };
    return points + (record.friendly ? config.friendlyPoint : 0);
  }

  /**
   * Player vanished or died mid-round: quiet teardown, session dropped, custody record
   * LEFT PENDING — the join/respawn handlers restore it (never restore into a dead or
   * absent player's inventory).
   */
  private void forfeitKeepCustody(MinecraftServer server, SafariSession session, String why) {
    teardown(server, session);
    if (server != null) {
      sweepStrayLures(server);
      // A hardcore death scatters the marked kit at the corpse — real safari balls
      // must never lie lootable on the grounds.
      sweepDroppedKit(server);
    }
    InitiativeInit.LOGGER.info(
      "Safari round dropped for {} ({}); custody stays pending for the return heal.",
      session.getPlayerId(), why
    );
  }

  /**
   * Join/respawn custody heal: a pending record with no live session means the round
   * ended without its gate ceremony (death, logout, crash, server stop). Sweep any
   * marked kit still on the player, hand back party + inventory, and KEEP whatever
   * they caught (both modes — an interrupted contest never got appraised, and a crash
   * path must never destroy a mon). An already-RESTORED record only re-runs the
   * idempotent UUID heal (crash inside Cobblemon's save window may have lost the
   * restored stores while the flag survived).
   */
  public void restorePendingCustody(ServerPlayer player) {
    CustodyRecord record = custody.get(player.getUUID());
    if (record == null || sessions.containsKey(player.getUUID())) return;
    MinecraftServer srv = player.getServer();
    if (srv != null) this.server = srv;

    // A crash-loaded record must fully deserialize BEFORE anything is cleared — a
    // failed load keeps the record on disk (daycare rule: never delete, never guess).
    if (!ensurePartyLoaded(record)) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7Helga is having trouble with your paperwork — your effects stay safe at the gate.")
      );
      return;
    }

    if (record.restored) {
      healRestoredRecord(player, record);
      return;
    }

    sweepIssuedItems(player);
    if (server != null) sweepDroppedKit(server);

    // Stale-store guard: a crash inside Cobblemon's ~30s save window can leave the
    // ORIGINAL party in the on-disk store — those occupants share UUIDs with the
    // custody mons the restore is about to re-add. Genuine round catches never can.
    Set<UUID> custodyUuids = new HashSet<>();
    for (Pokemon p : record.livePokemon) {
      custodyUuids.add(p.getUuid());
    }

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<Pokemon> occupants = new ArrayList<>();
    for (Pokemon p : party) {
      if (p != null) occupants.add(p);
    }
    for (Pokemon p : occupants) {
      party.remove(p);
    }
    List<Pokemon> catches = new ArrayList<>();
    int stale = 0;
    for (Pokemon p : occupants) {
      if (custodyUuids.contains(p.getUuid())) {
        stale++; // dropped, never re-added — the restored clone is the same mon
      } else {
        catches.add(p);
      }
    }
    if (stale > 0) {
      InitiativeInit.LOGGER.warn(
        "Safari custody heal for {}: dropped {} stale-store party occupant(s) whose UUIDs are already in custody (crash inside Cobblemon's save window).",
        player.getName().getString(), stale
      );
    }

    restoreParty(player, record);
    restoreInventory(player, record);

    for (Pokemon caught : catches) {
      if (!party.add(caught)) {
        Cobblemon.INSTANCE.getStorage().getPC(player).add(caught);
      }
    }

    // Mark restored, never delete: the restored stores are only durable after
    // Cobblemon's next interval save. Purged on SERVER_STOPPING / next write-through.
    record.restored = true;
    if (server != null) saveCustody(server);
    player.sendSystemMessage(
      Component.literal("§6[Preserve] §7The Preserve returns your effects.")
    );
    InitiativeInit.LOGGER.info(
      "Restored pending safari custody for {} ({} party mon(s), {} item(s), {} catch(es) kept).",
      player.getName().getString(), record.party.size(), record.items.size(), catches.size()
    );
  }

  /**
   * Idempotent re-heal for an already-restored record: only mons the durable stores
   * actually LOST come back (probed by UUID across party + PC) — never re-clears the
   * party, the inventory, or the catches, so re-running after a fully durable restore
   * is a no-op. Items are left to vanilla playerdata persistence: stacks carry no
   * UUID to dedupe on, and refilling slots could duplicate moved items.
   */
  private void healRestoredRecord(ServerPlayer player, CustodyRecord record) {
    sweepIssuedItems(player);
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
    int healed = 0;
    for (Pokemon p : record.livePokemon) {
      if (party.get(p.getUuid()) != null || pc.get(p.getUuid()) != null) continue;
      if (party.add(p) || pc.add(p)) {
        healed++;
      } else {
        InitiativeInit.LOGGER.error(
          "Safari custody re-heal: could not return {} to {} — party and PC both full.",
          p.getSpecies().getName(), player.getName().getString()
        );
      }
    }
    if (healed > 0) {
      player.sendSystemMessage(
        Component.literal(
          "§6[Preserve] §7Helga re-checks her ledger — §e" + healed +
          "§7 of yours come back over the counter."
        )
      );
      InitiativeInit.LOGGER.warn(
        "Safari custody re-heal for {}: re-added {} mon(s) lost to a crash inside Cobblemon's save window.",
        player.getName().getString(), healed
      );
    }
  }

  /** Idempotent cleanup: boss bar + every tracked lure body. */
  private void teardown(MinecraftServer server, SafariSession session) {
    ServerBossEvent bar = session.getBossBar();
    if (bar != null) {
      bar.removeAllPlayers();
      session.setBossBar(null);
    }
    if (server == null) return;
    ServerLevel level = resolveLevel(server, session.getDimension());
    if (level != null) {
      for (SafariSession.ActiveLure lure : session.getLures()) {
        Entity e = level.getEntity(lure.entityUuid);
        if (e != null && e.isAlive()) e.discard();
      }
    }
    session.getLures().clear();
    session.getPendingScatters().clear();
  }

  /** The end-of-round catch ledger. */
  private void printLedger(ServerPlayer player, SafariSession session, int sweptItems) {
    List<SafariSession.CatchRecord> catches = session.getCatches();
    if (catches.isEmpty()) {
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Preserve] §r§7Round ledger: nothing this time. Helga waves — the Preserve will be here."
        )
      );
    } else {
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Preserve] §r§eRound ledger — §6" + catches.size() + "§e catch(es):"
        )
      );
      for (SafariSession.CatchRecord record : catches) {
        player.sendSystemMessage(
          Component.literal(
            "§7  • §e" + record.species + " §7(Lv. " + record.level +
            (record.friendly ? "§7, §dbefriended" : "") + "§7)"
          )
        );
      }
    }
    if (sweptItems > 0) {
      player.sendSystemMessage(
        Component.literal(
          "§7Round gear handed back at the gate: §e" + sweptItems + "§7 piece(s)."
        )
      );
    }
  }

  // ── Custody serialization (DaycareManager idiom) ────────────────────────────────

  /** Snapshot every occupied slot across main(36)/armor(4)/offhand(1) as SNBT. */
  private void snapshotInventory(ServerPlayer player, CustodyRecord record) {
    Inventory inv = player.getInventory();
    snapshotContainer(inv.items, "main", record);
    snapshotContainer(inv.armor, "armor", record);
    snapshotContainer(inv.offhand, "offhand", record);
  }

  private void snapshotContainer(List<ItemStack> list, String container, CustodyRecord record) {
    for (int i = 0; i < list.size(); i++) {
      ItemStack stack = list.get(i);
      if (stack.isEmpty()) continue;
      CustodyItem item = new CustodyItem();
      item.slot = i;
      item.container = container;
      item.snbt = stack.save(server.registryAccess()).toString();
      record.items.add(item);
    }
  }

  /** Deserialize the custody party into {@link CustodyRecord#livePokemon} (all-or-nothing). */
  private boolean ensurePartyLoaded(CustodyRecord record) {
    if (record.livePokemon != null && record.livePokemon.size() == record.party.size()) {
      return true;
    }
    List<Pokemon> loaded = new ArrayList<>();
    for (JsonObject json : record.party) {
      try {
        loaded.add(Pokemon.Companion.loadFromJSON(server.registryAccess(), json));
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Safari custody: could not deserialize a boarded party mon — record kept on disk.", e
        );
        return false;
      }
    }
    record.livePokemon = loaded;
    return true;
  }

  /** Hand the custody party back — party.add in saved order refills slots 0..n. */
  private void restoreParty(ServerPlayer player, CustodyRecord record) {
    if (!ensurePartyLoaded(record)) return;
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (Pokemon p : record.livePokemon) {
      if (!party.add(p)) {
        // Can only happen if something filled the party out from under the restore;
        // add() falls back to the PC internally, so a false here is party AND PC full.
        InitiativeInit.LOGGER.error(
          "Safari custody: could not return {} to {} — party and PC both full.",
          p.getSpecies().getName(), player.getName().getString()
        );
      }
    }
  }

  /** Restore custody items into their exact slots; never overwrite an occupied slot. */
  private void restoreInventory(ServerPlayer player, CustodyRecord record) {
    Inventory inv = player.getInventory();
    for (CustodyItem item : record.items) {
      ItemStack stack;
      try {
        CompoundTag tag = TagParser.parseTag(item.snbt);
        stack = ItemStack.parse(server.registryAccess(), tag).orElse(ItemStack.EMPTY);
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Safari custody: could not parse an item for {} (slot {} {}).",
          player.getName().getString(), item.container, item.slot, e
        );
        continue;
      }
      if (stack.isEmpty()) continue;
      List<ItemStack> list = switch (item.container == null ? "main" : item.container) {
        case "armor" -> inv.armor;
        case "offhand" -> inv.offhand;
        default -> inv.items;
      };
      if (item.slot >= 0 && item.slot < list.size() && list.get(item.slot).isEmpty()) {
        list.set(item.slot, stack);
      } else {
        inv.placeItemBackInInventory(stack);
      }
    }
    inv.setChanged();
  }

  // ── Round kit (issue + gate sweep; marker components, never player properties) ───

  private void issueBalls(ServerPlayer player) {
    Item ball = BuiltInRegistries.ITEM
      .getOptional(ResourceLocation.parse(SAFARI_BALL_ID))
      .orElse(null);
    if (ball == null) {
      InitiativeInit.LOGGER.error("{} not in the item registry — no balls issued.", SAFARI_BALL_ID);
      return;
    }
    int maxStack = new ItemStack(ball).getMaxStackSize();
    int remaining = config.balls;
    while (remaining > 0) {
      int n = Math.min(remaining, maxStack);
      remaining -= n;
      ItemStack stack = new ItemStack(ball, n);
      CompoundTag tag = new CompoundTag();
      tag.putBoolean(BALL_MARKER, true);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      stack.set(
        DataComponents.CUSTOM_NAME,
        Component.literal("§fPreserve Safari Ball")
      );
      stack.set(
        DataComponents.LORE,
        new ItemLore(List.of(
          Component.literal("§8Property of the Ridgewatch Preserve."),
          Component.literal("§8Handed back at the gate.")
        ))
      );
      player.getInventory().placeItemBackInInventory(stack);
    }
  }

  private void issueSnowballs(ServerPlayer player) {
    int maxStack = new ItemStack(net.minecraft.world.item.Items.SNOWBALL).getMaxStackSize();
    int remaining = config.snowballs;
    while (remaining > 0) {
      int n = Math.min(remaining, maxStack);
      remaining -= n;
      ItemStack stack = new ItemStack(net.minecraft.world.item.Items.SNOWBALL, n);
      CompoundTag tag = new CompoundTag();
      tag.putBoolean(BALL_MARKER, true);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      stack.set(
        DataComponents.CUSTOM_NAME,
        Component.literal("§fPreserve Snowball")
      );
      stack.set(
        DataComponents.LORE,
        new ItemLore(List.of(
          Component.literal("§8Takes the fight out of a wild one."),
          Component.literal("§8Handed back at the gate.")
        ))
      );
      player.getInventory().placeItemBackInInventory(stack);
    }
  }

  /** Any round-issue marker (balls AND snowballs ride {@code ci_safari_issue}). */
  private static boolean hasIssueMarker(ItemStack stack) {
    if (stack.isEmpty()) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(BALL_MARKER);
  }

  private static boolean isBaitItem(ItemStack stack) {
    if (stack.isEmpty()) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && !data.copyTag().getString(BAIT_MARKER).isEmpty();
  }

  /** Issued SAFARI BALLS only — the snowballs share the marker, so gate on the item too. */
  private static boolean isIssuedBall(ItemStack stack) {
    if (!hasIssueMarker(stack)) return false;
    return SAFARI_BALL_ID.equals(
      BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
    );
  }

  /** Remove every ci_safari_issue / ci_bait item; player-owned gear is never touched. */
  private int sweepIssuedItems(ServerPlayer player) {
    Inventory inv = player.getInventory();
    int removed = 0;
    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack s = inv.getItem(i);
      if (hasIssueMarker(s) || isBaitItem(s)) {
        removed += s.getCount();
        inv.setItem(i, ItemStack.EMPTY);
      }
    }
    if (removed > 0) inv.setChanged();
    return removed;
  }

  private int countIssuedBalls(ServerPlayer player) {
    Inventory inv = player.getInventory();
    int count = 0;
    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack s = inv.getItem(i);
      if (isIssuedBall(s)) count += s.getCount();
    }
    return count;
  }

  // ── Bait items + scatter detection ─────────────────────────────────────────────

  /** /cobblemon-initiative safari bait <type> [count] — kiosk dialog-button-ready give. */
  public boolean giveBait(ServerPlayer player, String baitType, int count) {
    if (count <= 0) return true;
    SafariLureTables.Table table = lureTables.getTable(baitType);
    if (table == null) {
      player.sendSystemMessage(
        Component.literal("§cUnknown bait type: " + baitType + " §7(" + String.join(", ", getBaitTypes()) + ")")
      );
      return false;
    }
    String itemId = table.item != null ? table.item : "minecraft:bone_meal";
    Item item = BuiltInRegistries.ITEM
      .getOptional(ResourceLocation.parse(itemId))
      .orElse(null);
    if (item == null) {
      player.sendSystemMessage(Component.literal("§cBait item not registered: " + itemId));
      return false;
    }
    ItemStack stack = new ItemStack(item, Math.max(1, count));
    CompoundTag tag = new CompoundTag();
    tag.putString(BAIT_MARKER, baitType);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    stack.set(
      DataComponents.CUSTOM_NAME,
      Component.literal("§e" + displayName(table, baitType))
    );
    stack.set(
      DataComponents.LORE,
      new ItemLore(List.of(
        Component.literal("tree".equals(table.placement)
          ? "§8Smear on tree bark, or offer it by hand."
          : "§8Scatter on open ground, or offer it by hand."),
        Component.literal("§8Preserve property — surrendered at the bell.")
      ))
    );
    player.getInventory().placeItemBackInInventory(stack);
    return true;
  }

  private static String displayName(SafariLureTables.Table table, String baitType) {
    return table.displayName != null ? table.displayName : baitType;
  }

  /**
   * UseBlockCallback handler (InitiativeInit registers it alongside docprop/lootchest).
   * Fast PASS unless the held item carries the {@code ci_bait} custom-data marker.
   */
  public InteractionResult onUseBlock(
    Player player,
    Level level,
    InteractionHand hand,
    BlockHitResult hit
  ) {
    if (level.isClientSide()) return InteractionResult.PASS;
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

    ItemStack held = player.getItemInHand(hand);
    if (held.isEmpty()) return InteractionResult.PASS;
    CustomData data = held.get(DataComponents.CUSTOM_DATA);
    if (data == null) return InteractionResult.PASS;
    String baitType = data.copyTag().getString(BAIT_MARKER);
    if (baitType.isEmpty()) return InteractionResult.PASS;

    SafariSession session = sessions.get(serverPlayer.getUUID());
    if (session == null) {
      serverPlayer.displayClientMessage(
        Component.literal("§c[Preserve] §7Bait is for rounds — start one at the gate."),
        true
      );
      // FAIL: never let marked bait fall through to vanilla item behaviour.
      return InteractionResult.FAIL;
    }
    SafariLureTables.Table table = lureTables.getTable(baitType);
    if (table == null) {
      serverPlayer.displayClientMessage(
        Component.literal("§c[Preserve] §7That bait lot has been retired."),
        true
      );
      return InteractionResult.FAIL;
    }
    // Placement rule: a "tree" table's bait goes ON the tree — the CLICKED block must
    // be logs or leaves. A wrong click consumes NOTHING (FAIL keeps the bait in hand
    // and off vanilla item behaviour).
    if ("tree".equals(table.placement)) {
      BlockState clicked = level.getBlockState(hit.getBlockPos());
      if (!clicked.is(BlockTags.LOGS) && !clicked.is(BlockTags.LEAVES)) {
        serverPlayer.displayClientMessage(
          Component.literal(
            "§c[Preserve] §7" + displayName(table, baitType) + " goes on bark — find a tree."
          ),
          true
        );
        return InteractionResult.FAIL;
      }
    }

    held.shrink(1);
    enqueueScatter(serverPlayer, session, baitType, hit.getBlockPos());
    return InteractionResult.SUCCESS;
  }

  /**
   * UseEntityCallback handler — the bait's SECOND use: offered by hand to a tracked
   * lure it BEFRIENDS it (window extended, detection off, contest bonus point).
   * Fast PASS unless main-hand ci_bait on a tagged lure.
   */
  public InteractionResult onUseEntity(
    Player player,
    Level level,
    InteractionHand hand,
    Entity entity,
    EntityHitResult hit
  ) {
    if (level.isClientSide()) return InteractionResult.PASS;
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

    ItemStack held = player.getItemInHand(hand);
    if (!isBaitItem(held)) return InteractionResult.PASS;
    if (!(entity instanceof PokemonEntity mon)) return InteractionResult.PASS;
    if (!mon.getTags().contains(LURE_TAG)) return InteractionResult.PASS;

    SafariSession session = sessions.get(serverPlayer.getUUID());
    if (session == null) return InteractionResult.FAIL;
    SafariSession.ActiveLure lure = findLureByEntity(session, mon.getUUID());
    if (lure == null) return InteractionResult.FAIL;
    if (lure.friendly) {
      serverPlayer.displayClientMessage(
        Component.literal("§dIt's already at ease with you."),
        true
      );
      return InteractionResult.SUCCESS;
    }

    held.shrink(1);
    befriendLure(serverPlayer, session, lure, mon);
    return InteractionResult.SUCCESS;
  }

  /** Shared befriend body (hand-offering + the dev hook). */
  private void befriendLure(
    ServerPlayer player,
    SafariSession session,
    SafariSession.ActiveLure lure,
    PokemonEntity mon
  ) {
    lure.friendly = true;
    lure.alert = 0;
    lure.fleeTicksLeft = 0;
    lure.ticksRemaining += Math.max(0, config.friendlyBonusSeconds) * 20;

    ServerLevel level = player.serverLevel();
    level.sendParticles(
      ParticleTypes.HEART,
      mon.getX(), mon.getY() + mon.getBbHeight() + 0.3, mon.getZ(),
      6, 0.35, 0.25, 0.35, 0.01
    );
    level.playSound(
      null, mon.getX(), mon.getY(), mon.getZ(),
      SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.8f, 1.2f
    );
    player.displayClientMessage(
      Component.literal(
        "§dThe " + mon.getPokemon().getSpecies().getName() + " settles — it likes you."
      ),
      true
    );
  }

  /**
   * The scatter itself, shared by the bait-item click and the perm-2
   * {@code safari scatter} dev hook (Carpet bots cannot fire UseBlockCallback, so
   * headless verification drives this directly; the click path is the LootChest/
   * DocProp callback precedent). The dev hook DELIBERATELY bypasses per-table
   * placement rules (e.g. honey_smear's "tree") — bots can't click a trunk; the
   * tree gate lives in onUseBlock and stays an in-world verify item.
   */
  public boolean devScatter(ServerPlayer player, String baitType) {
    SafariSession session = sessions.get(player.getUUID());
    if (session == null || !lureTables.hasBait(baitType)) return false;
    enqueueScatter(player, session, baitType, player.blockPosition().below());
    return true;
  }

  /** Dev hook: spook the nearest tracked lure within 16 blocks (harness stand-in). */
  public boolean devSpook(ServerPlayer player) {
    SafariSession session = sessions.get(player.getUUID());
    if (session == null) return false;
    SafariSession.ActiveLure lure = nearestLure(player, session, 16.0);
    if (lure == null) return false;
    lure.friendly = false;
    startFlee(player, session, lure);
    return true;
  }

  /** Dev hook: befriend the nearest tracked lure within 16 blocks (no bait consumed). */
  public boolean devBefriend(ServerPlayer player) {
    SafariSession session = sessions.get(player.getUUID());
    if (session == null) return false;
    SafariSession.ActiveLure lure = nearestLure(player, session, 16.0);
    if (lure == null) return false;
    Entity entity = player.serverLevel().getEntity(lure.entityUuid);
    if (!(entity instanceof PokemonEntity mon)) return false;
    befriendLure(player, session, lure, mon);
    return true;
  }

  private SafariSession.ActiveLure findLureByEntity(SafariSession session, UUID entityUuid) {
    for (SafariSession.ActiveLure lure : session.getLures()) {
      if (lure.entityUuid.equals(entityUuid)) return lure;
    }
    return null;
  }

  private SafariSession.ActiveLure nearestLure(
    ServerPlayer player,
    SafariSession session,
    double maxRange
  ) {
    ServerLevel level = player.serverLevel();
    SafariSession.ActiveLure best = null;
    double bestSq = maxRange * maxRange;
    for (SafariSession.ActiveLure lure : session.getLures()) {
      Entity e = level.getEntity(lure.entityUuid);
      if (e == null || !e.isAlive()) continue;
      double sq = e.distanceToSqr(player);
      if (sq <= bestSq) {
        bestSq = sq;
        best = lure;
      }
    }
    return best;
  }

  private void enqueueScatter(
    ServerPlayer serverPlayer,
    SafariSession session,
    String baitType,
    BlockPos spot
  ) {
    long spotKey = spot.asLong();
    int warmth = warmthNear(session, spot);
    RandomSource random = serverPlayer.serverLevel().getRandom();
    int minT = Math.max(1, config.suspenseMinSeconds) * 20;
    int maxT = Math.max(config.suspenseMinSeconds, config.suspenseMaxSeconds) * 20;
    int delay = minT + (maxT > minT ? random.nextInt(maxT - minT + 1) : 0);

    session.getPendingScatters().add(
      new SafariSession.PendingScatter(
        baitType,
        spot.getX() + 0.5,
        spot.getY() + 1.0,
        spot.getZ() + 0.5,
        spotKey,
        warmth,
        delay
      )
    );

    serverPlayer.displayClientMessage(
      Component.literal(
        warmth > 0
          ? "§eThe grass stirs… §6the spot runs warm."
          : "§eThe grass stirs…"
      ),
      true
    );
    serverPlayer.serverLevel().sendParticles(
      ParticleTypes.COMPOSTER,
      spot.getX() + 0.5, spot.getY() + 1.1, spot.getZ() + 0.5,
      12, 0.4, 0.2, 0.4, 0.01
    );
  }

  /** A scatter inherits the warmth of any warmed spot within 8 blocks. */
  private int warmthNear(SafariSession session, BlockPos spot) {
    int best = 0;
    for (Map.Entry<Long, Integer> entry : session.getWarmSpots().entrySet()) {
      BlockPos warmed = BlockPos.of(entry.getKey());
      if (warmed.distSqr(spot) <= 64 && entry.getValue() > best) {
        best = entry.getValue();
      }
    }
    return best;
  }

  // ── Snowball weaken (SnowballWeakenMixin calls in) ─────────────────────────────

  /**
   * A round player's snowball hit on a tracked lure: shave {@code weakenFraction} of
   * max HP (floors at 1 — a snowball can never KO). Weakening does NOT raise alert —
   * stealth only reads sight-lines.
   */
  public boolean onSnowballWeaken(ServerPlayer thrower, PokemonEntity target) {
    SafariSession session = sessions.get(thrower.getUUID());
    if (session == null) return false;

    Pokemon pokemon = target.getPokemon();
    int cut = (int) Math.ceil(pokemon.getMaxHealth() * config.weakenFraction);
    pokemon.setCurrentHealth(Math.max(1, pokemon.getCurrentHealth() - cut));

    ServerLevel level = thrower.serverLevel();
    level.sendParticles(
      ParticleTypes.CRIT,
      target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
      10, 0.35, 0.3, 0.35, 0.1
    );
    level.playSound(
      null, target.getX(), target.getY(), target.getZ(),
      SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 0.7f, 1.0f
    );
    thrower.displayClientMessage(
      Component.literal("§eThe " + pokemon.getSpecies().getName() + " staggers!"),
      true
    );
    return true;
  }

  // ── Tick loop (driven from InitiativeInit END_SERVER_TICK) ─────────────────────

  public void tick(MinecraftServer server) {
    this.server = server;

    // Deferred permit resolution: the pay-probe's #sf_ok is reliable one tick after
    // the fee dispatch (see enter()). Resolve before session upkeep so a fresh
    // session ticks from its full clock.
    if (!pendingPermits.isEmpty()) {
      List<Map.Entry<UUID, SafariSession.Mode>> pending =
        new ArrayList<>(pendingPermits.entrySet());
      pendingPermits.clear();
      for (Map.Entry<UUID, SafariSession.Mode> entry : pending) {
        ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
        if (player != null) {
          resolvePendingPermit(player, entry.getValue());
        }
      }
    }

    // Deferred bait-order resolution — same one-tick contract as the permit probe.
    if (!pendingBaits.isEmpty()) {
      List<Map.Entry<UUID, PendingBait>> pendingOrders =
        new ArrayList<>(pendingBaits.entrySet());
      pendingBaits.clear();
      for (Map.Entry<UUID, PendingBait> entry : pendingOrders) {
        ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
        if (player != null) {
          resolvePendingBait(player, entry.getValue());
        }
      }
    }

    if (sessions.isEmpty()) return;

    List<UUID> gone = new ArrayList<>();
    List<ServerPlayer> endInPlace = new ArrayList<>();
    List<ServerPlayer> ejects = new ArrayList<>();

    for (SafariSession session : sessions.values()) {
      ServerPlayer player = server.getPlayerList().getPlayer(session.getPlayerId());
      if (player == null || player.isDeadOrDying()) {
        // Death/logout mid-round: the round is over but the CUSTODY RECORD STAYS —
        // the join/respawn handlers hand everything back (never touch a dead body's
        // inventory).
        forfeitKeepCustody(server, session, player == null ? "logout" : "death");
        gone.add(session.getPlayerId());
        continue;
      }

      int remaining = session.getTicksRemaining() - 1;
      session.setTicksRemaining(remaining);

      if (remaining <= 0) {
        if (holdEndForBusyLure(player, session)) {
          session.setTicksRemaining(0); // clock parks at the bell while the throw resolves
          continue;
        }
        ejects.add(player);
        continue;
      }

      if (remaining % 20 == 0) {
        ServerBossEvent bar = session.getBossBar();
        if (bar != null) {
          bar.setName(barName(remaining));
          bar.setProgress(
            Math.max(0f, Math.min(1f, remaining / (float) (config.roundSeconds * 20)))
          );
        }
      }
      if (!session.isWarned60() && remaining <= 60 * 20) {
        session.setWarned60(true);
        player.sendSystemMessage(
          Component.literal("§e[Preserve] §7One minute left — make it count.")
        );
      }
      if (!session.isWarned10() && remaining <= 10 * 20) {
        session.setWarned10(true);
        player.sendSystemMessage(
          Component.literal("§c[Preserve] §7Ten seconds — Helga's ringing the bell.")
        );
      }

      if (tickBoundary(player, session, remaining)) {
        endInPlace.add(player);
        continue;
      }
      if (tickBallsGrace(player, session, remaining)) {
        endInPlace.add(player);
        continue;
      }

      tickScatters(player, session);
      tickLures(player, session, remaining);

      // Rounds keep the grounds clear of hostiles (spec §9) — sweep on a slow cadence.
      if (remaining % HOSTILE_SWEEP_INTERVAL_TICKS == 0) {
        hostileSweep(server);
      }
    }

    gone.forEach(sessions::remove);
    for (ServerPlayer player : endInPlace) {
      SafariSession session = sessions.get(player.getUUID());
      if (session != null) endRound(server, player, session, false, "in-place end");
    }
    for (ServerPlayer player : ejects) {
      SafariSession session = sessions.get(player.getUUID());
      if (session != null) {
        player.sendSystemMessage(
          Component.literal("§6§l[Preserve] §r§eThe bell! Helga walks you back to the gate.")
        );
        endRound(server, player, session, true, "clock expiry");
      }
    }
  }

  /**
   * Boundary watch: every 20t re-test the polygon; outside starts a return countdown
   * (config.boundaryGraceSeconds), returning cancels it, expiring ends in place.
   * No "Safari Zone" SafeZone on this world → no boundary (standalone rule).
   */
  private boolean tickBoundary(ServerPlayer player, SafariSession session, int remaining) {
    if (remaining % 20 == 0) {
      NuzlockeConfig.SafeZone zone = findSafariZone();
      if (zone != null) {
        boolean inside = zone.contains(
          player.serverLevel().dimension().location().toString(),
          player.getBlockX(), player.getBlockY(), player.getBlockZ()
        );
        if (inside) {
          session.setBoundaryGraceTicks(-1);
        } else if (session.getBoundaryGraceTicks() < 0) {
          session.setBoundaryGraceTicks(Math.max(1, config.boundaryGraceSeconds) * 20);
          player.sendSystemMessage(
            Component.literal(
              "§c[Preserve] §7You've left the grounds — step back in within §e" +
              config.boundaryGraceSeconds + "s§7 or the round ends."
            )
          );
        }
      }
    }
    int grace = session.getBoundaryGraceTicks();
    if (grace > 0) {
      if (grace == 1) {
        if (holdEndForBusyLure(player, session)) {
          return false; // grace parks at 1 while the throw resolves
        }
        player.sendSystemMessage(
          Component.literal("§c[Preserve] §7Round called — you wandered off the grounds.")
        );
        return true;
      }
      session.setBoundaryGraceTicks(grace - 1);
    }
    return false;
  }

  /**
   * Mirror of the ball-exhaust grace for the two hard end paths (clock + boundary
   * expiry): a shaking Safari Ball holds its target busy, and tearing down now would
   * discard the entity mid-capture and eat the catch. Capped so a stuck busy flag can
   * never extend a round indefinitely. Voluntary exit + shutdown stay immediate.
   */
  private boolean holdEndForBusyLure(ServerPlayer player, SafariSession session) {
    if (!anyLureBusy(player.serverLevel(), session)) return false;
    int held = session.getEndBusyHoldTicks();
    if (held >= BUSY_GRACE_TICKS) return false;
    session.setEndBusyHoldTicks(held + 1);
    return true;
  }

  /**
   * Out-of-balls wrap-up: the last spent marked ball starts a 100t grace — cancelled
   * if one reappears (a missed throw picked back up), held while any lure is mid-shake
   * (the last ball may still land the catch), then the round ends in place.
   */
  private boolean tickBallsGrace(ServerPlayer player, SafariSession session, int remaining) {
    int grace = session.getBallsGraceTicks();
    if (grace < 0) {
      if (remaining % 20 == 0 && countIssuedBalls(player) == 0) {
        session.setBallsGraceTicks(BALLS_GRACE_TICKS);
        player.displayClientMessage(
          Component.literal("§eOut of Safari Balls — wrapping up…"),
          true
        );
      }
      return false;
    }
    if (countIssuedBalls(player) > 0) {
      session.setBallsGraceTicks(-1);
      return false;
    }
    if (anyLureBusy(player.serverLevel(), session)) {
      return false; // mid-shake — hold the countdown for the throw to resolve
    }
    if (grace <= 1) {
      return true;
    }
    session.setBallsGraceTicks(grace - 1);
    return false;
  }

  private boolean anyLureBusy(ServerLevel level, SafariSession session) {
    for (SafariSession.ActiveLure lure : session.getLures()) {
      Entity e = level.getEntity(lure.entityUuid);
      if (e instanceof PokemonEntity pe && pe.isBusy()) return true;
    }
    return false;
  }

  private void tickScatters(ServerPlayer player, SafariSession session) {
    Iterator<SafariSession.PendingScatter> it = session.getPendingScatters().iterator();
    while (it.hasNext()) {
      SafariSession.PendingScatter scatter = it.next();
      if (--scatter.ticksUntilSpawn > 0) continue;
      it.remove();
      spawnLures(player, session, scatter);
    }
  }

  /** Suspense over: roll 1–3 spawns from the bait's table at the spot. */
  private void spawnLures(
    ServerPlayer player,
    SafariSession session,
    SafariSession.PendingScatter scatter
  ) {
    ServerLevel level = player.serverLevel();
    RandomSource random = level.getRandom();
    int min = Math.max(1, config.spawnsMin);
    int max = Math.max(min, config.spawnsMax);
    int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
    int spawned = 0;

    for (int i = 0; i < count; i++) {
      SafariLureTables.Roll roll = lureTables.roll(scatter.baitType, scatter.warmth, random);
      if (roll == null) continue;
      try {
        PokemonProperties props = PokemonProperties.Companion.parse(roll.properties());
        PokemonEntity entity = props.createEntity(level);
        double x = scatter.x + (random.nextDouble() - 0.5) * 4.0;
        double z = scatter.z + (random.nextDouble() - 0.5) * 4.0;
        double y = snapToGround(level, x, scatter.y, z);
        float yaw = random.nextFloat() * 360f;
        entity.moveTo(x, y, z, yaw, 0f);
        // Deliberately NOT persistence-required: lures must stay sweepable — the
        // window discard owns their lifecycle, vanilla despawn is the safety net.
        entity.addTag(LURE_TAG);
        level.addFreshEntity(entity);
        entity.cry();
        session.getLures().add(
          new SafariSession.ActiveLure(
            entity.getUUID(),
            entity.getPokemon().getUuid(),
            scatter.spotKey,
            roll.rarity(),
            config.windowSeconds * 20
          )
        );
        spawned++;
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to spawn safari lure ({}) for {}", roll.properties(), player.getName().getString(), e
        );
      }
    }

    if (spawned > 0) {
      level.sendParticles(
        ParticleTypes.POOF, scatter.x, scatter.y + 0.5, scatter.z, 14, 1.2, 0.4, 1.2, 0.03
      );
      player.displayClientMessage(
        Component.literal(
          spawned == 1
            ? "§aSomething answers the bait."
            : "§aThe bait draws a crowd — §e" + spawned + "§a of them."
        ),
        true
      );
    } else {
      player.displayClientMessage(
        Component.literal("§7The bait sits untouched. Nothing answers."),
        true
      );
    }
  }

  /**
   * Ground-snap one spawn position: walk DOWN from the scatter spot (up to
   * {@link #GROUND_SNAP_RANGE} blocks) to the first passable block sitting on solid
   * ground, so a honey smear clicked high on a trunk or into the canopy drops its
   * lures at the tree's BASE — never mid-air. LEAVES never count as the supporting
   * ground (a canopy top is not the forest floor), which lets the walk carry through
   * the crown to the soil beneath. Every table rides this; on flat ground the very
   * first step matches (the scatter y is already the air above the clicked block).
   * Nothing standable within range → the unsnapped y (vanilla gravity settles it).
   */
  private static double snapToGround(ServerLevel level, double x, double y, double z) {
    BlockPos start = BlockPos.containing(x, y, z);
    for (int i = 0; i <= GROUND_SNAP_RANGE; i++) {
      BlockPos check = start.below(i);
      BlockPos below = check.below();
      BlockState belowState = level.getBlockState(below);
      if (level.getBlockState(check).getCollisionShape(level, check).isEmpty()
        && !belowState.getCollisionShape(level, below).isEmpty()
        && !belowState.is(BlockTags.LEAVES)) {
        return check.getY();
      }
    }
    return y;
  }

  /**
   * Lure upkeep: window countdown (expired lures wander off), stealth detection every
   * 10t (non-friendly only), and flee movement for spooked lures.
   */
  private void tickLures(ServerPlayer player, SafariSession session, int remaining) {
    ServerLevel level = player.serverLevel();
    boolean detectTick = remaining % 10 == 0; // per-tick cost cap: raycasts on a cadence
    Iterator<SafariSession.ActiveLure> it = session.getLures().iterator();
    while (it.hasNext()) {
      SafariSession.ActiveLure lure = it.next();
      Entity entity = level.getEntity(lure.entityUuid);
      if (entity == null || !entity.isAlive()) {
        it.remove(); // captured, KO'd, or naturally despawned — nothing to sweep
        continue;
      }

      // Spooked: flee straight away from the player, then poof.
      if (lure.fleeTicksLeft > 0) {
        if (entity instanceof PokemonEntity pe && pe.isBusy()) {
          continue; // a ball is mid-shake — never vaporize a resolving throw
        }
        Vec3 away = new Vec3(
          entity.getX() - player.getX(), 0, entity.getZ() - player.getZ()
        );
        Vec3 dir = away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize();
        entity.setDeltaMovement(dir.x * 0.45, entity.getDeltaMovement().y, dir.z * 0.45);
        entity.hurtMarked = true; // sync the server velocity to the client
        if (--lure.fleeTicksLeft <= 0) {
          String species = entity instanceof PokemonEntity pe
            ? pe.getPokemon().getSpecies().getName() : "Pokémon";
          level.sendParticles(
            ParticleTypes.CLOUD,
            entity.getX(), entity.getY() + 0.6, entity.getZ(),
            8, 0.4, 0.3, 0.4, 0.02
          );
          entity.discard();
          it.remove();
          player.displayClientMessage(
            Component.literal("§cThe " + species + " spooked and bolted!"),
            true
          );
        }
        continue;
      }

      // Stealth detection — friendly lures never aggro.
      if (detectTick && !lure.friendly) {
        tickDetection(player, session, lure, entity, level);
        if (lure.fleeTicksLeft > 0) continue; // just spooked — skip the window tick
      }

      if (--lure.ticksRemaining > 0) continue;

      // A ball mid-shake holds the entity busy — discarding now would strand the
      // capture. Give the throw a short grace and re-check.
      if (entity instanceof PokemonEntity pe && pe.isBusy()) {
        lure.ticksRemaining = BUSY_GRACE_TICKS;
        continue;
      }
      level.sendParticles(
        ParticleTypes.CLOUD,
        entity.getX(), entity.getY() + 0.6, entity.getZ(),
        8, 0.4, 0.3, 0.4, 0.02
      );
      entity.discard();
      it.remove();
    }
  }

  /**
   * One detection check (every 10t per non-friendly lure): the player is CONCEALED if
   * crouching in cover (feet or eye block in the concealment set); otherwise a COLLIDER
   * raycast lure-eye → player-eye decides (leaves are full collider blocks, so "behind
   * a bush" blocks the line naturally; grass does not — hence the crouch rule).
   * Seen → alert+1; unseen → decay 1 (floor 0); alert ≥ alertChecks → flee.
   */
  private void tickDetection(
    ServerPlayer player,
    SafariSession session,
    SafariSession.ActiveLure lure,
    Entity entity,
    ServerLevel level
  ) {
    double rangeSq = config.detectRange * config.detectRange;
    if (entity.distanceToSqr(player) > rangeSq) {
      lure.alert = Math.max(0, lure.alert - 1);
      return;
    }

    boolean seen;
    if (player.isCrouching() && isInConcealment(level, player)) {
      seen = false;
    } else {
      HitResult clip = level.clip(new ClipContext(
        entity.getEyePosition(),
        player.getEyePosition(),
        ClipContext.Block.COLLIDER,
        ClipContext.Fluid.NONE,
        entity
      ));
      seen = clip.getType() == HitResult.Type.MISS;
    }

    if (seen) {
      lure.alert++;
      if (lure.alert >= Math.max(1, config.alertChecks)) {
        startFlee(player, session, lure);
      }
    } else {
      lure.alert = Math.max(0, lure.alert - 1);
    }
  }

  /** The hardcoded concealment set — crouching inside any of these hides the player. */
  private static boolean isInConcealment(ServerLevel level, ServerPlayer player) {
    BlockState feet = level.getBlockState(player.blockPosition());
    BlockState eyes = level.getBlockState(BlockPos.containing(player.getEyePosition()));
    return isConcealmentBlock(feet) || isConcealmentBlock(eyes);
  }

  private static boolean isConcealmentBlock(BlockState state) {
    return state.is(Blocks.SHORT_GRASS)
      || state.is(Blocks.TALL_GRASS)
      || state.is(Blocks.FERN)
      || state.is(Blocks.LARGE_FERN)
      || state.is(Blocks.DEAD_BUSH)
      || state.is(Blocks.SWEET_BERRY_BUSH)
      || state.is(Blocks.AZALEA)
      || state.is(Blocks.FLOWERING_AZALEA)
      || state.is(BlockTags.LEAVES);
  }

  /** AGGRO: the lure bolts directly away for config.fleeTicks, then poofs. */
  private void startFlee(ServerPlayer player, SafariSession session, SafariSession.ActiveLure lure) {
    lure.fleeTicksLeft = Math.max(1, config.fleeTicks);
    Entity entity = player.serverLevel().getEntity(lure.entityUuid);
    if (entity instanceof PokemonEntity pe) {
      pe.cry();
    }
  }

  // ── Catch bookkeeping ───────────────────────────────────────────────────────────

  /**
   * Session-gated POKEMON_CAPTURED handler (LOWEST — after Nuzlocke's NORMAL handler
   * settles duplicate/PC routing): ledger ONLY catches whose Pokémon UUID matches a
   * tracked lure (custody makes anything else near-impossible), record the lure's
   * rarity + friendly flag for contest scoring, warm the spot, and advance the
   * lifetime counter + milestone latches.
   */
  private void onSafariCapture(
    ServerPlayer player,
    SafariSession session,
    Pokemon pokemon
  ) {
    SafariSession.ActiveLure matched = null;
    Iterator<SafariSession.ActiveLure> it = session.getLures().iterator();
    while (it.hasNext()) {
      SafariSession.ActiveLure lure = it.next();
      if (lure.pokemonUuid.equals(pokemon.getUuid())) {
        matched = lure;
        it.remove();
        break;
      }
    }
    if (matched == null) {
      return; // not one of ours — custody leaves no other catch path worth ledgering
    }

    String species = pokemon.getSpecies().getName();
    int level = pokemon.getLevel();
    session.getCatches().add(
      new SafariSession.CatchRecord(
        species, level, pokemon.getUuid(), matched.rarity, matched.friendly
      )
    );

    // Warm-spot bump: a catch of a lure heats ITS scatter spot (+1 tier, cap 2).
    session.bumpWarmth(matched.spotKey);
    player.displayClientMessage(
      Component.literal("§6The spot runs warm — the next scatter here draws better stock."),
      true
    );

    LifetimeStats stats = lifetime.computeIfAbsent(player.getUUID(), id -> new LifetimeStats());
    stats.lifetimeCatches++;
    mirrorCatchScore(player);

    player.sendSystemMessage(
      Component.literal(
        "§a[Preserve] §e" + species + "§a goes on the round ledger (§e" +
        session.getCatches().size() + "§a this round, §e" + stats.lifetimeCatches + "§a lifetime)."
      )
    );

    // One-time care-package milestones (10/25 lifetime) — never on repeatables;
    // these latches persist in the world dir and can each fire exactly once.
    MinecraftServer server = player.getServer();
    if (server != null) {
      if (!stats.milestone10 && stats.lifetimeCatches >= MILESTONE_FIRST) {
        stats.milestone10 = true;
        grantMilestone(server, player, "cobblemon_initiative:npc_gift/uncommon", MILESTONE_FIRST);
      }
      if (!stats.milestone25 && stats.lifetimeCatches >= MILESTONE_SECOND) {
        stats.milestone25 = true;
        grantMilestone(server, player, "cobblemon_initiative:npc_gift/rare", MILESTONE_SECOND);
      }
      saveStats(server); // write-through — milestone latches must survive a crash
    }
  }

  private void grantMilestone(
    MinecraftServer server,
    ServerPlayer player,
    String lootTable,
    int threshold
  ) {
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withSuppressedOutput(),
      "loot give " + player.getGameProfile().getName() + " loot " + lootTable
    );
    player.sendSystemMessage(
      Component.literal(
        "§6§l[Preserve] §r§eHelga beams — §6" + threshold +
        "§e lifetime Preserve catches. She presses a care package into your hands."
      )
    );
    InitiativeInit.LOGGER.info(
      "Safari milestone {} reached by {} — granted {}.",
      threshold, player.getName().getString(), lootTable
    );
  }

  /** Mirror the LIFETIME catch count into the ci_safari_catches objective. */
  private void mirrorCatchScore(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return;
    Scoreboard scoreboard = server.getScoreboard();
    Objective objective = scoreboard.getObjective(CATCH_OBJECTIVE);
    if (objective == null) {
      objective = scoreboard.addObjective(
        CATCH_OBJECTIVE,
        ObjectiveCriteria.DUMMY,
        Component.literal("Safari catches"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }
    LifetimeStats stats = lifetime.computeIfAbsent(player.getUUID(), id -> new LifetimeStats());
    scoreboard.getOrCreatePlayerScore(player, objective).set(stats.lifetimeCatches);
  }

  // ── Status ──────────────────────────────────────────────────────────────────────

  /** /cobblemon-initiative safari status */
  public int status(ServerPlayer player) {
    SafariSession session = sessions.get(player.getUUID());
    LifetimeStats stats = lifetime.get(player.getUUID());
    int lifetimeCatches = stats != null ? stats.lifetimeCatches : 0;
    if (session == null) {
      player.sendSystemMessage(
        Component.literal(
          "§e[Preserve] §7No round running. Lifetime catches: §e" + lifetimeCatches +
          "§7" + (stats != null && stats.bestContestScore > 0
            ? " · Best contest: §e" + stats.bestContestScore + "§7." : ".")
        )
      );
      return 1;
    }
    StringBuilder line = new StringBuilder()
      .append("§6§l[Preserve] §r§eRound: §6")
      .append(session.getMode() == SafariSession.Mode.CONTEST ? "contest" : "capture")
      .append("§e · Clock: §6").append(formatTime(session.getTicksRemaining()))
      .append("§e · Balls: §6").append(countIssuedBalls(player))
      .append("§e · Catches: §6").append(session.getCatches().size());
    if (session.getMode() == SafariSession.Mode.CONTEST) {
      int running = 0;
      for (SafariSession.CatchRecord record : session.getCatches()) {
        running += pointsFor(record);
      }
      line.append("§e · Score: §6").append(running);
    }
    player.sendSystemMessage(Component.literal(line.toString()));
    return 1;
  }

  // ── Zone geometry + sweeps ──────────────────────────────────────────────────────

  /** Name-keyed "Safari Zone" SafeZone lookup — null on bare-mod worlds (skip checks). */
  private NuzlockeConfig.SafeZone findSafariZone() {
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    if (cfg == null || cfg.getSafeZones() == null) return null;
    for (NuzlockeConfig.SafeZone zone : cfg.getSafeZones()) {
      if (SAFARI_ZONE_NAME.equals(zone.name)) return zone;
    }
    return null;
  }

  /** Full-height AABB over the zone's footprint (polygon bbox, or the radius square). */
  private AABB zoneBox(NuzlockeConfig.SafeZone zone, ServerLevel level) {
    int minX;
    int maxX;
    int minZ;
    int maxZ;
    if (zone.polygon != null && zone.polygon.length >= 3) {
      minX = Integer.MAX_VALUE;
      maxX = Integer.MIN_VALUE;
      minZ = Integer.MAX_VALUE;
      maxZ = Integer.MIN_VALUE;
      for (int[] vertex : zone.polygon) {
        minX = Math.min(minX, vertex[0]);
        maxX = Math.max(maxX, vertex[0]);
        minZ = Math.min(minZ, vertex[1]);
        maxZ = Math.max(maxZ, vertex[1]);
      }
    } else {
      minX = zone.centerX - zone.radius;
      maxX = zone.centerX + zone.radius;
      minZ = zone.centerZ - zone.radius;
      maxZ = zone.centerZ + zone.radius;
    }
    return new AABB(
      minX, level.getMinBuildHeight(), minZ,
      maxX + 1, level.getMaxBuildHeight(), maxZ + 1
    );
  }

  /**
   * Discard every Monster-category mob inside the Safari Zone (AABB prefilter from the
   * polygon's bounding box, then the exact polygon test). Runs at round start and every
   * {@link #HOSTILE_SWEEP_INTERVAL_TICKS} during rounds. Only natural wander-ins may
   * die: Easy NPC registers its monster-flavoured NPC types under MONSTER (the
   * bogged/husk casts), and named or persistence-flagged mobs are authored bodies or
   * player-marked — none of them are the sweep's business.
   */
  private int hostileSweep(MinecraftServer server) {
    NuzlockeConfig.SafeZone zone = findSafariZone();
    if (zone == null || server == null) return 0;
    ServerLevel level = resolveLevel(server, zone.dimension);
    if (level == null) return 0;

    List<Mob> hostiles = level.getEntitiesOfClass(
      Mob.class,
      zoneBox(zone, level),
      mob -> mob.getType().getCategory() == MobCategory.MONSTER
        && !"easy_npc".equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace())
        && !mob.isPersistenceRequired()
        && !mob.hasCustomName()
        && zone.contains(
          zone.dimension, mob.getBlockX(), mob.getBlockY(), mob.getBlockZ()
        )
    );
    for (Mob mob : hostiles) {
      mob.discard();
    }
    return hostiles.size();
  }

  /** Discard ground drops of the marked kit (a hardcore death scatters it at the corpse). */
  private int sweepDroppedKit(MinecraftServer server) {
    NuzlockeConfig.SafeZone zone = findSafariZone();
    if (zone == null || server == null) return 0;
    ServerLevel level = resolveLevel(server, zone.dimension);
    if (level == null) return 0;

    List<ItemEntity> drops = level.getEntitiesOfClass(
      ItemEntity.class,
      zoneBox(zone, level),
      drop -> hasIssueMarker(drop.getItem()) || isBaitItem(drop.getItem())
    );
    for (ItemEntity drop : drops) {
      drop.discard();
    }
    if (!drops.isEmpty()) {
      InitiativeInit.LOGGER.info(
        "Swept {} dropped safari kit item(s) off the grounds.", drops.size()
      );
    }
    return drops.size();
  }

  /** Discard every loaded entity carrying the lure tag (session end + server start). */
  private int sweepStrayLures(MinecraftServer server) {
    // Only sweep tags NOT tracked by a live session (a global sweep during one
    // player's exit must not vaporize another session's active lures).
    java.util.Set<UUID> tracked = new java.util.HashSet<>();
    for (SafariSession session : sessions.values()) {
      for (SafariSession.ActiveLure lure : session.getLures()) {
        tracked.add(lure.entityUuid);
      }
    }
    int swept = 0;
    for (ServerLevel level : server.getAllLevels()) {
      List<Entity> strays = new ArrayList<>();
      for (Entity entity : level.getAllEntities()) {
        if (entity.getTags().contains(LURE_TAG) && !tracked.contains(entity.getUUID())) {
          strays.add(entity);
        }
      }
      for (Entity stray : strays) {
        if (stray.isAlive()) {
          stray.discard();
          swept++;
        }
      }
    }
    return swept;
  }

  // ── Persistence (lifetime stats + custody — sessions stay volatile) ─────────────

  private Path statsPath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(STATS_FILE_NAME);
  }

  private Path custodyPath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(CUSTODY_FILE_NAME);
  }

  private void loadStats(MinecraftServer server) {
    lifetime.clear();
    Path path = statsPath(server);
    if (!Files.exists(path)) return;
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      Type type = new TypeToken<Map<String, LifetimeStats>>() {}.getType();
      Map<String, LifetimeStats> data = GSON.fromJson(reader, type);
      if (data != null) {
        for (Map.Entry<String, LifetimeStats> entry : data.entrySet()) {
          lifetime.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
      }
      InitiativeInit.LOGGER.info("Loaded safari stats for {} player(s).", lifetime.size());
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load safari stats.", e);
    }
  }

  private void saveStats(MinecraftServer server) {
    Path path = statsPath(server);
    try {
      Files.createDirectories(path.getParent());
      Map<String, LifetimeStats> data = new HashMap<>();
      for (Map.Entry<UUID, LifetimeStats> entry : lifetime.entrySet()) {
        data.put(entry.getKey().toString(), entry.getValue());
      }
      try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        GSON.toJson(data, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to save safari stats.", e);
    }
  }

  private void loadCustody(MinecraftServer server) {
    custody.clear();
    Path path = custodyPath(server);
    if (!Files.exists(path)) return;
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      CustodyFile data = GSON.fromJson(reader, CustodyFile.class);
      if (data != null && data.records != null) {
        for (Map.Entry<String, CustodyRecord> entry : data.records.entrySet()) {
          custody.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
      }
      if (!custody.isEmpty()) {
        InitiativeInit.LOGGER.info(
          "Loaded {} pending safari custody record(s) — will restore on join.", custody.size()
        );
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load safari custody.", e);
      // Quarantine, never clobber: with the parse failed, the next write-through
      // would truncate whatever the file still holds — and it is the sole copy of a
      // taken party. The sidecar keeps it recoverable by hand.
      try {
        Path corrupt = path.resolveSibling(
          CUSTODY_FILE_NAME + "." + System.currentTimeMillis() + ".corrupt"
        );
        Files.move(path, corrupt, StandardCopyOption.REPLACE_EXISTING);
        InitiativeInit.LOGGER.error(
          "Quarantined the unreadable safari custody file at {}.", corrupt
        );
      } catch (Exception moveError) {
        InitiativeInit.LOGGER.error(
          "Could not quarantine the unreadable safari custody file.", moveError
        );
      }
    }
  }

  /** @return false on a failed write — resolvePendingPermit ABORTS round entry on it. */
  private boolean saveCustody(MinecraftServer server) {
    Path path = custodyPath(server);
    try {
      Files.createDirectories(path.getParent());
      CustodyFile data = new CustodyFile();
      for (Map.Entry<UUID, CustodyRecord> entry : custody.entrySet()) {
        data.records.put(entry.getKey().toString(), entry.getValue());
      }
      // Temp sibling + atomic move: a crash mid-write must never torn-write the sole
      // recovery artifact (same directory, so ATOMIC_MOVE is always same-filesystem).
      Path tmp = path.resolveSibling(CUSTODY_FILE_NAME + ".tmp");
      try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
        GSON.toJson(data, writer);
      }
      Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to save safari custody.", e);
      return false;
    }
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────────

  private Component barName(int ticksRemaining) {
    return Component.literal(
      "§6Safari Round §7— §e" + formatTime(ticksRemaining)
    );
  }

  private static String formatTime(int ticks) {
    int seconds = Math.max(0, ticks / 20);
    return String.format("%d:%02d", seconds / 60, seconds % 60);
  }

  private ServerLevel resolveLevel(MinecraftServer server, String dimension) {
    if (server == null || dimension == null) return null;
    try {
      return server.getLevel(
        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension))
      );
    } catch (Exception e) {
      return null;
    }
  }
}

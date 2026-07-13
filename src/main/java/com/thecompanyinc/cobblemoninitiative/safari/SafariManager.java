package com.thecompanyinc.cobblemoninitiative.safari;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * The Biodiversity Asset Preserve — "the Baiting Yards" (showrunner-locked design,
 * docs/SAFARI_ZONE_CONCEPTS.md SELECTED DIRECTION). A paid, timed, CATCH-ONLY safari
 * whose core loop is LURING: scatter bait, wait out a suspense window, and wild
 * Pokémon from that bait's table answer at the spot, catchable for a limited window.
 *
 * <p>Pure CobbleDollars sink: the permit fee rides the shipped pay-probe idiom
 * ({@code safari/permit_fee.mcfunction}, gate on {@code store result} — CobbleDollars
 * {@code pay} soft-fails). No payouts inside, ever. The one keepable catch (the
 * "Acquisition") is a STREAM rule declared at exit via chat vote — this manager only
 * prints the visit's catch ledger; specimen custody is not enforced in code.
 *
 * <p>Hardcore invariants (the load-bearing safety items):
 * <ul>
 *   <li>BATTLE_STARTED_PRE is cancelled at {@link Priority#HIGHEST} while any player
 *       actor holds an active session — no battle can start, so no faint, flee-kill,
 *       sacrifice, or whiteout path is reachable inside a visit.</li>
 *   <li>No damage path exists here: session ends are teleports only, and the eject
 *       destination is the recorded ENTRY position (ground the player verifiably
 *       stood on) unless the config pins an eject pad.</li>
 *   <li>Logout forfeits the visit, no refund — sessions are never persisted, and on
 *       single-player a logout stops the integrated server (the concepts doc's
 *       stale-session latch reduces to "don't persist sessions").</li>
 * </ul>
 *
 * <p>Session gating is by SESSION STATE, not zone geometry — the shipped install.json
 * "Safari Zone" LANDMARK polygon stays untouched (its runtime containment is a
 * circumscribed circle that overhangs neighbouring routes; keying logic on it would
 * misattribute route catches).
 */
public class SafariManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final String STATS_FILE_NAME = "cobblemon_initiative_safari.json";

  /** Entity tag on every manager-spawned lure — swept on session end + server start. */
  public static final String LURE_TAG = "ci_safari_lure";

  /** Scoreboard objective mirroring LIFETIME safari catches (datapack-visible). */
  public static final String CATCH_OBJECTIVE = "ci_safari_catches";

  /** Custom-data key marking Company-issue safari balls (exit clawback target). */
  private static final String BALL_MARKER = "ci_safari_issue";

  /** Custom-data key carrying a bait item's table id. */
  private static final String BAIT_MARKER = "ci_bait";

  private static final String SAFARI_BALL_ID = "cobblemon:safari_ball";

  /** Grace extension when a lure's window ends mid-capture (ball shaking). */
  private static final int BUSY_GRACE_TICKS = 40;

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
  }

  private final Map<UUID, LifetimeStats> lifetime = new HashMap<>();
  /** Players whose permit fee is dispatched but unresolved — read #sf_ok next tick. */
  private final Set<UUID> pendingPermits = new HashSet<>();
  private MinecraftServer server;

  // ── Wiring ────────────────────────────────────────────────────────────────────

  public void load() {
    config = SafariConfig.load();
    lureTables = SafariLureTables.load();
  }

  /**
   * Cobblemon event subscriptions — called once from InitiativeInit.onInitialize().
   * POKEMON_CAPTURED already has two subscribers (NuzlockeInit, NobleEncounterInit);
   * a third self-contained one is idiomatic. The battle guard subscribes HIGHEST so it
   * cancels before any other handler sees the battle.
   */
  public void registerEvents() {
    CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGHEST, event -> {
      for (ServerPlayer player : event.getBattle().getPlayers()) {
        if (sessions.containsKey(player.getUUID())) {
          event.cancel();
          event.setReason(
            Component.literal("Preserve policy: your party stays holstered.")
          );
          player.displayClientMessage(
            Component.literal("§c[Preserve] §7Engagements are not Verified Activities. Your party stays holstered."),
            true
          );
          InitiativeInit.LOGGER.info(
            "Cancelled battle start inside safari session for {}",
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
  }

  public void onServerStarted(MinecraftServer server) {
    this.server = server;
    loadStats(server);
    // Sweep stray lures from a previous run (non-persistent entities usually despawn
    // on their own, but a save mid-window can strand one in the hardcore save).
    int swept = sweepStrayLures(server);
    if (swept > 0) {
      InitiativeInit.LOGGER.info("Swept {} stray safari lure(s) at server start.", swept);
    }
  }

  public void onServerStopping(MinecraftServer server) {
    // Sessions forfeit (never persisted); bodies and bars must not outlive them.
    for (SafariSession session : sessions.values()) {
      teardown(server, session);
    }
    sessions.clear();
    saveStats(server);
  }

  public boolean hasSession(UUID playerId) {
    return sessions.containsKey(playerId);
  }

  public java.util.Set<String> getBaitTypes() {
    return lureTables.baitTypes();
  }

  // ── Permit purchase / enter ───────────────────────────────────────────────────

  /** /cobblemon-initiative safari enter — badge gate → pay-probe → session start. */
  public boolean enter(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    this.server = server;

    if (sessions.containsKey(player.getUUID())) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7Your Day Permit is already active.")
      );
      return false;
    }

    int badges = badgeCount(player);
    if (badges < config.gateBadges) {
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7Intake requires §e" + config.gateBadges +
          "§7 verified gym badges (you hold §e" + badges + "§7). Credentials first, access second."
        )
      );
      return false;
    }

    // Dispatch the pay-probe now but read #sf_ok NEXT TICK: the function's effects are
    // not reliably visible immediately after performPrefixedCommand returns (runtime-
    // found 2026-07-12 — the fee left the balance while the same-tick read saw 0 and
    // the session never started). tick() resolves pendingPermits one tick later.
    dispatchPermitFee(server, player);
    pendingPermits.add(player.getUUID());
    return true;
  }

  /** Deferred half of enter(): #sf_ok is readable one tick after the fee dispatch. */
  private void resolvePendingPermit(ServerPlayer player) {
    if (!readPermitProbe(server)) {
      // The mcfunction already printed the branded actionbar decline; chat gets the receipt.
      player.sendSystemMessage(
        Component.literal(
          "§c[Preserve] §7Payment declined — the Preserve does not extend credit. (§e" +
          config.permitFee + " CD§7 required)"
        )
      );
      return;
    }

    SafariSession session = new SafariSession(
      player.getUUID(),
      player.serverLevel().dimension().location().toString(),
      player.getX(),
      player.getY(),
      player.getZ(),
      player.getYRot(),
      player.getXRot(),
      config.clockSeconds * 20
    );

    ServerBossEvent bar = new ServerBossEvent(
      barName(config.clockSeconds * 20),
      BossEvent.BossBarColor.YELLOW,
      BossEvent.BossBarOverlay.PROGRESS
    );
    bar.setProgress(1.0f);
    bar.addPlayer(player);
    session.setBossBar(bar);

    sessions.put(player.getUUID(), session);
    issueBalls(player);
    mirrorCatchScore(player);

    player.sendSystemMessage(
      Component.literal(
        "§6§l[Preserve] §r§aDay Permit approved — §e" + config.permitFee +
        " CD§a. §e" + config.balls + "§a Company-issue Safari Balls, §e" +
        formatTime(config.clockSeconds * 20) + "§a on the site clock."
      )
    );
    player.sendSystemMessage(
      Component.literal(
        "§7Catch-only grounds. Scatter bait on open ground and hold still — the Yards answer. " +
        "Unspent munitions are confiscated at egress."
      )
    );
    InitiativeInit.LOGGER.info(
      "Safari session started for {} ({} CD, {} balls, {}s).",
      player.getName().getString(), config.permitFee, config.balls, config.clockSeconds
    );
  }

  /**
   * Dispatch the permit pay-probe ({@code safari/permit_fee}) AS THE PLAYER (the
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

  /** One tick after {@link #dispatchPermitFee}: 0 = broke/declined, amount = paid. */
  private boolean readPermitProbe(MinecraftServer server) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective calc = scoreboard.getObjective("cd_calc");
    if (calc == null) return false;
    return scoreboard
      .getOrCreatePlayerScore(ScoreHolder.forNameOnly("#sf_ok"), calc)
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

  // ── Exit paths ────────────────────────────────────────────────────────────────

  /** /cobblemon-initiative safari exit — voluntary end: clawback + ledger, no teleport. */
  public boolean exitVoluntary(ServerPlayer player) {
    SafariSession session = sessions.remove(player.getUUID());
    if (session == null) {
      player.sendSystemMessage(
        Component.literal("§e[Preserve] §7No active Day Permit on file.")
      );
      return false;
    }
    MinecraftServer server = player.getServer();
    endSession(server, player, session, false);
    return true;
  }

  /** Site clock expired — escorted (teleported) back to the recorded entry position. */
  private void eject(MinecraftServer server, ServerPlayer player, SafariSession session) {
    sessions.remove(player.getUUID());
    player.sendSystemMessage(
      Component.literal("§6§l[Preserve] §r§eSite clock expired. You are escorted to the gate.")
    );
    endSession(server, player, session, true);
  }

  /** Shared end-of-visit: teardown, ball clawback, catch ledger, optional escort. */
  private void endSession(
    MinecraftServer server,
    ServerPlayer player,
    SafariSession session,
    boolean escort
  ) {
    teardown(server, session);
    if (server != null) sweepStrayLures(server);

    int confiscated = clawBackBalls(player);
    printLedger(player, session, confiscated);

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

    InitiativeInit.LOGGER.info(
      "Safari session ended for {} ({} catch(es), {} ball(s) confiscated, escort={}).",
      player.getName().getString(), session.getCatches().size(), confiscated, escort
    );
  }

  /** Player vanished mid-session (logout) — forfeit: quiet teardown, no refund. */
  private void forfeit(MinecraftServer server, SafariSession session) {
    teardown(server, session);
    if (server != null) sweepStrayLures(server);
    InitiativeInit.LOGGER.info(
      "Safari session forfeited (player {} logged out).", session.getPlayerId()
    );
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

  /** The end-of-visit catch ledger — the on-stream chat vote reads THIS. */
  private void printLedger(ServerPlayer player, SafariSession session, int confiscated) {
    List<SafariSession.CatchRecord> catches = session.getCatches();
    if (catches.isEmpty()) {
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Preserve] §r§7Visit ledger: no assets acquired. The Company thanks you for your contribution."
        )
      );
    } else {
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Preserve] §r§eVisit ledger — §6" + catches.size() + "§e acquisition candidate(s):"
        )
      );
      for (SafariSession.CatchRecord record : catches) {
        player.sendSystemMessage(
          Component.literal("§7  • §e" + record.species + " §7(Lv. " + record.level + ")")
        );
      }
      player.sendSystemMessage(
        Component.literal(
          "§7Declare the §eAcquisition§7 on stream — chat votes. Every other specimen files to Company Custody."
        )
      );
    }
    if (confiscated > 0) {
      player.sendSystemMessage(
        Component.literal(
          "§7Unspent munitions confiscated at egress: §e" + confiscated +
          "§7. Verified Access. Verified Value."
        )
      );
    }
  }

  // ── Safari balls (issue + clawback; marker component, never player property) ────

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
        Component.literal("§fPreserve-Issue Safari Ball")
      );
      stack.set(
        DataComponents.LORE,
        new ItemLore(List.of(
          Component.literal("§8Company property. Non-transferable."),
          Component.literal("§8Confiscated at egress.")
        ))
      );
      player.getInventory().placeItemBackInInventory(stack);
    }
  }

  private static boolean isIssuedBall(ItemStack stack) {
    if (stack.isEmpty()) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(BALL_MARKER);
  }

  /** Remove ONLY marked balls; player-bought safari balls are never touched. */
  private int clawBackBalls(ServerPlayer player) {
    Inventory inv = player.getInventory();
    int removed = 0;
    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack s = inv.getItem(i);
      if (isIssuedBall(s)) {
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
        Component.literal("§8Preserve bait — scatter on open ground."),
        Component.literal("§8Active Day Permit required.")
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
        Component.literal("§c[Preserve] §7Bait may only be scattered under an active Day Permit."),
        true
      );
      // FAIL: never let marked bait fall through to vanilla item behaviour.
      return InteractionResult.FAIL;
    }
    if (!lureTables.hasBait(baitType)) {
      serverPlayer.displayClientMessage(
        Component.literal("§c[Preserve] §7This bait lot has been discontinued."),
        true
      );
      return InteractionResult.FAIL;
    }

    held.shrink(1);
    enqueueScatter(serverPlayer, session, baitType, hit.getBlockPos());
    return InteractionResult.SUCCESS;
  }

  /**
   * The scatter itself, shared by the bait-item click and the perm-2
   * {@code safari scatter} dev hook (Carpet bots cannot fire UseBlockCallback, so
   * headless verification drives this directly; the click path is the LootChest/
   * DocProp callback precedent).
   */
  public boolean devScatter(ServerPlayer player, String baitType) {
    SafariSession session = sessions.get(player.getUUID());
    if (session == null || !lureTables.hasBait(baitType)) return false;
    enqueueScatter(player, session, baitType, player.blockPosition().below());
    return true;
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

  // ── Tick loop (driven from InitiativeInit END_SERVER_TICK) ─────────────────────

  public void tick(MinecraftServer server) {
    this.server = server;

    // Deferred permit resolution: the pay-probe's #sf_ok is reliable one tick after
    // the fee dispatch (see enter()). Resolve before session upkeep so a fresh
    // session ticks from its full clock.
    if (!pendingPermits.isEmpty()) {
      List<UUID> pending = new ArrayList<>(pendingPermits);
      pendingPermits.clear();
      for (UUID id : pending) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
          resolvePendingPermit(player);
        }
      }
    }

    if (sessions.isEmpty()) return;

    List<UUID> gone = new ArrayList<>();
    List<ServerPlayer> ejects = new ArrayList<>();

    for (SafariSession session : sessions.values()) {
      ServerPlayer player = server.getPlayerList().getPlayer(session.getPlayerId());
      if (player == null) {
        // Logout mid-visit = forfeit (no refund). On single-player this only triggers
        // in the LAN/dev-server case; the integrated-server path lands in onServerStopping.
        forfeit(server, session);
        gone.add(session.getPlayerId());
        continue;
      }

      int remaining = session.getTicksRemaining() - 1;
      session.setTicksRemaining(remaining);

      if (remaining <= 0) {
        ejects.add(player);
        continue;
      }

      if (remaining % 20 == 0) {
        ServerBossEvent bar = session.getBossBar();
        if (bar != null) {
          bar.setName(barName(remaining));
          bar.setProgress(
            Math.max(0f, Math.min(1f, remaining / (float) (config.clockSeconds * 20)))
          );
        }
      }
      if (!session.isWarned60() && remaining <= 60 * 20) {
        session.setWarned60(true);
        player.sendSystemMessage(
          Component.literal("§e[Preserve] §7One minute on the site clock. Egress procedures pending.")
        );
      }
      if (!session.isWarned10() && remaining <= 10 * 20) {
        session.setWarned10(true);
        player.sendSystemMessage(
          Component.literal("§c[Preserve] §7Ten seconds. Thank you for preserving with The Company.")
        );
      }

      tickScatters(player, session);
      tickLures(player, session);
    }

    gone.forEach(sessions::remove);
    for (ServerPlayer player : ejects) {
      SafariSession session = sessions.get(player.getUUID());
      if (session != null) eject(server, player, session);
    }
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
      String properties = lureTables.roll(scatter.baitType, scatter.warmth, random);
      if (properties == null) continue;
      try {
        PokemonProperties props = PokemonProperties.Companion.parse(properties);
        PokemonEntity entity = props.createEntity(level);
        double x = scatter.x + (random.nextDouble() - 0.5) * 4.0;
        double z = scatter.z + (random.nextDouble() - 0.5) * 4.0;
        float yaw = random.nextFloat() * 360f;
        entity.moveTo(x, scatter.y, z, yaw, 0f);
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
            config.windowSeconds * 20
          )
        );
        spawned++;
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to spawn safari lure ({}) for {}", properties, player.getName().getString(), e
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

  /** Window countdown: expired lures wander off (discard); captured ones drop out. */
  private void tickLures(ServerPlayer player, SafariSession session) {
    ServerLevel level = player.serverLevel();
    Iterator<SafariSession.ActiveLure> it = session.getLures().iterator();
    while (it.hasNext()) {
      SafariSession.ActiveLure lure = it.next();
      Entity entity = level.getEntity(lure.entityUuid);
      if (entity == null || !entity.isAlive()) {
        it.remove(); // captured, KO'd, or naturally despawned — nothing to sweep
        continue;
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

  // ── Catch bookkeeping ───────────────────────────────────────────────────────────

  /**
   * Session-gated POKEMON_CAPTURED handler: ledger the catch, warm the spot if the
   * mon was one of ours, advance the lifetime counter + milestone latches.
   */
  private void onSafariCapture(
    ServerPlayer player,
    SafariSession session,
    com.cobblemon.mod.common.pokemon.Pokemon pokemon
  ) {
    String species = pokemon.getSpecies().getName();
    int level = pokemon.getLevel();
    session.getCatches().add(new SafariSession.CatchRecord(species, level));

    // Warm-spot bump: a catch of a lure heats ITS scatter spot (+1 tier, cap 2).
    Iterator<SafariSession.ActiveLure> it = session.getLures().iterator();
    while (it.hasNext()) {
      SafariSession.ActiveLure lure = it.next();
      if (lure.pokemonUuid.equals(pokemon.getUuid())) {
        session.bumpWarmth(lure.spotKey);
        it.remove();
        player.displayClientMessage(
          Component.literal("§6The spot runs warm — the next scatter here draws better stock."),
          true
        );
        break;
      }
    }

    LifetimeStats stats = lifetime.computeIfAbsent(player.getUUID(), id -> new LifetimeStats());
    stats.lifetimeCatches++;
    mirrorCatchScore(player);

    player.sendSystemMessage(
      Component.literal(
        "§a[Preserve] §e" + species + "§a filed to the visit ledger (§e" +
        session.getCatches().size() + "§a this visit, §e" + stats.lifetimeCatches + "§a lifetime)."
      )
    );

    // One-time training-pack milestones (10/25 lifetime) — never on repeatables;
    // these latches persist in the world dir and can each fire exactly once.
    MinecraftServer server = player.getServer();
    if (server != null) {
      if (!stats.milestone10 && stats.lifetimeCatches >= MILESTONE_FIRST) {
        stats.milestone10 = true;
        grantMilestone(server, player, "cobblemon_initiative:npc_gift/training_minor", MILESTONE_FIRST);
      }
      if (!stats.milestone25 && stats.lifetimeCatches >= MILESTONE_SECOND) {
        stats.milestone25 = true;
        grantMilestone(server, player, "cobblemon_initiative:npc_gift/training_standard", MILESTONE_SECOND);
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
        "§6§l[Preserve] §r§eAcquisition volume bonus — §6" + threshold +
        "§e lifetime specimens. A training consignment has been released to you."
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
          "§e[Preserve] §7No active Day Permit. Lifetime specimens on file: §e" + lifetimeCatches + "§7."
        )
      );
      return 1;
    }
    player.sendSystemMessage(
      Component.literal(
        "§6§l[Preserve] §r§eSite clock: §6" + formatTime(session.getTicksRemaining()) +
        "§e · Issued balls: §6" + countIssuedBalls(player) +
        "§e · Ledger: §6" + session.getCatches().size() +
        "§e · Warm spots: §6" + session.getWarmSpots().size()
      )
    );
    return 1;
  }

  // ── Stray sweep ─────────────────────────────────────────────────────────────────

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

  // ── Persistence (lifetime stats only — sessions are deliberately volatile) ──────

  private Path statsPath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(STATS_FILE_NAME);
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

  // ── Helpers ─────────────────────────────────────────────────────────────────────

  private Component barName(int ticksRemaining) {
    return Component.literal(
      "§6Biodiversity Asset Preserve §7— §e" + formatTime(ticksRemaining)
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

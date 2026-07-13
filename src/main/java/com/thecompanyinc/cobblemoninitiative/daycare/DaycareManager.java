package com.thecompanyinc.cobblemoninitiative.daycare;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * Sango daycare — board up to {@value #MAX_SLOTS} party Pokémon with the keeper; they gain
 * slow tick-driven XP (always pre-clamped to the owner's live level cap) and reappear in the
 * pen as species-accurate stand-in entities. Withdrawal charges a CobbleDollars pickup fee
 * (feeBase + feePerLevel × levels gained) through the pay-probe command rail; a broke player's
 * Pokémon simply boards longer.
 *
 * HARDCORE INVARIANT: a boarded Pokémon can NEVER faint, die, or be lost. The real Pokemon
 * object lives ONLY in the custody JSON (single source of truth) plus this manager's in-memory
 * deserialized copy — never in any store, battle, or entity. The pen stand-in is a CLONE with
 * a fresh UUID (unbattleable, uncatchable, invulnerable, persistence-required); anything that
 * happens to it cannot touch the boarded mon. Custody survives relog/crash via write-through
 * saves on every deposit/withdraw/drip, and stand-ins are lazily reconciled from custody in
 * the tick once their chunk loads (SERVER_STARTED fires before chunks do — the same lazy
 * pattern as NpcPresetRefreshManager).
 */
public class DaycareManager {

  /** Entity tag marking pen stand-ins; custody JSON is truth — unmatched tags get discarded. */
  public static final String STANDIN_TAG = "ci_daycare_standin";
  /** Slots per player (spec constant — the party picker and withdraw range assume it). */
  public static final int MAX_SLOTS = 2;

  /** Stand-in reconcile cadence (ticks) — cheap loaded-entity sweep, single-player scale. */
  private static final int RECONCILE_INTERVAL_TICKS = 100;

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String CUSTODY_FILE_NAME = "cobblemon_initiative_daycare.json";

  // Server→client picker bridge (mirrors NuzlockeInit.pendingSacrifice): the deposit command
  // raises the flag; NuzlockeClientInit's tick poll opens DaycareSelectionScreen when no
  // other screen is up (which also sequences naturally after Easy NPC's deferred dialog close).
  private static boolean pendingPicker = false;

  private DaycareConfig config;
  private final Map<UUID, List<BoardedMon>> custody = new HashMap<>();
  private MinecraftServer server;
  private int tickCounter = 0;

  public DaycareManager() {
    this.config = DaycareConfig.load();
    this.config.save();
  }

  public static void triggerPicker() {
    pendingPicker = true;
  }

  public static boolean consumePendingPicker() {
    if (pendingPicker) {
      pendingPicker = false;
      return true;
    }
    return false;
  }

  public DaycareConfig getConfig() {
    return config;
  }

  public void reloadConfig() {
    this.config = DaycareConfig.load();
  }

  // ---------------------------------------------------------------------------
  // Persistence (verbatim PlayerProgressManager pattern + write-through)
  // ---------------------------------------------------------------------------

  private Path getSavePath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(CUSTODY_FILE_NAME);
  }

  public void load(MinecraftServer server) {
    this.server = server;
    custody.clear();
    Path savePath = getSavePath(server);
    if (!Files.exists(savePath)) return;

    try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
      Type type = new TypeToken<Map<String, List<BoardedMon>>>() {}.getType();
      Map<String, List<BoardedMon>> data = GSON.fromJson(reader, type);
      if (data == null) return;

      int mons = 0;
      for (Map.Entry<String, List<BoardedMon>> entry : data.entrySet()) {
        UUID owner = UUID.fromString(entry.getKey());
        List<BoardedMon> list = new ArrayList<>();
        for (BoardedMon mon : entry.getValue()) {
          if (mon == null || mon.monJson == null) continue;
          try {
            mon.pokemon = Pokemon.Companion.loadFromJSON(server.registryAccess(), mon.monJson);
          } catch (Exception e) {
            // NEVER drop custody on a bad deserialize (e.g. a removed datapack species):
            // keep the JSON so the mon is recoverable; drip/withdraw skip it until fixed.
            mon.pokemon = null;
            InitiativeInit.LOGGER.error(
              "Daycare: could not deserialize boarded Pokemon {} for {} — kept in custody JSON",
              mon.monUuid, owner, e);
          }
          list.add(mon);
          mons++;
        }
        if (!list.isEmpty()) custody.put(owner, list);
      }
      InitiativeInit.LOGGER.info("Daycare: loaded {} boarded Pokemon for {} players", mons, custody.size());
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Daycare: failed to load custody file", e);
    }
  }

  public void save() {
    if (server == null) return;
    Path savePath = getSavePath(server);
    try {
      Files.createDirectories(savePath.getParent());
      Map<String, List<BoardedMon>> data = new HashMap<>();
      for (Map.Entry<UUID, List<BoardedMon>> entry : custody.entrySet()) {
        data.put(entry.getKey().toString(), entry.getValue());
      }
      try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
        GSON.toJson(data, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Daycare: failed to save custody file", e);
    }
  }

  // ---------------------------------------------------------------------------
  // Deposit / withdraw / status
  // ---------------------------------------------------------------------------

  public int boardedCount(UUID playerUuid) {
    List<BoardedMon> list = custody.get(playerUuid);
    return list == null ? 0 : list.size();
  }

  /**
   * Boards the given party Pokémon (called over the singleplayer bridge from
   * DaycareSelectionScreen's confirm, on the server thread). Everything the client showed
   * is RE-VALIDATED here: slot capacity, mon still present in the party, not in battle,
   * and the never-board-your-last-mon guard.
   */
  public void deposit(UUID playerUuid, List<UUID> monUuids) {
    if (server == null || !config.isEnabled()) return;
    ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
    if (player == null || monUuids == null || monUuids.isEmpty()) return;

    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(Component.literal("§cThe keeper waits until your battle is over."));
      return;
    }

    List<BoardedMon> boarded = custody.computeIfAbsent(playerUuid, k -> new ArrayList<>());
    int capacity = MAX_SLOTS - boarded.size();
    if (capacity <= 0) {
      player.sendSystemMessage(Component.literal("§cYour daycare pens are already full."));
      return;
    }

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<Pokemon> toBoard = new ArrayList<>();
    int partySize = 0;
    for (Pokemon p : party) {
      if (p == null) continue;
      partySize++;
      if (toBoard.size() < capacity && monUuids.contains(p.getUuid())) {
        toBoard.add(p);
      }
    }
    if (toBoard.isEmpty()) {
      player.sendSystemMessage(Component.literal("§cThose Pokémon are no longer in your party."));
      return;
    }
    if (partySize - toBoard.size() < 1) {
      player.sendSystemMessage(Component.literal("§cYou cannot board your last Pokémon."));
      return;
    }

    ServerLevel level = player.serverLevel();
    for (Pokemon pokemon : toBoard) {
      // Take the mon out of the party FIRST (recalls its entity, nulls storeCoordinates —
      // ownerless/detached), then serialize the detached state as the custody record.
      party.remove(pokemon);

      BoardedMon mon = new BoardedMon();
      mon.monUuid = pokemon.getUuid().toString();
      mon.levelAtDeposit = pokemon.getLevel();
      mon.depositedGameTime = level.getGameTime();
      mon.penSlot = lowestFreeSlot(boarded);
      mon.monJson = pokemon.saveToJSON(server.registryAccess(), new JsonObject());
      mon.pokemon = pokemon;

      // Stand-in anchor: the configured pen, or (pen unset) where the player is standing —
      // the showrunner sets real pen coords in the daycare config later. Slots fan out on x
      // so two boarders never stack.
      double baseX = config.isPenSet() ? config.getPenX() + 0.5 : player.getX();
      double baseY = config.isPenSet() ? config.getPenY() : player.getY();
      double baseZ = config.isPenSet() ? config.getPenZ() + 0.5 : player.getZ();
      mon.standX = baseX + mon.penSlot * 2.0;
      mon.standY = baseY;
      mon.standZ = baseZ;

      boarded.add(mon);
      spawnStandIn(level, mon);

      player.sendSystemMessage(Component.literal(
        "§a" + displayName(pokemon) + " §7(Lv. " + pokemon.getLevel()
          + ") is now boarding at the daycare."));
    }
    save(); // write-through: a crash loses at most one drip interval

    player.sendSystemMessage(Component.literal(
      "§7Pickup rate: §e" + config.getFeeBase() + " CD §7+ §e"
        + config.getFeePerLevel() + " CD §7per level gained."));
  }

  /** Withdraws the boarded mon in {@code slotIndex} (0-based). Fee first, then return. */
  public void withdraw(ServerPlayer player, int slotIndex) {
    if (server == null) return;
    List<BoardedMon> boarded = custody.get(player.getUUID());
    if (boarded == null || slotIndex < 0 || slotIndex >= boarded.size()) {
      player.sendSystemMessage(Component.literal("§cNothing is boarding in that pen."));
      return;
    }

    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(Component.literal("§cThe keeper waits until your battle is over."));
      return;
    }

    BoardedMon mon = boarded.get(slotIndex);
    Pokemon pokemon = mon.pokemon;
    if (pokemon == null) {
      // Deserialization failed at load — custody is intact on disk, but we cannot hand the
      // mon back until the data issue is fixed. Never delete, never guess.
      player.sendSystemMessage(Component.literal(
        "§cThe keeper is having trouble with that one's paperwork. It stays safe in the pen."));
      return;
    }

    int levelsGained = Math.max(0, pokemon.getLevel() - mon.levelAtDeposit);
    int fee = config.getFeeBase() + config.getFeePerLevel() * levelsGained;

    // Dispatch the fee now, read #dc_ok NEXT TICK: pay-probe effects are not reliably
    // visible immediately after performPrefixedCommand returns (runtime-found on the
    // safari permit, 2026-07-12 — same rail). tick() resolves the pending withdrawal.
    dispatchPickupFee(player, fee);
    pendingWithdrawals.put(player.getUUID(), new PendingWithdrawal(slotIndex, fee));
  }

  /** Deferred half of withdraw(): #dc_ok is readable one tick after the fee dispatch. */
  private void resolvePendingWithdrawal(ServerPlayer player, int slotIndex, int fee) {
    List<BoardedMon> boarded = custody.get(player.getUUID());
    if (boarded == null || slotIndex < 0 || slotIndex >= boarded.size()) return;
    BoardedMon mon = boarded.get(slotIndex);
    Pokemon pokemon = mon.pokemon;
    if (pokemon == null) return;

    if (!readPickupProbe(player)) {
      player.sendSystemMessage(Component.literal(
        "§cPayment declined. The daycare does not extend credit — §e" + displayName(pokemon)
          + "§c boards a while longer. (§e" + fee + " CD§c required)"));
      return;
    }

    // Fee is paid — NOW hand the mon back. party.add has the built-in PC overflow fallback
    // and returns false only when party AND PC are both full; in that (near-impossible)
    // case the mon stays in custody and the fee is refunded so nothing is lost either way.
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    if (!party.add(pokemon)) {
      refundFee(player, fee);
      player.sendSystemMessage(Component.literal(
        "§cYour party and PC are both full — §e" + displayName(pokemon)
          + "§c stays boarded. (Fee refunded.)"));
      return;
    }

    boarded.remove(slotIndex);
    if (boarded.isEmpty()) custody.remove(player.getUUID());
    discardStandIn(mon);
    save(); // write-through

    int levelsGained = Math.max(0, pokemon.getLevel() - mon.levelAtDeposit);
    String gained = levelsGained > 0
      ? " §7— grew §e" + levelsGained + (levelsGained == 1 ? " level" : " levels") + "§7 while boarding"
      : "";
    player.sendSystemMessage(Component.literal(
      "§a" + displayName(pokemon) + " §7(Lv. " + pokemon.getLevel() + ") returned to you" + gained + "."));
    player.sendSystemMessage(Component.literal(
      "§7Boarding invoice settled at the posted rate: §e" + fee + " CD§7."));
  }

  /** /cobblemon-initiative daycare status — pens, levels gained, live pickup fee. */
  public void sendStatus(ServerPlayer player) {
    List<BoardedMon> boarded = custody.get(player.getUUID());
    if (boarded == null || boarded.isEmpty()) {
      player.sendSystemMessage(Component.literal("§7No Pokémon boarding. Pens free: §e" + MAX_SLOTS));
      return;
    }
    player.sendSystemMessage(Component.literal("§6=== Daycare ==="));
    for (int i = 0; i < boarded.size(); i++) {
      BoardedMon mon = boarded.get(i);
      if (mon.pokemon == null) {
        player.sendSystemMessage(Component.literal("§e" + (i + 1) + ". §7(records pending)"));
        continue;
      }
      int levelsGained = Math.max(0, mon.pokemon.getLevel() - mon.levelAtDeposit);
      int fee = config.getFeeBase() + config.getFeePerLevel() * levelsGained;
      player.sendSystemMessage(Component.literal(
        "§e" + (i + 1) + ". §f" + displayName(mon.pokemon)
          + " §7Lv. " + mon.levelAtDeposit + " → §fLv. " + mon.pokemon.getLevel()
          + " §7(+" + levelsGained + ") — pickup §e" + fee + " CD"));
    }
    if (boarded.size() < MAX_SLOTS) {
      player.sendSystemMessage(Component.literal("§7Pens free: §e" + (MAX_SLOTS - boarded.size())));
    }
  }

  private static int lowestFreeSlot(List<BoardedMon> boarded) {
    for (int slot = 0; slot < MAX_SLOTS; slot++) {
      final int s = slot;
      if (boarded.stream().noneMatch(m -> m.penSlot == s)) return slot;
    }
    return 0;
  }

  private static String displayName(Pokemon pokemon) {
    return pokemon.getSpecies().getName(); // repo convention (nickname-aware naming is a follow-up)
  }

  // ---------------------------------------------------------------------------
  // Pickup fee (pay-probe command rail — there is NO Java CobbleDollars API)
  // ---------------------------------------------------------------------------

  /**
   * Runs the daycare/pickup_fee macro AS the player (the probe's `pay @s` must see the
   * player's balance as the SOURCE). The #dc_ok result is read ONE TICK LATER by
   * {@link #readPickupProbe} — the function's effects are not reliably visible
   * immediately after performPrefixedCommand returns. The probe gates on `store
   * result` — CobbleDollars `pay` soft-fails, so `store success` would read 1 either
   * way. Missing objective/score reads as "declined", which fails in the SAFE
   * direction: the mon just boards longer.
   */
  private void dispatchPickupFee(ServerPlayer player, int fee) {
    MinecraftServer srv = player.getServer();
    if (srv == null) return;
    srv.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
      "function cobblemon_initiative:daycare/pickup_fee {fee:" + fee + "}");
  }

  private boolean readPickupProbe(ServerPlayer player) {
    MinecraftServer srv = player.getServer();
    if (srv == null) return false;
    Scoreboard sb = srv.getScoreboard();
    Objective obj = sb.getObjective("cd_calc");
    if (obj == null) return false;
    ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly("#dc_ok"), obj);
    return info != null && info.value() >= 1;
  }

  /** A fee-dispatched withdrawal awaiting its next-tick #dc_ok read. */
  private record PendingWithdrawal(int slotIndex, int fee) {}

  private final Map<UUID, PendingWithdrawal> pendingWithdrawals = new HashMap<>();

  /** Refund path for the fee-paid-but-return-impossible edge (party AND PC full). */
  private void refundFee(ServerPlayer player, int fee) {
    MinecraftServer srv = player.getServer();
    if (srv == null) return;
    srv.getCommands().performPrefixedCommand(
      srv.createCommandSourceStack().withSuppressedOutput(),
      "cobbledollars give " + player.getGameProfile().getName() + " " + fee);
  }

  // ---------------------------------------------------------------------------
  // Tick: XP drip + stand-in reconcile
  // ---------------------------------------------------------------------------

  public void tick(MinecraftServer server) {
    // Deferred withdrawal resolution runs BEFORE the enabled/empty short-circuits —
    // a dispatched fee must always resolve (see dispatchPickupFee).
    if (!pendingWithdrawals.isEmpty()) {
      Map<UUID, PendingWithdrawal> pending = new HashMap<>(pendingWithdrawals);
      pendingWithdrawals.clear();
      pending.forEach((id, pw) -> {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
          resolvePendingWithdrawal(player, pw.slotIndex(), pw.fee());
        }
      });
    }

    if (!config.isEnabled() || custody.isEmpty()) return;
    tickCounter++;
    if (tickCounter % RECONCILE_INTERVAL_TICKS == 0) reconcileStandIns(server);
    if (tickCounter % Math.max(1, config.getIntervalTicks()) == 0) dripXp(server);
  }

  /**
   * Slow XP drip. THE CLAMP TRAP: the global level-cap clamps in InitiativeInit
   * (EXPERIENCE_GAINED_EVENT_PRE + LEVEL_UP) key off getOwnerPlayer(), which is NULL for
   * a custody mon (party.remove nulled its storeCoordinates) — they silently no-op here.
   * So the daycare SELF-CLAMPS before every award, using the same semantics: cap the gain
   * at experience-to-cap, resolved through the UUID overload so offline owners still
   * clamp correctly.
   */
  private void dripXp(MinecraftServer server) {
    boolean dirty = false;
    for (Map.Entry<UUID, List<BoardedMon>> entry : custody.entrySet()) {
      UUID owner = entry.getKey();
      int cap = InitiativeInit.getLevelCapManager().getLevelCap(owner);
      for (BoardedMon mon : entry.getValue()) {
        Pokemon pokemon = mon.pokemon;
        if (pokemon == null) continue;
        int maxGain = Math.max(0, pokemon.getExperienceToLevel(cap));
        int award = Math.min(config.getXpPerInterval(), maxGain);
        if (award <= 0) continue;
        try {
          pokemon.addExperience(new SidemodExperienceSource(InitiativeInit.MOD_ID), award);
          // Custody JSON is the single source of truth — keep it in step with every award.
          mon.monJson = pokemon.saveToJSON(server.registryAccess(), new JsonObject());
          dirty = true;
        } catch (Exception e) {
          InitiativeInit.LOGGER.error("Daycare: XP drip failed for {}", mon.monUuid, e);
        }
      }
    }
    if (dirty) save();
  }

  /**
   * Custody JSON is truth; stand-ins are derived. Lazily (only where chunks are loaded):
   * discard tagged stand-ins that no longer match custody, respawn missing ones. A stand-in
   * that wanders into unloaded chunks gets replaced and the orphan is swept when its chunk
   * next loads — self-healing in both directions.
   */
  private void reconcileStandIns(MinecraftServer server) {
    ServerLevel level = server.overworld();

    Set<String> expected = new HashSet<>();
    for (List<BoardedMon> list : custody.values()) {
      for (BoardedMon mon : list) {
        if (mon.standInEntityUuid != null) expected.add(mon.standInEntityUuid);
      }
    }

    // Sweep strays (collect first — discarding while iterating the entity getter is unsafe).
    List<Entity> strays = new ArrayList<>();
    for (Entity e : level.getAllEntities()) {
      if (e.getTags().contains(STANDIN_TAG) && !expected.contains(e.getUUID().toString())) {
        strays.add(e);
      }
    }
    for (Entity stray : strays) stray.discard();

    // Respawn missing stand-ins where the pen chunk is actually ticking.
    boolean dirty = false;
    for (List<BoardedMon> list : custody.values()) {
      for (BoardedMon mon : list) {
        if (mon.pokemon == null) continue;
        BlockPos pos = BlockPos.containing(mon.standX, mon.standY, mon.standZ);
        if (!level.isPositionEntityTicking(pos)) continue;
        Entity existing = mon.standInEntityUuid != null
          ? level.getEntity(UUID.fromString(mon.standInEntityUuid))
          : null;
        if (existing == null || !existing.isAlive()) {
          spawnStandIn(level, mon);
          dirty = true;
        }
      }
    }
    if (dirty) save();
  }

  /**
   * Pen stand-in: a real PokemonEntity built from a display CLONE of the boarded mon
   * (clone(true) = fresh UUID + cleared tetheringId), hardened so it can never be fought,
   * caught, killed, or despawned. Spawn pattern per NobleEncounterManager (moveTo →
   * addFreshEntity).
   */
  private void spawnStandIn(ServerLevel level, BoardedMon mon) {
    if (mon.pokemon == null) return;
    try {
      Pokemon standIn = mon.pokemon.clone(true, level.registryAccess());
      UncatchableProperty.INSTANCE.uncatchable().apply(standIn);

      PokemonEntity entity = new PokemonEntity(level, standIn, CobblemonEntities.POKEMON);
      entity.getEntityData().set(PokemonEntity.getUNBATTLEABLE(), true); // persists as NBT "Unbattleable"
      entity.setPersistenceRequired();
      entity.setCountsTowardsSpawnCap(false);
      entity.setInvulnerable(true);
      entity.addTag(STANDIN_TAG);
      entity.moveTo(mon.standX, mon.standY, mon.standZ, level.getRandom().nextFloat() * 360f, 0f);
      level.addFreshEntity(entity);

      mon.standInEntityUuid = entity.getUUID().toString();
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Daycare: failed to spawn stand-in for {}", mon.monUuid, e);
    }
  }

  private void discardStandIn(BoardedMon mon) {
    if (server == null || mon.standInEntityUuid == null) return;
    Entity entity = server.overworld().getEntity(UUID.fromString(mon.standInEntityUuid));
    if (entity != null) entity.discard();
    mon.standInEntityUuid = null;
    // If the entity's chunk was unloaded, the reconcile sweep removes it on next load.
  }

  // ---------------------------------------------------------------------------
  // Custody record
  // ---------------------------------------------------------------------------

  /** One boarded Pokémon. Gson-persisted as-is; {@link #pokemon} is the live deserialized copy. */
  public static class BoardedMon {
    JsonObject monJson;
    String monUuid;
    int levelAtDeposit;
    long depositedGameTime;
    String standInEntityUuid;
    int penSlot;
    double standX;
    double standY;
    double standZ;

    transient Pokemon pokemon;
  }
}

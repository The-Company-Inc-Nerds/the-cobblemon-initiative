package com.thecompanyinc.cobblemoninitiative.momcare;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * Mom's friendship care (docs/PHONE_AND_CARE.md §3). Leave ONE Pokémon with Mom; each in-game day
 * it boards, its Cobblemon friendship rises toward the cap — enabling friendship evolutions,
 * powering Return, and just feeling like a bonded team. A non-Company, purely-loving service
 * (pairs with the daycare's "not Company" line).
 *
 * Modeled on {@link com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager}'s hardcore-safe
 * boarding, trimmed to one slot and friendship-only (no XP, no pen stand-in, free by default):
 * the boarded Pokémon lives ONLY in custody JSON + this manager's in-memory copy, never in any
 * store/battle/entity, so it can NEVER faint, die, or be lost. Custody survives relog/crash via
 * write-through saves. Withdrawal is free unless a fee is configured (then charged through the
 * daycare pay-probe rail).
 */
public class MomCareManager {

  public static final int MAX_SLOTS = 1;

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String CUSTODY_FILE_NAME = "cobblemon_initiative_momcare.json";
  private static final int TICK_INTERVAL = 40; // day-latch poll cadence (2s)

  // Server→client picker bridge (mirrors DaycareManager): the deposit command raises this flag;
  // NuzlockeClientInit's tick poll opens the picker (a 1-slot DaycareSelectionScreen) when idle.
  private static boolean pendingPicker = false;

  private MomCareConfig config;
  private final Map<UUID, BoardedMon> custody = new HashMap<>();
  private final Map<UUID, Integer> pendingWithdrawFees = new HashMap<>();
  private MinecraftServer server;
  private int tickCounter = 0;
  private long lastDrippedDay = Long.MIN_VALUE;

  public MomCareManager() {
    this.config = MomCareConfig.load();
    this.config.save();
  }

  public static void triggerPicker() { pendingPicker = true; }

  public static boolean consumePendingPicker() {
    if (pendingPicker) { pendingPicker = false; return true; }
    return false;
  }

  public MomCareConfig getConfig() { return config; }

  public void reloadConfig() { this.config = MomCareConfig.load(); }

  public int boardedCount(UUID playerUuid) {
    return custody.containsKey(playerUuid) ? 1 : 0;
  }

  // ── Persistence (DaycareManager pattern) ────────────────────────────────────────

  private Path getSavePath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(CUSTODY_FILE_NAME);
  }

  public void load(MinecraftServer server) {
    this.server = server;
    custody.clear();
    lastDrippedDay = server.overworld().getDayTime() / 24000L; // never drip on boot
    Path savePath = getSavePath(server);
    if (!Files.exists(savePath)) return;
    try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
      Type type = new TypeToken<Map<String, BoardedMon>>() {}.getType();
      Map<String, BoardedMon> data = GSON.fromJson(reader, type);
      if (data == null) return;
      for (Map.Entry<String, BoardedMon> entry : data.entrySet()) {
        BoardedMon mon = entry.getValue();
        if (mon == null || mon.monJson == null) continue;
        try {
          mon.pokemon = Pokemon.Companion.loadFromJSON(server.registryAccess(), mon.monJson);
        } catch (Exception e) {
          mon.pokemon = null; // never drop custody on a bad deserialize — keep it recoverable
          InitiativeInit.LOGGER.error("MomCare: could not deserialize boarded Pokemon for {}", entry.getKey(), e);
        }
        custody.put(UUID.fromString(entry.getKey()), mon);
      }
      InitiativeInit.LOGGER.info("MomCare: loaded {} boarded Pokemon", custody.size());
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("MomCare: failed to load custody file", e);
    }
  }

  public void save() {
    if (server == null) return;
    Path savePath = getSavePath(server);
    try {
      Files.createDirectories(savePath.getParent());
      Map<String, BoardedMon> data = new HashMap<>();
      for (Map.Entry<UUID, BoardedMon> e : custody.entrySet()) data.put(e.getKey().toString(), e.getValue());
      try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
        GSON.toJson(data, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("MomCare: failed to save custody file", e);
    }
  }

  // ── Deposit / withdraw / status ─────────────────────────────────────────────────

  /** Boards ONE chosen party Pokémon with Mom (re-validated server-side; picker bridge). */
  public void deposit(UUID playerUuid, List<UUID> monUuids) {
    if (server == null || !config.isEnabled()) return;
    ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
    if (player == null || monUuids == null || monUuids.isEmpty()) return;

    if (custody.containsKey(playerUuid)) {
      player.sendSystemMessage(Component.literal("§cMom already has one of your Pokémon. Bring it home first."));
      return;
    }
    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(Component.literal("§cFinish your battle first."));
      return;
    }

    UUID chosen = monUuids.get(0);
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon toBoard = null;
    int partySize = 0;
    for (Pokemon p : party) {
      if (p == null) continue;
      partySize++;
      if (toBoard == null && p.getUuid().equals(chosen)) toBoard = p;
    }
    if (toBoard == null) {
      player.sendSystemMessage(Component.literal("§cThat Pokémon is no longer in your party."));
      return;
    }
    if (partySize <= 1) {
      player.sendSystemMessage(Component.literal("§cYou cannot leave your last Pokémon."));
      return;
    }

    party.remove(toBoard);
    BoardedMon mon = new BoardedMon();
    mon.monUuid = toBoard.getUuid().toString();
    mon.friendshipAtDeposit = toBoard.getFriendship();
    mon.depositedGameTime = player.serverLevel().getGameTime();
    mon.monJson = toBoard.saveToJSON(server.registryAccess(), new JsonObject());
    mon.pokemon = toBoard;
    custody.put(playerUuid, mon);
    save();

    player.sendSystemMessage(Component.literal(
      "§d" + displayName(toBoard) + " §7is staying with Mom now (friendship §e" + toBoard.getFriendship()
        + "§7). Come home when you can — a mother is good for a Pokémon's heart."));
  }

  /** Withdraw the boarded mon — free unless a fee is configured. */
  public void withdraw(ServerPlayer player) {
    if (server == null) return;
    BoardedMon mon = custody.get(player.getUUID());
    if (mon == null) {
      player.sendSystemMessage(Component.literal("§7Mom isn't watching any of your Pokémon."));
      return;
    }
    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(Component.literal("§cFinish your battle first."));
      return;
    }
    if (mon.pokemon == null) {
      player.sendSystemMessage(Component.literal("§cMom is having trouble with that one's paperwork. It stays safe with her."));
      return;
    }

    int fee = config.getFee();
    if (fee <= 0) {
      returnMon(player, mon);
      return;
    }
    // Fee configured — charge through the daycare pay-probe rail, read #mc_ok next tick.
    MinecraftServer srv = player.getServer();
    if (srv != null) {
      srv.getCommands().performPrefixedCommand(
        player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
        "function cobblemon_initiative:momcare/care_fee {fee:" + fee + "}");
      pendingWithdrawFees.put(player.getUUID(), fee);
    }
  }

  private void resolvePendingWithdraw(ServerPlayer player, int fee) {
    BoardedMon mon = custody.get(player.getUUID());
    if (mon == null || mon.pokemon == null) return;
    Scoreboard sb = player.getServer().getScoreboard();
    Objective obj = sb.getObjective("cd_calc");
    boolean paid = false;
    if (obj != null) {
      ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly("#mc_ok"), obj);
      paid = info != null && info.value() >= 1;
    }
    if (!paid) {
      player.sendSystemMessage(Component.literal("§cYou can't cover the care fee (§e" + fee + " CD§c) — Mom keeps it a while longer."));
      return;
    }
    returnMon(player, mon);
  }

  private void returnMon(ServerPlayer player, BoardedMon mon) {
    Pokemon pokemon = mon.pokemon;
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    if (!party.add(pokemon)) {
      player.sendSystemMessage(Component.literal("§cYour party and PC are both full — " + displayName(pokemon) + " stays with Mom."));
      return;
    }
    custody.remove(player.getUUID());
    save();
    int gained = Math.max(0, pokemon.getFriendship() - mon.friendshipAtDeposit);
    String grew = gained > 0 ? " §7— its heart grew closer (friendship §e+" + gained + "§7)" : "";
    player.sendSystemMessage(Component.literal(
      "§d" + displayName(pokemon) + " §7is back with you (friendship §e" + pokemon.getFriendship() + "§7)" + grew + "."));
  }

  public void sendStatus(ServerPlayer player) {
    BoardedMon mon = custody.get(player.getUUID());
    if (mon == null) {
      player.sendSystemMessage(Component.literal("§7Mom's care is open. Leave one Pokémon with her to grow its friendship."));
      return;
    }
    if (mon.pokemon == null) {
      player.sendSystemMessage(Component.literal("§7Mom is watching one of yours (records pending)."));
      return;
    }
    player.sendSystemMessage(Component.literal(
      "§d§lMom's Care§r §7— §f" + displayName(mon.pokemon) + " §7friendship §e" + mon.pokemon.getFriendship()
        + "§7/" + config.getCap() + " §7(+" + config.gainPerDay() + "/day)"));
  }

  private static String displayName(Pokemon pokemon) {
    return pokemon.getSpecies().getName();
  }

  // ── Tick: deferred fee resolution + daily friendship drip ───────────────────────

  public void tick(MinecraftServer server) {
    if (!pendingWithdrawFees.isEmpty()) {
      Map<UUID, Integer> pending = new HashMap<>(pendingWithdrawFees);
      pendingWithdrawFees.clear();
      pending.forEach((id, fee) -> {
        ServerPlayer p = server.getPlayerList().getPlayer(id);
        if (p != null) resolvePendingWithdraw(p, fee);
      });
    }
    if (!config.isEnabled() || custody.isEmpty()) return;
    if (++tickCounter < TICK_INTERVAL) return;
    tickCounter = 0;

    long day = server.overworld().getDayTime() / 24000L;
    if (day == lastDrippedDay) return;
    if (day < lastDrippedDay) { lastDrippedDay = day; return; }
    long elapsed = day - lastDrippedDay;
    lastDrippedDay = day;
    dripFriendship(server, (int) Math.min(elapsed, 30)); // bound a long time-skip
  }

  /** Raise each boarded mon's friendship by gainPerDay × days, clamped to the config cap. */
  private void dripFriendship(MinecraftServer server, int days) {
    if (days <= 0) return;
    int perDay = config.gainPerDay();
    if (perDay <= 0) return;
    int cap = Math.max(0, config.getCap());
    boolean dirty = false;
    for (BoardedMon mon : custody.values()) {
      Pokemon pokemon = mon.pokemon;
      if (pokemon == null) continue;
      int current = pokemon.getFriendship();
      if (current >= cap) continue;
      int target = Math.min(cap, current + perDay * days);
      int delta = target - current;
      if (delta <= 0) continue;
      try {
        pokemon.incrementFriendship(delta, true);
        mon.monJson = pokemon.saveToJSON(server.registryAccess(), new JsonObject());
        dirty = true;
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("MomCare: friendship drip failed for {}", mon.monUuid, e);
      }
    }
    if (dirty) save();
  }

  // ── Custody record ──────────────────────────────────────────────────────────────

  /** One boarded Pokémon. Gson-persisted; {@link #pokemon} is the live deserialized copy. */
  public static class BoardedMon {
    JsonObject monJson;
    String monUuid;
    int friendshipAtDeposit;
    long depositedGameTime;

    transient Pokemon pokemon;
  }
}

package com.thecompanyinc.cobblemoninitiative.homestead;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.MinecraftFlavorConfig;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Homestead beacons (docs/MINECRAFT_FLAVOR.md §1). A liberated field pays the player a daily
 * CobbleDollars trickle scaled by the beacon they raise on it. Fork (c): the pyramid is read
 * DIRECTLY (size = highest complete layer 1–4; material = the block mix under the beacon), so
 * income works before any vanilla beacon activation — a nether star (right-click the beacon with
 * one) unlocks the top tier. Mining the pyramid up IS the economy engine.
 *
 * Registration is explicit (the player runs `homestead claim` near a beacon they placed / were
 * granted — usually from the returning-farmer or Mayor Suzune dialog button). Payout runs on a
 * Java in-game-day latch (aligns with economy/dawn) and is delivered via `cobbledollars give`
 * (there is no Java CobbleDollars API — the give rail is server-sourced). Beacon RESALE (Suzune's
 * escalating price) is charged through the datapack pay-probe rail, mirroring the daycare fee.
 */
public class HomesteadManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String SAVE_FILE_NAME = "cobblemon_initiative_homestead.json";
  private static final int TICK_INTERVAL = 40; // day-latch poll cadence (2s) — cheap

  /** The player next-beacon price, mirrored to ci_flavor so the phone can probe affordability. */
  private static final String PRICE_HOLDER = "#beacon_price";

  private HomesteadConfig config;
  private final Map<UUID, PlayerData> data = new HashMap<>();
  private MinecraftServer server;

  private int tickCounter = 0;
  private long lastPaidDay = Long.MIN_VALUE;

  // Deferred beacon-purchase reads (pay-probe effects settle one tick after dispatch —
  // the daycare fee rail found this the hard way; reuse the deferred pattern).
  private final Map<UUID, Integer> pendingBuys = new HashMap<>();

  public HomesteadManager() {
    this.config = HomesteadConfig.load();
    this.config.save();
  }

  public HomesteadConfig getConfig() { return config; }

  public void reloadConfig() {
    this.config = HomesteadConfig.load();
    if (server != null) refreshAllPrices();
  }

  // ── Persistence (PlayerProgressManager / DaycareManager pattern) ────────────────

  private Path getSavePath(MinecraftServer server) {
    return server.getWorldPath(LevelResource.ROOT).resolve(SAVE_FILE_NAME);
  }

  public void load(MinecraftServer server) {
    this.server = server;
    data.clear();
    // Never pay on boot: seed the latch to the current day so only the NEXT dawn pays.
    lastPaidDay = server.overworld().getDayTime() / 24000L;
    Path savePath = getSavePath(server);
    if (Files.exists(savePath)) {
      try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
        Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
        Map<String, PlayerData> loaded = GSON.fromJson(reader, type);
        if (loaded != null) {
          for (Map.Entry<String, PlayerData> e : loaded.entrySet()) {
            if (e.getValue() == null) continue;
            if (e.getValue().beacons == null) e.getValue().beacons = new ArrayList<>();
            data.put(UUID.fromString(e.getKey()), e.getValue());
          }
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("Homestead: failed to load save file", e);
      }
    }
    refreshAllPrices();
    InitiativeInit.LOGGER.info("Homestead: loaded {} player homestead record(s)", data.size());
  }

  public void save() {
    if (server == null) return;
    Path savePath = getSavePath(server);
    try {
      Files.createDirectories(savePath.getParent());
      Map<String, PlayerData> out = new HashMap<>();
      for (Map.Entry<UUID, PlayerData> e : data.entrySet()) out.put(e.getKey().toString(), e.getValue());
      try (Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)) {
        GSON.toJson(out, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Homestead: failed to save file", e);
    }
  }

  private PlayerData dataFor(UUID id) {
    return data.computeIfAbsent(id, k -> new PlayerData());
  }

  // ── Registration (player-facing `homestead claim/unclaim/list/status`) ───────────

  /** Register the nearest beacon within claimRadius of the player as a homestead. */
  public void claimNearestBeacon(ServerPlayer player) {
    if (!config.isEnabled()) {
      player.sendSystemMessage(Component.literal("§cThe homestead system is disabled."));
      return;
    }
    ServerLevel level = player.serverLevel();
    BlockPos found = findNearestBeacon(level, player.blockPosition(), config.getClaimRadius());
    if (found == null) {
      player.sendSystemMessage(Component.literal(
        "§cNo beacon within " + config.getClaimRadius() + " blocks. Place your homestead beacon, then claim it."));
      return;
    }
    PlayerData pd = dataFor(player.getUUID());
    String dim = level.dimension().location().toString();
    for (Homestead h : pd.beacons) {
      if (h.x == found.getX() && h.y == found.getY() && h.z == found.getZ() && dim.equals(h.dim)) {
        player.sendSystemMessage(Component.literal("§7That beacon is already your homestead."));
        return;
      }
    }
    Homestead h = new Homestead();
    h.x = found.getX(); h.y = found.getY(); h.z = found.getZ(); h.dim = dim; h.starCharged = false;
    pd.beacons.add(h);
    save();
    player.sendSystemMessage(Component.literal(
      "§a§lHomestead claimed. §r§7The field at §f" + found.getX() + " " + found.getY() + " " + found.getZ()
        + "§7 pays its harvest to you now. Mine the pyramid higher to grow the yield."));
    int tier = pyramidTier(level, found);
    player.sendSystemMessage(Component.literal(
      "§7Beacon tier: §e" + tier + "§7/4" + (tier >= 4 && config.isStarForTopTier() && !h.starCharged
        ? " §8(feed it a nether star to unlock the top tier)" : "")));
  }

  /** Drop the registered homestead nearest the player. */
  public void unclaimNearestBeacon(ServerPlayer player) {
    PlayerData pd = data.get(player.getUUID());
    if (pd == null || pd.beacons.isEmpty()) {
      player.sendSystemMessage(Component.literal("§7You have no homesteads."));
      return;
    }
    BlockPos p = player.blockPosition();
    Homestead nearest = null;
    double best = Double.MAX_VALUE;
    for (Homestead h : pd.beacons) {
      double d = p.distSqr(new BlockPos(h.x, h.y, h.z));
      if (d < best) { best = d; nearest = h; }
    }
    if (nearest != null) {
      pd.beacons.remove(nearest);
      save();
      player.sendSystemMessage(Component.literal("§7Homestead at §f" + nearest.x + " " + nearest.y + " "
        + nearest.z + "§7 released. It no longer pays you."));
    }
  }

  public void sendStatus(ServerPlayer player) {
    PlayerData pd = data.get(player.getUUID());
    if (pd == null || pd.beacons.isEmpty()) {
      player.sendSystemMessage(Component.literal("§7No homesteads claimed. Free a field, raise a beacon, then §e/cobblemon-initiative homestead claim§7."));
      return;
    }
    ServerLevel level = player.serverLevel();
    player.sendSystemMessage(Component.literal("§6=== Homesteads ==="));
    int total = 0;
    for (Homestead h : pd.beacons) {
      BlockPos pos = new BlockPos(h.x, h.y, h.z);
      int yield = yieldFor(level, h);
      total += yield;
      int tier = pyramidTier(level, pos);
      player.sendSystemMessage(Component.literal(
        "§e• §f" + h.x + " " + h.y + " " + h.z + " §7— tier §e" + tier + "§7/4"
          + (h.starCharged ? " §b★" : "") + " → §a" + yield + " CD/day"));
    }
    total = Math.min(total, config.getTotalCap());
    player.sendSystemMessage(Component.literal("§7Daily harvest (capped): §a" + total + " CD§7. Next beacon from Suzune: §e"
      + config.priceForNext(pd.purchased) + " CD"));
  }

  // ── Nether-star charge (UseBlockCallback) ───────────────────────────────────────

  /**
   * Right-click a registered homestead beacon holding a nether star → consume the star and
   * unlock the top tier (fork (c)). Returns PASS for anything else so the vanilla beacon GUI
   * opens normally. Registered here in InitiativeInit alongside the loot-chest handler.
   */
  public InteractionResult onUseBlock(net.minecraft.world.entity.player.Player player, Level level,
                                      InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
    if (!config.isEnabled() || !config.isStarForTopTier()) return InteractionResult.PASS;
    ItemStack held = player.getItemInHand(hand);
    if (!held.is(Items.NETHER_STAR)) return InteractionResult.PASS;

    BlockPos pos = hit.getBlockPos();
    if (!level.getBlockState(pos).is(Blocks.BEACON)) return InteractionResult.PASS;

    PlayerData pd = data.get(sp.getUUID());
    if (pd == null) return InteractionResult.PASS;
    String dim = level.dimension().location().toString();
    Homestead target = null;
    for (Homestead h : pd.beacons) {
      if (h.x == pos.getX() && h.y == pos.getY() && h.z == pos.getZ() && dim.equals(h.dim)) { target = h; break; }
    }
    if (target == null) return InteractionResult.PASS; // a beacon, but not a claimed homestead
    if (target.starCharged) {
      sp.sendSystemMessage(Component.literal("§7This homestead is already charged. Its top tier is unlocked."));
      return InteractionResult.SUCCESS;
    }

    if (!sp.getAbilities().instabuild) held.shrink(1);
    target.starCharged = true;
    save();
    sp.sendSystemMessage(Component.literal(
      "§b§lThe star sinks into the beacon. §r§7The Company's currency, backing your ground now. Top tier unlocked."));
    if (level instanceof ServerLevel sl) {
      sl.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT,
        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
    }
    return InteractionResult.SUCCESS;
  }

  // ── Beacon purchase (Suzune's resale — deferred pay-probe, daycare pattern) ──────

  /** Dispatch the escalating beacon price; the #hs_ok read + grant resolves next tick. */
  public void buyBeacon(ServerPlayer player) {
    if (!config.isEnabled()) {
      player.sendSystemMessage(Component.literal("§cThe homestead system is disabled."));
      return;
    }
    PlayerData pd = dataFor(player.getUUID());
    int price = config.priceForNext(pd.purchased);
    MinecraftServer srv = player.getServer();
    if (srv == null) return;
    srv.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withSuppressedOutput().withPermission(2),
      "function cobblemon_initiative:homestead/buy_probe {price:" + price + "}");
    pendingBuys.put(player.getUUID(), price);
  }

  private void resolvePendingBuy(ServerPlayer player, int price) {
    Scoreboard sb = player.getServer().getScoreboard();
    Objective obj = sb.getObjective("cd_calc");
    boolean paid = false;
    if (obj != null) {
      ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly("#hs_ok"), obj);
      paid = info != null && info.value() >= 1;
    }
    if (!paid) {
      player.sendSystemMessage(Component.literal(
        "§cSuzune shakes her head. §7A beacon runs §e" + price + " CD§7 — come back when you have it."));
      return;
    }
    PlayerData pd = dataFor(player.getUUID());
    pd.purchased++;
    save();
    refreshPrice(player.getUUID());
    // Deliver the beacon.
    player.getServer().getCommands().performPrefixedCommand(
      player.getServer().createCommandSourceStack().withSuppressedOutput(),
      "give " + player.getGameProfile().getName() + " minecraft:beacon 1");
    player.sendSystemMessage(Component.literal(
      "§a§lAnother beacon, off the coast. §r§7Raise it on a field you have freed, mine the pyramid, and claim it. Next one: §e"
        + config.priceForNext(pd.purchased) + " CD"));
  }

  // ── Daily payout (in-game-day latch) ────────────────────────────────────────────

  public void tick(MinecraftServer server) {
    if (!pendingBuys.isEmpty()) {
      Map<UUID, Integer> pending = new HashMap<>(pendingBuys);
      pendingBuys.clear();
      pending.forEach((id, price) -> {
        ServerPlayer p = server.getPlayerList().getPlayer(id);
        if (p != null) resolvePendingBuy(p, price);
      });
    }
    if (!config.isEnabled()) return;
    if (++tickCounter < TICK_INTERVAL) return;
    tickCounter = 0;

    long day = server.overworld().getDayTime() / 24000L;
    if (day == lastPaidDay) return;
    if (day < lastPaidDay) { lastPaidDay = day; return; } // /time set backwards — resync, no pay
    lastPaidDay = day;
    payoutAll(server);
  }

  private void payoutAll(MinecraftServer server) {
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      PlayerData pd = data.get(player.getUUID());
      if (pd == null || pd.beacons.isEmpty()) continue;
      ServerLevel level = player.serverLevel();
      int total = 0;
      List<Homestead> gone = new ArrayList<>();
      for (Homestead h : pd.beacons) {
        BlockPos pos = new BlockPos(h.x, h.y, h.z);
        if (!level.dimension().location().toString().equals(h.dim)) continue; // wrong dimension loaded
        if (!level.hasChunkAt(pos)) continue; // unloaded — can't verify, skip (no pay, no prune)
        if (!level.getBlockState(pos).is(Blocks.BEACON)) { gone.add(h); continue; }
        total += yieldFor(level, h);
      }
      if (!gone.isEmpty()) {
        pd.beacons.removeAll(gone);
        save();
        player.sendSystemMessage(Component.literal("§7A homestead beacon is gone; that field pays no more."));
      }
      total = Math.min(total, config.getTotalCap());
      if (total <= 0) continue;
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withSuppressedOutput(),
        "cobbledollars give " + player.getGameProfile().getName() + " " + total);
      player.sendSystemMessage(Component.literal(
        "§6§l[Harvest] §r§aYour homesteads paid out §e" + total + " CD§a today."));
    }
  }

  // ── Pyramid read (fork (c): size + material, no vanilla activation) ──────────────

  /** Full daily yield for one homestead: base(effective tier) × material mix × income mult, field-capped. */
  private int yieldFor(ServerLevel level, Homestead h) {
    BlockPos beacon = new BlockPos(h.x, h.y, h.z);
    Scan scan = scanPyramid(level, beacon);
    int tier = scan.tier;
    if (tier <= 0) return 0;
    // Star gate: a full 4-layer pyramid is capped at tier-3 output until charged.
    if (tier >= 4 && config.isStarForTopTier() && !h.starCharged) tier = 3;
    double base = config.baseForTier(tier);
    double raw = base * scan.avgMaterialMult() * config.getIncomeMultiplier();
    return Math.min((int) Math.round(raw), config.getFieldCap());
  }

  /** Highest complete pyramid layer under the beacon (0–4). */
  private int pyramidTier(ServerLevel level, BlockPos beacon) {
    return scanPyramid(level, beacon).tier;
  }

  /**
   * Scan the up-to-4 pyramid layers directly below the beacon. Layer L (1..4) is the
   * (2L+1)² square at y = beaconY − L. tier = the highest L for which every block in
   * layers 1..L is a valid beacon-base block; the material tally covers only those
   * complete layers. Bounded at ≤164 block reads.
   */
  private Scan scanPyramid(ServerLevel level, BlockPos beacon) {
    Scan scan = new Scan();
    for (int layer = 1; layer <= 4; layer++) {
      int y = beacon.getY() - layer;
      boolean complete = true;
      int liron = 0, lgold = 0, ldiamond = 0, lemerald = 0, lnetherite = 0;
      for (int dx = -layer; dx <= layer && complete; dx++) {
        for (int dz = -layer; dz <= layer; dz++) {
          BlockPos p = new BlockPos(beacon.getX() + dx, y, beacon.getZ() + dz);
          Block b = level.getBlockState(p).getBlock();
          if (b == Blocks.IRON_BLOCK) liron++;
          else if (b == Blocks.GOLD_BLOCK) lgold++;
          else if (b == Blocks.DIAMOND_BLOCK) ldiamond++;
          else if (b == Blocks.EMERALD_BLOCK) lemerald++;
          else if (b == Blocks.NETHERITE_BLOCK) lnetherite++;
          else { complete = false; break; }
        }
      }
      if (!complete) break;
      scan.tier = layer;
      scan.iron += liron; scan.gold += lgold; scan.diamond += ldiamond;
      scan.emerald += lemerald; scan.netherite += lnetherite;
    }
    return scan;
  }

  private BlockPos findNearestBeacon(Level level, BlockPos center, int radius) {
    BlockPos best = null;
    double bestDist = Double.MAX_VALUE;
    BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -radius; dy <= radius; dy++) {
        for (int dz = -radius; dz <= radius; dz++) {
          m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
          if (level.getBlockState(m).is(Blocks.BEACON)) {
            double d = center.distSqr(m);
            if (d < bestDist) { bestDist = d; best = m.immutable(); }
          }
        }
      }
    }
    return best;
  }

  // ── Price mirror (for the PokéPhone affordability probe) ────────────────────────

  private void refreshAllPrices() {
    for (UUID id : data.keySet()) refreshPrice(id);
    // Also seed a default global price so the phone probe has a value before any purchase.
    if (server != null) {
      Objective obj = flavorObjective();
      if (obj != null) {
        server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly(PRICE_HOLDER), obj)
          .set(config.priceForNext(0));
      }
    }
  }

  /**
   * Publish the player's next-beacon price to ci_flavor #beacon_price (single-player: the one
   * player's price is the global one the phone probes).
   */
  private void refreshPrice(UUID id) {
    if (server == null) return;
    Objective obj = flavorObjective();
    if (obj == null) return;
    int purchased = data.containsKey(id) ? data.get(id).purchased : 0;
    server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly(PRICE_HOLDER), obj)
      .set(config.priceForNext(purchased));
  }

  private Objective flavorObjective() {
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(MinecraftFlavorConfig.OBJECTIVE);
    if (obj == null) {
      obj = sb.addObjective(MinecraftFlavorConfig.OBJECTIVE, ObjectiveCriteria.DUMMY,
        Component.literal("Minecraft Flavor"), ObjectiveCriteria.RenderType.INTEGER, true, null);
    }
    return obj;
  }

  // ── Records ─────────────────────────────────────────────────────────────────

  /** Per-player homestead state (Gson-persisted). */
  public static class PlayerData {
    List<Homestead> beacons = new ArrayList<>();
    int purchased = 0; // beacons bought from Suzune (drives the escalating price)
  }

  /** One claimed homestead beacon. */
  public static class Homestead {
    int x, y, z;
    String dim;
    boolean starCharged;
  }

  /** Result of a pyramid scan: tier + per-material block counts across the complete layers. */
  private static class Scan {
    int tier = 0;
    int iron, gold, diamond, emerald, netherite;

    /** Block-mix-weighted quality multiplier (1.0 if empty). */
    double avgMaterialMult() {
      int n = iron + gold + diamond + emerald + netherite;
      if (n == 0) return 1.0;
      HomesteadConfig c = InitiativeInit.getHomesteadManager().getConfig();
      double sum = iron * c.getMaterialMultIron()
        + gold * c.getMaterialMultGold()
        + diamond * c.getMaterialMultDiamond()
        + emerald * c.getMaterialMultEmerald()
        + netherite * c.getMaterialMultNetherite();
      return sum / n;
    }
  }
}

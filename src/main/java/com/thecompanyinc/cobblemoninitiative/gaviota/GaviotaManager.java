package com.thecompanyinc.cobblemoninitiative.gaviota;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Runs the two Gaviota Port set-pieces (see {@link GaviotaConfig}): the drainable water gym (a
 * randomised talk-in-order valve puzzle that lowers the water in smooth animated stages, gating any
 * submerged NPC until the water drops past it) and the donation aquarium (donate species for tank
 * displays + three tiers of rewards). Everything is coord-driven from the config, so the geometry is
 * filled in later. World drain state lives on the {@code ci_gaviota} scoreboard (single-player, so
 * per-world); per-player donation state rides player tags + the {@code ci_gaviota_donations} score.
 */
public final class GaviotaManager {

  private GaviotaManager() {}

  private static final String OBJ = "ci_gaviota";
  private static final String H_PROGRESS = "#gav_progress";
  private static final String H_SURFACE = "#gav_surface";
  private static final String H_FLOODED = "#gav_flooded";
  private static final String H_TARGET = "#gav_anim_target";
  private static final String H_RAISE = "#gav_anim_raise";
  private static final String H_PRIMED = "#gav_primed";
  private static final String DON_OBJ = "ci_gaviota_donations";

  /** One colour per pump pair, so linked pumps are readable (dust particles above each pump). */
  private static final float[][] PAIR_COLORS = {
    {1.0f, 0.2f, 0.2f}, {1.0f, 0.6f, 0.1f}, {1.0f, 1.0f, 0.2f},
    {0.3f, 1.0f, 0.3f}, {0.3f, 0.5f, 1.0f}, {0.75f, 0.3f, 1.0f},
    {0.1f, 0.9f, 0.9f}, {1.0f, 0.4f, 0.8f}
  };

  private static long nextAnimTick = 0;
  /** One auto-flood attempt per server session (guards the fresh-world seed below from retry-spam if
   *  flood ever no-ops; the persistent #gav_flooded score is the real once-per-world latch). */
  private static boolean autoFloodChecked = false;

  public static void init() {
    ServerTickEvents.END_SERVER_TICK.register(GaviotaManager::tick);
    UseEntityCallback.EVENT.register(GaviotaManager::onInteractEntity);
  }

  // ── flood / drain / raise ───────────────────────────────────────────────────────

  /** {@code /cobblemon-initiative gaviota flood} — fill the arena, spawn the pump bots, and randomise
   *  the pump pairs. Op-run set-up once the coords are latched. */
  public static int flood(MinecraftServer server) {
    GaviotaConfig cfg = GaviotaConfig.get();
    if (!cfg.enabled) return 0;
    if (cfg.drain.region.isPlaceholder()) {
      InitiativeInit.LOGGER.warn("[Gaviota] flood: region is a placeholder — fill the coords in the config first.");
      return 0;
    }
    ServerLevel level = levelFor(server, cfg);
    if (level == null) return 0;

    GaviotaConfig.Box b = cfg.drain.region;
    forceload(server, level, b, true);
    fillLayerRange(server, level, b, b.minY, b.maxY, "water");
    set(server, H_FLOODED, 1);
    set(server, H_PROGRESS, 0);
    set(server, H_SURFACE, cfg.drain.waterTopY);
    set(server, H_PRIMED, -1);
    clear(server, H_TARGET);
    // Fresh flood = water at the top = the Jr. Apprentice / Apprentice / Leader are re-locked.
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
      "tag @a remove gaviota_drained");

    // Randomise the pairs: build 2 of each pair id, shuffle onto the pumps.
    int n = cfg.drain.pumps.size();
    int pairs = n / 2;
    List<Integer> ids = new ArrayList<>();
    for (int p = 0; p < pairs; p++) { ids.add(p); ids.add(p); }
    while (ids.size() < n) ids.add(0);
    Collections.shuffle(ids);
    for (int i = 0; i < n; i++) { set(server, "#gav_pump_" + i, ids.get(i)); set(server, "#gav_pdone_" + i, 0); }

    spawnPumps(server, level, cfg);
    forceload(server, level, b, false);
    InitiativeInit.LOGGER.info("[Gaviota] Flooded arena + spawned {} pumps in {} pairs.", n, pairs);
    return 1;
  }

  /** {@code /cobblemon-initiative gaviota raise} — manual re-flood/reset (animated, re-arms trainers). */
  public static int raise(MinecraftServer server) {
    GaviotaConfig cfg = GaviotaConfig.get();
    if (!cfg.enabled || cfg.drain.region.isPlaceholder()) return 0;
    resetGym(server, cfg, server.getPlayerList().getPlayers().stream().findFirst().orElse(null), false);
    return 1;
  }

  /** A pump bot was right-clicked (from onInteractEntity). Prime it, complete its pair, or reset. */
  private static void activatePump(ServerPlayer player, int idx) {
    MinecraftServer server = player.getServer();
    if (server == null) return;
    GaviotaConfig cfg = GaviotaConfig.get();
    if (get(server, H_FLOODED) != 1) return;
    if (get(server, "#gav_pdone_" + idx) == 1) {
      player.displayClientMessage(Component.literal("§9This pump is spent — its level is already drained."), true);
      return;
    }
    int primed = get(server, H_PRIMED);
    if (primed == idx) return; // same pump — ignore
    if (primed < 0) {
      set(server, H_PRIMED, idx);
      player.level().playSound(null, player.blockPosition(),
        SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.8f, 1.2f);
      player.displayClientMessage(Component.literal(
        "§bThe pump shudders to life and holds — now find and start its twin."), true);
      return;
    }
    // Two pumps active: is it a pair?
    int pairA = get(server, "#gav_pump_" + primed);
    int pairB = get(server, "#gav_pump_" + idx);
    set(server, H_PRIMED, -1);
    if (pairA == pairB) {
      set(server, "#gav_pdone_" + primed, 1);
      set(server, "#gav_pdone_" + idx, 1);
      int progress = get(server, H_PROGRESS) + 1;
      set(server, H_PROGRESS, progress);
      int pairs = Math.max(1, cfg.drain.pumps.size() / 2);
      int span = Math.max(1, cfg.drain.waterTopY - cfg.drain.waterBottomY);
      int target = cfg.drain.waterTopY - (int) Math.round((double) span * progress / pairs);
      startAnim(server, target, false);
      if (!cfg.drain.drainCutscene.isBlank()) runAsPlayer(player, "cutscene play " + cfg.drain.drainCutscene);
      player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.4f);
      player.displayClientMessage(Component.literal(progress >= pairs
        ? "§bThe last pair syncs — the pumps roar and the arena drains to the boards. The way is open."
        : "§bThe pair syncs — the pumps roar and the water drops a level."), true);
      if (progress >= pairs) {
        // Fully drained — unlock the Jr. Apprentice / Apprentice / Leader (gaviota_drained gate).
        var dsrc = server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(dsrc, "tag @a add gaviota_drained");
      }
    } else {
      resetGym(server, cfg, player, true);
    }
  }

  /** Wrong pair (or manual raise): re-flood, un-do every pump, and re-arm the eyesight trainers. */
  private static void resetGym(MinecraftServer server, GaviotaConfig cfg, ServerPlayer player, boolean mismatch) {
    set(server, H_PROGRESS, 0);
    set(server, H_PRIMED, -1);
    for (int i = 0; i < cfg.drain.pumps.size(); i++) set(server, "#gav_pdone_" + i, 0);
    startAnim(server, cfg.drain.waterTopY, true);
    var src = server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    for (String tag : cfg.drain.gymTrainerDefeatedTags) {
      server.getCommands().performPrefixedCommand(src, "tag @a remove " + tag);
    }
    // Water is back up — re-lock the Jr. Apprentice / Apprentice / Leader.
    server.getCommands().performPrefixedCommand(src, "tag @a remove gaviota_drained");
    if (mismatch && player != null) {
      if (!cfg.drain.raiseCutscene.isBlank()) runAsPlayer(player, "cutscene play " + cfg.drain.raiseCutscene);
      player.level().playSound(null, player.blockPosition(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.6f);
      player.displayClientMessage(Component.literal(
        "§cWrong pair — the pumps overload and the harbour floods back in. The guards return to their posts."), false);
    }
  }

  private static void spawnPumps(MinecraftServer server, ServerLevel level, GaviotaConfig cfg) {
    var src = server.createCommandSourceStack().withLevel(level).withPermission(2).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "kill @e[tag=gaviota_pump]");
    for (GaviotaConfig.Pos p : cfg.drain.pumps) {
      server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
        "easy_npc preset import_new data %s %.2f %.2f %.2f", cfg.drain.pumpPreset, p.x, p.y, p.z));
    }
  }

  private static void startAnim(MinecraftServer server, int targetSurface, boolean raising) {
    set(server, H_TARGET, targetSurface);
    set(server, H_RAISE, raising ? 1 : 0);
    nextAnimTick = 0; // fire on the next tick
  }

  /** Smooth water animation toward the target + pump pair-colour hints, each tick. */
  private static void tick(MinecraftServer server) {
    GaviotaConfig cfg = GaviotaConfig.get();
    // Auto-seed the drain puzzle ONCE per world: a fresh (or map-swapped) world must start with the
    // arena FLOODED so the pump puzzle runs and the gaviota_drained gate on the Jr. Apprentice /
    // Apprentice / Leader can ever open — otherwise the gym soft-locks at the crew. Guarded by the
    // persistent #gav_flooded score (set by flood, never cleared) + a session flag against retry spam.
    if (!autoFloodChecked && cfg.enabled && !cfg.drain.region.isPlaceholder()
        && get(server, H_FLOODED) != 1) {
      autoFloodChecked = true;
      flood(server);
      return;
    }
    if (!cfg.enabled || get(server, H_FLOODED) != 1) return;
    ServerLevel level = levelFor(server, cfg);
    if (level == null) return;
    long now = server.overworld().getGameTime();

    // (1) Water level animation.
    if (get(server, H_TARGET) != Integer.MIN_VALUE && now >= nextAnimTick) {
      nextAnimTick = now + Math.max(1, cfg.drain.animPeriodTicks);
      GaviotaConfig.Box b = cfg.drain.region;
      int surface = get(server, H_SURFACE);
      int target = get(server, H_TARGET);
      boolean raising = get(server, H_RAISE) == 1;
      if (surface == target) {
        clear(server, H_TARGET);
        forceload(server, level, b, false);
      } else {
        forceload(server, level, b, true);
        if (!raising && surface > target) { fillLayerRange(server, level, b, surface, surface, "air_replace_water"); surface--; }
        else if (raising && surface < target) { surface++; fillLayerRange(server, level, b, surface, surface, "water"); }
        set(server, H_SURFACE, surface);
      }
    }

    // (2) Pump pair-colour hints (every 10 ticks).
    if (now % 10 == 0) {
      for (int i = 0; i < cfg.drain.pumps.size(); i++) {
        if (get(server, "#gav_pdone_" + i) == 1) continue;
        int pair = get(server, "#gav_pump_" + i);
        float[] c = PAIR_COLORS[Math.floorMod(pair, PAIR_COLORS.length)];
        GaviotaConfig.Pos p = cfg.drain.pumps.get(i);
        level.sendParticles(
          new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(c[0], c[1], c[2]), 1.4f),
          p.x, p.y + 1.4, p.z, 3, 0.12, 0.2, 0.12, 0.0);
      }
    }
  }

  /** The config pump nearest {@code entity} (within 4 blocks), or -1. */
  private static int nearestPumpIndex(GaviotaConfig cfg, Entity entity) {
    double best = 16.0; int bestI = -1; // 4 blocks squared
    for (int i = 0; i < cfg.drain.pumps.size(); i++) {
      GaviotaConfig.Pos p = cfg.drain.pumps.get(i);
      double dx = entity.getX() - p.x, dy = entity.getY() - p.y, dz = entity.getZ() - p.z;
      double d = dx * dx + dy * dy + dz * dz;
      if (d < best) { best = d; bestI = i; }
    }
    return bestI;
  }

  // ── donation aquarium ────────────────────────────────────────────────────────────

  /** {@code /cobblemon-initiative gaviota donate} — curator button. Donates the first un-donated
   *  donatable fish in the party: it leaves the team, appears in a tank, and counts toward the tiers. */
  public static int donate(ServerPlayer player) {
    GaviotaConfig cfg = GaviotaConfig.get();
    if (!cfg.enabled) return 0;

    // 1. A Cobblemon fish from the party.
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    for (int i = 0; i < 6; i++) {
      Pokemon mon = party.get(i);
      if (mon == null) continue;
      String sp = mon.getSpecies().getResourceIdentifier().getPath().toLowerCase(Locale.ROOT);
      if (cfg.aquarium.donatablePokemon.contains(sp) && !player.getTags().contains("donated_" + sp)) {
        party.remove(mon);
        player.addTag("donated_" + sp);
        spawnTankDisplay(player.getServer(), cfg, sp, true, null);
        return afterDonate(player, cfg, sp);
      }
    }

    // 2. A vanilla fish item from the inventory.
    for (GaviotaConfig.Aquarium.FishItem fi : cfg.aquarium.donatableFishItems) {
      String path = itemPath(fi.item);
      if (player.getTags().contains("donated_fish_" + path)) continue;
      if (consumeItem(player, fi.item)) {
        player.addTag("donated_fish_" + path);
        spawnTankDisplay(player.getServer(), cfg, path, false, fi.entity);
        return afterDonate(player, cfg, path);
      }
    }

    player.displayClientMessage(Component.literal(
      "§9The curator checks your team and your creel — nothing new here. Bring a fresh catch, mon or fish."), true);
    return 0;
  }

  private static int afterDonate(ServerPlayer player, GaviotaConfig cfg, String id) {
    int count = bumpDonations(player);
    player.displayClientMessage(Component.literal(
      "§bYou donate the " + id + " to the aquarium — it takes to the tank at once. §7(" + count
        + "/" + cfg.aquarium.totalDonatable() + " donated)"), false);
    if (count == cfg.aquarium.tier1Count && !player.getTags().contains("aquarium_tier1_done")) {
      player.addTag("aquarium_tier1_done");
      player.displayClientMessage(Component.literal(
        "§3Aquarium Tier 1 — the harbourmaster nods. Leader Neptune will see you now."), false);
    }
    if (count == cfg.aquarium.tier2Count) {
      runAsPlayer(player, "function cobblemon_initiative:sidequest/aquarium/reward_bait");
    }
    if (count >= cfg.aquarium.totalDonatable()) {
      player.addTag("aquarium_complete");
      runAsPlayer(player, "function cobblemon_initiative:sidequest/aquarium/reward_rod");
    }
    return 1;
  }

  /** Drop a small swimming SCHOOL of the donated fish into ONE randomly-chosen tank: Cobblemon
   *  (spawnpokemon) or a vanilla fish mob (summon). Each keeps light AI (NO NoAI) so it mills/swims,
   *  PersistenceRequired so it never despawns, and a random tank name. Scatters the school around the
   *  tank centre so they do not stack. */
  private static void spawnTankDisplay(MinecraftServer server, GaviotaConfig cfg, String id,
      boolean isPokemon, String entityType) {
    if (server == null || cfg.aquarium.tanks.isEmpty()) return;
    ServerLevel level = levelFor(server, cfg);
    if (level == null) return;
    // Pick a RANDOM latched tank as the school's spawn point (skip un-placed 0,0,0 placeholders).
    List<GaviotaConfig.Aquarium.Tank> valid = new ArrayList<>();
    for (GaviotaConfig.Aquarium.Tank t : cfg.aquarium.tanks) if (t.x != 0 || t.z != 0) valid.add(t);
    if (valid.isEmpty()) return;
    GaviotaConfig.Aquarium.Tank tank = valid.get(level.random.nextInt(valid.size()));
    int school = Math.max(1, cfg.aquarium.schoolSize);
    for (int i = 0; i < school; i++) {
      double sx = tank.x + (level.random.nextDouble() - 0.5) * 1.2;
      double sy = tank.y + (level.random.nextDouble() - 0.5) * 0.6;
      double sz = tank.z + (level.random.nextDouble() - 0.5) * 1.2;
      String name = cfg.aquarium.fishNames.isEmpty() ? id
        : cfg.aquarium.fishNames.get(level.random.nextInt(cfg.aquarium.fishNames.size()));
      String nameJson = String.format(Locale.ROOT, "{\"text\":\"%s\",\"color\":\"aqua\"}", name);
      var src = server.createCommandSourceStack().withLevel(level)
        .withPosition(new net.minecraft.world.phys.Vec3(sx, sy, sz))
        .withPermission(2).withSuppressedOutput();
      if (isPokemon) {
        server.getCommands().performPrefixedCommand(src,
          String.format(Locale.ROOT, "spawnpokemon %s level=10", id));
        // Target the just-spawned mon: nearest cobblemon:pokemon that is NOT already a tank fish
        // (excludes the rest of the school so an overlapping scatter never re-tags a prior member).
        server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
          "data merge entity @e[type=cobblemon:pokemon,tag=!aquarium_fish,limit=1,sort=nearest,distance=..3] "
            + "{PersistenceRequired:1b,Silent:1b,CustomName:'%s',Tags:[\"aquarium_fish\"]}",
          nameJson));
      } else {
        server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
          "summon %s %.2f %.2f %.2f "
            + "{PersistenceRequired:1b,Silent:1b,CustomName:'%s',Tags:[\"aquarium_fish\"]}",
          entityType, sx, sy, sz, nameJson));
      }
    }
  }

  private static String itemPath(String id) {
    int i = id.indexOf(':');
    return (i >= 0 ? id.substring(i + 1) : id).toLowerCase(Locale.ROOT);
  }

  private static boolean consumeItem(ServerPlayer player, String itemId) {
    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
      .get(net.minecraft.resources.ResourceLocation.parse(itemId));
    if (item == net.minecraft.world.item.Items.AIR) return false;
    var inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); i++) {
      net.minecraft.world.item.ItemStack s = inv.getItem(i);
      if (!s.isEmpty() && s.getItem() == item) { s.shrink(1); return true; }
    }
    return false;
  }

  // ── interaction gate (submerged NPCs are un-talkable) ────────────────────────────

  private static InteractionResult onInteractEntity(Player player, Level level, InteractionHand hand,
      Entity entity, EntityHitResult hit) {
    if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    GaviotaConfig cfg = GaviotaConfig.get();
    if (!cfg.enabled || entity == null) return InteractionResult.PASS;
    MinecraftServer server = level.getServer();
    if (server == null || get(server, H_FLOODED) != 1) return InteractionResult.PASS;

    // Right-clicking a pump bot activates it (prime / complete pair / reset).
    if (entity.getTags().contains("gaviota_pump") && player instanceof ServerPlayer pumpPlayer) {
      int idx = nearestPumpIndex(cfg, entity);
      if (idx >= 0) { activatePump(pumpPlayer, idx); return InteractionResult.SUCCESS; }
    }

    // A gym NPC below the water surface cannot be talked to yet.
    if (entity.getTags().contains(cfg.drain.gymNpcTag)) {
      int surface = get(server, H_SURFACE);
      if (entity.getY() >= surface) return InteractionResult.PASS; // above the water line — reachable
      if (player instanceof ServerPlayer sp) {
        sp.displayClientMessage(Component.literal(
          "§9The water is still too high here. Drain the arena to reach them."), true);
      }
      return InteractionResult.FAIL;
    }
    return InteractionResult.PASS;
  }

  // ── helpers ──────────────────────────────────────────────────────────────────────

  private static ServerLevel levelFor(MinecraftServer server, GaviotaConfig cfg) {
    for (ServerLevel l : server.getAllLevels()) {
      if (l.dimension().location().toString().equals(cfg.dimension)) return l;
    }
    return server.overworld();
  }

  /** Fill every Y layer in [yFrom,yTo] of the region with water, or clear water to air. */
  private static void fillLayerRange(MinecraftServer server, ServerLevel level, GaviotaConfig.Box b,
      int yFrom, int yTo, String mode) {
    var src = server.createCommandSourceStack().withLevel(level).withPermission(2).withSuppressedOutput();
    for (int y = yFrom; y <= yTo; y++) {
      String cmd = "air_replace_water".equals(mode)
        ? String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:air replace minecraft:water",
            b.minX, y, b.minZ, b.maxX, y, b.maxZ)
        : String.format(Locale.ROOT, "fill %d %d %d %d %d %d minecraft:water replace minecraft:air",
            b.minX, y, b.minZ, b.maxX, y, b.maxZ);
      server.getCommands().performPrefixedCommand(src, cmd);
    }
  }

  private static void forceload(MinecraftServer server, ServerLevel level, GaviotaConfig.Box b, boolean add) {
    var src = server.createCommandSourceStack().withLevel(level).withPermission(2).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
      "forceload %s %d %d %d %d", add ? "add" : "remove", b.minX, b.minZ, b.maxX, b.maxZ));
  }

  private static void runAsPlayer(ServerPlayer sp, String cmd) {
    sp.getServer().getCommands().performPrefixedCommand(
      sp.createCommandSourceStack().withPermission(2).withSuppressedOutput(), cmd);
  }

  private static int bumpDonations(ServerPlayer player) {
    Scoreboard sb = player.getServer().getScoreboard();
    Objective obj = sb.getObjective(DON_OBJ);
    if (obj == null) obj = sb.addObjective(DON_OBJ, ObjectiveCriteria.DUMMY,
      Component.literal("Aquarium Donations"), ObjectiveCriteria.RenderType.INTEGER, false, null);
    var score = sb.getOrCreatePlayerScore(player, obj);
    int v = score.get() + 1;
    score.set(v);
    return v;
  }

  private static void set(MinecraftServer server, String holder, int value) {
    Scoreboard sb = server.getScoreboard();
    Objective obj = worldObj(sb);
    sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(holder), obj).set(value);
  }

  private static int get(MinecraftServer server, String holder) {
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(OBJ);
    if (obj == null) return holder.equals(H_TARGET) ? Integer.MIN_VALUE : 0;
    ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(holder), obj);
    if (info == null) return holder.equals(H_TARGET) ? Integer.MIN_VALUE : 0;
    return info.value();
  }

  private static void clear(MinecraftServer server, String holder) {
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(OBJ);
    if (obj != null) sb.resetSinglePlayerScore(ScoreHolder.forNameOnly(holder), obj);
  }

  private static Objective worldObj(Scoreboard sb) {
    Objective obj = sb.getObjective(OBJ);
    if (obj == null) obj = sb.addObjective(OBJ, ObjectiveCriteria.DUMMY,
      Component.literal("Gaviota"), ObjectiveCriteria.RenderType.INTEGER, false, null);
    return obj;
  }
}

package com.thecompanyinc.cobblemoninitiative.achievement;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.PokedexManager;
import com.cobblemon.mod.common.api.pokedex.SpeciesDexRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.ConfigLoader;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterManager;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncEvents;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncInit;
import com.thecompanyinc.cobblemoninitiative.streamsync.StreamSyncStats;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * The "global achievement" engine — the auto-grant + display-split half of the achievements
 * work (TODO §3).
 *
 * <p><b>Two grant paths, one manifest.</b> Every entry is keyed on derived/global state (badge
 * count, dex count, fields liberated, roster size, run deaths, quests done) via a predicate, so
 * it can be granted the instant the state qualifies — no per-achievement wiring.
 *
 * <ul>
 *   <li><b>Silent backfill</b> ({@link #onPlayerJoin}) — on join, every qualifying-but-unearned
 *       achievement is granted with NO toast, chat, or overlay. This is what lets a fresh
 *       achievement batch ship mid-run into an already-deep hardcore save without toast-spamming
 *       every retroactive unlock. The advancement JSONs ship {@code show_toast:false}, so the
 *       {@code advancement grant} is itself silent; the live/silent decision lives entirely here.
 *       A per-world latch (mod version) drives a single "records reconciled" summary line the
 *       first time a new batch backfills — never on ordinary rejoins.</li>
 *   <li><b>Live earn</b> ({@link #evaluateLive}) — during play (periodic tick + right after a
 *       trainer defeat), a newly-qualifying achievement fires the full fanfare: a client toast,
 *       the OBS overlay New-Achievement alert (via the streamsync bus), and a chat line. Gated on
 *       {@link #liveReady} so it can never pre-empt a player's join backfill.</li>
 * </ul>
 */
public final class AchievementManager {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String LATCH_FILE = "data/cobblemon_initiative_achievements.json";
  private static final int EVAL_INTERVAL_TICKS = 40;

  /** The eight Battle Frontier halls (FrontierManager clear tags: {@code frontier_<hall>_cleared}). */
  private static final String[] HALLS = {
    "tower", "factory", "castle", "arcade", "port", "pyramid", "market", "cave"
  };

  private final List<GlobalAchievement> manifest = new ArrayList<>();

  /** Players whose join backfill has run — only these get the live (toasting) path. */
  private final Set<UUID> liveReady = new HashSet<>();

  private int tickCounter = 0;

  public AchievementManager() {
    buildManifest();
  }

  // ── Manifest ─────────────────────────────────────────────────────────────────

  private void add(
    String id, String advancement, String title, String description, String icon,
    Predicate<AchievementContext> qualifies
  ) {
    manifest.add(new GlobalAchievement(id, advancement, title, description, icon, qualifies));
  }

  private void buildManifest() {
    // Battle Frontier — one per Brain, then the whole circuit. Keyed on FrontierManager's
    // per-hall clear tags (a Brain defeat sets frontier_<hall>_cleared).
    add("hall_tower", "cobblemon_initiative:frontier/hall_tower",
      "Top Floor", "Best Tower Tycoon Palmer at the Battle Tower.",
      "minecraft:end_rod", c -> c.hasFrontierHall("tower"));
    add("hall_factory", "cobblemon_initiative:frontier/hall_factory",
      "Off the Line", "Best Factory Head Noland with a rented team.",
      "minecraft:piston", c -> c.hasFrontierHall("factory"));
    add("hall_castle", "cobblemon_initiative:frontier/hall_castle",
      "Castle Standing", "Best Castle Lord Percival at the Battle Castle.",
      "minecraft:stone_bricks", c -> c.hasFrontierHall("castle"));
    add("hall_arcade", "cobblemon_initiative:frontier/hall_arcade",
      "House Edge", "Best Arcade Star Dahlia at her own game.",
      "minecraft:note_block", c -> c.hasFrontierHall("arcade"));
    add("hall_port", "cobblemon_initiative:frontier/hall_port",
      "Clear Waters", "Best Port Admiral Horatio at the Battle Port.",
      "minecraft:heart_of_the_sea", c -> c.hasFrontierHall("port"));
    add("hall_pyramid", "cobblemon_initiative:frontier/hall_pyramid",
      "King's Ransom", "Best Pyramid King Brandon at the Battle Pyramid.",
      "minecraft:gold_ingot", c -> c.hasFrontierHall("pyramid"));
    add("hall_market", "cobblemon_initiative:frontier/hall_market",
      "Hostile Takeover", "Best Market Mogul Sterling at the Battle Market.",
      "minecraft:emerald", c -> c.hasFrontierHall("market"));
    add("hall_cave", "cobblemon_initiative:frontier/hall_cave",
      "Warden's Leave", "Best Cave Warden Selene at the Battle Cave.",
      "minecraft:amethyst_cluster", c -> c.hasFrontierHall("cave"));
    add("all_halls", "cobblemon_initiative:frontier/all_halls",
      "The Whole Circuit", "Conquer every Battle Frontier hall.",
      "minecraft:echo_shard", c -> c.frontierHalls >= HALLS.length);

    // Nobles — the roster milestones on top of the existing per-noble advancements.
    add("nobles_5", "cobblemon_initiative:nobles/roster_5",
      "A Handful of Sovereigns", "Befriend five noble Pokémon.",
      "minecraft:amethyst_shard", c -> c.nobles >= 5);
    add("nobles_10", "cobblemon_initiative:nobles/roster_10",
      "Warden of the Wilds", "Befriend ten noble Pokémon.",
      "minecraft:amethyst_cluster", c -> c.nobles >= 10);
    add("nobles_15", "cobblemon_initiative:nobles/roster_15",
      "The Full Menagerie", "Befriend every noble Pokémon.",
      "minecraft:nether_star", c -> c.nobles >= 15);

    // Wheat War — fields liberated from the Company (the fields_liberated score; caps at 6).
    add("fields_1", "cobblemon_initiative:wheat/fields_1",
      "First Furrow", "Liberate your first field from the Company.",
      "minecraft:wheat_seeds", c -> c.fields >= 1);
    add("fields_3", "cobblemon_initiative:wheat/fields_3",
      "Breadbasket", "Liberate three fields.",
      "minecraft:bread", c -> c.fields >= 3);
    add("fields_6", "cobblemon_initiative:wheat/fields_6",
      "The Fields Are Free", "Liberate every field the Company held.",
      "minecraft:hay_block", c -> c.fields >= 6);

    // Pokédex — caught-species milestones.
    add("dex_25", "cobblemon_initiative:dex/caught_25",
      "Field Notes", "Register 25 species as caught.",
      "minecraft:book", c -> c.dexCaught >= 25);
    add("dex_50", "cobblemon_initiative:dex/caught_50",
      "Naturalist", "Register 50 species as caught.",
      "minecraft:writable_book", c -> c.dexCaught >= 50);
    add("dex_100", "cobblemon_initiative:dex/caught_100",
      "A Hundred Strong", "Register 100 species as caught.",
      "minecraft:written_book", c -> c.dexCaught >= 100);
    add("dex_151", "cobblemon_initiative:dex/caught_151",
      "The Long Ledger", "Register 151 species as caught.",
      "minecraft:knowledge_book", c -> c.dexCaught >= 151);

    // Nuzlocke — deathless flexes (deaths == 0 = a still-flawless run).
    add("flawless_5", "cobblemon_initiative:nuzlocke/flawless_5",
      "Five Clean", "Earn five badges without losing a single Pokémon.",
      "minecraft:feather", c -> c.deathsKnown && c.badges >= 5 && c.deaths == 0);
    add("flawless_10", "cobblemon_initiative:nuzlocke/flawless_10",
      "Spotless Circuit", "Earn all ten badges without losing a single Pokémon.",
      "minecraft:phantom_membrane", c -> c.deathsKnown && c.badges >= 10 && c.deaths == 0);
    add("deathless_champion", "cobblemon_initiative:nuzlocke/deathless_champion",
      "Unbroken", "Become Champion without losing a single Pokémon.",
      "minecraft:totem_of_undying",
      c -> c.deathsKnown && c.progress.hasAchievement("royal_league_champion") && c.deaths == 0);

    // Quests — completion-count milestones (the quest ledger in QuestTrackManager).
    add("quests_10", "cobblemon_initiative:quests/quests_10",
      "Errand Runner", "Complete ten quests.",
      "minecraft:paper", c -> c.quests >= 10);
    add("quests_30", "cobblemon_initiative:quests/quests_30",
      "Odd-Jobs Veteran", "Complete thirty quests.",
      "minecraft:map", c -> c.quests >= 30);
    add("quests_60", "cobblemon_initiative:quests/quests_60",
      "The Whole To-Do List", "Complete sixty quests.",
      "minecraft:filled_map", c -> c.quests >= 60);
  }

  // ── Derived-state snapshot ─────────────────────────────────────────────────────

  private AchievementContext buildContext(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);

    int badges = 0;
    int shrines = 0;
    ConfigLoader cfg = InitiativeInit.getConfigLoader();
    if (cfg != null) {
      for (TrainerConfig t : cfg.getTrainersByCategory("gym")) {
        if ("leader".equals(t.getTrainerType()) && progress.hasDefeatedTrainer(t.getId())) badges++;
      }
      for (TrainerConfig t : cfg.getTrainersByCategory("shrine")) {
        if ("cult_leader".equals(t.getTrainerType()) && progress.hasDefeatedTrainer(t.getId())) shrines++;
      }
    }

    int frontierHalls = 0;
    for (String hall : HALLS) {
      if (player.getTags().contains("frontier_" + hall + "_cleared")) frontierHalls++;
    }

    int nobles = 0;
    for (String id : NobleEncounterManager.NOBLE_IDS) {
      if (advancementDone(player, "cobblemon_initiative:nobles/" + id)) nobles++;
    }

    int fields = 0;
    if (server != null) {
      Scoreboard sb = server.getScoreboard();
      Objective obj = sb.getObjective("fields_liberated");
      if (obj != null) {
        ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(
          ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
        if (info != null) fields = info.value();
      }
    }

    int dexCaught = 0;
    try {
      PokedexManager dex = Cobblemon.playerDataManager.getPokedexData(player);
      for (SpeciesDexRecord record : dex.getSpeciesRecords().values()) {
        if (record.getKnowledge() == PokedexEntryProgress.CAUGHT) dexCaught++;
      }
    } catch (Exception ignored) {
      // Pokédex not ready (very early join) — treat as 0; the tick pass re-evaluates.
    }

    return new AchievementContext(
      player, progress, badges, shrines, frontierHalls, nobles,
      fields, dexCaught, progress.getPokemonLost(), progress.isDeathsKnown(),
      progress.getCompletedQuestCount()
    );
  }

  private boolean advancementDone(ServerPlayer player, String path) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    AdvancementHolder holder = server.getAdvancements().get(ResourceLocation.parse(path));
    if (holder == null) return false;
    return player.getAdvancements().getOrStartProgress(holder).isDone();
  }

  // ── Grant paths ────────────────────────────────────────────────────────────────

  /** Live earn during play — full fanfare (toast + overlay + chat). No-op until join backfill ran. */
  public void evaluateLive(ServerPlayer player) {
    if (!liveReady.contains(player.getUUID())) return;
    AchievementContext ctx = buildContext(player);
    int granted = 0;
    for (GlobalAchievement ga : manifest) {
      if (!ctx.progress.hasAchievement(ga.id()) && ga.qualifies().test(ctx)) {
        grant(player, ga, true);
        granted++;
      }
    }
    if (granted > 0 && player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }
  }

  /** Silent catch-up of everything the player already qualifies for. Returns the count granted. */
  public int backfillSilently(ServerPlayer player) {
    AchievementContext ctx = buildContext(player);
    int granted = 0;
    for (GlobalAchievement ga : manifest) {
      if (!ctx.progress.hasAchievement(ga.id()) && ga.qualifies().test(ctx)) {
        grant(player, ga, false);
        granted++;
      }
    }
    return granted;
  }

  private void grant(ServerPlayer player, GlobalAchievement ga, boolean live) {
    InitiativeInit.getProgressManager().getProgress(player).addAchievement(ga.id());
    grantAdvancementCommand(player, ga.advancement());
    if (live) {
      player.sendSystemMessage(Component.literal(
        "§6§l[Achievement Unlocked] §r§e" + ga.title()));
      player.sendSystemMessage(Component.literal("§7" + ga.description()));
      StreamSyncEvents.achievementEarned(
        player, ga.id(), ga.title(), ga.description(), ga.icon());
      InitiativePayloads.sendAchievementToast(player, ga.advancement(), ga.title());
    }
    InitiativeInit.LOGGER.info(
      "Achievement '{}' {} for {}",
      ga.id(), live ? "earned live" : "backfilled", player.getName().getString());
  }

  /** Silent grant — the JSON's {@code show_toast:false} keeps it quiet; output suppressed too. */
  private void grantAdvancementCommand(ServerPlayer player, String path) {
    MinecraftServer server = player.getServer();
    if (server == null) return;
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
      "advancement grant " + player.getName().getString() + " only " + path);
  }

  // ── Lifecycle ───────────────────────────────────────────────────────────────────

  /** Join: silent backfill, then arm the live path. One summary line per genuine batch bump. */
  public void onPlayerJoin(ServerPlayer player, MinecraftServer server) {
    boolean seeded = seedDeathsFromStreamStats(player);
    String current = currentModVersion();
    String latched = readLatchVersion(server);

    int granted = backfillSilently(player);
    liveReady.add(player.getUUID());

    if (granted > 0 || seeded) {
      InitiativeInit.getProgressManager().saveProgress(server);
    }
    if (granted > 0) {
      if (!current.equals(latched)) {
        player.sendSystemMessage(Component.literal(
          "§7The ledger has been reconciled — " + granted
            + (granted == 1 ? " past commendation" : " past commendations") + " recorded."));
      }
      // Advance the latch ONLY when a batch actually backfilled — an empty first join after a
      // version bump must not stamp the version and suppress the summary for the real backfill.
      writeLatch(server, current);
    }
    InitiativeInit.LOGGER.info(
      "[Achievements] Backfill for {} granted {} (batch {} -> {}).",
      player.getName().getString(), granted, latched, current);
  }

  /**
   * Reconcile the run death counter up to the stream stats' all-cause total. Needed only when
   * this feature lands mid-run onto an already-deep save: {@code pokemonLost} loads as 0 there,
   * which would wrongly qualify the "flawless / deathless" tiers on a run that HAS lost mons.
   * The streamsync stats have counted losses all along, so they are the trustworthy seed. Both
   * counters increment together from here on, so taking the max is monotonic and idempotent.
   *
   * @return true if the seed raised the counter (a save is worth writing)
   */
  private boolean seedDeathsFromStreamStats(ServerPlayer player) {
    StreamSyncStats stats = StreamSyncInit.getStats();
    if (stats == null || !stats.isLoaded()) return false;
    int tracked = (int) Math.min(Integer.MAX_VALUE, stats.getPokemonLost());
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    boolean changed = false;
    if (tracked > progress.getPokemonLost()) {
      progress.setPokemonLost(tracked);
      changed = true;
    }
    // The stream stats ARE a trustworthy per-save loss history, so once we have read them the
    // count is verified — even a legacy save whose own field was absent. This is what lets the
    // deathless/flawless tiers grant on a streamsync-run deep save.
    if (!progress.isDeathsKnown()) {
      progress.setDeathsKnown(true);
      changed = true;
    }
    return changed;
  }

  public void onPlayerDisconnect(UUID playerId) {
    liveReady.remove(playerId);
  }

  /** END_SERVER_TICK: periodic live re-evaluation catches scoreboard/Pokédex-derived crossings. */
  public void tick(MinecraftServer server) {
    if (++tickCounter < EVAL_INTERVAL_TICKS) return;
    tickCounter = 0;
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      evaluateLive(player);
    }
  }

  // ── Per-world batch latch ────────────────────────────────────────────────────────

  private String readLatchVersion(MinecraftServer server) {
    Path latch = server.getWorldPath(LevelResource.ROOT).resolve(LATCH_FILE);
    if (!Files.exists(latch)) return "unknown";
    try (Reader reader = Files.newBufferedReader(latch)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      if (json != null && json.has("batchVersion")) {
        return json.get("batchVersion").getAsString();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.warn("[Achievements] Could not read batch latch.", e);
    }
    return "unknown";
  }

  private void writeLatch(MinecraftServer server, String version) {
    Path latch = server.getWorldPath(LevelResource.ROOT).resolve(LATCH_FILE);
    try {
      Files.createDirectories(latch.getParent());
      JsonObject json = new JsonObject();
      json.addProperty("batchVersion", version);
      try (Writer writer = Files.newBufferedWriter(latch)) {
        GSON.toJson(json, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.warn("[Achievements] Could not write batch latch.", e);
    }
  }

  private static String currentModVersion() {
    return FabricLoader.getInstance()
      .getModContainer("cobblemon-initiative")
      .map(c -> c.getMetadata().getVersion().getFriendlyString())
      .orElse("unknown");
  }
}

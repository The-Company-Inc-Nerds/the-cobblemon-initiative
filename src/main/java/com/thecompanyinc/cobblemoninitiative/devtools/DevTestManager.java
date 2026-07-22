package com.thecompanyinc.cobblemoninitiative.devtools;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DEV-ONLY test harness (strip the whole package at 1.0.0 — TODO §2).
 *
 * <p>{@code /cobblemon-initiative dev team <stage>} — banks the current party to the PC and
 * provisions the bundled counter team for that stage: species/moves/items authored against
 * the REAL enemy team files, level = the era entry cap, perfect 31 IVs, tuned EV spreads.
 * Deliberately overpowered: a lost test is a permadeath, so the harness errs hot.
 *
 * <p>{@code /cobblemon-initiative dev stage <stage>} — one-shot progression setup for the
 * same stage names: badges (cap follows), gate scores (fields_liberated for the HQ raid),
 * defeat tags + achievements for the board/founder gates, and a teleport to the stage's
 * anchor trainer when its coordinates are authored (skipped while still [0,0,0]).
 */
public final class DevTestManager {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-devtest");
  private static final Gson GSON = new Gson();

  private static final ResourceLocation COUNTER_TEAMS_RESOURCE =
    ResourceLocation.fromNamespaceAndPath("cobblemon_initiative", "devtest/counter_teams.json");

  /** stage id → parsed counter team (lazy, re-read per server so /reload picks up edits). */
  private static Map<String, JsonObject> teams;
  private static MinecraftServer teamsFrom;

  private DevTestManager() {}

  // ---------------------------------------------------------------------------
  // Stage setup table
  // ---------------------------------------------------------------------------

  private record StageSetup(
    int badges,
    List<String> achievements,
    List<String> tags,
    Map<String, Integer> scores,
    List<String> defeatedTrainers,
    String gotoTrainer
  ) {}

  private static final String[] GYM_TOWNS = {
    "takehara", "hua_zhan", "mystic", "deepcore", "gaviota",
    "kalahar", "cyber", "ryujin", "nifl", "scorchspire",
  };

  private static final Map<String, StageSetup> STAGE_SETUPS = buildStageSetups();

  private static Map<String, StageSetup> buildStageSetups() {
    Map<String, StageSetup> m = new LinkedHashMap<>();
    for (int gym = 1; gym <= 10; gym++) {
      m.put("gym_" + gym, new StageSetup(
        gym - 1, List.of(), List.of(), Map.of(), List.of(), GYM_TOWNS[gym - 1] + "_leader"));
    }
    m.put("shrine_fairy",  new StageSetup(2, List.of(), List.of(), Map.of(), List.of(), "fairy_shrine_leader"));
    m.put("shrine_ground", new StageSetup(5, List.of(), List.of(), Map.of(), List.of(), "ground_shrine_leader"));
    m.put("shrine_dragon", new StageSetup(7, List.of(), List.of(), Map.of(), List.of(), "dragon_shrine_leader"));
    m.put("shrine_ice",    new StageSetup(8, List.of(), List.of(), Map.of(), List.of(), "ice_shrine_leader"));
    m.put("shrine_fire",   new StageSetup(9, List.of(), List.of(), Map.of(), List.of(), "fire_shrine_leader"));
    // HQ raid: badge 7 + the fields_liberated>=4 gate DJ's battle button reads.
    m.put("hq", new StageSetup(
      7, List.of(), List.of("wheat_war_active"), Map.of("fields_liberated", 4), List.of(), "villain_boss"));
    m.put("royal", new StageSetup(
      10, List.of(), List.of(), Map.of("fields_liberated", 4), List.of(), "royal_elite_1"));
    // Board era: post-champion (cap 85) + post-HQ; the royal_league_champion TAG is what
    // the quest HUD reads, the achievement is what raises the cap.
    m.put("board", new StageSetup(
      10,
      List.of("royal_league_champion"),
      List.of("royal_league_champion", "defeated_villain_boss", "wheat_war_active"),
      Map.of("fields_liberated", 4),
      List.of("royal_champion", "villain_boss"),
      "board_lauren"));
    // Founder: board cleared (cap 100) — tags gate his dialog, the achievement lifts the cap.
    m.put("founder", new StageSetup(
      10,
      List.of("royal_league_champion", "board_cleared"),
      List.of(
        "royal_league_champion", "defeated_villain_boss", "wheat_war_active",
        "defeated_board_lauren", "defeated_board_madeline", "defeated_board_matt", "defeated_board_micah"),
      Map.of("fields_liberated", 4),
      List.of("royal_champion", "villain_boss", "board_lauren", "board_madeline", "board_matt", "board_micah"),
      "villain_final_boss"));
    return m;
  }

  public static Set<String> stageIds() {
    return STAGE_SETUPS.keySet();
  }

  // ---------------------------------------------------------------------------
  // dev team <stage>
  // ---------------------------------------------------------------------------

  /** Levels per stage = the era entry cap (what a legal player has at that door). */
  private static final Map<String, Integer> STAGE_LEVELS = Map.ofEntries(
    Map.entry("gym_1", 15), Map.entry("gym_2", 22), Map.entry("gym_3", 30),
    Map.entry("gym_4", 37), Map.entry("gym_5", 44), Map.entry("gym_6", 50),
    Map.entry("gym_7", 56), Map.entry("gym_8", 62), Map.entry("gym_9", 68),
    Map.entry("gym_10", 74), Map.entry("shrine_fairy", 30), Map.entry("shrine_ground", 50),
    Map.entry("shrine_dragon", 62), Map.entry("shrine_ice", 68), Map.entry("shrine_fire", 74),
    Map.entry("hq", 62), Map.entry("royal", 80), Map.entry("board", 85), Map.entry("founder", 100)
  );

  public static int giveTeam(ServerPlayer player, String stage) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;

    JsonObject team = loadTeams(server).get(stage);
    if (team == null) {
      player.sendSystemMessage(Component.literal(
        "§cNo counter team for stage '" + stage + "'. Stages: " + String.join(", ", loadTeams(server).keySet())));
      return 0;
    }
    int level = STAGE_LEVELS.getOrDefault(stage, 50);

    // Bank the current party to the PC first — never destroy a tester's mons.
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<Pokemon> current = new ArrayList<>();
    for (Pokemon p : party) if (p != null) current.add(p);
    PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
    for (Pokemon p : current) {
      var pos = pc.getFirstAvailablePosition();
      if (pos == null) {
        player.sendSystemMessage(Component.literal("§cPC is full — cannot bank your current party. Aborting."));
        return 0;
      }
      party.remove(p);
      pc.set(pos, p);
    }

    int built = 0;
    for (JsonElement el : team.getAsJsonArray("members")) {
      JsonObject m = el.getAsJsonObject();
      Pokemon mon = buildMember(m, level);
      if (mon == null) {
        LOGGER.warn("[DevTest] Could not build member {} for stage {}", m, stage);
        continue;
      }
      party.add(mon);
      built++;
    }
    String label = team.has("label") ? team.get("label").getAsString() : stage;
    final int count = built;
    player.sendSystemMessage(Component.literal(
      "§aDev counter team for §e" + label + "§a: " + count + " mons at L" + level +
      " (31 IVs), old party banked to PC."));
    return built;
  }

  private static Pokemon buildMember(JsonObject m, int level) {
    try {
      String props = m.get("species").getAsString()
        + " level=" + level
        + " nature=" + m.get("nature").getAsString()
        + " ability=" + m.get("ability").getAsString();
      Pokemon mon = PokemonProperties.Companion.parse(props).create();

      for (Stats stat : new Stats[] {
        Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED,
      }) {
        mon.setIV(stat, 31);
      }
      applyEvFocus(mon, m.has("evFocus") ? m.get("evFocus").getAsString() : "mixed");

      JsonArray moves = m.getAsJsonArray("moves");
      for (int i = 0; i < moves.size() && i < 4; i++) {
        MoveTemplate tpl = Moves.INSTANCE.getByName(moves.get(i).getAsString());
        if (tpl != null) mon.getMoveSet().setMove(i, tpl.create());
      }

      if (m.has("heldItem")) {
        ResourceLocation rl = ResourceLocation.parse(m.get("heldItem").getAsString());
        var item = BuiltInRegistries.ITEM.get(rl);
        if (item != net.minecraft.world.item.Items.AIR) {
          // (stack, decrement, sendUpdate) — no player inventory involved, so no decrement.
          mon.swapHeldItem(new ItemStack(item), false, true);
        }
      }
      mon.heal();
      return mon;
    } catch (Exception e) {
      LOGGER.warn("[DevTest] buildMember failed: {}", e.getMessage());
      return null;
    }
  }

  private static void applyEvFocus(Pokemon mon, String focus) {
    switch (focus) {
      case "physical" -> { ev(mon, Stats.ATTACK, 252); ev(mon, Stats.SPEED, 252); ev(mon, Stats.HP, 4); }
      case "special" -> { ev(mon, Stats.SPECIAL_ATTACK, 252); ev(mon, Stats.SPEED, 252); ev(mon, Stats.HP, 4); }
      case "bulk" -> { ev(mon, Stats.HP, 252); ev(mon, Stats.DEFENCE, 128); ev(mon, Stats.SPECIAL_DEFENCE, 126); }
      default -> { ev(mon, Stats.ATTACK, 128); ev(mon, Stats.SPECIAL_ATTACK, 124); ev(mon, Stats.SPEED, 252); }
    }
  }

  private static void ev(Pokemon mon, Stats stat, int value) {
    mon.setEV(stat, value);
  }

  // ---------------------------------------------------------------------------
  // dev stage <stage>
  // ---------------------------------------------------------------------------

  public static int applyStage(ServerPlayer player, String stage) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    StageSetup setup = STAGE_SETUPS.get(stage);
    if (setup == null) {
      player.sendSystemMessage(Component.literal(
        "§cUnknown stage '" + stage + "'. Stages: " + String.join(", ", STAGE_SETUPS.keySet())));
      return 0;
    }

    setBadges(player, setup.badges());

    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    for (String id : setup.defeatedTrainers()) progress.addDefeatedTrainer(id);
    for (String a : setup.achievements()) progress.addAchievement(a);
    for (String t : setup.tags()) player.addTag(t);
    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    InitiativeInit.getProgressManager().saveProgress(server);

    var src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    String name = player.getGameProfile().getName();
    setup.scores().forEach((obj, val) ->
      server.getCommands().performPrefixedCommand(src, "scoreboard players set " + name + " " + obj + " " + val));

    boolean teleported = teleportToTrainer(player, setup.gotoTrainer());
    int cap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    player.sendSystemMessage(Component.literal(
      "§aStage §e" + stage + "§a set: " + setup.badges() + " badge(s), cap " + cap +
      (setup.scores().isEmpty() ? "" : ", scores " + setup.scores()) +
      (teleported ? ", teleported to " + setup.gotoTrainer() : " §7(anchor " + setup.gotoTrainer() + " has no coords yet)")));
    return 1;
  }

  /** Mirror of the `dev badges` logic (kept here so the whole package strips cleanly). */
  private static void setBadges(ServerPlayer player, int n) {
    var configLoader = InitiativeInit.getConfigLoader();
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);

    List<String> badgeAchievements = new ArrayList<>();
    for (var cap : configLoader.getLevelCaps()) {
      String a = cap.getAchievementId();
      if (a != null && a.startsWith("badge_")) badgeAchievements.add(a);
    }
    progress.getEarnedAchievements().removeIf(a -> a.startsWith("badge_"));
    Set<String> grant = new java.util.HashSet<>(
      badgeAchievements.subList(0, Math.min(n, badgeAchievements.size())));
    for (TrainerConfig t : configLoader.getAllTrainers()) {
      String a = t.getAchievementOnDefeat();
      if (a == null || !a.startsWith("badge_")) continue;
      // Toggle the defeated_<leader> PLAYER TAG too — dialog/quest gates check the tag, not
      // the progress Set (kept in step with DevCommands.devBadges).
      if (grant.contains(a)) {
        progress.addDefeatedTrainer(t.getId());
        player.addTag("defeated_" + t.getId());
      } else {
        progress.getDefeatedTrainers().remove(t.getId());
        player.removeTag("defeated_" + t.getId());
      }
    }
    for (String a : grant) progress.addAchievement(a);
    InitiativeInit.getLevelCapManager().updateLevelCap(player);
  }

  /** Teleport to a trainer's authored coords; false when unset or still the [0,0,0] placeholder. */
  private static boolean teleportToTrainer(ServerPlayer player, String trainerId) {
    TrainerConfig t = InitiativeInit.getConfigLoader().getTrainer(trainerId);
    if (t == null || t.getCoordinates() == null || t.getCoordinates().length < 3) return false;
    int[] c = t.getCoordinates();
    if (c[0] == 0 && c[1] == 0 && c[2] == 0) return false;
    player.connection.teleport(c[0] + 0.5, c[1], c[2] + 0.5, player.getYRot(), player.getXRot());
    return true;
  }

  // ---------------------------------------------------------------------------
  // Resource loading
  // ---------------------------------------------------------------------------

  private static Map<String, JsonObject> loadTeams(MinecraftServer server) {
    if (teams != null && teamsFrom == server) return teams;
    Map<String, JsonObject> parsed = new LinkedHashMap<>();
    try {
      Resource resource = server.getResourceManager().getResource(COUNTER_TEAMS_RESOURCE).orElse(null);
      if (resource == null) {
        LOGGER.warn("[DevTest] {} not found — dev team disabled.", COUNTER_TEAMS_RESOURCE);
      } else {
        try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
          JsonObject root = GSON.fromJson(reader, JsonObject.class);
          for (JsonElement el : root.getAsJsonArray("stages")) {
            JsonObject s = el.getAsJsonObject();
            parsed.put(s.get("id").getAsString(), s);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.error("[DevTest] Failed to read counter teams: {}", e.getMessage());
    }
    teams = parsed;
    teamsFrom = server;
    return parsed;
  }
}

package com.thecompanyinc.cobblemoninitiative.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.LootChestConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.config.ShrineConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import com.thecompanyinc.cobblemoninitiative.economy.ShopTierManager;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightInit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CobblemonInitiativeCommands {

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher
  ) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("progress").executes(
            CobblemonInitiativeCommands::showProgress
          )
        )
        .then(
          Commands.literal("levelcap").executes(
            CobblemonInitiativeCommands::showLevelCap
          )
        )
        .then(
          Commands.literal("reset").executes(
            CobblemonInitiativeCommands::resetProgress
          )
        )
        .then(
          Commands.literal("info").then(
            Commands.argument("trainer", StringArgumentType.string())
              .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                  InitiativeInit.getConfigLoader().getTrainerIds(),
                  builder
                )
              )
              .executes(CobblemonInitiativeCommands::showTrainerInfo)
          )
        )
        .then(
          Commands.literal("list")
            .then(
              Commands.literal("gyms").executes(ctx -> listTrainers(ctx, "gym"))
            )
            .then(
              Commands.literal("shrines").executes(ctx ->
                listTrainers(ctx, "shrine")
              )
            )
            .then(
              Commands.literal("groups").executes(
                CobblemonInitiativeCommands::listGroups
              )
            )
            .then(
              Commands.literal("all").executes(ctx -> listTrainers(ctx, null))
            )
        )
        .then(
          Commands.literal("defeat").then(
            Commands.argument("trainer", StringArgumentType.string())
              .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                  InitiativeInit.getConfigLoader().getTrainerIds(),
                  builder
                )
              )
              .executes(CobblemonInitiativeCommands::markDefeated)
          )
        )
        .then(
          Commands.literal("shrine").then(
            Commands.argument("shrine", StringArgumentType.word())
              .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                  Arrays.asList(
                    InitiativeInit.getShrineChallengeManager().getShrineIds()
                  ),
                  builder
                )
              )
              .then(
                Commands.literal("start").executes(
                  CobblemonInitiativeCommands::shrineStart
                )
              )
              .then(
                Commands.literal("stop").executes(
                  CobblemonInitiativeCommands::shrineStop
                )
              )
              .then(
                Commands.literal("test").then(
                  Commands.argument("testName", StringArgumentType.word())
                    .suggests((context, builder) ->
                      SharedSuggestionProvider.suggest(
                        List.of("friendship", "fullness", "nickname", "shiny", "resolve"),
                        builder
                      )
                    )
                    .executes(CobblemonInitiativeCommands::shrineTest)
                )
              )
              .then(
                Commands.literal("complete").executes(
                  CobblemonInitiativeCommands::shrineComplete
                )
              )
              .then(
                Commands.literal("path")
                  .then(
                    Commands.literal("record").executes(
                      CobblemonInitiativeCommands::shrinePathRecord
                    )
                  )
                  .then(
                    Commands.literal("here").executes(
                      CobblemonInitiativeCommands::shrinePathHere
                    )
                  )
                  .then(
                    Commands.literal("clear").executes(
                      CobblemonInitiativeCommands::shrinePathClear
                    )
                  )
                  .then(
                    Commands.literal("show").executes(
                      CobblemonInitiativeCommands::shrinePathShow
                    )
                  )
                  .then(
                    Commands.literal("export").executes(
                      CobblemonInitiativeCommands::shrinePathExport
                    )
                  )
              )
          )
        )
        .then(
          Commands.literal("quest")
            .then(Commands.literal("show").executes(CobblemonInitiativeCommands::questShow))
            .then(Commands.literal("hide").executes(CobblemonInitiativeCommands::questHide))
            .then(Commands.literal("refresh").executes(CobblemonInitiativeCommands::questRefresh))
        )
        .then(
          Commands.literal("reload").executes(CobblemonInitiativeCommands::reloadAll)
        )
        .then(
          Commands.literal("dev")
            .then(
              Commands.literal("goto").then(
                Commands.argument("trainer", StringArgumentType.word())
                  .suggests((context, builder) ->
                    SharedSuggestionProvider.suggest(trainerIdsWithCoords(), builder)
                  )
                  .executes(CobblemonInitiativeCommands::devGoto)
              )
            )
            .then(
              Commands.literal("badges").then(
                Commands.argument("count", IntegerArgumentType.integer(0, 10))
                  .executes(CobblemonInitiativeCommands::devBadges)
              )
            )
            .then(
              Commands.literal("grant").then(
                Commands.argument("achievement", StringArgumentType.word())
                  .suggests((context, builder) ->
                    SharedSuggestionProvider.suggest(badgeAchievementIds(), builder)
                  )
                  .executes(CobblemonInitiativeCommands::devGrant)
              )
            )
            .then(
              Commands.literal("kit").executes(CobblemonInitiativeCommands::devKit)
            )
        )
        .then(
          Commands.literal("shop")
            .then(
              Commands.literal("refresh").executes(CobblemonInitiativeCommands::refreshShopTier)
            )
            .then(
              Commands.argument("tier", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(ShopTierManager.TIERS, builder)
                )
                .executes(CobblemonInitiativeCommands::applyShopTier)
            )
        )
    );

    dispatcher.register(
      Commands.literal("ca")
        .requires(source -> source.hasPermission(2))
        .redirect(dispatcher.getRoot().getChild("cobblemon-initiative"))
    );
  }

  // Quest HUD toggle — thin wrappers that dispatch the datapack quest functions
  // (the boss bar + sidebar logic lives in data/.../function/quest/).
  private static int questDispatch(
    CommandContext<CommandSourceStack> context, String fn
  ) {
    context.getSource().getServer().getCommands().performPrefixedCommand(
      context.getSource(), "function cobblemon_initiative:quest/" + fn
    );
    return 1;
  }

  private static int questShow(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "show");
  }

  private static int questHide(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "hide");
  }

  private static int questRefresh(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "refresh");
  }

  /**
   * /cobblemon-initiative shop &lt;tier&gt;
   * Swaps the CobbleDollars default shop to a pre-baked tier and hot-reloads it. Invoked from gym
   * leader / Acting CEO DJ reward commands as the badge progresses; also a manual admin/test lever.
   */
  private static int applyShopTier(CommandContext<CommandSourceStack> context) {
    String tier = StringArgumentType.getString(context, "tier");
    boolean ok = ShopTierManager.applyTier(context.getSource().getServer(), tier);
    if (ok) {
      context.getSource().sendSuccess(
        () ->
          Component.literal(
            "§a[Shop] Applied tier §e" + tier + " §aand reloaded CobbleDollars."
          ),
        true
      );
      return 1;
    }
    context.getSource().sendFailure(
      Component.literal("§cUnknown or unreadable shop tier: " + tier)
    );
    return 0;
  }

  /**
   * /cobblemon-initiative shop refresh — re-apply the source player's current base tier
   * so the liberation-relief level (fields_liberated) is re-resolved. Fired by
   * liberation/free_field_apply after each field liberation; safe to run any time.
   */
  private static int refreshShopTier(CommandContext<CommandSourceStack> context) {
    net.minecraft.server.level.ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      context.getSource().sendFailure(
        Component.literal("§cshop refresh needs a player source (it reads badge/field state).")
      );
      return 0;
    }
    String base = ShopTierManager.currentBaseTier(player);
    boolean ok = ShopTierManager.applyTier(context.getSource().getServer(), base);
    if (ok) {
      context.getSource().sendSuccess(
        () -> Component.literal("§a[Shop] Re-applied tier for §e" + base + "§a (relief re-resolved)."),
        true
      );
      return 1;
    }
    context.getSource().sendFailure(Component.literal("§cCould not re-apply tier " + base));
    return 0;
  }

  private static int listGroups(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();

    source.sendSuccess(
      () -> Component.literal("§6=== Available Groups ==="),
      false
    );

    for (String group : InitiativeInit.getConfigLoader().getAvailableGroups()) {
      List<TrainerConfig> trainers =
        InitiativeInit.getConfigLoader().getTrainersByGroup(group);
      source.sendSuccess(
        () ->
          Component.literal(
            "§e" + group + " §7(" + trainers.size() + " trainers)"
          ),
        false
      );
    }

    return 1;
  }

  private static int showProgress(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);

    source.sendSuccess(
      () -> Component.literal("§6=== Your Progress ==="),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eTrainers Defeated: §f" + progress.getDefeatedTrainers().size()
        ),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eAchievements Earned: §f" + progress.getEarnedAchievements().size()
        ),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eCurrent Level Cap: §f" + progress.getCurrentLevelCap()
        ),
      false
    );

    source.sendSuccess(() -> Component.literal("§6--- Gym Badges ---"), false);
    List<TrainerConfig> gymLeaders =
      InitiativeInit.getConfigLoader().getTrainersByCategory("gym").stream()
        .filter(t -> "leader".equals(t.getTrainerType()))
        .toList();
    int badges = (int) gymLeaders.stream()
      .filter(t -> progress.hasDefeatedTrainer(t.getId()))
      .count();
    final int totalGymBadges = gymLeaders.size();
    final int finalBadges = badges;
    source.sendSuccess(
      () -> Component.literal("§eBadges: §f" + finalBadges + "/" + totalGymBadges),
      false
    );

    return 1;
  }

  private static int showLevelCap(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    int levelCap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    source.sendSuccess(
      () -> Component.literal("§eYour current level cap is: §f" + levelCap),
      false
    );

    return 1;
  }

  private static int resetProgress(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
    progress.getDefeatedTrainers().clear();
    progress.getEarnedAchievements().clear();
    progress.setCurrentLevelCap(20);

    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }

    source.sendSuccess(
      () -> Component.literal("§aYour progress has been reset!"),
      false
    );

    return 1;
  }

  private static int showTrainerInfo(
    CommandContext<CommandSourceStack> context
  ) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    source.sendSuccess(
      () -> Component.literal("§6=== " + trainer.getDisplayName() + " ==="),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eID: §f" + trainer.getId()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eCategory: §f" + trainer.getCategory()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eLocation: §f" + trainer.getLocation()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eType: §f" + trainer.getTrainerType()),
      false
    );

    if (trainer.getGroup() != null) {
      source.sendSuccess(
        () -> Component.literal("§eGroup: §f" + trainer.getGroup()),
        false
      );
    }

    if (
      trainer.getCoordinates() != null && trainer.getCoordinates().length == 3
    ) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eCoordinates: §f" +
              trainer.getCoordinates()[0] +
              ", " +
              trainer.getCoordinates()[1] +
              ", " +
              trainer.getCoordinates()[2]
          ),
        false
      );
    }

    if (!trainer.getPrerequisites().isEmpty()) {
      source.sendSuccess(() -> Component.literal("§ePrerequisites:"), false);
      for (String prereq : trainer.getPrerequisites()) {
        TrainerConfig prereqTrainer =
          InitiativeInit.getConfigLoader().getTrainer(prereq);
        String name =
          prereqTrainer != null ? prereqTrainer.getDisplayName() : prereq;
        source.sendSuccess(() -> Component.literal("§7  - " + name), false);
      }
    }

    if (trainer.getSpawnOnDefeat() != null) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eSpawns on Defeat: §f" +
              trainer.getSpawnOnDefeat().getSpecies() +
              " (Lv. " +
              trainer.getSpawnOnDefeat().getLevel() +
              ")"
          ),
        false
      );
    }

    if (trainer.getTeam() != null && trainer.getTeam().getPokemon() != null) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eTeam Size: §f" +
              trainer.getTeam().getPokemon().size() +
              " Pokemon"
          ),
        false
      );
    }

    return 1;
  }

  private static int listTrainers(
    CommandContext<CommandSourceStack> context,
    String category
  ) {
    CommandSourceStack source = context.getSource();

    source.sendSuccess(
      () ->
        Component.literal(
          "§6=== Trainers" +
            (category != null ? " (" + category + ")" : "") +
            " ==="
        ),
      false
    );

    for (TrainerConfig trainer : InitiativeInit.getConfigLoader().getAllTrainers()) {
      if (category == null || trainer.getCategory().contains(category)) {
        source.sendSuccess(
          () ->
            Component.literal(
              "§e" + trainer.getId() + " §7- " + trainer.getDisplayName()
            ),
          false
        );
      }
    }

    return 1;
  }

  private static int markDefeated(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    InitiativeInit.getProgressManager().onTrainerDefeated(player, trainerId);
    source.sendSuccess(
      () ->
        Component.literal(
          "§aMarked §e" + trainer.getDisplayName() + " §aas defeated!"
        ),
      true
    );

    return 1;
  }

  // ── Shrine challenge commands ─────────────────────────────────────────────────

  /**
   * /cobblemon-initiative shrine <id> start
   * Intended to be triggered from an Easy NPC dialog or a pressure plate.
   */
  private static int shrineStart(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String shrineId = StringArgumentType.getString(context, "shrine");
    boolean started = InitiativeInit.getShrineChallengeManager().startChallenge(
      player,
      shrineId
    );
    return started ? 1 : 0;
  }

  /**
   * /cobblemon-initiative shrine <id> stop
   * Aborts the active challenge (no penalty). Also registered as /shrine-abort.
   */
  private static int shrineStop(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getShrineChallengeManager().stopChallenge(player);
    return 1;
  }

  /**
   * /cobblemon-initiative shrine fairy test <testName>
   * Runs an individual Fairy shrine test on the player's lead Pokémon.
   * testName: friendship | fullness | nickname | shiny | resolve
   * Triggered from an Easy NPC altar dialog or command block.
   */
  private static int shrineTest(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String testName = StringArgumentType.getString(context, "testName");
    boolean passed = InitiativeInit.getShrineChallengeManager().runFairyTest(player, testName);
    return passed ? 1 : 0;
  }

  /**
   * /cobblemon-initiative shrine <id> complete
   * Called by a command block at the parkour finish line:
   *   execute as @a[distance=..3] run cobblemon-initiative shrine fire complete
   */
  private static int shrineComplete(
    CommandContext<CommandSourceStack> context
  ) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String shrineId = StringArgumentType.getString(context, "shrine");
    boolean completed =
      InitiativeInit.getShrineChallengeManager().completeParkour(
        player,
        shrineId
      );
    return completed ? 1 : 0;
  }

  // ── Safe-path authoring (dev) ─────────────────────────────────────────────────

  /** Resolves the player + shrine id shared by every path subcommand. */
  private static ServerPlayer pathPlayer(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
    }
    return player;
  }

  /** /cobblemon-initiative shrine <id> path record — toggle continuous recording. */
  private static int shrinePathRecord(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().toggleRecording(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path here — add the block underfoot. */
  private static int shrinePathHere(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().recordHere(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path clear — wipe the recorded path. */
  private static int shrinePathClear(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().clearPath(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path show — particle-highlight safe blocks. */
  private static int shrinePathShow(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().showPath(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path export — print a safePositions snippet. */
  private static int shrinePathExport(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().exportPath(player, shrineId);
    return 1;
  }

  // ── Reload & dev/testing helpers ──────────────────────────────────────────────

  /**
   * /cobblemon-initiative reload — re-read trainers, level caps, shrine challenges,
   * and all runtime configs without a relaunch. NOTE: reads BUILT resources — in the
   * dev client run `gradle processResources` first; a packaged jar needs a rebuild.
   */
  private static int reloadAll(CommandContext<CommandSourceStack> context) {
    InitiativeInit.getConfigLoader().loadAllConfigs();
    InitiativeInit.getShrineChallengeManager().loadChallenges();
    NuzlockeInit.reloadConfig();
    NpcSightInit.reloadConfig();
    ShrineConfig.reload();
    LootChestConfig.reload();
    ProgressionConfig.reload();
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aReloaded trainers, level caps, shrine challenges, and all configs. " +
          "§7(Reads built resources — run `gradle processResources` in the dev client, or rebuild for a packaged jar.)"
        ),
      true
    );
    return 1;
  }

  private static List<String> trainerIdsWithCoords() {
    List<String> ids = new ArrayList<>();
    for (TrainerConfig t : InitiativeInit.getConfigLoader().getAllTrainers()) {
      int[] c = t.getCoordinates();
      if (c != null && c.length >= 3) ids.add(t.getId());
    }
    return ids;
  }

  private static List<String> badgeAchievementIds() {
    List<String> ids = new ArrayList<>();
    for (LevelCapConfig cap : InitiativeInit.getConfigLoader().getLevelCaps()) {
      if (cap.getAchievementId() != null) ids.add(cap.getAchievementId());
    }
    return ids;
  }

  /** /cobblemon-initiative dev goto &lt;trainer&gt; — teleport to a trainer's coordinates. */
  private static int devGoto(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String id = StringArgumentType.getString(context, "trainer");
    TrainerConfig t = InitiativeInit.getConfigLoader().getTrainer(id);
    if (t == null || t.getCoordinates() == null || t.getCoordinates().length < 3) {
      context.getSource().sendFailure(
        Component.literal("§cNo coordinates for trainer: " + id)
      );
      return 0;
    }
    int[] c = t.getCoordinates();
    player.connection.teleport(
      c[0] + 0.5, c[1], c[2] + 0.5, player.getYRot(), player.getXRot()
    );
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aTeleported to §e" + id + "§a (" + c[0] + " " + c[1] + " " + c[2] + ")."
        ),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev badges &lt;n&gt; — set progression to exactly N gym badges. */
  private static int devBadges(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    int n = IntegerArgumentType.getInteger(context, "count");

    var configLoader = InitiativeInit.getConfigLoader();
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);

    // Badge achievements in gym order.
    List<String> badgeAchievements = new ArrayList<>();
    for (LevelCapConfig cap : configLoader.getLevelCaps()) {
      String a = cap.getAchievementId();
      if (a != null && a.startsWith("badge_")) badgeAchievements.add(a);
    }

    // Reset badge state, then grant the first N (achievements + their gym leaders).
    progress.getEarnedAchievements().removeIf(a -> a.startsWith("badge_"));
    Set<String> grant = new HashSet<>(
      badgeAchievements.subList(0, Math.min(n, badgeAchievements.size()))
    );
    for (TrainerConfig t : configLoader.getAllTrainers()) {
      String a = t.getAchievementOnDefeat();
      if (a == null || !a.startsWith("badge_")) continue;
      if (grant.contains(a)) progress.addDefeatedTrainer(t.getId());
      else progress.getDefeatedTrainers().remove(t.getId());
    }
    for (String a : grant) progress.addAchievement(a);

    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }

    int cap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aSet progression to §e" + n + "§a badge(s); level cap now §e" + cap + "§a."
        ),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev grant &lt;achievement&gt; — grant one achievement + refresh cap. */
  private static int devGrant(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String achievement = StringArgumentType.getString(context, "achievement");
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    progress.addAchievement(achievement);
    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }
    context.getSource().sendSuccess(
      () -> Component.literal("§aGranted achievement §e" + achievement + "§a."),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev kit — give the shrine crystals + test items. */
  private static int devKit(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    var server = player.getServer();
    if (server == null) return 0;

    String name = player.getName().getString();
    String[] gives = {
      "cobblemon-initiative:fire_shrine_crystal",
      "cobblemon-initiative:ground_shrine_crystal",
      "cobblemon-initiative:ice_shrine_crystal",
      "cobblemon-initiative:dragon_shrine_crystal",
      "cobblemon-initiative:fairy_shrine_crystal",
      "cobblemon:rare_candy 16",
      "cobblemon:potion 16",
    };
    var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    for (String give : gives) {
      server.getCommands().performPrefixedCommand(source, "give " + name + " " + give);
    }
    context.getSource().sendSuccess(
      () -> Component.literal("§aDev kit granted: 5 shrine crystals + rare candy + potions."),
      true
    );
    return 1;
  }
}

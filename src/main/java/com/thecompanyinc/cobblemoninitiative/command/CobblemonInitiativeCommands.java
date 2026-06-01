package com.thecompanyinc.cobblemoninitiative.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.Arrays;
import java.util.List;
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
          )
        )
    );

    dispatcher.register(
      Commands.literal("ca")
        .requires(source -> source.hasPermission(2))
        .redirect(dispatcher.getRoot().getChild("cobblemon-initiative"))
    );
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
}

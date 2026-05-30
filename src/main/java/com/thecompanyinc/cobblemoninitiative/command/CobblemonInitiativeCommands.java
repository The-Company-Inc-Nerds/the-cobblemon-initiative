package com.thecompanyinc.cobblemoninitiative.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.AchievementsInit;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
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
          Commands.literal("spawn").then(
            Commands.argument("trainer", StringArgumentType.string())
              .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                  AchievementsInit.getConfigLoader().getTrainerIds(),
                  builder
                )
              )
              .executes(CobblemonInitiativeCommands::spawnTrainer)
          )
        )
        .then(
          Commands.literal("autospawn")
            .then(
              Commands.argument("group", StringArgumentType.string())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(
                    AchievementsInit.getConfigLoader().getAvailableGroups(),
                    builder
                  )
                )
                .executes(CobblemonInitiativeCommands::autospawnGroup)
            )
            .then(
              Commands.literal("all").executes(
                CobblemonInitiativeCommands::autospawnAll
              )
            )
        )
        .then(
          Commands.literal("despawn")
            .then(
              Commands.argument("group", StringArgumentType.string())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(
                    AchievementsInit.getConfigLoader().getAvailableGroups(),
                    builder
                  )
                )
                .executes(CobblemonInitiativeCommands::despawnGroup)
            )
            .then(
              Commands.literal("all").executes(
                CobblemonInitiativeCommands::despawnAll
              )
            )
        )
        .then(
          Commands.literal("battle").then(
            Commands.argument("trainer", StringArgumentType.string())
              .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                  AchievementsInit.getConfigLoader().getTrainerIds(),
                  builder
                )
              )
              .executes(CobblemonInitiativeCommands::startBattle)
          )
        )
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
                  AchievementsInit.getConfigLoader().getTrainerIds(),
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
                  AchievementsInit.getConfigLoader().getTrainerIds(),
                  builder
                )
              )
              .executes(CobblemonInitiativeCommands::markDefeated)
          )
        )
    );

    dispatcher.register(
      Commands.literal("ca")
        .requires(source -> source.hasPermission(2))
        .redirect(dispatcher.getRoot().getChild("cobblemon-initiative"))
    );
  }

  private static int autospawnGroup(
    CommandContext<CommandSourceStack> context
  ) {
    CommandSourceStack source = context.getSource();
    String group = StringArgumentType.getString(context, "group");

    List<TrainerConfig> trainers =
      AchievementsInit.getConfigLoader().getTrainersByGroup(group);

    if (trainers.isEmpty()) {
      source.sendFailure(
        Component.literal("§cNo trainers found for group: " + group)
      );
      return 0;
    }

    int spawned = 0;
    int skipped = 0;

    for (TrainerConfig trainer : trainers) {
      int[] coords = trainer.getCoordinates();
      if (coords == null || coords.length != 3) {
        AchievementsInit.LOGGER.warn(
          "Trainer {} has no valid coordinates, skipping",
          trainer.getId()
        );
        skipped++;
        continue;
      }

      String spawnCommand = String.format(
        "give @p rctmod:trainer_spawner[minecraft:custom_name='{\"text\":\"%s Spawner\"}',minecraft:block_entity_data={id:\"rctmod:trainer_spawner\",TrainerIds:[\"%s\"]}]",
        trainer.getDisplayName(),
        trainer.getId()
      );

      source
        .getServer()
        .getCommands()
        .performPrefixedCommand(source.withPermission(4), spawnCommand);

      spawned++;
      AchievementsInit.LOGGER.info(
        "Autospawned {} at {}, {}, {}",
        trainer.getId(),
        coords[0],
        coords[1],
        coords[2]
      );
    }

    final int finalSpawned = spawned;
    final int finalSkipped = skipped;
    source.sendSuccess(
      () ->
        Component.literal(
          "§aAutospawned §e" +
            finalSpawned +
            "§a trainers from group §e" +
            group +
            (finalSkipped > 0
              ? " §7(" + finalSkipped + " skipped - no coordinates)"
              : "")
        ),
      true
    );

    return spawned;
  }

  private static int autospawnAll(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();

    int spawned = 0;
    int skipped = 0;

    for (TrainerConfig trainer : AchievementsInit.getConfigLoader().getAllTrainers()) {
      int[] coords = trainer.getCoordinates();
      if (coords == null || coords.length != 3) {
        skipped++;
        continue;
      }

      String spawnCommand = String.format(
        "rctmod trainer summon_persistent %s %d %d %d {HomePos:[I;%d,%d,%d]}",
        trainer.getId(),
        coords[0],
        coords[1],
        coords[2],
        coords[0],
        coords[1],
        coords[2]
      );

      source
        .getServer()
        .getCommands()
        .performPrefixedCommand(source.withPermission(4), spawnCommand);

      spawned++;
    }

    final int finalSpawned = spawned;
    final int finalSkipped = skipped;
    source.sendSuccess(
      () ->
        Component.literal(
          "§aAutospawned §e" +
            finalSpawned +
            "§a trainers" +
            (finalSkipped > 0
              ? " §7(" + finalSkipped + " skipped - no coordinates)"
              : "")
        ),
      true
    );

    return spawned;
  }

  private static int despawnGroup(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    String group = StringArgumentType.getString(context, "group");

    List<TrainerConfig> trainers =
      AchievementsInit.getConfigLoader().getTrainersByGroup(group);

    if (trainers.isEmpty()) {
      source.sendFailure(
        Component.literal("§cNo trainers found for group: " + group)
      );
      return 0;
    }

    int despawned = 0;

    for (TrainerConfig trainer : trainers) {
      String despawnCommand = String.format(
        "rctmod trainer remove %s",
        trainer.getId()
      );

      source
        .getServer()
        .getCommands()
        .performPrefixedCommand(source.withPermission(4), despawnCommand);

      despawned++;
    }

    final int finalDespawned = despawned;
    source.sendSuccess(
      () ->
        Component.literal(
          "§aDespawned §e" +
            finalDespawned +
            "§a trainers from group §e" +
            group
        ),
      true
    );

    return despawned;
  }

  private static int despawnAll(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();

    source
      .getServer()
      .getCommands()
      .performPrefixedCommand(
        source.withPermission(4),
        "rctmod trainer removeall"
      );

    source.sendSuccess(
      () -> Component.literal("§aDespawned all RCT trainers"),
      true
    );

    return 1;
  }

  private static int listGroups(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();

    source.sendSuccess(
      () -> Component.literal("§6=== Available Groups ==="),
      false
    );

    for (String group : AchievementsInit.getConfigLoader().getAvailableGroups()) {
      List<TrainerConfig> trainers =
        AchievementsInit.getConfigLoader().getTrainersByGroup(group);
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

  private static int spawnTrainer(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    TrainerConfig trainer = AchievementsInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    int x = player.getBlockX();
    int y = player.getBlockY();
    int z = player.getBlockZ();

    String spawnCommand = String.format(
      "rctmod trainer summon_persistent %s %d %d %d {HomePos:[I;%d,%d,%d]}",
      trainerId,
      x,
      y,
      z,
      x,
      y,
      z
    );

    AchievementsInit.LOGGER.info("Executing spawn command: {}", spawnCommand);

    source
      .getServer()
      .getCommands()
      .performPrefixedCommand(source.withPermission(4), spawnCommand);

    source.sendSuccess(
      () ->
        Component.literal(
          "§aSpawned trainer §e" +
            trainer.getDisplayName() +
            " §aat your location!"
        ),
      true
    );

    return 1;
  }

  private static int startBattle(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    TrainerConfig trainer = AchievementsInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    if (
      !AchievementsInit.getProgressManager().canBattleTrainer(
        player,
        trainerId
      )
    ) {
      List<String> missing =
        AchievementsInit.getProgressManager().getMissingPrerequisites(
          player,
          trainerId
        );
      source.sendFailure(
        Component.literal("§cYou must defeat the following trainers first:")
      );
      for (String prereq : missing) {
        source.sendFailure(Component.literal("§7- " + prereq));
      }
      return 0;
    }

    int x = player.getBlockX() + 2;
    int y = player.getBlockY();
    int z = player.getBlockZ();

    String spawnCommand = String.format(
      "rctmod trainer summon %s %d %d %d",
      trainerId,
      x,
      y,
      z
    );

    source
      .getServer()
      .getCommands()
      .performPrefixedCommand(source.withPermission(4), spawnCommand);

    source.sendSuccess(
      () ->
        Component.literal(
          "§aSpawned §e" +
            trainer.getDisplayName() +
            "§a! Right-click to battle."
        ),
      false
    );

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
      AchievementsInit.getProgressManager().getProgress(player);

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
    String[] gymLeaders = {
      "hua_zhan_leader",
      "takehara_leader",
      "mystic_leader",
      "deepcore_leader",
      "gaviota_leader",
      "kalahar_leader",
      "cyber_leader",
      "ryujin_leader",
      "nifl_leader",
      "scorchspire_leader",
    };

    int badges = 0;
    for (String leader : gymLeaders) {
      if (progress.hasDefeatedTrainer(leader)) {
        badges++;
      }
    }
    final int finalBadges = badges;
    source.sendSuccess(
      () -> Component.literal("§eBadges: §f" + finalBadges + "/10"),
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

    int levelCap = AchievementsInit.getLevelCapManager().getLevelCap(player);
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
      AchievementsInit.getProgressManager().getProgress(player);
    progress.getDefeatedTrainers().clear();
    progress.getEarnedAchievements().clear();
    progress.setCurrentLevelCap(20);

    if (player.getServer() != null) {
      AchievementsInit.getProgressManager().saveProgress(player.getServer());
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

    TrainerConfig trainer = AchievementsInit.getConfigLoader().getTrainer(
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
          AchievementsInit.getConfigLoader().getTrainer(prereq);
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

    for (TrainerConfig trainer : AchievementsInit.getConfigLoader().getAllTrainers()) {
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

    TrainerConfig trainer = AchievementsInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    AchievementsInit.getProgressManager().onTrainerDefeated(
      player,
      trainerId
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§aMarked §e" + trainer.getDisplayName() + " §aas defeated!"
        ),
      true
    );

    return 1;
  }
}

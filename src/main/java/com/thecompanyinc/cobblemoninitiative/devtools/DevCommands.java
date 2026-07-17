package com.thecompanyinc.cobblemoninitiative.devtools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The {@code /cobblemon-initiative dev …} subtree — every dev-only command in one
 * registration (strips with the devtools package at 1.0.0, TODO §2). Brigadier merges the
 * re-registered {@code cobblemon-initiative} root literal into the shipping tree (the same
 * pattern DevNoteCommand has always used), so the {@code /ca} alias reaches these too.
 *
 * <p>Handlers for goto/badges/grant/kit moved verbatim from CobblemonInitiativeCommands
 * (2026-07-11 consolidation); team/stage/place delegate to
 * {@link DevTestManager}/{@link DevPlaceManager}.
 */
public final class DevCommands {

  private DevCommands() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative").then(
        Commands.literal("dev")
          .requires(source -> source.hasPermission(2))
          .then(
            Commands.literal("goto").then(
              Commands.argument("trainer", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(trainerIdsWithCoords(), builder)
                )
                .executes(DevCommands::devGoto)
            )
          )
          .then(
            Commands.literal("badges").then(
              Commands.argument("count", IntegerArgumentType.integer(0, 10))
                .executes(DevCommands::devBadges)
            )
          )
          .then(
            Commands.literal("grant").then(
              Commands.argument("achievement", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(badgeAchievementIds(), builder)
                )
                .executes(DevCommands::devGrant)
            )
          )
          .then(
            Commands.literal("kit").executes(DevCommands::devKit)
          )
          // Vanilla-A* route probe for the client driver (see PathProbe).
          .then(
            Commands.literal("path").then(
              Commands.argument("player", EntityArgument.player()).then(
                Commands.argument("target", BlockPosArgument.blockPos())
                  .executes(ctx -> PathProbe.cmdPath(
                    ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "player"),
                    BlockPosArgument.getLoadedBlockPos(ctx, "target")))
              )
            )
          )
          // THE PRODUCER'S TOOL — one item for the whole marking walk (see DevWandTool).
          .then(
            Commands.literal("tool").executes(ctx -> withPlayer(ctx, DevWandTool::cmdTool))
          )
          // Test harness: bank party → bundled counter team / one-shot stage setup.
          .then(
            Commands.literal("team").then(
              Commands.argument("stage", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(DevTestManager.stageIds(), builder)
                )
                .executes(ctx -> withPlayer(ctx, p ->
                  DevTestManager.giveTeam(p, StringArgumentType.getString(ctx, "stage"))))
            )
          )
          .then(
            Commands.literal("stage").then(
              Commands.argument("stage", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(DevTestManager.stageIds(), builder)
                )
                .executes(ctx -> withPlayer(ctx, p ->
                  DevTestManager.applyStage(p, StringArgumentType.getString(ctx, "stage"))))
            )
          )
          // Guided placement walk: tp through proposals, record placements/adoptions.
          .then(
            Commands.literal("place")
              .then(Commands.literal("list").executes(ctx -> withPlayer(ctx, DevPlaceManager::cmdList)))
              .then(Commands.literal("next").executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdNext(p, true))))
              .then(Commands.literal("prev").executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdNext(p, false))))
              .then(Commands.literal("export").executes(ctx -> withPlayer(ctx, DevPlaceManager::cmdExport)))
              .then(Commands.literal("goto").then(placeIdArg().executes(
                ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdGoto(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("here")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdHere(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdHere(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("adopt")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdAdopt(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdAdopt(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("skip")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdSkip(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdSkip(p, StringArgumentType.getString(ctx, "id"))))))
          )
      )
    );
  }

  // ---------------------------------------------------------------------------
  // Handlers (moved verbatim from CobblemonInitiativeCommands)
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // Plumbing
  // ---------------------------------------------------------------------------

  /** Plan-id argument with suggestions from the bundled placement plan. */
  private static RequiredArgumentBuilder<CommandSourceStack, String> placeIdArg() {
    return Commands.argument("id", StringArgumentType.word())
      .suggests((context, builder) ->
        SharedSuggestionProvider.suggest(
          DevPlaceManager.planIds(context.getSource().getServer()), builder)
      );
  }

  /** Run a player-only dev handler, failing cleanly from a non-player source. */
  private static int withPlayer(
    CommandContext<CommandSourceStack> ctx, java.util.function.ToIntFunction<ServerPlayer> fn
  ) {
    ServerPlayer p = ctx.getSource().getPlayer();
    if (p == null) {
      ctx.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return fn.applyAsInt(p);
  }
}

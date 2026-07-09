package com.thecompanyinc.cobblemoninitiative.noble;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Admin command tree for noble encounters: {@code /noble start|stop|list}. */
public final class NobleCommands {

  private NobleCommands() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("noble")
        .requires(source -> source.hasPermission(2))
        .then(Commands.literal("start")
          .then(Commands.argument("noble", StringArgumentType.word())
            .suggests((ctx, builder) ->
              SharedSuggestionProvider.suggest(Arrays.asList(NobleEncounterInit.getManager().getNobleIds()), builder))
            .executes(NobleCommands::start)))
        .then(Commands.literal("stop").executes(NobleCommands::stop))
        .then(Commands.literal("list").executes(NobleCommands::list))
    );
  }

  private static int start(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    String id = StringArgumentType.getString(ctx, "noble");
    return NobleEncounterInit.getManager().start(player, id) ? 1 : 0;
  }

  private static int stop(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    NobleEncounterInit.getManager().abort(player);
    return 1;
  }

  private static int list(CommandContext<CommandSourceStack> ctx) {
    String ids = String.join(", ", NobleEncounterInit.getManager().getNobleIds());
    ctx.getSource().sendSystemMessage(Component.literal("§6Nobles: §e" + ids));
    return 1;
  }
}

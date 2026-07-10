package com.thecompanyinc.cobblemoninitiative.cutscene;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /cutscene play <id>|stop|list} (OP 2 — the trigger surface for dialog buttons via
 * as_player, and for admins), plus the player-facing {@code /cutscene-skip} (no OP — bind a
 * key to it client-side).
 */
public final class CutsceneCommands {

  private CutsceneCommands() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("cutscene")
        .requires(source -> source.hasPermission(2))
        .then(Commands.literal("play")
          .then(Commands.argument("id", StringArgumentType.word())
            .executes(CutsceneCommands::play)))
        .then(Commands.literal("stop").executes(CutsceneCommands::skip))
        .then(Commands.literal("list").executes(CutsceneCommands::list))
    );
    dispatcher.register(
      Commands.literal("cutscene-skip").executes(CutsceneCommands::skip)
    );
  }

  private static int play(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    String id = StringArgumentType.getString(ctx, "id");
    return CutsceneInit.getManager().play(player, id) ? 1 : 0;
  }

  private static int skip(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    CutsceneInit.getManager().skip(player);
    return 1;
  }

  private static int list(CommandContext<CommandSourceStack> ctx) {
    String ids = String.join(", ", CutsceneInit.getManager().getLoadedIds());
    ctx.getSource().sendSystemMessage(Component.literal(
      "§6Loaded cutscenes: §e" + (ids.isEmpty() ? "(none cached yet — scenes load lazily by id)" : ids)));
    return 1;
  }
}

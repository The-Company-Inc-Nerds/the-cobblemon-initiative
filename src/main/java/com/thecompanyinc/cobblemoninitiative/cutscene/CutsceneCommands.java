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
            .executes(CutsceneCommands::play)
            // Optional chain: a command run AS the player when the scene ends or is
            // skipped (the gym-leader intros pass their engage function here).
            .then(Commands.argument("end", StringArgumentType.greedyString())
              .executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayer();
                if (p == null) return 0;
                return CutsceneInit.getManager().play(p,
                  StringArgumentType.getString(ctx, "id"),
                  StringArgumentType.getString(ctx, "end")) ? 1 : 0;
              }))))
        .then(Commands.literal("stop").executes(CutsceneCommands::skip))
        .then(Commands.literal("list").executes(CutsceneCommands::list))
        // reload — drop the script cache so edited override files re-read on next play.
        .then(Commands.literal("reload").executes(ctx -> {
          int n = CutsceneInit.getManager().reloadScripts();
          ctx.getSource().sendSuccess(() -> Component.literal(
            "§a[Cutscene] §7Cache cleared (" + n + " scene(s)) — overrides re-read on next play."), false);
          return 1;
        }))
        // record — fly the path yourself and capture it as a playable scene.
        .then(Commands.literal("record")
          .then(Commands.literal("add").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayer();
            return p != null ? CutsceneRecorder.add(p) : 0;
          }))
          .then(Commands.literal("undo").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayer();
            return (p != null && CutsceneRecorder.undo(p)) ? 1 : 0;
          }))
          .then(Commands.literal("clear").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayer();
            if (p != null) CutsceneRecorder.clear(p);
            return 1;
          }))
          .then(Commands.literal("status").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayer();
            if (p != null) CutsceneRecorder.status(p);
            return 1;
          }))
          .then(Commands.literal("save")
            .then(Commands.argument("id", StringArgumentType.word())
              .executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayer();
                return (p != null && CutsceneRecorder.save(p, StringArgumentType.getString(ctx, "id"))) ? 1 : 0;
              }))))
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

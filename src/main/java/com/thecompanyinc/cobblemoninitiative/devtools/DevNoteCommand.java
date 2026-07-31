package com.thecompanyinc.cobblemoninitiative.devtools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Dev-only, under the {@code /ca-dev} root: {@code /ca-dev pos …}, {@code /ca-dev npcnote
 * stick|note|move|log|clear|undo}, and {@code /ca-dev debug victini}. (The {@code smoke}
 * checklist was retired 2026-07-31.) See {@link DevNoteInit} for the whack / right-click workflow.
 */
public final class DevNoteCommand {

  private DevNoteCommand() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    // Quick position capture: /cobblemon-initiative pos [ "title" [note...] ]
    dispatcher.register(
      Commands.literal("ca-dev")
        .requires(src -> src.hasPermission(2))
        .then(
        Commands.literal("pos")
          .requires(src -> src.hasPermission(2))
          .executes(ctx -> {
            DevNoteInit.addPosition(ctx.getSource().getPlayerOrException(), null, null);
            return 1;
          })
          .then(Commands.argument("title", StringArgumentType.string())
            .executes(ctx -> {
              DevNoteInit.addPosition(ctx.getSource().getPlayerOrException(),
                StringArgumentType.getString(ctx, "title"), null);
              return 1;
            })
            .then(Commands.argument("note", StringArgumentType.greedyString())
              .executes(ctx -> {
                DevNoteInit.addPosition(ctx.getSource().getPlayerOrException(),
                  StringArgumentType.getString(ctx, "title"),
                  StringArgumentType.getString(ctx, "note"));
                return 1;
              })))
      )
    );

    dispatcher.register(
      Commands.literal("ca-dev")
        .requires(src -> src.hasPermission(2))
        .then(
        Commands.literal("npcnote")
          .requires(src -> src.hasPermission(2))
          .then(Commands.literal("stick").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            p.getInventory().add(DevNoteInit.makeNoter());
            p.sendSystemMessage(Component.literal(
              "§bNoter §7given. Whack an NPC to select it, then note/move/log."));
            return 1;
          }))
          .then(Commands.literal("note")
            .then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
              ServerPlayer p = ctx.getSource().getPlayerOrException();
              if (!DevNoteInit.addComment(p, StringArgumentType.getString(ctx, "text"))) {
                p.sendSystemMessage(Component.literal("§cNo NPC selected — whack one first."));
                return 0;
              }
              return 1;
            })))
          .then(Commands.literal("move").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            if (!DevNoteInit.setNewPos(p, p.getX(), p.getY(), p.getZ())) {
              p.sendSystemMessage(Component.literal("§cNo NPC selected — whack one first."));
              return 0;
            }
            return 1;
          }))
          .then(Commands.literal("log").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            return DevNoteInit.logToChat(p);
          }))
          .then(Commands.literal("undo").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            p.sendSystemMessage(Component.literal(
              DevNoteInit.undoLast(p) ? "§bNoter §7removed the last note." : "§7No notes to undo."));
            return 1;
          }))
          .then(Commands.literal("clear").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            int n = DevNoteInit.clearNotes(p);
            p.sendSystemMessage(Component.literal("§bNoter §7cleared " + n + " note(s)."));
            return 1;
          }))
      )
    );

    // Debug readouts: /ca-dev debug victini
    dispatcher.register(
      Commands.literal("ca-dev")
        .requires(src -> src.hasPermission(2))
        .then(
        Commands.literal("debug")
          .requires(src -> src.hasPermission(2))
          .then(Commands.literal("victini").executes(ctx -> {
            DevNoteInit.victiniStatus(ctx.getSource().getPlayerOrException());
            return 1;
          }))
      )
    );
  }
}

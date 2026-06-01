package com.thecompanyinc.cobblemoninitiative.npcsight;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.thecompanyinc.cobblemoninitiative.util.EntityLookup;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class NpcSightCommand {

  private static NpcSightManager manager;
  private static NpcSightStorage storage;

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher,
    CommandBuildContext buildContext,
    NpcSightManager mgr,
    NpcSightStorage stor
  ) {
    manager = mgr;
    storage = stor;

    dispatcher.register(
      Commands.literal("npcsight")
        .requires(src -> src.hasPermission(2))

        // /npcsight add <uuid> [range] [dialog]
        .then(
          Commands.literal("add")
            .then(
              Commands.argument("uuid", StringArgumentType.word())
                .suggests(NpcSightCommand::suggestLookedAt)
                .executes(ctx -> cmdAdd(ctx,
                  StringArgumentType.getString(ctx, "uuid"),
                  -1, null))
                .then(
                  Commands.argument("range", IntegerArgumentType.integer(1, 512))
                    .executes(ctx -> cmdAdd(ctx,
                      StringArgumentType.getString(ctx, "uuid"),
                      IntegerArgumentType.getInteger(ctx, "range"), null))
                    .then(
                      Commands.argument("dialog", StringArgumentType.word())
                        .executes(ctx -> cmdAdd(ctx,
                          StringArgumentType.getString(ctx, "uuid"),
                          IntegerArgumentType.getInteger(ctx, "range"),
                          StringArgumentType.getString(ctx, "dialog")))
                    )
                )
            )
        )

        // /npcsight remove <uuid>
        .then(
          Commands.literal("remove")
            .then(
              Commands.argument("uuid", StringArgumentType.word())
                .suggests(NpcSightCommand::suggestRegistered)
                .executes(ctx -> cmdRemove(ctx,
                  StringArgumentType.getString(ctx, "uuid")))
            )
        )

        // /npcsight range <uuid> <blocks>
        .then(
          Commands.literal("range")
            .then(
              Commands.argument("uuid", StringArgumentType.word())
                .suggests(NpcSightCommand::suggestRegistered)
                .then(
                  Commands.argument("blocks", IntegerArgumentType.integer(-1, 512))
                    .executes(ctx -> cmdSetRange(ctx,
                      StringArgumentType.getString(ctx, "uuid"),
                      IntegerArgumentType.getInteger(ctx, "blocks")))
                )
            )
        )

        // /npcsight dialog <uuid> <name|clear>
        .then(
          Commands.literal("dialog")
            .then(
              Commands.argument("uuid", StringArgumentType.word())
                .suggests(NpcSightCommand::suggestRegistered)
                .then(
                  Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> cmdSetDialog(ctx,
                      StringArgumentType.getString(ctx, "uuid"),
                      StringArgumentType.getString(ctx, "name")))
                )
            )
        )

        // /npcsight list
        .then(
          Commands.literal("list")
            .executes(NpcSightCommand::cmdList)
        )

        // /npcsight info <uuid>
        .then(
          Commands.literal("info")
            .then(
              Commands.argument("uuid", StringArgumentType.word())
                .suggests(NpcSightCommand::suggestRegistered)
                .executes(ctx -> cmdInfo(ctx,
                  StringArgumentType.getString(ctx, "uuid")))
            )
        )

        // /npcsight reload
        .then(
          Commands.literal("reload")
            .requires(src -> src.hasPermission(3))
            .executes(NpcSightCommand::cmdReload)
        )
    );
  }

  // ---------------------------------------------------------------------------
  // Command handlers
  // ---------------------------------------------------------------------------

  private static int cmdAdd(
    CommandContext<CommandSourceStack> ctx,
    String uuidStr,
    int range,
    String dialog
  ) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    if (storage.contains(uuid)) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Sight] UUID already registered: " + uuidStr)
      );
      return 0;
    }

    NpcSightData data = new NpcSightData(uuid, range, dialog);
    storage.put(data);

    ctx.getSource().sendSuccess(
      () -> Component.literal(
        "[NPC Sight] Registered " + uuidStr
          + " | range=" + (range < 0 ? "default" : range)
          + " | dialog=" + (dialog == null ? "none" : dialog)
      ), true
    );
    return 1;
  }

  private static int cmdRemove(CommandContext<CommandSourceStack> ctx, String uuidStr) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    if (storage.remove(uuid)) {
      ctx.getSource().sendSuccess(
        () -> Component.literal("[NPC Sight] Unregistered " + uuidStr), true
      );
      return 1;
    }
    ctx.getSource().sendFailure(
      Component.literal("[NPC Sight] UUID not found: " + uuidStr)
    );
    return 0;
  }

  private static int cmdSetRange(
    CommandContext<CommandSourceStack> ctx, String uuidStr, int blocks
  ) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    NpcSightData data = storage.get(uuid);
    if (data == null) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Sight] UUID not registered: " + uuidStr)
      );
      return 0;
    }

    data.sightRange = blocks;
    storage.put(data);

    String display = blocks < 0 ? "global default" : blocks + " blocks";
    ctx.getSource().sendSuccess(
      () -> Component.literal("[NPC Sight] " + uuidStr + " range → " + display),
      true
    );
    return 1;
  }

  private static int cmdSetDialog(
    CommandContext<CommandSourceStack> ctx, String uuidStr, String name
  ) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    NpcSightData data = storage.get(uuid);
    if (data == null) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Sight] UUID not registered: " + uuidStr)
      );
      return 0;
    }

    boolean clearing = name.equalsIgnoreCase("clear") || name.equalsIgnoreCase("none");
    data.dialogName = clearing ? null : name;
    storage.put(data);

    String display = clearing ? "disabled" : name;
    ctx.getSource().sendSuccess(
      () -> Component.literal("[NPC Sight] " + uuidStr + " dialog → " + display),
      true
    );
    return 1;
  }

  private static int cmdList(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendSuccess(
        () -> Component.literal("[NPC Sight] No NPCs registered."), false
      );
      return 0;
    }

    StringBuilder sb = new StringBuilder("[NPC Sight] Registered NPCs:\n");
    for (NpcSightData d : storage.getAll()) {
      sb.append("  ").append(d.uuid)
        .append(" | range=").append(d.sightRange < 0 ? "default" : d.sightRange)
        .append(" | dialog=").append(d.dialogName == null ? "none" : d.dialogName)
        .append(" | seeing=").append(d.canSeePlayer)
        .append("\n");
    }
    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return storage.size();
  }

  private static int cmdInfo(
    CommandContext<CommandSourceStack> ctx, String uuidStr
  ) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    NpcSightData data = storage.get(uuid);
    if (data == null) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Sight] UUID not registered: " + uuidStr)
      );
      return 0;
    }

    String info = String.format(
      "[NPC Sight] %s\n  range=%s\n  dialog=%s\n  canSeePlayer=%b",
      data.uuid,
      data.sightRange < 0 ? "default (" + manager.getConfig().getDefaultSightRange() + ")" : data.sightRange,
      data.dialogName == null ? "none" : data.dialogName,
      data.canSeePlayer
    );
    ctx.getSource().sendSuccess(() -> Component.literal(info), false);
    return 1;
  }

  private static int cmdReload(CommandContext<CommandSourceStack> ctx) {
    NpcSightConfig newConfig = NpcSightConfig.load();
    manager.reloadConfig(newConfig);
    ctx.getSource().sendSuccess(
      () -> Component.literal("[NPC Sight] Config reloaded."), true
    );
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Suggestions
  // ---------------------------------------------------------------------------

  /**
   * Suggests the UUID of the entity the player is currently looking at
   * (within 10 blocks), with the entity's display name as tooltip.
   */
  private static CompletableFuture<Suggestions> suggestLookedAt(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    try {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      Entity target = EntityLookup.getEntityLookedAt(player, 10.0);
      if (target != null) {
        builder.suggest(
          target.getUUID().toString(),
          target.getDisplayName()
        );
      }
    } catch (Exception ignored) {}
    return builder.buildFuture();
  }

  /** Suggests UUIDs already registered in storage. */
  private static CompletableFuture<Suggestions> suggestRegistered(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (NpcSightData data : storage.getAll()) {
      builder.suggest(data.uuid.toString());
    }
    return builder.buildFuture();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static UUID parseUUID(CommandContext<CommandSourceStack> ctx, String str) {
    try {
      return UUID.fromString(str);
    } catch (IllegalArgumentException e) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Sight] Invalid UUID: " + str)
      );
      return null;
    }
  }

}

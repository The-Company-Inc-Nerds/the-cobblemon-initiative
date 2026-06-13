package com.thecompanyinc.cobblemoninitiative.fieldmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * /cobblemon-initiative field-mark — dev tool to capture Wheat War liberation
 * field locations in-world. Stand at a field's center and 'add' it; tune
 * radius/setpiece; 'export' dumps a fields.json fragment to the server log.
 *
 * <p>Dev-only — remove before final release (see the dev-only cleanup checklist).
 */
public class FieldMarkCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** Region slugs matching the Wheat War economy (wheat_price_<region>). */
  private static final String[] REGIONS = {
    "takehara", "hua_zhan", "mystic_marsh", "deepcore", "gaviota",
    "kalahar", "cyber", "ryujin", "nifl", "scorchspire"
  };

  private static FieldMarkStorage storage;

  // ---------------------------------------------------------------------------
  // Registration
  // ---------------------------------------------------------------------------

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher,
    CommandBuildContext buildContext,
    FieldMarkStorage stor
  ) {
    storage = stor;

    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .then(
          Commands.literal("field-mark")
            .requires(src -> src.hasPermission(2))

            // add <id> <region> — capture the player's current position as the field center
            .then(Commands.literal("add")
              .then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("region", StringArgumentType.word())
                  .suggests(FieldMarkCommand::suggestRegions)
                  .executes(ctx -> cmdAdd(ctx,
                    StringArgumentType.getString(ctx, "id"),
                    StringArgumentType.getString(ctx, "region"))))))

            // radius <id> <blocks>
            .then(Commands.literal("radius")
              .then(Commands.argument("id", StringArgumentType.word())
                .suggests(FieldMarkCommand::suggestIds)
                .then(Commands.argument("blocks", IntegerArgumentType.integer(1, 256))
                  .executes(ctx -> cmdRadius(ctx,
                    StringArgumentType.getString(ctx, "id"),
                    IntegerArgumentType.getInteger(ctx, "blocks"))))))

            // setpiece <id> <true|false>  (false = scattered minor field, bounty only)
            .then(Commands.literal("setpiece")
              .then(Commands.argument("id", StringArgumentType.word())
                .suggests(FieldMarkCommand::suggestIds)
                .then(Commands.argument("value", BoolArgumentType.bool())
                  .executes(ctx -> cmdSetpiece(ctx,
                    StringArgumentType.getString(ctx, "id"),
                    BoolArgumentType.getBool(ctx, "value"))))))

            // list
            .then(Commands.literal("list")
              .executes(FieldMarkCommand::cmdList))

            // delete <id>
            .then(Commands.literal("delete")
              .then(Commands.argument("id", StringArgumentType.word())
                .suggests(FieldMarkCommand::suggestIds)
                .executes(ctx -> cmdDelete(ctx, StringArgumentType.getString(ctx, "id")))))

            // export — all marked fields as a JSON fragment (to the server log)
            .then(Commands.literal("export")
              .executes(FieldMarkCommand::cmdExport))
        )
    );
  }

  // ---------------------------------------------------------------------------
  // Command handlers
  // ---------------------------------------------------------------------------

  private static int cmdAdd(CommandContext<CommandSourceStack> ctx, String id, String region) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    FieldMarkStorage.FieldEntry entry = new FieldMarkStorage.FieldEntry();
    entry.id = id;
    entry.region = region.toLowerCase();
    entry.dimension = player.level().dimension().location().toString();
    entry.centerX = (int) Math.floor(player.getX());
    entry.centerY = (int) Math.floor(player.getY());
    entry.centerZ = (int) Math.floor(player.getZ());
    storage.put(entry);

    ctx.getSource().sendSuccess(() -> Component.literal(
      "[FieldMark] Marked '" + entry.id + "' (" + entry.region + ") at "
      + entry.centerX + " " + entry.centerY + " " + entry.centerZ
      + "  r" + entry.radius + "  setpiece=" + entry.setpiece + ".\n"
      + "  Tune with 'field-mark radius/setpiece " + entry.id + " ...', then 'field-mark export'."
    ), false);
    return 1;
  }

  private static int cmdRadius(CommandContext<CommandSourceStack> ctx, String id, int radius) {
    FieldMarkStorage.FieldEntry e = storage.get(id);
    if (e == null) return notFound(ctx, id);
    e.radius = radius;
    storage.put(e);
    ctx.getSource().sendSuccess(() -> Component.literal("[FieldMark] " + id + " radius = " + radius), false);
    return 1;
  }

  private static int cmdSetpiece(CommandContext<CommandSourceStack> ctx, String id, boolean value) {
    FieldMarkStorage.FieldEntry e = storage.get(id);
    if (e == null) return notFound(ctx, id);
    e.setpiece = value;
    storage.put(e);
    ctx.getSource().sendSuccess(() -> Component.literal("[FieldMark] " + id + " setpiece = " + value), false);
    return 1;
  }

  private static int cmdList(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendSuccess(() -> Component.literal("[FieldMark] No marked fields."), false);
      return 0;
    }
    StringBuilder sb = new StringBuilder("[FieldMark] Marked fields (" + storage.size() + "):\n");
    for (FieldMarkStorage.FieldEntry e : storage.getAll()) {
      sb.append("  ").append(e.setpiece ? "[set-piece]" : "[minor]    ").append(" ")
        .append(e.id).append(" (").append(e.region).append(")  ")
        .append(e.centerX).append(" ").append(e.centerY).append(" ").append(e.centerZ)
        .append("  r").append(e.radius).append("\n");
    }
    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return storage.size();
  }

  private static int cmdDelete(CommandContext<CommandSourceStack> ctx, String id) {
    if (storage.remove(id)) {
      ctx.getSource().sendSuccess(() -> Component.literal("[FieldMark] Deleted '" + id + "'."), false);
      return 1;
    }
    return notFound(ctx, id);
  }

  private static int cmdExport(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendFailure(Component.literal("[FieldMark] No marked fields to export."));
      return 0;
    }

    JsonArray array = new JsonArray();
    for (FieldMarkStorage.FieldEntry e : storage.getAll()) {
      JsonObject o = new JsonObject();
      o.addProperty("id", e.id);
      o.addProperty("region", e.region);
      o.addProperty("dimension", e.dimension);
      o.addProperty("centerX", e.centerX);
      o.addProperty("centerY", e.centerY);
      o.addProperty("centerZ", e.centerZ);
      o.addProperty("radius", e.radius);
      o.addProperty("setpiece", e.setpiece);
      array.add(o);
    }

    String json = GSON.toJson(array);
    LOGGER.info("[FieldMark] Export ({} fields):\n{}", storage.size(), json);
    ctx.getSource().sendSuccess(() -> Component.literal(
      "[FieldMark] Exported " + storage.size() + " field(s) to the server log.\n"
      + "  Copy the JSON from the console into the liberation fields config."
    ), false);
    return storage.size();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> ctx) {
    try {
      return ctx.getSource().getPlayerOrException();
    } catch (Exception e) {
      ctx.getSource().sendFailure(Component.literal("[FieldMark] Must be run by a player."));
      return null;
    }
  }

  private static int notFound(CommandContext<CommandSourceStack> ctx, String id) {
    ctx.getSource().sendFailure(Component.literal("[FieldMark] No marked field with id '" + id + "'."));
    return 0;
  }

  private static CompletableFuture<Suggestions> suggestRegions(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (String r : REGIONS) builder.suggest(r);
    return builder.buildFuture();
  }

  private static CompletableFuture<Suggestions> suggestIds(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (FieldMarkStorage.FieldEntry e : storage.getAll()) builder.suggest(e.id);
    return builder.buildFuture();
  }
}

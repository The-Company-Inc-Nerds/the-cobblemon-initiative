package com.thecompanyinc.cobblemoninitiative.zonetrace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * /cobblemon-initiative zone-trace — development tool for tracing zone polygon
 * boundaries on the map. Right-click blocks with the Zone Tracer wand to record
 * vertices; finish to save into zone_trace.json for pasting into install.json.
 *
 * <p>These commands are <b>dev-only</b> and should be removed before final release.
 */
public class ZoneTraceCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  static final String TRACER_NAME = "Zone Tracer";
  private static final Map<UUID, ZoneTraceSession> sessions = new ConcurrentHashMap<>();
  private static ZoneTraceStorage storage;

  // ---------------------------------------------------------------------------
  // Registration
  // ---------------------------------------------------------------------------

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher,
    CommandBuildContext buildContext,
    ZoneTraceStorage stor
  ) {
    storage = stor;

    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .then(
          Commands.literal("zone-trace")
            .requires(src -> src.hasPermission(2))

            // begin <name>
            .then(Commands.literal("begin")
              .then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(ctx -> cmdBegin(ctx, StringArgumentType.getString(ctx, "name")))))

            // point  (manual fallback — records player foot position)
            .then(Commands.literal("point")
              .executes(ZoneTraceCommand::cmdPoint))

            // undo
            .then(Commands.literal("undo")
              .executes(ZoneTraceCommand::cmdUndo))

            // type <value>
            .then(Commands.literal("type")
              .then(Commands.argument("value", StringArgumentType.word())
                .suggests(ZoneTraceCommand::suggestTypes)
                .executes(ctx -> cmdType(ctx, StringArgumentType.getString(ctx, "value")))))

            // color <hex>
            .then(Commands.literal("color")
              .then(Commands.argument("hex", StringArgumentType.word())
                .executes(ctx -> cmdColor(ctx, StringArgumentType.getString(ctx, "hex")))))

            // subtitle <text>
            .then(Commands.literal("subtitle")
              .then(Commands.argument("text", StringArgumentType.greedyString())
                .executes(ctx -> cmdSubtitle(ctx, StringArgumentType.getString(ctx, "text")))))

            // announce <true|false>
            .then(Commands.literal("announce")
              .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(ctx -> cmdAnnounce(ctx, BoolArgumentType.getBool(ctx, "value")))))

            // hostile <true|false>
            .then(Commands.literal("hostile")
              .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(ctx -> cmdHostile(ctx, BoolArgumentType.getBool(ctx, "value")))))

            // finish
            .then(Commands.literal("finish")
              .executes(ZoneTraceCommand::cmdFinish))

            // status
            .then(Commands.literal("status")
              .executes(ZoneTraceCommand::cmdStatus))

            // list
            .then(Commands.literal("list")
              .executes(ZoneTraceCommand::cmdList))

            // delete <name>
            .then(Commands.literal("delete")
              .then(Commands.argument("name", StringArgumentType.greedyString())
                .suggests(ZoneTraceCommand::suggestSaved)
                .executes(ctx -> cmdDelete(ctx, StringArgumentType.getString(ctx, "name")))))

            // export  (all saved zones as install.json fragment)
            .then(Commands.literal("export")
              .executes(ZoneTraceCommand::cmdExport))
        )
    );
  }

  /**
   * Registers the Zone Tracer wand right-click listener.
   * Must be called once during mod initialisation (not during command registration).
   */
  public static void registerItemListener() {
    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      if (world.isClientSide) return InteractionResult.PASS;
      if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

      ItemStack held = player.getItemInHand(hand);
      if (!isZoneTracer(held)) return InteractionResult.PASS;

      if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

      ZoneTraceSession session = sessions.get(sp.getUUID());
      if (session == null) {
        sp.sendSystemMessage(Component.literal(
          "[ZoneTrace] No active session — run /cobblemon-initiative zone-trace begin <name> first."
        ));
        return InteractionResult.SUCCESS; // cancel block interaction, inform player
      }

      BlockHitResult blockHit = (BlockHitResult) hitResult;
      int bx = blockHit.getBlockPos().getX();
      int bz = blockHit.getBlockPos().getZ();
      session.vertices.add(new int[]{bx, bz});

      sp.sendSystemMessage(Component.literal(
        "[ZoneTrace] " + session.name + " — vertex " + session.vertices.size()
        + ": (" + bx + ", " + bz + ")"
      ));
      return InteractionResult.SUCCESS; // prevent block from being opened/used
    });
  }

  // ---------------------------------------------------------------------------
  // Command handlers
  // ---------------------------------------------------------------------------

  private static int cmdBegin(CommandContext<CommandSourceStack> ctx, String name) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    if (sessions.containsKey(player.getUUID())) {
      ctx.getSource().sendFailure(Component.literal(
        "[ZoneTrace] Session already active for '" + sessions.get(player.getUUID()).name
        + "'. Run 'finish' or 'discard' first."
      ));
      return 0;
    }

    String dimension = player.level().dimension().location().toString();
    ZoneTraceSession session = new ZoneTraceSession(name, dimension);
    sessions.put(player.getUUID(), session);

    // Give the wand
    ItemStack wand = makeZoneTracer();
    if (!player.getInventory().add(wand)) {
      player.drop(wand, false);
    }

    ctx.getSource().sendSuccess(() -> Component.literal(
      "[ZoneTrace] Started tracing '" + name + "' in " + dimension + ".\n"
      + "  Right-click blocks with the Zone Tracer wand to add vertices.\n"
      + "  Use 'zone-trace type/color/subtitle' to set metadata.\n"
      + "  Run 'zone-trace finish' when done."
    ), false);
    return 1;
  }

  private static int cmdPoint(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    ZoneTraceSession session = sessions.get(player.getUUID());
    if (session == null) return noSession(ctx);

    int x = (int) Math.floor(player.getX());
    int z = (int) Math.floor(player.getZ());
    session.vertices.add(new int[]{x, z});
    session.centerY = (int) Math.floor(player.getY());

    ctx.getSource().sendSuccess(() -> Component.literal(
      "[ZoneTrace] " + session.name + " — vertex " + session.vertices.size()
      + ": (" + x + ", " + z + ")"
    ), false);
    return 1;
  }

  private static int cmdUndo(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    ZoneTraceSession session = sessions.get(player.getUUID());
    if (session == null) return noSession(ctx);

    if (session.vertices.isEmpty()) {
      ctx.getSource().sendFailure(Component.literal("[ZoneTrace] No vertices to undo."));
      return 0;
    }

    int[] removed = session.vertices.remove(session.vertices.size() - 1);
    ctx.getSource().sendSuccess(() -> Component.literal(
      "[ZoneTrace] Removed vertex (" + removed[0] + ", " + removed[1] + "). "
      + session.vertices.size() + " remaining."
    ), false);
    return 1;
  }

  private static int cmdType(CommandContext<CommandSourceStack> ctx, String value) {
    return withSession(ctx, session -> {
      session.type = value.toUpperCase();
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] type = " + session.type), false);
    });
  }

  private static int cmdColor(CommandContext<CommandSourceStack> ctx, String hex) {
    return withSession(ctx, session -> {
      session.color = hex.startsWith("#") ? hex : "#" + hex;
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] color = " + session.color), false);
    });
  }

  private static int cmdSubtitle(CommandContext<CommandSourceStack> ctx, String text) {
    return withSession(ctx, session -> {
      session.subtitle = text;
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] subtitle = " + text), false);
    });
  }

  private static int cmdAnnounce(CommandContext<CommandSourceStack> ctx, boolean value) {
    return withSession(ctx, session -> {
      session.announce = value;
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] announce = " + value), false);
    });
  }

  private static int cmdHostile(CommandContext<CommandSourceStack> ctx, boolean value) {
    return withSession(ctx, session -> {
      session.hostileOnly = value;
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] hostileOnly = " + value), false);
    });
  }

  private static int cmdFinish(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    ZoneTraceSession session = sessions.remove(player.getUUID());
    if (session == null) return noSession(ctx);

    if (session.vertices.size() < 3) {
      ctx.getSource().sendFailure(Component.literal(
        "[ZoneTrace] Need at least 3 vertices to form a polygon (have "
        + session.vertices.size() + ")."
      ));
      sessions.put(player.getUUID(), session); // put it back
      return 0;
    }

    // Convert session → storage entry
    ZoneTraceStorage.ZoneEntry entry = new ZoneTraceStorage.ZoneEntry();
    entry.name        = session.name;
    entry.subtitle    = session.subtitle;
    entry.type        = session.type;
    entry.dimension   = session.dimension;
    entry.announce    = session.announce;
    entry.color       = session.color;
    entry.hostileOnly = session.hostileOnly;
    entry.cylindrical = session.cylindrical;
    entry.centerY     = session.centerY;
    for (int[] v : session.vertices) {
      entry.vertices.add(new ZoneTraceStorage.ZoneEntry.Vertex(v[0], v[1]));
    }
    storage.put(entry);

    // Remove wand from player inventory
    removeZoneTracer(player);

    final int count = entry.vertices.size();
    ctx.getSource().sendSuccess(() -> Component.literal(
      "[ZoneTrace] Saved '" + entry.name + "' with " + count + " vertices.\n"
      + "  Run 'zone-trace export' to get the install.json fragment."
    ), false);
    return 1;
  }

  private static int cmdStatus(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;

    ZoneTraceSession session = sessions.get(player.getUUID());
    if (session == null) {
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] No active session."), false);
      return 0;
    }

    StringBuilder sb = new StringBuilder("[ZoneTrace] Active session: '" + session.name + "'\n");
    sb.append("  Type: ").append(session.type).append("  Color: ").append(session.color).append("\n");
    sb.append("  Announce: ").append(session.announce).append("  HostileOnly: ").append(session.hostileOnly).append("\n");
    sb.append("  Vertices (").append(session.vertices.size()).append("):\n");
    for (int i = 0; i < session.vertices.size(); i++) {
      int[] v = session.vertices.get(i);
      sb.append("    ").append(i + 1).append(". (").append(v[0]).append(", ").append(v[1]).append(")\n");
    }
    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return session.vertices.size();
  }

  private static int cmdList(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] No saved zones."), false);
      return 0;
    }

    StringBuilder sb = new StringBuilder("[ZoneTrace] Saved zones (" + storage.size() + "):\n");
    for (ZoneTraceStorage.ZoneEntry e : storage.getAll()) {
      sb.append("  [").append(e.type).append("] ").append(e.name)
        .append(" — ").append(e.vertices.size()).append(" vertices  ").append(e.color).append("\n");
    }
    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return storage.size();
  }

  private static int cmdDelete(CommandContext<CommandSourceStack> ctx, String name) {
    if (storage.remove(name)) {
      ctx.getSource().sendSuccess(() -> Component.literal("[ZoneTrace] Deleted '" + name + "'."), false);
      return 1;
    }
    ctx.getSource().sendFailure(Component.literal("[ZoneTrace] Zone not found: '" + name + "'"));
    return 0;
  }

  private static int cmdExport(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendFailure(Component.literal("[ZoneTrace] No saved zones to export."));
      return 0;
    }

    // Build a JSON array of objects matching the InstallZone format (with vertices).
    // Each entry omits centerX/Z/radius since InstallCommand derives them from vertices.
    com.google.gson.JsonArray array = new com.google.gson.JsonArray();
    for (ZoneTraceStorage.ZoneEntry e : storage.getAll()) {
      com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
      obj.addProperty("name",       e.name);
      obj.addProperty("subtitle",   e.subtitle);
      obj.addProperty("type",       e.type);
      obj.addProperty("centerY",    e.centerY);
      obj.addProperty("dimension",  e.dimension);
      obj.addProperty("hostileOnly", e.hostileOnly);
      obj.addProperty("cylindrical", e.cylindrical);
      obj.addProperty("announce",   e.announce);
      obj.addProperty("color",      e.color);

      com.google.gson.JsonArray verts = new com.google.gson.JsonArray();
      for (ZoneTraceStorage.ZoneEntry.Vertex v : e.vertices) {
        com.google.gson.JsonObject vo = new com.google.gson.JsonObject();
        vo.addProperty("x", v.x);
        vo.addProperty("z", v.z);
        verts.add(vo);
      }
      obj.add("vertices", verts);
      array.add(obj);
    }

    String json = GSON.toJson(array);
    LOGGER.info("[ZoneTrace] Export ({} zones):\n{}", storage.size(), json);
    ctx.getSource().sendSuccess(() -> Component.literal(
      "[ZoneTrace] Exported " + storage.size() + " zone(s) to server log.\n"
      + "  Copy the JSON from the console into install.json \"zones\" array."
    ), false);
    return storage.size();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  static boolean isZoneTracer(ItemStack stack) {
    if (stack == null || stack.isEmpty() || !stack.is(Items.STICK)) return false;
    Component name = stack.get(DataComponents.CUSTOM_NAME);
    return name != null && name.getString().equals(TRACER_NAME);
  }

  static ItemStack makeZoneTracer() {
    ItemStack stack = new ItemStack(Items.STICK);
    stack.set(DataComponents.CUSTOM_NAME,
      Component.literal(TRACER_NAME).withStyle(s -> s.withColor(0x00FF88).withItalic(false))
    );
    return stack;
  }

  private static void removeZoneTracer(ServerPlayer player) {
    var inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); i++) {
      if (isZoneTracer(inv.getItem(i))) {
        inv.removeItemNoUpdate(i);
        break;
      }
    }
  }

  private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> ctx) {
    try {
      return ctx.getSource().getPlayerOrException();
    } catch (Exception e) {
      ctx.getSource().sendFailure(Component.literal("[ZoneTrace] Must be run by a player."));
      return null;
    }
  }

  private static int noSession(CommandContext<CommandSourceStack> ctx) {
    ctx.getSource().sendFailure(Component.literal(
      "[ZoneTrace] No active session. Use /cobblemon-initiative zone-trace begin <name>."
    ));
    return 0;
  }

  private interface SessionAction {
    void run(ZoneTraceSession session);
  }

  private static int withSession(CommandContext<CommandSourceStack> ctx, SessionAction action) {
    ServerPlayer player = getPlayer(ctx);
    if (player == null) return 0;
    ZoneTraceSession session = sessions.get(player.getUUID());
    if (session == null) return noSession(ctx);
    action.run(session);
    return 1;
  }

  private static CompletableFuture<Suggestions> suggestTypes(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (String t : new String[]{"TOWN", "ROUTE", "SHRINE", "VILLAIN", "BATTLE_FRONTIER", "LANDMARK"}) {
      builder.suggest(t);
    }
    return builder.buildFuture();
  }

  private static CompletableFuture<Suggestions> suggestSaved(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (ZoneTraceStorage.ZoneEntry e : storage.getAll()) {
      builder.suggest(e.name);
    }
    return builder.buildFuture();
  }
}

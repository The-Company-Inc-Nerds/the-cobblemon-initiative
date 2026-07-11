package com.thecompanyinc.cobblemoninitiative.devtools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.util.EntityLookup;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DEV-ONLY guided placement walk (strip with the devtest package at 1.0.0 — TODO §2).
 *
 * <p>The bundled plan ({@code devtest/placement_plan.json}) lists every NPC still needing a
 * world position, each with a proposed spot, its story purpose, and field direction for the
 * ideal placement. The showrunner walks it:
 * <pre>
 *   /cobblemon-initiative dev place next        tp to the next pending proposal + brief
 *   /cobblemon-initiative dev place here        record MY feet as this NPC's placement
 *   /cobblemon-initiative dev place adopt       record the NPC I am LOOKING AT as its body
 *   /cobblemon-initiative dev place skip|list|goto <id>|export
 * </pre>
 * Results land in {@code {world}/data/npc_placements.json} — hand that back and Claude folds
 * placements/uuids into dialog-src, recompiles, and the latches/waypoints light up.
 * Proposals without an authored y are teleported to surface height (heightmap) for tuning.
 */
public final class DevPlaceManager {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-devtest");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final ResourceLocation PLAN_RESOURCE =
    ResourceLocation.fromNamespaceAndPath("cobblemon_initiative", "devtest/placement_plan.json");
  private static final String STATE_FILE = "data/npc_placements.json";

  private static List<JsonObject> plan;
  private static MinecraftServer planFrom;
  private static int cursor = -1;

  private DevPlaceManager() {}

  // ---------------------------------------------------------------------------
  // Commands
  // ---------------------------------------------------------------------------

  public static List<String> planIds(MinecraftServer server) {
    List<String> ids = new ArrayList<>();
    for (JsonObject e : loadPlan(server)) ids.add(e.get("id").getAsString());
    return ids;
  }

  public static int cmdList(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    List<JsonObject> entries = loadPlan(server);
    if (entries.isEmpty()) {
      player.sendSystemMessage(Component.literal("§cNo placement plan bundled (devtest/placement_plan.json)."));
      return 0;
    }
    JsonObject state = loadState(server);
    Map<String, List<String>> byArea = new LinkedHashMap<>();
    int done = 0;
    for (JsonObject e : entries) {
      String id = e.get("id").getAsString();
      String glyph = status(state, id);
      if (!glyph.equals("§7·")) done++;
      byArea
        .computeIfAbsent(e.get("area").getAsString(), a -> new ArrayList<>())
        .add(glyph + " §f" + id);
    }
    player.sendSystemMessage(Component.literal(
      "§6=== Placement walk — " + done + "/" + entries.size() + " resolved ==="));
    byArea.forEach((area, list) ->
      player.sendSystemMessage(Component.literal("§e" + area + "§7: " + String.join("§7, ", list))));
    player.sendSystemMessage(Component.literal(
      "§8✔ placed  ⚑ adopted  ✖ skipped  ·  pending — `dev place next` to continue"));
    return 1;
  }

  public static int cmdNext(ServerPlayer player, boolean forward) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    List<JsonObject> entries = loadPlan(server);
    if (entries.isEmpty()) {
      player.sendSystemMessage(Component.literal("§cNo placement plan bundled."));
      return 0;
    }
    JsonObject state = loadState(server);
    int step = forward ? 1 : -1;
    for (int i = 1; i <= entries.size(); i++) {
      int idx = Math.floorMod(cursor + step * i, entries.size());
      if (status(state, entries.get(idx).get("id").getAsString()).equals("§7·")) {
        cursor = idx;
        return visit(player, entries.get(idx));
      }
    }
    player.sendSystemMessage(Component.literal("§aNothing pending — the walk is complete. `dev place export` to hand off."));
    return 1;
  }

  public static int cmdGoto(ServerPlayer player, String id) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    List<JsonObject> entries = loadPlan(server);
    for (int i = 0; i < entries.size(); i++) {
      if (entries.get(i).get("id").getAsString().equals(id)) {
        cursor = i;
        return visit(player, entries.get(i));
      }
    }
    player.sendSystemMessage(Component.literal("§cUnknown plan id: " + id));
    return 0;
  }

  /** Record the player's feet as the placement for the current (or named) entry. */
  public static int cmdHere(ServerPlayer player, String idOrNull) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    JsonObject entry = resolveEntry(server, player, idOrNull);
    if (entry == null) return 0;
    String id = entry.get("id").getAsString();
    recordPlacement(server, id, player.getBlockX(), player.getBlockY(), player.getBlockZ());
    player.sendSystemMessage(Component.literal(
      "§a✔ §e" + id + "§a placed at §f" + player.getBlockX() + " " + player.getBlockY() + " " + player.getBlockZ()
        + "§a — `dev place next` to continue."));
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Programmatic API (the Producer's Tool drives the same state)
  // ---------------------------------------------------------------------------

  /** All plan entries, in order (empty when no plan is bundled). */
  static List<JsonObject> planEntries(MinecraftServer server) {
    return loadPlan(server);
  }

  /** The plan entry for an id, or null. */
  static JsonObject entryById(MinecraftServer server, String id) {
    for (JsonObject e : loadPlan(server)) {
      if (e.get("id").getAsString().equals(id)) return e;
    }
    return null;
  }

  /** True when the id already has a placement, adoption, or skip recorded. */
  static boolean isResolved(MinecraftServer server, String id) {
    return !status(loadState(server), id).equals("§7·");
  }

  static void recordPlacement(MinecraftServer server, String id, int x, int y, int z) {
    JsonObject pos = new JsonObject();
    pos.addProperty("x", x);
    pos.addProperty("y", y);
    pos.addProperty("z", z);
    JsonObject state = loadState(server);
    state.getAsJsonObject("placements").add(id, pos);
    state.getAsJsonObject("adoptions").remove(id);
    saveState(server, state);
  }

  static void recordAdoption(MinecraftServer server, String id, Entity target) {
    recordAdoption(server, id, target.getUUID(), target.getName().getString(), target.blockPosition());
  }

  /** Snapshot form — the Producer's Tool must never hold a live Entity across staging. */
  static void recordAdoption(
    MinecraftServer server, String id, java.util.UUID uuid, String label, net.minecraft.core.BlockPos pos
  ) {
    JsonObject rec = new JsonObject();
    rec.addProperty("uuid", uuid.toString());
    rec.addProperty("label", label);
    rec.addProperty("x", pos.getX());
    rec.addProperty("y", pos.getY());
    rec.addProperty("z", pos.getZ());
    JsonObject state = loadState(server);
    state.getAsJsonObject("adoptions").add(id, rec);
    state.getAsJsonObject("placements").remove(id);
    saveState(server, state);
  }

  static void recordSkip(MinecraftServer server, String id) {
    JsonObject state = loadState(server);
    state.getAsJsonArray("skipped").add(id);
    saveState(server, state);
  }

  /** Append a free-text note under any key ("<plan id>" or "gym:<slot>") — same handoff file. */
  static void addNote(MinecraftServer server, String key, String note) {
    JsonObject state = loadState(server);
    if (!state.has("notes")) state.add("notes", new JsonObject());
    JsonObject notes = state.getAsJsonObject("notes");
    if (!notes.has(key)) notes.add(key, new JsonArray());
    notes.getAsJsonArray(key).add(note);
    saveState(server, state);
  }

  /** Teleport to the entry's proposal and print the brief (the Producer's Tool advance).
   *  Also syncs THIS manager's cursor, so the typed no-arg fallbacks the brief mentions
   *  (`dev place here`/`adopt`/`skip`) act on the same stop the tool is showing. */
  static void visitEntry(ServerPlayer player, JsonObject entry) {
    MinecraftServer server = player.getServer();
    if (server != null) {
      List<JsonObject> entries = loadPlan(server);
      for (int i = 0; i < entries.size(); i++) {
        if (entries.get(i) == entry || entries.get(i).get("id").getAsString().equals(entry.get("id").getAsString())) {
          cursor = i;
          break;
        }
      }
    }
    visit(player, entry);
  }

  /** Record the looked-at NPC's uuid as the body takeover for the current (or named) entry. */
  public static int cmdAdopt(ServerPlayer player, String idOrNull) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    JsonObject entry = resolveEntry(server, player, idOrNull);
    if (entry == null) return 0;
    String id = entry.get("id").getAsString();

    Entity target = EntityLookup.getEntityLookedAt(player, 10.0);
    if (target == null || target instanceof ServerPlayer) {
      player.sendSystemMessage(Component.literal("§cLook at the NPC you want to take over (within 10 blocks)."));
      return 0;
    }
    JsonObject rec = new JsonObject();
    rec.addProperty("uuid", target.getUUID().toString());
    rec.addProperty("label", target.getName().getString());
    rec.addProperty("x", target.getBlockX());
    rec.addProperty("y", target.getBlockY());
    rec.addProperty("z", target.getBlockZ());
    JsonObject state = loadState(server);
    state.getAsJsonObject("adoptions").add(id, rec);
    state.getAsJsonObject("placements").remove(id);
    saveState(server, state);
    player.sendSystemMessage(Component.literal(
      "§b⚑ §e" + id + "§b now takes over body §f" + target.getName().getString() + "§b (" + target.getUUID()
        + ") — authored name/skin/dialog will repaint it on the next content pass."));
    return 1;
  }

  public static int cmdSkip(ServerPlayer player, String idOrNull) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    JsonObject entry = resolveEntry(server, player, idOrNull);
    if (entry == null) return 0;
    String id = entry.get("id").getAsString();
    JsonObject state = loadState(server);
    JsonArray skipped = state.getAsJsonArray("skipped");
    skipped.add(id);
    saveState(server, state);
    player.sendSystemMessage(Component.literal("§7✖ " + id + " skipped."));
    return 1;
  }

  public static int cmdExport(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return 0;
    JsonObject state = loadState(server);
    int p = state.getAsJsonObject("placements").size();
    int a = state.getAsJsonObject("adoptions").size();
    player.sendSystemMessage(Component.literal(
      "§a" + p + " placement(s) + " + a + " adoption(s) recorded in §f{world}/" + STATE_FILE
        + "§a — hand that file to Claude to fold into dialog-src and recompile."));
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Internals
  // ---------------------------------------------------------------------------

  /** Teleport to the proposal (surface height when no authored y) and print the brief. */
  private static int visit(ServerPlayer player, JsonObject e) {
    int x = e.get("x").getAsInt();
    int z = e.get("z").getAsInt();
    ServerLevel level = player.serverLevel();
    int y = e.has("y")
      ? e.get("y").getAsInt()
      : level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    player.connection.teleport(x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());

    player.sendSystemMessage(Component.literal("§6=== " + e.get("name").getAsString()
      + " §7(" + e.get("id").getAsString() + ", " + e.get("area").getAsString() + ") §6==="));
    player.sendSystemMessage(Component.literal("§7Purpose: §f" + e.get("purpose").getAsString()));
    player.sendSystemMessage(Component.literal("§7Ideal spot: §e" + e.get("idealSpot").getAsString()));
    String kind = e.get("kind").getAsString();
    String hint = switch (kind) {
      case "adopt-ok" -> "§8here = spawn fresh at your feet · adopt = take over the body you're facing · skip / next";
      case "stub" -> "§8character not authored yet — just mark the spot: here = record your feet · skip / next";
      default -> "§8here = record your feet as the spawn · skip / next";
    };
    player.sendSystemMessage(Component.literal(hint));
    return 1;
  }

  /** The named entry, or the cursor's entry when idOrNull is null. */
  private static JsonObject resolveEntry(MinecraftServer server, ServerPlayer player, String idOrNull) {
    List<JsonObject> entries = loadPlan(server);
    if (idOrNull != null) {
      for (JsonObject e : entries) {
        if (e.get("id").getAsString().equals(idOrNull)) return e;
      }
      player.sendSystemMessage(Component.literal("§cUnknown plan id: " + idOrNull));
      return null;
    }
    if (cursor < 0 || cursor >= entries.size()) {
      player.sendSystemMessage(Component.literal("§cNo current entry — use `dev place next` first (or name an id)."));
      return null;
    }
    return entries.get(cursor);
  }

  private static String status(JsonObject state, String id) {
    if (state.getAsJsonObject("placements").has(id)) return "§a✔";
    if (state.getAsJsonObject("adoptions").has(id)) return "§b⚑";
    for (JsonElement s : state.getAsJsonArray("skipped")) {
      if (s.getAsString().equals(id)) return "§8✖";
    }
    return "§7·";
  }

  private static List<JsonObject> loadPlan(MinecraftServer server) {
    if (plan != null && planFrom == server) return plan;
    List<JsonObject> parsed = new ArrayList<>();
    try {
      Resource resource = server.getResourceManager().getResource(PLAN_RESOURCE).orElse(null);
      if (resource != null) {
        try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
          JsonObject root = GSON.fromJson(reader, JsonObject.class);
          for (JsonElement el : root.getAsJsonArray("entries")) parsed.add(el.getAsJsonObject());
        }
      }
    } catch (Exception e) {
      LOGGER.error("[DevPlace] Failed to read placement plan: {}", e.getMessage());
    }
    plan = parsed;
    planFrom = server;
    return parsed;
  }

  private static JsonObject loadState(MinecraftServer server) {
    var file = server.getWorldPath(LevelResource.ROOT).resolve(STATE_FILE).toFile();
    if (file.exists()) {
      try (FileReader reader = new FileReader(file)) {
        JsonObject root = GSON.fromJson(reader, JsonObject.class);
        if (root != null && root.has("placements")) return root;
      } catch (Exception e) {
        LOGGER.error("[DevPlace] Error loading state: {}", e.getMessage());
      }
    }
    JsonObject fresh = new JsonObject();
    fresh.add("placements", new JsonObject());
    fresh.add("adoptions", new JsonObject());
    fresh.add("skipped", new JsonArray());
    return fresh;
  }

  private static void saveState(MinecraftServer server, JsonObject state) {
    try {
      var file = server.getWorldPath(LevelResource.ROOT).resolve(STATE_FILE).toFile();
      file.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(file)) {
        GSON.toJson(state, writer);
      }
    } catch (Exception e) {
      LOGGER.error("[DevPlace] Error saving state: {}", e.getMessage());
    }
  }
}

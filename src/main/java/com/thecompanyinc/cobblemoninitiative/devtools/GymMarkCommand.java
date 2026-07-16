package com.thecompanyinc.cobblemoninitiative.devtools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code /cobblemon-initiative gym-mark} — dev tool to capture the gym-gimmick placeholder
 * coordinates in-world, so the showrunner never has to read F3 into chat.
 *
 * <p>POINT marks: stand on the spot, {@code set <slot>}. BOX marks (vine walls, corridors,
 * arena boxes): stand at one corner, {@code start <slot>}, walk to the far corner,
 * {@code stop <slot>} — the tool records the full box and previews it with particles.
 * {@code list} shows every expected slot + status; {@code show <slot>} re-previews;
 * {@code export} dumps the JSON block (also saved per-world at data/gym_marks.json) to
 * hand back to the source tree where the TODO(showrunner) placeholders live.
 *
 * <p>Slot keys are free-form (the registry below only powers suggestions/status), so new
 * gimmicks can be marked without touching this file. Dev-only — remove before final
 * release (ships with the field-mark tool; see the dev-only cleanup checklist).
 */
public class GymMarkCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** Expected slots: key -> {kind, where it lands in the source tree}. Free-form keys allowed.
   * Package-visible: the GymMark wand walks this same registry in order. */
  static final Map<String, String[]> SLOTS = new LinkedHashMap<>();
  static {
    SLOTS.put("hz_wall_1", new String[] {"box", "Hua Zhan Moss Court vine wall (gym/hz_wall_1.mcfunction)"});
    SLOTS.put("hz_wall_2", new String[] {"box", "Hua Zhan Orchard Rows vine wall (gym/hz_wall_2.mcfunction)"});
    SLOTS.put("hz_wall_3", new String[] {"box", "Hua Zhan Water Terrace vine wall (gym/hz_wall_3.mcfunction)"});
    SLOTS.put("hz_wall_4", new String[] {"box", "Hua Zhan Still Pond vine wall (gym/hz_wall_4.mcfunction)"});
    SLOTS.put("nifl_corridor", new String[] {"box", "Nifl whiteout approach corridor (gym/nifl_whiteout.mcfunction)"});
    SLOTS.put("scorch_heat_box", new String[] {"box", "Scorchspire arena heat volume (gym/scorchspire_heat.mcfunction)"});
    SLOTS.put("rift_origin", new String[] {"point", "Ryujin rift dragon fight origin — where it circles (rift_dragon.json)"});
    SLOTS.put("rift_crystal_1", new String[] {"point", "Rift crystal pillar 1 (rift_dragon.json crystals[])"});
    SLOTS.put("rift_crystal_2", new String[] {"point", "Rift crystal pillar 2 (rift_dragon.json crystals[])"});
    SLOTS.put("rift_crystal_3", new String[] {"point", "Rift crystal pillar 3 (rift_dragon.json crystals[])"});
    SLOTS.put("rift_crystal_4", new String[] {"point", "Rift crystal pillar 4 (rift_dragon.json crystals[])"});
    SLOTS.put("nifl_sentry_1", new String[] {"point", "Frost Sentinel 1 (nifl_sentry_1 placement)"});
    SLOTS.put("nifl_sentry_2", new String[] {"point", "Frost Sentinel 2 (nifl_sentry_2 placement)"});
    SLOTS.put("nifl_sentry_3", new String[] {"point", "Frost Sentinel 3 (nifl_sentry_3 placement)"});
    SLOTS.put("nifl_clear", new String[] {"point", "Nifl whiteout clean-crossing point (gym/nifl_whiteout.mcfunction)"});
    SLOTS.put("gaviota_bell", new String[] {"point", "Gaviota pier tide-bell anchor (gym/gaviota_tide.mcfunction)"});
    SLOTS.put("deepcore_marshal", new String[] {"point", "Gauntlet Marshal Osei (deepcore_marshal placement)"});
    SLOTS.put("kalahar_mirage_1", new String[] {"point", "Kalahar mirage 1 (kalahar_mirage_1 placement)"});
    SLOTS.put("kalahar_mirage_2", new String[] {"point", "Kalahar mirage 2 (kalahar_mirage_2 placement)"});
    SLOTS.put("kalahar_mirage_3", new String[] {"point", "Kalahar mirage 3 (kalahar_mirage_3 placement)"});
    SLOTS.put("kalahar_mirage_4", new String[] {"point", "Kalahar mirage 4 (kalahar_mirage_4 placement)"});
    SLOTS.put("kalahar_mirage_5", new String[] {"point", "Kalahar mirage 5 (kalahar_mirage_5 placement)"});
    SLOTS.put("kalahar_mirage_6", new String[] {"point", "Kalahar mirage 6 (kalahar_mirage_6 placement)"});
    // Leader stage-back spots: where the CHALLENGER stands when the post-intro battle
    // opens (battle.stage_pos — overrides the scene's generic 7-blocks-back). Stand on
    // the spot facing the leader and set it.
    SLOTS.put("stage_takehara", new String[] {"point", "Cicada challenger stage spot (takehara_leader battle.stage_pos)"});
    SLOTS.put("stage_hua_zhan", new String[] {"point", "Blossom challenger stage spot (hua_zhan_leader battle.stage_pos)"});
    SLOTS.put("stage_mystic", new String[] {"point", "Titania challenger stage spot (mystic_leader battle.stage_pos)"});
    SLOTS.put("stage_deepcore", new String[] {"point", "Bruno challenger stage spot (deepcore_leader battle.stage_pos)"});
    SLOTS.put("stage_gaviota", new String[] {"point", "Neptune challenger stage spot (gaviota_leader battle.stage_pos)"});
    SLOTS.put("stage_kalahar", new String[] {"point", "Gaia challenger stage spot (kalahar_leader battle.stage_pos)"});
    SLOTS.put("stage_cyber", new String[] {"point", "Volt challenger stage spot (cyber_leader battle.stage_pos)"});
    SLOTS.put("stage_ryujin", new String[] {"point", "Ryujin challenger stage spot (ryujin_leader battle.stage_pos)"});
    SLOTS.put("stage_nifl", new String[] {"point", "Boreas challenger stage spot (nifl_leader battle.stage_pos)"});
    SLOTS.put("stage_scorchspire", new String[] {"point", "Vulcan challenger stage spot (scorchspire_leader battle.stage_pos)"});

    // Company HQ vertical geometry (showrunner ruling 2026-07-13): the raid DESCENDS to the
    // BASEMENT (Acting CEO DJ at the bottom); the player's old PENTHOUSE is the TOP floor —
    // lore + a Master Ball pickup, and the arena where the POST-GAME Founder mirror is fought.
    SLOTS.put("hq_basement_dj", new String[] {"point", "Acting CEO DJ — bottom of the basement raid (acting_ceo_dj placement; canon [1590 51 1028])"});
    SLOTS.put("hq_penthouse_lore", new String[] {"point", "Penthouse lore prop — the founder's old top-floor office (DocProp/loot)"});
    SLOTS.put("hq_penthouse_masterball", new String[] {"point", "ITEM: Penthouse MASTER BALL pickup — mark the top-floor loot chest / DocProp block spot"});
    SLOTS.put("hq_penthouse_mirror", new String[] {"point", "Penthouse mirror arena — POST-GAME Founder fight spot (villain_final_boss placement, top floor)"});

    // Gym interior cast RELOCATION (2026-07-13): the trainers/apprentices latch in a default
    // cluster AROUND the leader — mark real spots to spread them through the gym floor. Each
    // note carries that gym's signature GIMMICK so the placer knows the room's mechanic.
    // POINT marks: stand where the NPC should stand, facing the way they should face.
    String[][] gymCast = {
      {"takehara",    "Bug — floating Cicada leader (perch y173 -> glide)",           "trainer_1,trainer_2,trainer_3,trainer_4"},
      {"hua_zhan",    "Grass — living-statue wardens drop vine walls",                 "trainer_1,trainer_2,trainer_3,trainer_4"},
      {"mystic",      "Fairy — Mirror Match (declare your lead -> illusion team)",     "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"deepcore",    "Fighting — Gauntlet + 2v1 GEN_9_MULTI finale (Marshal Osei)",   "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"gaviota",     "Water — Tide Clock (4-min high/low, rain-variant teams)",       "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"kalahar",     "Ground — 6 click-to-poof mirages (mark those separately)",      "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"cyber",       "Electric — Stadium tease (Volt; gate flips post-Stadium)",      "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"ryujin",      "Dragon — THE RIFT (overworld Ender Dragon gates the leader)",   "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"nifl",        "Ice — Whiteout Approach (3 Frost Sentinels: seen = debuff)",    "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
      {"scorchspire", "Fire — Banked Coals (heat bossbar; wardens vent to cool it)",   "trainer_1,trainer_2,trainer_3,trainer_4,jr_apprentice,apprentice"},
    };
    for (String[] g : gymCast) {
      for (String member : g[2].split(",")) {
        SLOTS.put(
          "gym_" + g[0] + "_" + member,
          new String[] {"point", g[0] + " " + member + " — GIMMICK: " + g[1] + " (" + g[0] + "_" + member + " placement)"});
      }
    }

    // "Make it Minecraft" flavor (alpha.20) coord marks. The cutscenes ship as portable
    // relative scenes; mark these to author absolute per-location variants with /cutscene record.
    SLOTS.put("cutscene_starter_lab", new String[] {"point", "Starter reveal — camera anchor / where the 3 starter stand-ins face the player (cutscenes/starter_reveal.json)"});
    SLOTS.put("cutscene_starter_1", new String[] {"point", "Starter stand-in 1 — Skiddo spot (starter_reveal keyframe target)"});
    SLOTS.put("cutscene_starter_2", new String[] {"point", "Starter stand-in 2 — Totodile spot (starter_reveal keyframe target)"});
    SLOTS.put("cutscene_starter_3", new String[] {"point", "Starter stand-in 3 — Hisuian Growlithe spot (starter_reveal keyframe target)"});
    SLOTS.put("shrine_fire_statue", new String[] {"point", "Fire Shrine — the big statue the fly-through lands on (shrine_reveal per-shrine variant)"});
    SLOTS.put("shrine_ice_statue", new String[] {"point", "Ice Shrine statue — cutscene fly-through target"});
    SLOTS.put("shrine_ground_statue", new String[] {"point", "Ground Shrine statue — cutscene fly-through target"});
    SLOTS.put("shrine_dragon_statue", new String[] {"point", "Dragon Shrine statue — cutscene fly-through target"});
    SLOTS.put("shrine_fairy_statue", new String[] {"point", "Fairy Shrine statue — cutscene fly-through target"});
    SLOTS.put("daycare_keeper", new String[] {"point", "Gaviota daycare keeper body spot (daycare_keeper placement — add uuid/placement to the character)"});
    SLOTS.put("daycare_pen", new String[] {"point", "Daycare pen — where boarded stand-ins appear (config penX/Y/Z)"});
  }

  private static GymMarkStorage storage;

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher, GymMarkStorage stor) {
    storage = stor;

    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .then(Commands.literal("gym-mark")
          .requires(src -> src.hasPermission(2))

          // set <slot> — a POINT mark at the player's feet.
          .then(Commands.literal("set")
            .then(Commands.argument("slot", StringArgumentType.word())
              .suggests((ctx, b) -> suggestSlots(b, "point"))
              .executes(ctx -> cmdSet(ctx, StringArgumentType.getString(ctx, "slot")))))

          // start/stop <slot> — the two corners of a BOX mark.
          .then(Commands.literal("start")
            .then(Commands.argument("slot", StringArgumentType.word())
              .suggests((ctx, b) -> suggestSlots(b, "box"))
              .executes(ctx -> cmdStart(ctx, StringArgumentType.getString(ctx, "slot")))))
          .then(Commands.literal("stop")
            .then(Commands.argument("slot", StringArgumentType.word())
              .suggests((ctx, b) -> suggestSlots(b, "box"))
              .executes(ctx -> cmdStop(ctx, StringArgumentType.getString(ctx, "slot")))))

          .then(Commands.literal("show")
            .then(Commands.argument("slot", StringArgumentType.word())
              .suggests((ctx, b) -> suggestSlots(b, null))
              .executes(ctx -> cmdShow(ctx, StringArgumentType.getString(ctx, "slot")))))
          .then(Commands.literal("clear")
            .then(Commands.argument("slot", StringArgumentType.word())
              .suggests((ctx, b) -> suggestSlots(b, null))
              .executes(ctx -> cmdClear(ctx, StringArgumentType.getString(ctx, "slot")))))
          .then(Commands.literal("list").executes(GymMarkCommand::cmdList))
          .then(Commands.literal("export").executes(GymMarkCommand::cmdExport))
          // wand — the double-click marking tool (its name shows the current slot).
          .then(Commands.literal("wand").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player == null) return 0;
            GymMarkWand.give(player, storage);
            return 1;
          }))
        )
    );
  }

  // ── Subcommands ──────────────────────────────────────────────────────────────

  private static int cmdSet(CommandContext<CommandSourceStack> ctx, String slot) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    BlockPos pos = player.blockPosition();

    GymMarkStorage.MarkEntry entry = new GymMarkStorage.MarkEntry();
    entry.key = slot;
    entry.kind = "point";
    entry.dimension = player.serverLevel().dimension().location().toString();
    entry.x = pos.getX(); entry.y = pos.getY(); entry.z = pos.getZ();
    entry.complete = true;
    storage.put(entry);

    previewPoint(player.serverLevel(), pos);
    ctx.getSource().sendSuccess(() -> Component.literal(
      "§a[GymMark] §f" + slot + " §7point set at §e" + fmt(pos) + "§7." + hint(slot)), false);
    return 1;
  }

  private static int cmdStart(CommandContext<CommandSourceStack> ctx, String slot) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    BlockPos pos = player.blockPosition();

    GymMarkStorage.MarkEntry entry = new GymMarkStorage.MarkEntry();
    entry.key = slot;
    entry.kind = "box";
    entry.dimension = player.serverLevel().dimension().location().toString();
    entry.x = pos.getX(); entry.y = pos.getY(); entry.z = pos.getZ();
    entry.complete = false;
    storage.put(entry);

    previewPoint(player.serverLevel(), pos);
    ctx.getSource().sendSuccess(() -> Component.literal(
      "§a[GymMark] §f" + slot + " §7start corner at §e" + fmt(pos)
        + "§7 — walk to the far corner and run §f/cobblemon-initiative gym-mark stop " + slot), false);
    return 1;
  }

  private static int cmdStop(CommandContext<CommandSourceStack> ctx, String slot) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    GymMarkStorage.MarkEntry entry = storage.get(slot);
    if (entry == null || !"box".equals(entry.kind)) {
      ctx.getSource().sendFailure(Component.literal(
        "§c[GymMark] No start corner for '" + slot + "' — run §fstart " + slot + "§c first."));
      return 0;
    }
    BlockPos pos = player.blockPosition();
    entry.x2 = pos.getX(); entry.y2 = pos.getY(); entry.z2 = pos.getZ();
    entry.complete = true;
    storage.put(entry);

    previewBox(player.serverLevel(), entry);
    int dx = Math.abs(entry.x2 - entry.x) + 1;
    int dy = Math.abs(entry.y2 - entry.y) + 1;
    int dz = Math.abs(entry.z2 - entry.z) + 1;
    ctx.getSource().sendSuccess(() -> Component.literal(
      "§a[GymMark] §f" + slot + " §7box complete: §e" + entry.x + " " + entry.y + " " + entry.z
        + " §7-> §e" + entry.x2 + " " + entry.y2 + " " + entry.z2
        + " §7(" + dx + "x" + dy + "x" + dz + ")." + hint(slot)), false);
    return 1;
  }

  private static int cmdShow(CommandContext<CommandSourceStack> ctx, String slot) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) return 0;
    GymMarkStorage.MarkEntry entry = storage.get(slot);
    if (entry == null) {
      ctx.getSource().sendFailure(Component.literal("§c[GymMark] Nothing marked for '" + slot + "'."));
      return 0;
    }
    if ("box".equals(entry.kind) && entry.complete) {
      previewBox(player.serverLevel(), entry);
    } else {
      previewPoint(player.serverLevel(), new BlockPos(entry.x, entry.y, entry.z));
    }
    ctx.getSource().sendSuccess(() -> Component.literal("§a[GymMark] §7Previewing §f" + slot + "§7."), false);
    return 1;
  }

  private static int cmdClear(CommandContext<CommandSourceStack> ctx, String slot) {
    boolean removed = storage.remove(slot);
    if (removed) {
      ctx.getSource().sendSuccess(() -> Component.literal("§a[GymMark] §7Cleared §f" + slot + "§7."), false);
    } else {
      ctx.getSource().sendFailure(Component.literal("§c[GymMark] Nothing marked for '" + slot + "'."));
    }
    return removed ? 1 : 0;
  }

  private static int cmdList(CommandContext<CommandSourceStack> ctx) {
    StringBuilder sb = new StringBuilder("§6[GymMark] Slots:\n");
    for (Map.Entry<String, String[]> s : SLOTS.entrySet()) {
      GymMarkStorage.MarkEntry m = storage.get(s.getKey());
      String status = (m == null) ? "§c✗"
        : (!m.complete ? "§e◐ start only" : "§a✓ " + m.x + " " + m.y + " " + m.z
          + ("box".equals(m.kind) ? " -> " + m.x2 + " " + m.y2 + " " + m.z2 : ""));
      sb.append("§f").append(s.getKey()).append(" §8[").append(s.getValue()[0]).append("] ")
        .append(status).append(" §7— ").append(s.getValue()[1]).append('\n');
    }
    // Any free-form marks outside the registry.
    for (GymMarkStorage.MarkEntry m : storage.getAll()) {
      if (!SLOTS.containsKey(m.key.toLowerCase())) {
        sb.append("§f").append(m.key).append(" §8[").append(m.kind).append("] §a✓ custom\n");
      }
    }
    final String out = sb.toString();
    ctx.getSource().sendSuccess(() -> Component.literal(out), false);
    return 1;
  }

  private static int cmdExport(CommandContext<CommandSourceStack> ctx) {
    JsonObject root = new JsonObject();
    JsonArray array = new JsonArray();
    int incomplete = 0;
    for (GymMarkStorage.MarkEntry m : storage.getAll()) {
      if (!m.complete) { incomplete++; continue; }
      array.add(GSON.toJsonTree(m));
    }
    root.add("marks", array);
    String json = GSON.toJson(root);
    LOGGER.info("[GymMark] EXPORT ({} mark(s)):\n{}", array.size(), json);
    final int n = array.size();
    final int skipped = incomplete;
    final String path = storage.getDataFile() != null ? storage.getDataFile().getPath() : "(unsaved)";
    ctx.getSource().sendSuccess(() -> Component.literal(
      "§a[GymMark] §7Exported §f" + n + "§7 mark(s) to the server log and §f" + path
        + (skipped > 0 ? " §7(§e" + skipped + " box(es) missing their stop corner — not exported§7)" : "")
        + "§7. Hand that file (or the log block) back for integration."), false);
    return 1;
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private static String hint(String slot) {
    String[] def = SLOTS.get(slot.toLowerCase());
    return def != null ? " §8(" + def[1] + ")" : "";
  }

  private static String fmt(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static CompletableFuture<Suggestions> suggestSlots(SuggestionsBuilder builder, String kindFilter) {
    for (Map.Entry<String, String[]> s : SLOTS.entrySet()) {
      if (kindFilter == null || s.getValue()[0].equals(kindFilter)) {
        builder.suggest(s.getKey());
      }
    }
    return builder.buildFuture();
  }

  /** A bright column so a point mark is visible from across the arena. */
  static void previewPoint(ServerLevel level, BlockPos pos) {
    for (int i = 0; i <= 12; i++) {
      level.sendParticles(ParticleTypes.END_ROD,
        pos.getX() + 0.5, pos.getY() + i * 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
    }
    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
      pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.4, 0.6, 0.4, 0);
  }

  /** Outline the 12 edges of a completed box mark (capped so huge boxes stay cheap). */
  static void previewBox(ServerLevel level, GymMarkStorage.MarkEntry m) {
    double x1 = Math.min(m.x, m.x2), x2 = Math.max(m.x, m.x2) + 1;
    double y1 = Math.min(m.y, m.y2), y2 = Math.max(m.y, m.y2) + 1;
    double z1 = Math.min(m.z, m.z2), z2 = Math.max(m.z, m.z2) + 1;
    double step = Math.max(0.5, Math.max(x2 - x1, Math.max(y2 - y1, z2 - z1)) / 64.0);
    for (double t = 0; t <= x2 - x1; t += step) {
      edge(level, x1 + t, y1, z1); edge(level, x1 + t, y1, z2);
      edge(level, x1 + t, y2, z1); edge(level, x1 + t, y2, z2);
    }
    for (double t = 0; t <= y2 - y1; t += step) {
      edge(level, x1, y1 + t, z1); edge(level, x1, y1 + t, z2);
      edge(level, x2, y1 + t, z1); edge(level, x2, y1 + t, z2);
    }
    for (double t = 0; t <= z2 - z1; t += step) {
      edge(level, x1, y1, z1 + t); edge(level, x2, y1, z1 + t);
      edge(level, x1, y2, z1 + t); edge(level, x2, y2, z1 + t);
    }
  }

  private static void edge(ServerLevel level, double x, double y, double z) {
    level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
  }
}

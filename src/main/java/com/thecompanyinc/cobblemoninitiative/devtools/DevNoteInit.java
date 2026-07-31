package com.thecompanyinc.cobblemoninitiative.devtools;

import com.google.gson.Gson;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only NPC review tool. Give yourself the noter with
 * {@code /cobblemon-initiative npcnote stick}, then:
 * <ul>
 *   <li><b>Left-click (whack) an NPC</b> → selects it (records name / tags / position).</li>
 *   <li><b>Right-click a block</b> with the noter → sets the selected NPC's requested new
 *       position to that block.</li>
 *   <li>{@code /cobblemon-initiative npcnote note <text>} → attach a comment.</li>
 *   <li>{@code /cobblemon-initiative npcnote move} → set the new position to where you stand.</li>
 *   <li>{@code /cobblemon-initiative npcnote log} → dump every note to chat (paste it back).</li>
 * </ul>
 * Registered by DevToolsInit — strips with the devtools package at 1.0.0 (TODO §2).
 */
public final class DevNoteInit {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-devnote");
  public static final String NOTER_NAME = "NPC Noter";

  private static DevNoteStorage storage;
  // Each player's currently-selected NPC uuid (the target of note/move commands).
  private static final Map<UUID, String> selection = new HashMap<>();

  // The smoke-test checklist, compiled from SMOKETEST.md into a jar resource.
  private static final List<SmokeItem> SMOKE_ITEMS = new ArrayList<>();

  /** One compiled smoke-test line: an R-id and its (trimmed) description. */
  public static class SmokeItem {
    public String id;
    public String text;
  }

  /** Wire commands, item callbacks, and storage — called once by DevToolsInit. */
  public static void register() {
    storage = new DevNoteStorage();
    loadSmokeItems();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
      DevNoteCommand.register(dispatcher));

    // Whack an NPC with the noter → select it (and never damage it).
    AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
      if (world.isClientSide() || !holdingNoter(player)) return InteractionResult.PASS;
      if (!(entity instanceof LivingEntity) || entity instanceof Player) return InteractionResult.PASS;
      selectEntity(player, entity);
      return InteractionResult.SUCCESS; // consume — no damage dealt
    });

    // Right-click a block with the noter → set the selected NPC's new position.
    UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
      if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !holdingNoter(player)) {
        return InteractionResult.PASS;
      }
      BlockPos target = ((BlockHitResult) hit).getBlockPos().above();
      if (setNewPos(player, target.getX() + 0.5, target.getY(), target.getZ() + 0.5)) {
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
    });

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      storage.load(server);
      LOGGER.info("DevNote loaded {} NPC review note(s).", storage.getNotes().size());
    });
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> storage.save());

    LOGGER.info("DevNote (NPC review) dev tool initialized.");
  }

  // ── Item ──────────────────────────────────────────────────────────────────────

  public static ItemStack makeNoter() {
    ItemStack stick = new ItemStack(Items.STICK);
    stick.set(DataComponents.CUSTOM_NAME, Component.literal("§b" + NOTER_NAME));
    return stick;
  }

  private static boolean holdingNoter(Player player) {
    ItemStack held = player.getMainHandItem();
    Component name = held.get(DataComponents.CUSTOM_NAME);
    return held.is(Items.STICK) && name != null && name.getString().contains(NOTER_NAME);
  }

  // ── Selection / notes ───────────────────────────────────────────────────────────

  private static void selectEntity(Player player, Entity entity) {
    String uuid = entity.getStringUUID();
    DevNoteStorage.NpcNote note = storage.find(uuid);
    if (note == null) {
      note = new DevNoteStorage.NpcNote();
      note.uuid = uuid;
      note.tags.addAll(entity.getTags());
      storage.getNotes().add(note);
    }
    // Always refresh name + observed position on (re)select.
    note.name = entity.getName().getString();
    note.ox = round(entity.getX());
    note.oy = round(entity.getY());
    note.oz = round(entity.getZ());
    selection.put(player.getUUID(), uuid);
    storage.save();
    player.sendSystemMessage(Component.literal(
      "§bNoter §7selected §f" + note.name + " §7@ " + fmt(note.ox, note.oy, note.oz)
      + (note.tags.isEmpty() ? "" : " §8" + note.tags)
      + "\n§7  /ca npcnote note <text>, /ca npcnote move (or right-click a block), /ca npcnote log"));
  }

  /** Attach a comment to the player's current selection. Returns false if nothing selected. */
  public static boolean addComment(Player player, String text) {
    DevNoteStorage.NpcNote note = selected(player);
    if (note == null) return false;
    note.comment = (note.comment == null || note.comment.isBlank()) ? text : note.comment + " | " + text;
    storage.save();
    player.sendSystemMessage(Component.literal("§bNoter §7comment on §f" + note.name + "§7: §f" + text));
    return true;
  }

  /** Set the new position of the player's selection. Returns false if nothing selected. */
  public static boolean setNewPos(Player player, double x, double y, double z) {
    DevNoteStorage.NpcNote note = selected(player);
    if (note == null) return false;
    note.nx = round(x);
    note.ny = round(y);
    note.nz = round(z);
    storage.save();
    player.sendSystemMessage(Component.literal(
      "§bNoter §7move §f" + note.name + " §7→ " + fmt(note.nx, note.ny, note.nz)));
    return true;
  }

  /** Capture the player's current position with an optional title + note. */
  public static void addPosition(Player player, String title, String note) {
    DevNoteStorage.PosMark m = new DevNoteStorage.PosMark();
    m.x = round(player.getX());
    m.y = round(player.getY());
    m.z = round(player.getZ());
    m.title = (title != null && !title.isBlank()) ? title : null;
    m.note = (note != null && !note.isBlank()) ? note : null;
    storage.getPositions().add(m);
    storage.save();
    StringBuilder sb = new StringBuilder("§bPos §f").append(fmt(m.x, m.y, m.z));
    if (m.title != null) sb.append(" §7\"").append(m.title).append("\"");
    if (m.note != null) sb.append(" §7— ").append(m.note);
    player.sendSystemMessage(Component.literal(sb.toString()));
  }

  public static int logToChat(Player player) {
    var notes = storage.getNotes();
    var positions = storage.getPositions();
    player.sendSystemMessage(Component.literal(
      "§b===== NPC DEV NOTES (" + notes.size() + " npc, " + positions.size()
      + " pos) — copy the lines below ====="));
    int n = 0;
    for (DevNoteStorage.NpcNote note : notes) {
      n++;
      StringBuilder sb = new StringBuilder();
      sb.append("N").append(n).append(". ").append(note.name)
        .append(" @ ").append(fmt(note.ox, note.oy, note.oz));
      if (note.nx != null) sb.append(" -> MOVE ").append(fmt(note.nx, note.ny, note.nz));
      if (!note.tags.isEmpty()) sb.append(" tags=").append(note.tags);
      if (note.comment != null && !note.comment.isBlank()) sb.append(" | ").append(note.comment);
      player.sendSystemMessage(Component.literal("§f" + sb));
    }
    int pi = 0;
    for (DevNoteStorage.PosMark m : positions) {
      pi++;
      StringBuilder sb = new StringBuilder();
      sb.append("P").append(pi).append(". @ ").append(fmt(m.x, m.y, m.z));
      if (m.title != null) sb.append(" \"").append(m.title).append("\"");
      if (m.note != null) sb.append(" | ").append(m.note);
      player.sendSystemMessage(Component.literal("§f" + sb));
    }
    player.sendSystemMessage(Component.literal("§b===== end ====="));
    return notes.size() + positions.size();
  }

  // ── /ca dev note + marker + log (freeform notes, block markers, file export) ─────

  /** Shared storage accessor so the marker tool + dev commands write to the same journal. */
  public static DevNoteStorage getStorage() {
    return storage;
  }

  /** /ca dev note &lt;text&gt; — jot a quick freeform note with the player's position + a timestamp. */
  public static void addFreeNote(net.minecraft.server.level.ServerPlayer player, String text) {
    DevNoteStorage.FreeNote fn = new DevNoteStorage.FreeNote();
    fn.stamp = stamp();
    fn.text = text;
    fn.x = round(player.getX());
    fn.y = round(player.getY());
    fn.z = round(player.getZ());
    fn.dim = player.level().dimension().location().toString();
    storage.getFreeNotes().add(fn);
    storage.save();
    player.sendSystemMessage(Component.literal(
      "§bNote §7saved §8(" + storage.getFreeNotes().size() + ") §f" + text));
  }

  /**
   * /ca dev log — dump every dev record (freeform notes, markers, npc notes, position marks,
   * and the list of cutscene recordings) to chat AND write a markdown file next to the save at
   * {@code <world>/dev_playtest_notes/dev_log_<stamp>.md} for easy upload.
   */
  public static int devLog(net.minecraft.server.level.ServerPlayer player) {
    String when = stamp();
    var free = storage.getFreeNotes();
    var markers = storage.getMarkers();
    var npc = storage.getNotes();
    var pos = storage.getPositions();

    StringBuilder md = new StringBuilder();
    md.append("# Dev playtest log — ").append(when).append("\n\n");

    md.append("## Notes (").append(free.size()).append(")\n\n");
    int i = 0;
    for (DevNoteStorage.FreeNote fn : free) {
      i++;
      md.append("- ").append(i).append(". [").append(fn.stamp).append("] ").append(fn.text)
        .append("  (@ ").append(fmt(fn.x, fn.y, fn.z)).append(")\n");
    }
    md.append("\n## Markers (").append(markers.size()).append(")\n\n");
    int mi = 0;
    for (DevNoteStorage.Marker m : markers) {
      mi++;
      md.append("### M").append(mi).append(". ").append(m.title == null ? "(untitled)" : m.title);
      if (m.description != null && !m.description.isBlank()) md.append(" — ").append(m.description);
      md.append("\n- ").append(m.blocks.size()).append(" block(s)");
      if (!m.blocks.isEmpty()) md.append(", bounds ").append(bounds(m.blocks));
      if (m.stamp != null) md.append(", ").append(m.stamp);
      md.append("\n- blocks: ").append(blocksJson(m.blocks)).append("\n\n");
    }
    md.append("## NPC notes (").append(npc.size()).append(")\n\n");
    int ni = 0;
    for (DevNoteStorage.NpcNote note : npc) {
      ni++;
      StringBuilder sb = new StringBuilder("N").append(ni).append(". ").append(note.name)
        .append(" @ ").append(fmt(note.ox, note.oy, note.oz));
      if (note.nx != null) sb.append(" -> MOVE ").append(fmt(note.nx, note.ny, note.nz));
      if (!note.tags.isEmpty()) sb.append(" tags=").append(note.tags);
      if (note.comment != null && !note.comment.isBlank()) sb.append(" | ").append(note.comment);
      md.append("- ").append(sb).append("\n");
    }
    md.append("\n## Position marks (").append(pos.size()).append(")\n\n");
    int pi = 0;
    for (DevNoteStorage.PosMark m : pos) {
      pi++;
      StringBuilder sb = new StringBuilder("P").append(pi).append(". @ ").append(fmt(m.x, m.y, m.z));
      if (m.title != null) sb.append(" \"").append(m.title).append("\"");
      if (m.note != null) sb.append(" | ").append(m.note);
      md.append("- ").append(sb).append("\n");
    }

    md.append("\n## Cutscene recordings\n\n");
    int cs = 0;
    try {
      java.nio.file.Path dir = com.thecompanyinc.cobblemoninitiative.cutscene.CutsceneManager.overrideDir();
      if (dir != null && Files.isDirectory(dir)) {
        try (var stream = Files.list(dir)) {
          for (java.nio.file.Path f : stream.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
            cs++;
            md.append("- ").append(f.getFileName().toString()).append("\n");
          }
        }
      }
    } catch (Exception e) {
      md.append("- (could not list cutscenes: ").append(e.getMessage()).append(")\n");
    }

    // Write the file next to the save.
    String path = "(not written — no world dir)";
    try {
      File root = storage.worldRoot();
      if (root != null) {
        File outDir = new File(root, "dev_playtest_notes");
        outDir.mkdirs();
        File out = new File(outDir, "dev_log_" + fileStamp() + ".md");
        Files.writeString(out.toPath(), md.toString(), StandardCharsets.UTF_8);
        path = out.getAbsolutePath();
      }
    } catch (Exception e) {
      path = "(write failed: " + e.getMessage() + ")";
    }

    // Chat summary (the file has the full detail).
    player.sendSystemMessage(Component.literal("§b===== DEV LOG (" + when + ") ====="));
    for (DevNoteStorage.FreeNote fn : free) {
      player.sendSystemMessage(Component.literal("§7[note] §f" + fn.text + " §8(" + fmt(fn.x, fn.y, fn.z) + ")"));
    }
    for (DevNoteStorage.Marker m : markers) {
      player.sendSystemMessage(Component.literal("§7[marker] §f" + (m.title == null ? "(untitled)" : m.title)
        + " §8(" + m.blocks.size() + " blocks)"));
    }
    player.sendSystemMessage(Component.literal("§b===== §7" + free.size() + " notes, " + markers.size()
      + " markers, " + npc.size() + " npc, " + pos.size() + " pos, " + cs + " cutscene(s) ====="));
    player.sendSystemMessage(Component.literal("§aWrote §f" + path));
    return free.size() + markers.size();
  }

  private static String stamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private static String fileStamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
  }

  private static String bounds(List<int[]> blocks) {
    int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, minz = Integer.MAX_VALUE;
    int maxx = Integer.MIN_VALUE, maxy = Integer.MIN_VALUE, maxz = Integer.MIN_VALUE;
    for (int[] b : blocks) {
      minx = Math.min(minx, b[0]); miny = Math.min(miny, b[1]); minz = Math.min(minz, b[2]);
      maxx = Math.max(maxx, b[0]); maxy = Math.max(maxy, b[1]); maxz = Math.max(maxz, b[2]);
    }
    return "[" + minx + "," + miny + "," + minz + "] .. [" + maxx + "," + maxy + "," + maxz + "]";
  }

  private static String blocksJson(List<int[]> blocks) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < blocks.size(); i++) {
      int[] b = blocks.get(i);
      if (i > 0) sb.append(", ");
      sb.append("[").append(b[0]).append(",").append(b[1]).append(",").append(b[2]).append("]");
    }
    return sb.append("]").toString();
  }

  public static int clearNotes(Player player) {
    int n = storage.getNotes().size() + storage.getPositions().size();
    storage.getNotes().clear();
    storage.getPositions().clear();
    selection.remove(player.getUUID());
    storage.save();
    return n;
  }

  public static boolean undoLast(Player player) {
    var positions = storage.getPositions();
    if (!positions.isEmpty()) {
      positions.remove(positions.size() - 1);
      storage.save();
      return true;
    }
    var notes = storage.getNotes();
    if (notes.isEmpty()) return false;
    notes.remove(notes.size() - 1);
    storage.save();
    return true;
  }

  private static DevNoteStorage.NpcNote selected(Player player) {
    String uuid = selection.get(player.getUUID());
    return uuid == null ? null : storage.find(uuid);
  }

  // ── Smoke test ────────────────────────────────────────────────────────────────

  /** Read the compiled checklist (data/cobblemon_initiative/smoketest_items.json) from the jar. */
  private static void loadSmokeItems() {
    SMOKE_ITEMS.clear();
    try (InputStream in = DevNoteInit.class.getClassLoader()
        .getResourceAsStream("data/cobblemon_initiative/smoketest_items.json")) {
      if (in == null) {
        LOGGER.warn("DevNote: smoketest_items.json not on classpath — smoke checklist empty.");
        return;
      }
      SmokeItem[] items = new Gson().fromJson(
        new InputStreamReader(in, StandardCharsets.UTF_8), SmokeItem[].class);
      if (items != null) {
        for (SmokeItem it : items) {
          if (it != null && it.id != null) SMOKE_ITEMS.add(it);
        }
      }
      LOGGER.info("DevNote loaded {} smoke-test item(s).", SMOKE_ITEMS.size());
    } catch (Exception e) {
      LOGGER.warn("DevNote: could not load smoketest_items.json: {}", e.getMessage());
    }
  }

  private static SmokeItem smokeItem(String id) {
    for (SmokeItem it : SMOKE_ITEMS) {
      if (it.id.equalsIgnoreCase(id)) return it;
    }
    return null;
  }

  private static String statusColor(String status) {
    if (status == null) return "§7";
    return switch (status) {
      case "PASS" -> "§a";
      case "FAIL" -> "§c";
      case "COMMENT" -> "§e";
      default -> "§7";
    };
  }

  /** Overview: how many marked, plus each item's id + status glyph + first words. */
  public static int smokeList(Player player) {
    if (SMOKE_ITEMS.isEmpty()) {
      player.sendSystemMessage(Component.literal(
        "§cNo smoke-test items compiled. Run content_compile after editing SMOKETEST.md."));
      return 0;
    }
    var results = storage.getSmoke();
    long done = SMOKE_ITEMS.stream().filter(it -> results.containsKey(it.id)).count();
    player.sendSystemMessage(Component.literal(
      "§b===== SMOKE TEST §7(" + done + "/" + SMOKE_ITEMS.size() + " marked) ====="));
    for (SmokeItem it : SMOKE_ITEMS) {
      DevNoteStorage.SmokeResult r = results.get(it.id);
      String glyph = r == null ? "§8☐" : statusColor(r.status) + "▣";
      player.sendSystemMessage(Component.literal(
        glyph + " §f" + it.id + " §7" + trim(it.text, 60)));
    }
    player.sendSystemMessage(Component.literal(
      "§7Use §f/ca smoke next §7for the first unmarked item, or §f/ca smoke show <id>§7."));
    return SMOKE_ITEMS.size();
  }

  /** Show the full text of one item (so you know what to test). */
  public static boolean smokeShow(Player player, String id) {
    SmokeItem it = smokeItem(id);
    if (it == null) {
      player.sendSystemMessage(Component.literal("§cNo smoke item §f" + id + "§c."));
      return false;
    }
    DevNoteStorage.SmokeResult r = storage.getSmoke().get(it.id);
    player.sendSystemMessage(Component.literal("§b" + it.id + " §f" + it.text));
    if (r != null) {
      player.sendSystemMessage(Component.literal(
        "  " + statusColor(r.status) + r.status
        + (r.note != null && !r.note.isBlank() ? " §7— " + r.note : "")));
    }
    return true;
  }

  /** Show the first item that has no result yet. */
  public static int smokeNext(Player player) {
    if (SMOKE_ITEMS.isEmpty()) {
      player.sendSystemMessage(Component.literal(
        "§cNo smoke-test items compiled. Run content_compile after editing SMOKETEST.md."));
      return 0;
    }
    var results = storage.getSmoke();
    for (SmokeItem it : SMOKE_ITEMS) {
      if (!results.containsKey(it.id)) {
        player.sendSystemMessage(Component.literal("§b» NEXT §f" + it.id + " §7" + it.text));
        player.sendSystemMessage(Component.literal(
          "§7  /ca smoke pass " + it.id + " §8| §7comment " + it.id
          + " <note> §8| §7fail " + it.id + " <note>"));
        return 1;
      }
    }
    player.sendSystemMessage(Component.literal(
      "§aAll " + SMOKE_ITEMS.size() + " smoke items marked. §7Run §f/ca smoke log§7 to dump them."));
    return 0;
  }

  /** Record PASS / COMMENT / FAIL (+ optional note) for an item. Returns false if id unknown. */
  public static boolean smokeMark(Player player, String id, String status, String note) {
    SmokeItem it = smokeItem(id);
    if (it == null) {
      player.sendSystemMessage(Component.literal(
        "§cNo smoke item §f" + id + "§c. Try /ca smoke list."));
      return false;
    }
    DevNoteStorage.SmokeResult r = new DevNoteStorage.SmokeResult();
    r.status = status;
    r.note = (note != null && !note.isBlank()) ? note : null;
    storage.getSmoke().put(it.id, r);
    storage.save();
    player.sendSystemMessage(Component.literal(
      statusColor(status) + status + " §7" + it.id
      + (r.note != null ? " §f— " + r.note : "")));
    return true;
  }

  /** Dump every recorded result in a copy-pasteable form (paste back to fill SMOKETEST.md). */
  public static int smokeLog(Player player) {
    var results = storage.getSmoke();
    player.sendSystemMessage(Component.literal(
      "§b===== SMOKE RESULTS (" + results.size() + "/" + SMOKE_ITEMS.size()
      + ") — copy the lines below ====="));
    if (results.isEmpty()) {
      player.sendSystemMessage(Component.literal("§7(nothing marked yet)"));
    }
    // Emit in checklist order so the paste reads top-to-bottom.
    for (SmokeItem it : SMOKE_ITEMS) {
      DevNoteStorage.SmokeResult r = results.get(it.id);
      if (r == null) continue;
      String line = it.id + " " + r.status + (r.note != null ? " — " + r.note : "");
      player.sendSystemMessage(Component.literal("§f" + line));
    }
    // Any results whose id no longer matches a compiled item (checklist changed).
    for (var e : results.entrySet()) {
      if (smokeItem(e.getKey()) == null) {
        DevNoteStorage.SmokeResult r = e.getValue();
        player.sendSystemMessage(Component.literal(
          "§8" + e.getKey() + " " + r.status + (r.note != null ? " — " + r.note : "") + " (stale)"));
      }
    }
    player.sendSystemMessage(Component.literal("§b===== end ====="));
    return results.size();
  }

  public static int smokeReset(Player player) {
    int n = storage.getSmoke().size();
    storage.getSmoke().clear();
    storage.save();
    return n;
  }

  // ── Victini gate check ──────────────────────────────────────────────────────────

  /** Dev readout: is @s currently eligible for the Victor -> Victini transform, and why not?
   *  Mirrors the gate in victor_apprentice.json (present victor_hint + docs_filed + lane_done
   *  + census_refused + bought_magikarp). */
  public static void victiniStatus(net.minecraft.server.level.ServerPlayer p) {
    var tags = p.getTags();
    boolean hint   = tags.contains("victor_hint");
    boolean file   = tags.contains("docs_filed");
    boolean lane   = tags.contains("lane_done");
    boolean census = tags.contains("census_refused");
    boolean karp   = tags.contains("bought_magikarp");
    boolean transformed = tags.contains("victor_transformed");
    boolean joined      = tags.contains("victini_joined");
    boolean valid = hint && file && lane && census && karp;

    p.sendSystemMessage(Component.literal("§b===== VICTINI GATE ====="));
    p.sendSystemMessage(gateLine("Heard of Victor from Kesi", "victor_hint", hint));
    p.sendSystemMessage(gateLine("Filed the Incomplete File (kept the papers)", "docs_filed", file));
    p.sendSystemMessage(gateLine("Completed Down the Lane", "lane_done", lane));
    p.sendSystemMessage(gateLine("Refused the Company census", "census_refused", census));
    p.sendSystemMessage(gateLine("Bought Deka's Magikarp (faith in the worthless fish)", "bought_magikarp", karp));
    if (joined) {
      p.sendSystemMessage(Component.literal("§d» Victini ALREADY JOINED (victini_joined)."));
    } else if (transformed) {
      p.sendSystemMessage(Component.literal("§d» Victor has TRANSFORMED — talk to the Victini to claim it."));
    } else if (valid) {
      p.sendSystemMessage(Component.literal("§a» VALID — talking to Victor will transform him."));
    } else {
      p.sendSystemMessage(Component.literal("§c» NOT valid yet — clear the §f✗§c lines above."));
    }
  }

  private static Component gateLine(String label, String tag, boolean ok) {
    return Component.literal((ok ? "§a ✔ " : "§c ✗ ") + "§f" + label + " §8(" + tag + ")");
  }

  // ── Formatting ────────────────────────────────────────────────────────────────

  private static double round(double v) {
    return Math.round(v * 10.0) / 10.0;
  }

  private static String fmt(double x, double y, double z) {
    return x + " " + y + " " + z;
  }

  private static String trim(String s, int max) {
    if (s == null) return "";
    return s.length() <= max ? s : s.substring(0, max - 1) + "…";
  }
}

package com.thecompanyinc.cobblemoninitiative.cutscene;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

/**
 * In-game cutscene authoring: fly the camera path yourself and capture it.
 *
 * <p>{@code /cutscene record add} captures your current EYE position + look angles as the
 * next keyframe (segment duration is auto-derived from the distance flown — tweak the
 * ticks in the saved JSON if a pan should linger). {@code undo}/{@code clear}/{@code status}
 * manage the take; {@code save <id>} writes a ready-to-play scene to
 * {@code config/cobblemon-initiative/cutscenes/<id>.json} — the manager loads that dir
 * FIRST, so {@code /cutscene play <id>} works immediately, and edit + {@code /cutscene
 * reload} iterates without a rebuild. Promote the file into
 * {@code src/main/resources/data/cobblemon_initiative/cutscenes/} verbatim to ship it.
 */
public final class CutsceneRecorder {

  /** Auto segment duration: clamp(distance x 1.5 ticks/block, 20..120). */
  private static final double TICKS_PER_BLOCK = 1.5;
  private static final int MIN_TICKS = 20;
  private static final int MAX_TICKS = 120;

  private record Frame(double x, double y, double z, float yaw, float pitch, int ticks) {}

  private static final Map<UUID, List<Frame>> takes = new HashMap<>();

  private CutsceneRecorder() {}

  public static int add(ServerPlayer player) {
    List<Frame> take = takes.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
    double x = player.getX(), y = player.getEyeY(), z = player.getZ();
    int ticks;
    if (take.isEmpty()) {
      ticks = 30; // ease-in from wherever the viewer stands at play time
    } else {
      Frame prev = take.get(take.size() - 1);
      double dist = Math.sqrt(Mth.square(x - prev.x()) + Mth.square(y - prev.y()) + Mth.square(z - prev.z()));
      ticks = (int) Mth.clamp(Math.round(dist * TICKS_PER_BLOCK), MIN_TICKS, MAX_TICKS);
    }
    take.add(new Frame(x, y, z, player.getYRot(), player.getXRot(), ticks));
    player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
      "§a[Cutscene] §7Keyframe §f%d§7 at §e%.1f %.1f %.1f§7 (yaw %.0f, pitch %.0f, %d ticks from previous).",
      take.size(), x, y, z, player.getYRot(), player.getXRot(), ticks)));
    return take.size();
  }

  public static boolean undo(ServerPlayer player) {
    List<Frame> take = takes.get(player.getUUID());
    if (take == null || take.isEmpty()) return false;
    take.remove(take.size() - 1);
    player.sendSystemMessage(Component.literal("§a[Cutscene] §7Removed the last keyframe (" + take.size() + " left)."));
    return true;
  }

  public static void clear(ServerPlayer player) {
    takes.remove(player.getUUID());
    player.sendSystemMessage(Component.literal("§a[Cutscene] §7Take cleared."));
  }

  public static void status(ServerPlayer player) {
    List<Frame> take = takes.get(player.getUUID());
    if (take == null || take.isEmpty()) {
      player.sendSystemMessage(Component.literal(
        "§a[Cutscene] §7No take in progress — fly to your first shot and run §frecord add§7."));
      return;
    }
    int total = take.stream().mapToInt(Frame::ticks).sum();
    player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
      "§a[Cutscene] §7Take: §f%d§7 keyframe(s), ~§f%.1fs§7. §8(add / undo / clear / save <id>)",
      take.size(), total / 20.0)));
  }

  /** Write the take as a playable scene into the override dir. */
  public static boolean save(ServerPlayer player, String id) {
    List<Frame> take = takes.get(player.getUUID());
    if (take == null || take.isEmpty()) {
      player.sendSystemMessage(Component.literal("§c[Cutscene] Nothing recorded — run §frecord add§c first."));
      return false;
    }
    JsonObject root = new JsonObject();
    root.addProperty("_comment",
      "Recorded in-game with /cutscene record. Tweak keyframe ticks for pacing, add cues "
        + "({tick,title/subtitle/sound/command}) and startTitle/doublePreset as needed, then "
        + "/cutscene reload + /cutscene play " + id + " to iterate. Promote into "
        + "src/main/resources/data/cobblemon_initiative/cutscenes/ to ship.");
    root.addProperty("relative", false);
    root.addProperty("skippable", true);
    JsonArray frames = new JsonArray();
    for (Frame f : take) {
      JsonObject o = new JsonObject();
      o.addProperty("x", round1(f.x()));
      o.addProperty("y", round1(f.y()));
      o.addProperty("z", round1(f.z()));
      o.addProperty("yaw", round1(f.yaw()));
      o.addProperty("pitch", round1(f.pitch()));
      o.addProperty("ticks", f.ticks());
      frames.add(o);
    }
    root.add("keyframes", frames);
    root.add("cues", new JsonArray());

    try {
      Path dir = CutsceneManager.overrideDir();
      Files.createDirectories(dir);
      Path file = dir.resolve(id + ".json");
      Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
      takes.remove(player.getUUID());
      player.sendSystemMessage(Component.literal(
        "§a[Cutscene] §7Saved §f" + take.size() + "§7 keyframe(s) to §f" + file
          + "§7 — try it now: §f/cutscene play " + id));
      return true;
    } catch (Exception e) {
      player.sendSystemMessage(Component.literal("§c[Cutscene] Save failed: " + e.getMessage()));
      return false;
    }
  }

  private static double round1(double v) {
    return Math.round(v * 10.0) / 10.0;
  }
}

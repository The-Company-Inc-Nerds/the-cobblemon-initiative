package com.thecompanyinc.cobblemoninitiative.devtools.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/**
 * Op implementations for the client test driver. Every op that touches game state runs on
 * the render thread ({@link Minecraft#execute}) and blocks the socket thread until done —
 * the synchronous-RCON model. Op vocabulary is documented in docs/TESTING_TOOLKIT.md and
 * mirrored by scripts/mc_client.py; keep the three in sync.
 *
 * <p>Screen access is GENERIC on purpose: widgets come from recursing
 * {@code Screen.children()} (any mod's screen — Easy NPC dialogs, Cobblemon summary, our
 * own pickers), and {@code screen.dump}'s {@code texts} adds a shallow reflection scan of
 * the screen's Component/String fields, which is how body text that never becomes a widget
 * (Easy NPC dialog prose) gets surfaced without compile-depending on Easy NPC classes.
 */
final class DriverOps {

  private static final int OP_TIMEOUT_SECONDS = 20;

  private DriverOps() {}

  static JsonObject handle(String op, JsonObject args) throws Exception {
    return switch (op) {
      case "ping" -> onGameThread(DriverOps::ping);
      case "state" -> onGameThread(DriverOps::state);
      case "screen.dump" -> onGameThread(DriverOps::screenDump);
      case "screen.click" -> onGameThread(() -> screenClick(args));
      case "screen.close" -> onGameThread(DriverOps::screenClose);
      case "screen.key" -> onGameThread(() -> screenKey(args));
      case "entity.list" -> onGameThread(() -> entityList(args));
      case "interact.entity" -> onGameThread(() -> interactEntity(args));
      case "attack.entity" -> onGameThread(() -> attackEntity(args));
      case "interact.block" -> onGameThread(() -> interactBlock(args));
      case "use.item" -> onGameThread(DriverOps::useItem);
      case "look.at" -> onGameThread(() -> lookAt(args));
      case "move.to" -> onGameThread(() -> moveTo(args));
      case "move.path" -> onGameThread(() -> movePath(args));
      case "move.status" -> onGameThread(DriverOps::moveStatus);
      case "move.stop" -> onGameThread(DriverOps::moveStop);
      case "input.key" -> onGameThread(() -> inputKey(args));
      case "hud.chat" -> hudChat(args); // ring buffer is thread-safe; no game thread needed
      case "hud.sidebar" -> onGameThread(DriverOps::hudSidebar);
      case "party" -> onGameThread(DriverOps::party);
      case "screenshot" -> onGameThread(() -> screenshot(args));
      default -> throw new IllegalArgumentException("unknown op: " + op);
    };
  }

  private static JsonObject onGameThread(Supplier<JsonObject> task) throws Exception {
    Minecraft mc = Minecraft.getInstance();
    CompletableFuture<JsonObject> future = new CompletableFuture<>();
    mc.execute(() -> {
      try {
        future.complete(task.get());
      } catch (Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future.get(OP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }

  // ---------------------------------------------------------------------------
  // Basics

  private static JsonObject ping() {
    Minecraft mc = Minecraft.getInstance();
    JsonObject out = new JsonObject();
    out.addProperty("pong", true);
    out.addProperty("player", mc.player == null ? null : mc.player.getName().getString());
    return out;
  }

  private static JsonObject state() {
    Minecraft mc = Minecraft.getInstance();
    JsonObject out = new JsonObject();
    LocalPlayer player = mc.player;
    out.addProperty("connected", player != null);
    out.addProperty(
      "screen", mc.screen == null ? null : mc.screen.getClass().getSimpleName());
    out.addProperty(
      "screenClass", mc.screen == null ? null : mc.screen.getClass().getName());
    if (player != null) {
      JsonObject p = new JsonObject();
      p.addProperty("name", player.getName().getString());
      p.addProperty("x", player.getX());
      p.addProperty("y", player.getY());
      p.addProperty("z", player.getZ());
      p.addProperty("yaw", player.getYRot());
      p.addProperty("pitch", player.getXRot());
      p.addProperty("health", player.getHealth());
      p.addProperty("food", player.getFoodData().getFoodLevel());
      p.addProperty("dimension", player.level().dimension().location().toString());
      out.add("player", p);
    }
    Object[] walk = Walker.status(mc);
    out.addProperty("moveActive", (Boolean) walk[0]);
    return out;
  }

  // ---------------------------------------------------------------------------
  // Screens

  private static JsonObject screenDump() {
    Minecraft mc = Minecraft.getInstance();
    JsonObject out = new JsonObject();
    Screen screen = mc.screen;
    if (screen == null) {
      out.addProperty("open", false);
      return out;
    }
    out.addProperty("open", true);
    out.addProperty("class", screen.getClass().getName());
    out.addProperty("title", screen.getTitle().getString());

    JsonArray widgets = new JsonArray();
    List<AbstractWidget> flat = new ArrayList<>();
    collectWidgets(screen.children(), flat);
    int i = 0;
    for (AbstractWidget w : flat) {
      JsonObject entry = new JsonObject();
      entry.addProperty("index", i++);
      entry.addProperty("type", w.getClass().getSimpleName());
      entry.addProperty("text", w.getMessage().getString());
      entry.addProperty("x", w.getX());
      entry.addProperty("y", w.getY());
      entry.addProperty("w", w.getWidth());
      entry.addProperty("h", w.getHeight());
      entry.addProperty("active", w.active);
      entry.addProperty("visible", w.visible);
      widgets.add(entry);
    }
    out.add("widgets", widgets);

    JsonArray texts = new JsonArray();
    for (String t : scanTexts(screen)) texts.add(t);
    out.add("texts", texts);
    return out;
  }

  private static void collectWidgets(
      List<? extends GuiEventListener> children, List<AbstractWidget> out) {
    for (GuiEventListener el : children) {
      if (el instanceof AbstractWidget w) out.add(w);
      if (el instanceof ContainerEventHandler c) collectWidgets(c.children(), out);
    }
  }

  /**
   * Shallow reflection sweep for on-screen prose that isn't widget-backed (dialog body
   * text). Own fields only, superclasses up to (exclusive) Screen; Component / String /
   * List-of-those values. Best-effort: unreadable fields are skipped silently.
   */
  private static List<String> scanTexts(Screen screen) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (Class<?> c = screen.getClass();
        c != null && c != Screen.class && out.size() < 80;
        c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        if (Modifier.isStatic(f.getModifiers())) continue;
        try {
          f.setAccessible(true);
          addText(out, f.get(screen), 0);
        } catch (Throwable ignored) {
          // inaccessible module / security — skip
        }
      }
    }
    return new ArrayList<>(out);
  }

  private static void addText(LinkedHashSet<String> out, Object value, int depth) {
    if (value == null || out.size() >= 80) return;
    if (value instanceof Component c) {
      String s = c.getString().strip();
      if (!s.isEmpty() && s.length() <= 800) out.add(s);
    } else if (value instanceof String s) {
      s = s.strip();
      if (!s.isEmpty() && s.length() <= 800 && !s.startsWith("textures/")) out.add(s);
    } else if (value instanceof List<?> list && depth < 1) {
      int n = 0;
      for (Object el : list) {
        if (n++ >= 40) break;
        addText(out, el, depth + 1);
      }
    }
  }

  private static JsonObject screenClick(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    Screen screen = mc.screen;
    if (screen == null) throw new IllegalStateException("no screen open");

    List<AbstractWidget> flat = new ArrayList<>();
    collectWidgets(screen.children(), flat);

    AbstractWidget target = null;
    if (args.has("index")) {
      int idx = args.get("index").getAsInt();
      if (idx < 0 || idx >= flat.size()) {
        throw new IllegalArgumentException("index " + idx + " out of range (" + flat.size() + " widgets)");
      }
      target = flat.get(idx);
    } else if (args.has("text")) {
      String needle = args.get("text").getAsString().toLowerCase(Locale.ROOT);
      for (AbstractWidget w : flat) {
        if (w.visible && w.getMessage().getString().toLowerCase(Locale.ROOT).contains(needle)) {
          target = w;
          break;
        }
      }
      if (target == null) throw new IllegalArgumentException("no visible widget matching '" + args.get("text").getAsString() + "'");
    } else {
      throw new IllegalArgumentException("screen.click needs text or index");
    }

    String label = target.getMessage().getString();
    if (target instanceof AbstractButton button) {
      button.onPress();
    } else {
      // Non-button widget: synthesize a click at its center (GUI-scaled coords).
      double cx = target.getX() + target.getWidth() / 2.0;
      double cy = target.getY() + target.getHeight() / 2.0;
      screen.mouseClicked(cx, cy, 0);
      screen.mouseReleased(cx, cy, 0);
    }
    JsonObject out = new JsonObject();
    out.addProperty("clicked", label);
    out.addProperty("type", target.getClass().getSimpleName());
    return out;
  }

  private static JsonObject screenClose() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen != null) mc.screen.onClose();
    JsonObject out = new JsonObject();
    out.addProperty("closed", true);
    return out;
  }

  private static JsonObject screenKey(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    Screen screen = mc.screen;
    if (screen == null) throw new IllegalStateException("no screen open");
    JsonObject out = new JsonObject();
    if (args.has("key")) {
      int key = args.get("key").getAsInt();
      out.addProperty("keyPressed", screen.keyPressed(key, 0, 0));
    }
    if (args.has("char")) {
      String s = args.get("char").getAsString();
      for (char ch : s.toCharArray()) screen.charTyped(ch, 0);
      out.addProperty("typed", s);
    }
    return out;
  }

  // ---------------------------------------------------------------------------
  // World interaction

  private static JsonObject entityList(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    double radius = args.has("radius") ? args.get("radius").getAsDouble() : 48.0;
    String match =
      args.has("match") ? args.get("match").getAsString().toLowerCase(Locale.ROOT) : null;

    List<Entity> found = new ArrayList<>();
    for (Entity e : mc.level.entitiesForRendering()) {
      if (e == player) continue;
      if (e.distanceTo(player) > radius) continue;
      if (match != null) {
        String name = e.getName().getString().toLowerCase(Locale.ROOT);
        String type = EntityType.getKey(e.getType()).toString();
        if (!name.contains(match) && !type.contains(match)) continue;
      }
      found.add(e);
    }
    found.sort((a, b) -> Float.compare(a.distanceTo(player), b.distanceTo(player)));

    JsonArray arr = new JsonArray();
    int n = 0;
    for (Entity e : found) {
      if (n++ >= 50) break;
      JsonObject entry = new JsonObject();
      entry.addProperty("uuid", e.getUUID().toString());
      entry.addProperty("name", e.getName().getString());
      entry.addProperty("type", EntityType.getKey(e.getType()).toString());
      entry.addProperty("x", e.getX());
      entry.addProperty("y", e.getY());
      entry.addProperty("z", e.getZ());
      entry.addProperty("dist", e.distanceTo(player));
      arr.add(entry);
    }
    JsonObject out = new JsonObject();
    out.add("entities", arr);
    out.addProperty("total", found.size());
    return out;
  }

  private static JsonObject interactEntity(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    Entity target = findEntity(mc, player, args);
    lookAtEntity(player, target);
    InteractionResult result = mc.gameMode.interact(player, target, InteractionHand.MAIN_HAND);
    player.swing(InteractionHand.MAIN_HAND);
    JsonObject out = new JsonObject();
    out.addProperty("target", target.getName().getString());
    out.addProperty("uuid", target.getUUID().toString());
    out.addProperty("dist", target.distanceTo(player));
    out.addProperty("result", result.toString());
    return out;
  }

  private static JsonObject attackEntity(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    Entity target = findEntity(mc, player, args);
    lookAtEntity(player, target);
    mc.gameMode.attack(player, target);
    player.swing(InteractionHand.MAIN_HAND);
    JsonObject out = new JsonObject();
    out.addProperty("target", target.getName().getString());
    out.addProperty("dist", target.distanceTo(player));
    return out;
  }

  private static JsonObject interactBlock(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    BlockPos pos = new BlockPos(
      args.get("x").getAsInt(), args.get("y").getAsInt(), args.get("z").getAsInt());
    Vec3 center = Vec3.atCenterOf(pos);
    lookAt(player, center);
    BlockHitResult hit = new BlockHitResult(
      center, net.minecraft.core.Direction.UP, pos, false);
    InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
    player.swing(InteractionHand.MAIN_HAND);
    JsonObject out = new JsonObject();
    out.addProperty("result", result.toString());
    return out;
  }

  private static JsonObject useItem() {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    InteractionResult result = mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
    JsonObject out = new JsonObject();
    out.addProperty("result", result.toString());
    return out;
  }

  private static JsonObject lookAt(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = requirePlayer(mc);
    if (args.has("uuid") || args.has("name")) {
      Entity target = findEntity(mc, player, args);
      lookAtEntity(player, target);
    } else {
      lookAt(player, new Vec3(
        args.get("x").getAsDouble(), args.get("y").getAsDouble(), args.get("z").getAsDouble()));
    }
    JsonObject out = new JsonObject();
    out.addProperty("yaw", player.getYRot());
    out.addProperty("pitch", player.getXRot());
    return out;
  }

  private static void lookAtEntity(LocalPlayer player, Entity target) {
    lookAt(player, target.getEyePosition());
  }

  private static void lookAt(LocalPlayer player, Vec3 target) {
    Vec3 eye = player.getEyePosition();
    double dx = target.x - eye.x;
    double dy = target.y - eye.y;
    double dz = target.z - eye.z;
    double horiz = Math.sqrt(dx * dx + dz * dz);
    player.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
    player.setXRot((float) -Math.toDegrees(Math.atan2(dy, horiz)));
  }

  private static Entity findEntity(Minecraft mc, LocalPlayer player, JsonObject args) {
    String uuid = args.has("uuid") ? args.get("uuid").getAsString() : null;
    String name =
      args.has("name") ? args.get("name").getAsString().toLowerCase(Locale.ROOT) : null;
    if (uuid == null && name == null) {
      throw new IllegalArgumentException("need uuid or name");
    }
    Entity best = null;
    float bestDist = Float.MAX_VALUE;
    for (Entity e : mc.level.entitiesForRendering()) {
      if (e == player) continue;
      if (uuid != null && !e.getUUID().toString().equalsIgnoreCase(uuid)) continue;
      if (name != null
          && !e.getName().getString().toLowerCase(Locale.ROOT).contains(name)) continue;
      float d = e.distanceTo(player);
      if (d < bestDist) {
        best = e;
        bestDist = d;
      }
    }
    if (best == null) {
      throw new IllegalArgumentException(
        "no loaded entity matches " + (uuid != null ? uuid : name)
          + " (client only sees ~tracked range — move closer or check entity.list)");
    }
    return best;
  }

  // ---------------------------------------------------------------------------
  // Movement / input

  private static JsonObject moveTo(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    requirePlayer(mc);
    double x = args.get("x").getAsDouble();
    double z = args.get("z").getAsDouble();
    double tol = args.has("tol") ? args.get("tol").getAsDouble() : 1.5;
    int timeout = args.has("timeoutTicks") ? args.get("timeoutTicks").getAsInt() : 1200;
    boolean sprint = args.has("sprint") && args.get("sprint").getAsBoolean();
    Walker.start(x, z, tol, timeout, sprint);
    JsonObject out = new JsonObject();
    out.addProperty("started", true);
    return out;
  }

  /**
   * {@code move.path} — follow a vanilla-A* node list from the server's {@code dev path}
   * probe. Nodes arrive as [[x,y,z],...] block ints; centered here (+0.5) for steering.
   */
  private static JsonObject movePath(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    requirePlayer(mc);
    JsonArray arr = args.getAsJsonArray("nodes");
    if (arr == null || arr.isEmpty()) {
      throw new IllegalArgumentException("move.path needs nodes=[[x,y,z],...]");
    }
    List<double[]> nodes = new ArrayList<>();
    for (var el : arr) {
      JsonArray p = el.getAsJsonArray();
      nodes.add(new double[] {
        p.get(0).getAsDouble() + 0.5, p.get(1).getAsDouble(), p.get(2).getAsDouble() + 0.5,
      });
    }
    double tol = args.has("tol") ? args.get("tol").getAsDouble() : 1.5;
    int timeout = args.has("timeoutTicks") ? args.get("timeoutTicks").getAsInt() : 2400;
    boolean sprint = args.has("sprint") && args.get("sprint").getAsBoolean();
    Walker.startPath(nodes, tol, timeout, sprint);
    JsonObject out = new JsonObject();
    out.addProperty("started", true);
    out.addProperty("nodes", nodes.size());
    return out;
  }

  private static JsonObject moveStatus() {
    Object[] s = Walker.status(Minecraft.getInstance());
    JsonObject out = new JsonObject();
    out.addProperty("active", (Boolean) s[0]);
    out.addProperty("result", (String) s[1]);
    out.addProperty("dist", (Double) s[2]);
    return out;
  }

  private static JsonObject moveStop() {
    Walker.stop();
    JsonObject out = new JsonObject();
    out.addProperty("stopped", true);
    return out;
  }

  private static JsonObject inputKey(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    String name = args.get("name").getAsString();
    boolean down = args.get("down").getAsBoolean();
    var key = switch (name) {
      case "forward" -> mc.options.keyUp;
      case "back" -> mc.options.keyDown;
      case "left" -> mc.options.keyLeft;
      case "right" -> mc.options.keyRight;
      case "jump" -> mc.options.keyJump;
      case "sneak" -> mc.options.keyShift;
      case "sprint" -> mc.options.keySprint;
      default -> throw new IllegalArgumentException(
        "unknown key '" + name + "' (forward/back/left/right/jump/sneak/sprint)");
    };
    key.setDown(down);
    JsonObject out = new JsonObject();
    out.addProperty("key", name);
    out.addProperty("down", down);
    return out;
  }

  // ---------------------------------------------------------------------------
  // HUD / party / evidence

  private static JsonObject hudChat(JsonObject args) {
    long since = args.has("since") ? args.get("since").getAsLong() : -1;
    JsonArray arr = new JsonArray();
    long last = since;
    for (HudLog.Entry e : HudLog.since(since)) {
      JsonObject entry = new JsonObject();
      entry.addProperty("seq", e.seq());
      entry.addProperty("kind", e.kind());
      entry.addProperty("text", e.text());
      arr.add(entry);
      last = e.seq();
    }
    JsonObject out = new JsonObject();
    out.add("entries", arr);
    out.addProperty("last", last);
    return out;
  }

  private static JsonObject hudSidebar() {
    Minecraft mc = Minecraft.getInstance();
    requirePlayer(mc);
    Scoreboard scoreboard = mc.level.getScoreboard();
    Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
    JsonObject out = new JsonObject();
    if (objective == null) {
      out.addProperty("present", false);
      return out;
    }
    out.addProperty("present", true);
    out.addProperty("title", objective.getDisplayName().getString());
    JsonArray lines = new JsonArray();
    for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
      JsonObject line = new JsonObject();
      line.addProperty("owner", entry.owner());
      line.addProperty("value", entry.value());
      PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
      String text = entry.display() != null
        ? entry.display().getString()
        : PlayerTeam.formatNameForTeam(team, Component.literal(entry.owner())).getString();
      line.addProperty("text", text);
      lines.add(line);
    }
    out.add("lines", lines);
    return out;
  }

  private static JsonObject party() {
    Minecraft mc = Minecraft.getInstance();
    requirePlayer(mc);
    ClientParty clientParty = CobblemonClient.INSTANCE.getStorage().getParty();
    JsonArray arr = new JsonArray();
    int slot = 0;
    for (Pokemon pokemon : clientParty) {
      if (pokemon != null) {
        JsonObject entry = new JsonObject();
        entry.addProperty("slot", slot);
        entry.addProperty("species", pokemon.getSpecies().getName());
        entry.addProperty("level", pokemon.getLevel());
        entry.addProperty("hp", pokemon.getCurrentHealth());
        arr.add(entry);
      }
      slot++;
    }
    JsonObject out = new JsonObject();
    out.add("party", arr);
    return out;
  }

  /**
   * Framebuffer read via MC's own {@link Screenshot} — immune to the compositor entirely
   * (no more grim-shoots-the-lock-screen; see reference-gui-screenshot-camera-workflow).
   */
  private static JsonObject screenshot(JsonObject args) {
    Minecraft mc = Minecraft.getInstance();
    String path = args.has("path") ? args.get("path").getAsString() : null;
    File file = path != null
      ? (new File(path).isAbsolute() ? new File(path) : new File(mc.gameDirectory, path))
      : new File(mc.gameDirectory, "screenshots/driver_" + System.currentTimeMillis() + ".png");
    file.getParentFile().mkdirs();
    try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
      JsonObject out = new JsonObject();
      out.addProperty("width", image.getWidth());
      out.addProperty("height", image.getHeight());
      image.writeToFile(file);
      out.addProperty("path", file.getAbsolutePath());
      return out;
    } catch (Exception e) {
      throw new RuntimeException("screenshot failed: " + e.getMessage(), e);
    }
  }

  private static LocalPlayer requirePlayer(Minecraft mc) {
    LocalPlayer player = mc.player;
    if (player == null || mc.level == null) {
      throw new IllegalStateException("not in a world yet");
    }
    return player;
  }
}

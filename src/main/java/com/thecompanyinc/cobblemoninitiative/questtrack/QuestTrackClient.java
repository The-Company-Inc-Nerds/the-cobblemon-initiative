package com.thecompanyinc.cobblemoninitiative.questtrack;

import com.mojang.blaze3d.platform.InputConstants;
import com.thecompanyinc.cobblemoninitiative.compat.journeymap.JourneyMapWaypointBridge;
import java.util.Objects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.glfw.GLFW;

/**
 * Client half of quest tracking: the ] / [ keybinds (which just send the player-facing
 * /cobblemon-initiative track commands), and the single-player in-process waypoint
 * poller (the pendingSacrifice client-poll is the precedent) — it reads the tracked
 * objective straight off the integrated server every tick and pushes changes into the
 * JourneyMap bridge on the CLIENT thread, exactly where JM requires its calls.
 */
public final class QuestTrackClient {

  private static final int WAYPOINT_COLOR = 0x55FFFF;

  private static KeyMapping trackNextKey;
  private static KeyMapping trackPrevKey;
  private static QuestTrackManager.TrackedWaypoint lastPushed;

  private QuestTrackClient() {}

  public static void init() {
    trackNextKey = KeyBindingHelper.registerKeyBinding(
      new KeyMapping(
        "key.cobblemon-initiative.track_next",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_BRACKET,
        "key.category.cobblemon-initiative"
      )
    );
    trackPrevKey = KeyBindingHelper.registerKeyBinding(
      new KeyMapping(
        "key.cobblemon-initiative.track_prev",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_LEFT_BRACKET,
        "key.category.cobblemon-initiative"
      )
    );

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (trackNextKey.consumeClick()) {
        sendTrackCommand(client, "next");
      }
      while (trackPrevKey.consumeClick()) {
        sendTrackCommand(client, "prev");
      }
      pushTrackedWaypoint(client);
    });
  }

  private static void sendTrackCommand(Minecraft client, String direction) {
    if (client.player != null) {
      client.player.connection.sendCommand("cobblemon-initiative track " + direction);
    }
  }

  private static void pushTrackedWaypoint(Minecraft client) {
    QuestTrackManager.TrackedWaypoint current = null;
    if (client.getSingleplayerServer() != null && client.player != null) {
      ServerPlayer player = client
        .getSingleplayerServer()
        .getPlayerList()
        .getPlayer(client.player.getUUID());
      if (player != null) {
        current = QuestTrackManager.current(player);
      }
    }

    if (Objects.equals(current, lastPushed)) return;
    if (current != null && current.hasPos()) {
      JourneyMapWaypointBridge.set(
        current.holder(),
        current.label(),
        current.x(),
        current.y(),
        current.z(),
        WAYPOINT_COLOR
      );
    } else {
      JourneyMapWaypointBridge.clear();
    }
    lastPushed = current;
  }
}

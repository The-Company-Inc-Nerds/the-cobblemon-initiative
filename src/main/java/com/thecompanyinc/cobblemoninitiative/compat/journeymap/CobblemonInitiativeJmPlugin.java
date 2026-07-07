package com.thecompanyinc.cobblemoninitiative.compat.journeymap;

import java.util.Objects;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ONLY class that imports journeymap.* — referenced solely from the "journeymap"
 * entrypoint in fabric.mod.json (JM queries FabricLoader.getEntrypoints("journeymap",
 * IJourneyMapPlugin.class); when JM is absent this class is never touched, so the soft
 * dependency stays clean). It installs the {@link JourneyMapWaypointBridge} sink and
 * maintains our single session waypoint for the tracked quest objective.
 */
@journeymap.api.v2.common.JourneyMapPlugin(apiVersion = "2.0.0")
public class CobblemonInitiativeJmPlugin implements IClientPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private IClientAPI api;
  /** Guid of our single waypoint; null = not currently in JM's store. */
  private String guid;
  /** Last requested waypoint — lets the MAPPING_STARTED resync re-create it without
   *  the poller's help (JM wipes non-persistent waypoints on every dimension change /
   *  world exit and fires MAPPING_STARTED once the store is ready again). */
  private volatile Desired desired;

  private record Desired(String questId, String name, double x, double y, double z, int rgb) {}

  // JM re-instantiates this class reflectively (it is constructed twice) — the no-arg
  // constructor must stay public and side-effect-free.
  public CobblemonInitiativeJmPlugin() {}

  @Override
  public String getModId() {
    return "cobblemon-initiative";
  }

  @Override
  public void initialize(IClientAPI clientApi) {
    this.api = clientApi;

    ClientEventRegistry.MAPPING_EVENT.subscribe(getModId(), event -> {
      if (event.getStage() == MappingEvent.Stage.MAPPING_STARTED) {
        guid = null;
        Desired current = desired;
        if (current != null) {
          Minecraft.getInstance().execute(() -> apply(current));
        }
      }
    });

    // Install the sink LAST — the client waypoint factory is not installed yet inside
    // initialize, so no waypoint may be created before MAPPING_STARTED / poller calls.
    JourneyMapWaypointBridge.install(new JourneyMapWaypointBridge.Sink() {
      @Override
      public void set(String questId, String name, double x, double y, double z, int rgb) {
        Desired next = new Desired(questId, name, x, y, z, rgb);
        desired = next;
        Minecraft.getInstance().execute(() -> apply(next));
      }

      @Override
      public void clear() {
        desired = null;
        Minecraft.getInstance().execute(() -> remove());
      }
    });
  }

  // createClientWaypoint is deprecated upstream but is the CLIENT-store factory path
  // (bytecode-verified: a distinct WaypointStore call, not a renamed createWaypoint) —
  // it is what keeps the waypoint session-only in JM's client store.
  @SuppressWarnings("deprecation")
  private void apply(Desired target) {
    if (api == null || !Objects.equals(desired, target)) return; // superseded
    try {
      BlockPos pos = BlockPos.containing(target.x(), target.y(), target.z());
      Waypoint waypoint = guid == null ? null : api.getWaypoint(getModId(), guid);
      if (waypoint == null) {
        // persistent=false — session-only, never written to the player's waypoint file.
        waypoint = WaypointFactory.createClientWaypoint(
          getModId(), pos, target.name(), "minecraft:overworld", false);
        waypoint.setColor(target.rgb());
        guid = waypoint.getGuid();
      } else {
        waypoint.setName(target.name());
        waypoint.setBlockPos(pos);
        waypoint.setColor(target.rgb());
      }
      api.addWaypoint(getModId(), waypoint); // add = create AND update
    } catch (Exception e) {
      LOGGER.warn("[Quest Track] Could not set the JourneyMap waypoint: {}", e.getMessage());
    }
  }

  private void remove() {
    if (api == null || guid == null) return;
    try {
      Waypoint waypoint = api.getWaypoint(getModId(), guid);
      if (waypoint != null) {
        api.removeWaypoint(getModId(), waypoint);
      }
    } catch (Exception e) {
      LOGGER.warn("[Quest Track] Could not clear the JourneyMap waypoint: {}", e.getMessage());
    }
    guid = null;
  }
}

package com.thecompanyinc.cobblemoninitiative.devtools.client;

import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * {@code move.to} / {@code move.path} tick steering — REAL movement, not tp, so latch-spawn
 * radii, safe-zone enters, and cutscene proximity triggers fire exactly as they would for a
 * human. Presses the forward key via {@code KeyMapping.setDown} (bypasses GLFW, works
 * unfocused), steers yaw directly at the target each tick, hops on horizontal collision.
 *
 * <p>Two modes: {@code move.to} is a single dumb straight leg (unchanged — scenarios chain
 * short legs). {@code move.path} follows a vanilla-A* node list from the server's
 * {@code dev path} probe (PathProbe): consecutive nodes are block-adjacent, so straight-line
 * steering node-to-node walks stairs, steps, and corners exactly the way mobs do. A node is
 * "reached" at 1.25 blocks in 3D (final node uses the caller's tolerance); adjacent-node
 * corner cutting is allowed when the NEXT node is already closer in 3D — Y must count, or
 * switchback staircases (stacked flights, same X/Z) collapse the node queue. Path mode adds stuck detection
 * (no distance progress for 60 ticks → result {@code stuck:node=i/n}) because a mid-route
 * snag should fail fast with a diagnosable node index, not burn the whole timeout.
 *
 * <p>Pauses (keys released, timeout still counting) while any Screen is open — vanilla
 * ignores movement keys under a screen anyway, so walking through a surprise dialog would
 * otherwise silently stall forever.
 */
final class Walker {

  private static final double NODE_REACH = 0.9;
  /** Corner-cut locality: a node may only be skipped if the player is this close to it. */
  private static final double CUT_NEAR_SQ = 2.0 * 2.0;
  private static final int STUCK_TICKS = 60;

  private static final Object LOCK = new Object();

  private static boolean active = false;
  private static double tx, tz;
  private static double tolerance;
  private static boolean sprint;
  private static int ticksLeft;
  private static String lastResult = null; // arrived | timeout | stopped | stuck:node=i/n

  // Path mode (null when running a plain move.to leg).
  private static List<double[]> path = null; // [x, y, z] block centers
  private static int doorCooldown = 0;
  private static int flatCollideTicks = 0;
  private static int pathIdx = 0;
  private static double bestDist = Double.MAX_VALUE;
  private static int noProgressTicks = 0;

  private Walker() {}

  static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(Walker::tick);
  }

  static void start(double x, double z, double tol, int timeoutTicks, boolean sprintRun) {
    synchronized (LOCK) {
      tx = x;
      tz = z;
      tolerance = Math.max(0.5, tol);
      ticksLeft = timeoutTicks;
      sprint = sprintRun;
      lastResult = null;
      path = null;
      active = true;
    }
  }

  static void startPath(List<double[]> nodes, double tol, int timeoutTicks, boolean sprintRun) {
    synchronized (LOCK) {
      path = nodes;
      pathIdx = 0;
      bestDist = Double.MAX_VALUE;
      noProgressTicks = 0;
      double[] last = nodes.get(nodes.size() - 1);
      tx = last[0];
      tz = last[2];
      tolerance = Math.max(0.5, tol);
      ticksLeft = timeoutTicks;
      sprint = sprintRun;
      lastResult = null;
      active = true;
    }
  }

  static void stop() {
    synchronized (LOCK) {
      if (active) {
        active = false;
        lastResult = "stopped";
      }
    }
    releaseKeys(Minecraft.getInstance());
  }

  /** [active, result, distance] snapshot for the move.status op (game thread). */
  static Object[] status(Minecraft mc) {
    synchronized (LOCK) {
      double dist = -1;
      if (mc.player != null) {
        double dx = tx - mc.player.getX();
        double dz = tz - mc.player.getZ();
        dist = Math.sqrt(dx * dx + dz * dz);
      }
      return new Object[] {active, lastResult, dist};
    }
  }

  private static void tick(Minecraft mc) {
    boolean run;
    synchronized (LOCK) {
      run = active;
    }
    if (!run) return;

    LocalPlayer player = mc.player;
    if (player == null) {
      finish(mc, "timeout");
      return;
    }
    if (mc.screen != null) {
      // Already within tolerance? Arrival must not become a timeout just because a
      // leftover dialog is up (verified live: tp landed ON the target, dialog open).
      synchronized (LOCK) {
        if (path == null) {
          double ddx = tx - player.getX();
          double ddz = tz - player.getZ();
          if (Math.sqrt(ddx * ddx + ddz * ddz) <= tolerance) {
            active = false;
            lastResult = "arrived";
            releaseKeys(mc);
            return;
          }
        }
      }
      releaseKeys(mc); // paused under a dialog; timeout keeps ticking
      countDown(mc);
      return;
    }

    // Pick the steering target: current path node, or the plain move.to target.
    double gx, gz;
    double gy = Double.NaN; // path mode only: current node's Y, gates the step-up hop
    boolean finalLeg;
    synchronized (LOCK) {
      if (path != null) {
        // Corner cutting: if the NEXT node is already closer, the current one is passed.
        // 3D distance, NOT horizontal — switchback staircases stack flights at the same
        // X/Z (verified live at the Sango lab), so 2D comparison "advances" into nodes
        // 12 blocks overhead and strands the walker at the bottom flight.
        // Y-gate on BOTH advance and reach: a node ≤1 block up sits within 3D reach
        // measured from below its lip — crediting it from underneath steers the walker
        // along the ledge line while still standing in the stairwell below (verified
        // live: repeatable fall off the lab landing). Nodes at/below the player may be
        // skipped or reached; nodes still above it must be climbed first.
        // Locality bound: only skip a node the player actually passed nearby. A mob path
        // guarantees connectivity between CONSECUTIVE nodes only; cutting from afar
        // beelines across exactly the geometry the route detoured around (verified live:
        // skipping the landing's L-approach steered into the blocked diagonal).
        while (pathIdx + 1 < path.size()
               && distSq(player, path.get(pathIdx)) <= CUT_NEAR_SQ
               && distSq(player, path.get(pathIdx + 1)) < distSq(player, path.get(pathIdx))
               && player.getY() >= path.get(pathIdx)[1] - 0.5) {
          advanceNode();
        }
        double[] node = path.get(pathIdx);
        double reach = pathIdx == path.size() - 1 ? tolerance : NODE_REACH;
        if (Math.sqrt(distSq(player, node)) <= reach && player.getY() >= node[1] - 0.5) {
          if (pathIdx == path.size() - 1) {
            path = null;
          } else {
            advanceNode();
          }
        }
        if (path == null) {
          gx = gz = 0;
          gy = Double.NaN;
          finalLeg = true; // fall through to arrival below
        } else {
          double[] cur = path.get(pathIdx);
          gx = cur[0];
          gy = cur[1];
          gz = cur[2];
          finalLeg = false;
          // Stuck detection: distance to the current node must keep improving.
          double d = Math.sqrt(distSq(player, cur));
          if (d < bestDist - 0.05) {
            bestDist = d;
            noProgressTicks = 0;
          } else if (++noProgressTicks >= STUCK_TICKS) {
            active = false;
            lastResult = "stuck:node=" + pathIdx + "/" + path.size();
            releaseKeys(mc);
            return;
          }
        }
      } else {
        gx = tx;
        gz = tz;
        gy = Double.NaN;
        finalLeg = false;
      }
    }

    if (finalLeg) {
      finish(mc, "arrived");
      return;
    }

    double dx = gx - player.getX();
    double dz = gz - player.getZ();
    double dist = Math.sqrt(dx * dx + dz * dz);
    boolean pathMode;
    synchronized (LOCK) {
      pathMode = path != null;
    }
    if (!pathMode && dist <= tolerance) {
      finish(mc, "arrived");
      return;
    }

    // MC yaw convention: 0 = +Z, increases clockwise → atan2(-dx, dz).
    player.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
    player.setXRot(0f);
    mc.options.keyUp.setDown(true);
    mc.options.keySprint.setDown(sprint && dist > 4);

    // Door reflex: the A* probe paths THROUGH closed wooden doors (setCanOpenDoors), so
    // the walker must actually open them — right-click the closed door/fence gate ahead,
    // once per cooldown (a second click would just close it again). Iron doors are
    // skipped: the probe's node evaluator keeps them blocked too, so no route uses them.
    if (doorCooldown > 0) doorCooldown--;
    if (player.horizontalCollision && doorCooldown <= 0 && mc.gameMode != null) {
      double len = Math.max(dist, 0.001);
      BlockPos ahead = BlockPos.containing(player.getX() + dx / len * 0.8,
                                           player.getY() + 0.4,
                                           player.getZ() + dz / len * 0.8);
      for (BlockPos p : new BlockPos[] {ahead, ahead.above()}) {
        BlockState st = player.level().getBlockState(p);
        boolean closedDoor = st.getBlock() instanceof DoorBlock
            && !st.getValue(DoorBlock.OPEN) && !st.is(Blocks.IRON_DOOR);
        boolean closedGate = st.getBlock() instanceof FenceGateBlock
            && !st.getValue(FenceGateBlock.OPEN);
        if (closedDoor || closedGate) {
          mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
              new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p, false));
          player.swing(InteractionHand.MAIN_HAND);
          doorCooldown = 10;
          break;
        }
      }
    }
    // Step-up hop: colliding into a block face while grounded → jump — but in path mode
    // only when the current node is actually above the player. A collision on a flat
    // segment usually means the walker is off the route line; hopping there mounts
    // furniture the route never climbs (verified live: the lab landing's roof rim).
    // EXCEPT: after sustained flat collision (slabs/carpets/stall counters the probe
    // deems walkable but the 0.6-step player snags on — verified live at the Hua Zhan
    // market), allow a fallback hop rather than riding the collision into stuck.
    boolean wantUp = Double.isNaN(gy) || gy > player.getY() + 0.5;
    if (player.horizontalCollision && player.onGround()) {
      flatCollideTicks = wantUp ? 0 : flatCollideTicks + 1;
    } else {
      flatCollideTicks = 0;
    }
    boolean fallbackHop = flatCollideTicks >= 8;
    if (fallbackHop) flatCollideTicks = 0;
    mc.options.keyJump.setDown(player.horizontalCollision && player.onGround()
                               && (wantUp || fallbackHop));

    countDown(mc);
  }

  private static void advanceNode() {
    pathIdx++;
    bestDist = Double.MAX_VALUE;
    noProgressTicks = 0;
  }

  /** Full 3D distance — path nodes carry Y, and Y is what disambiguates stacked flights. */
  private static double distSq(LocalPlayer player, double[] node) {
    double dx = node[0] - player.getX();
    double dy = node[1] - player.getY();
    double dz = node[2] - player.getZ();
    return dx * dx + dy * dy + dz * dz;
  }

  private static void countDown(Minecraft mc) {
    synchronized (LOCK) {
      if (--ticksLeft <= 0 && active) {
        active = false;
        lastResult = "timeout";
      } else {
        return;
      }
    }
    releaseKeys(mc);
  }

  private static void finish(Minecraft mc, String result) {
    synchronized (LOCK) {
      active = false;
      lastResult = result;
    }
    releaseKeys(mc);
  }

  private static void releaseKeys(Minecraft mc) {
    mc.options.keyUp.setDown(false);
    mc.options.keyJump.setDown(false);
    mc.options.keySprint.setDown(false);
  }
}

package com.thecompanyinc.cobblemoninitiative.devtools.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * {@code move.to} tick steering — REAL movement, not tp, so latch-spawn radii, safe-zone
 * enters, and cutscene proximity triggers fire exactly as they would for a human. Presses
 * the forward key via {@code KeyMapping.setDown} (bypasses GLFW, works unfocused), steers
 * yaw directly at the target each tick, hops on horizontal collision. Deliberately dumb:
 * no pathfinding — scenarios chain short straight legs (server tp covers long hauls).
 *
 * <p>Pauses (keys released, timeout still counting) while any Screen is open — vanilla
 * ignores movement keys under a screen anyway, so walking through a surprise dialog would
 * otherwise silently stall forever.
 */
final class Walker {

  private static final Object LOCK = new Object();

  private static boolean active = false;
  private static double tx, tz;
  private static double tolerance;
  private static boolean sprint;
  private static int ticksLeft;
  private static String lastResult = null; // arrived | timeout | stopped

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
      releaseKeys(mc); // paused under a dialog; timeout keeps ticking
      countDown(mc);
      return;
    }

    double dx = tx - player.getX();
    double dz = tz - player.getZ();
    double dist = Math.sqrt(dx * dx + dz * dz);
    if (dist <= tolerance) {
      finish(mc, "arrived");
      return;
    }

    // MC yaw convention: 0 = +Z, increases clockwise → atan2(-dx, dz).
    player.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
    player.setXRot(0f);
    mc.options.keyUp.setDown(true);
    mc.options.keySprint.setDown(sprint && dist > 4);
    // Step-up hop: colliding into a block face while grounded → jump.
    mc.options.keyJump.setDown(player.horizontalCollision && player.onGround());

    countDown(mc);
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

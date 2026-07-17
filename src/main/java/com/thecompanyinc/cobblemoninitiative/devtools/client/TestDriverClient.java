package com.thecompanyinc.cobblemoninitiative.devtools.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DEV-ONLY client test driver — a localhost JSON-lines socket that turns the game client
 * into a scriptable actor (the client-side twin of the RCON channel). Strips with the
 * devtools package at 1.0.0 (TODO §2 / docs/DEVTOOL_STRIP.md).
 *
 * <p>Dormant unless activated: set {@code CI_DRIVER_PORT} in the environment (survives the
 * gradle → game-JVM fork) or {@code -Dci.driver.port}. Without it this entrypoint returns
 * immediately — no socket, no event handlers, no mixin overhead (the Gui title mixin's
 * {@link HudLog#push} is a static-boolean no-op).
 *
 * <p>Why in-process instead of input injection: there is no xdotool/ydotool/wtype on the
 * dev box, and pixel-clicking is blind anyway. In-process gives SEMANTIC access — widget
 * labels, dialog text, entity UUIDs — so scenarios read like quest walkthroughs, not
 * coordinate scripts. See docs/TESTING_TOOLKIT.md § Client driver for the op table;
 * scripts/mc_client.py is the Python side.
 */
public class TestDriverClient implements ClientModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-driver");

  private static volatile boolean enabled = false;

  public static boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onInitializeClient() {
    String port = System.getProperty("ci.driver.port", System.getenv("CI_DRIVER_PORT"));
    if (port == null || port.isBlank()) {
      return; // release path: dead code until the env var opts in
    }
    int p;
    try {
      p = Integer.parseInt(port.trim());
    } catch (NumberFormatException e) {
      LOGGER.error("[driver] CI_DRIVER_PORT is not a number: '{}' — driver disabled", port);
      return;
    }
    enabled = true;
    HudLog.register();   // chat/system/overlay ring buffer (titles arrive via GuiTitleMixin)
    Walker.register();   // move.to tick steering
    new DriverServer(p).start();
    LOGGER.info("[driver] test driver listening on 127.0.0.1:{}", p);
  }
}

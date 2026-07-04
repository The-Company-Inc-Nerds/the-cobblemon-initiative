package com.thecompanyinc.cobblemoninitiative.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pack-only first-join auto-install.
 *
 * <p>The mrpack build drops a marker file ({@code config/cobblemon-initiative-autoinstall.json})
 * into the pack overrides. When the marker is present AND the world has never been
 * auto-installed (world-dir latch), the full {@code /cobblemon-initiative install run}
 * is dispatched a couple of seconds after the first player joins — finishing the
 * pieces the build cannot pre-bake (zones → safe zones, Map Frontiers overlays; the
 * frontier bridge needs a live player as owner).
 *
 * <p>A bare-mod install (no mrpack) has no marker, so nothing ever auto-runs — the
 * standalone contract stays: {@code install run} is manual and optional. The run is
 * idempotent, and on a pre-baked world hardcore is already set, so no kick fires
 * (the install disconnect is hardcoreFlipped-gated).
 */
public final class AutoInstall {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final String MARKER_FILE = "cobblemon-initiative-autoinstall.json";
  private static final String LATCH_FILE = "data/cobblemon_initiative_autoinstall.json";

  /** Ticks to wait after the first player joins before dispatching (~2s). */
  private static final int SETTLE_TICKS = 40;

  private static boolean armed;
  private static int ticksWithPlayer;

  private AutoInstall() {}

  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(AutoInstall::onServerStarted);
    ServerTickEvents.END_SERVER_TICK.register(AutoInstall::onTick);
  }

  private static void onServerStarted(MinecraftServer server) {
    armed = false;
    ticksWithPlayer = 0;

    Path marker = FabricLoader.getInstance().getConfigDir().resolve(MARKER_FILE);
    if (!Files.exists(marker)) {
      return; // bare-mod install — never auto-run
    }
    try (Reader reader = Files.newBufferedReader(marker)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      if (json == null || !json.has("enabled") || !json.get("enabled").getAsBoolean()) {
        return;
      }
    } catch (Exception e) {
      LOGGER.warn("[Auto-Install] Unreadable marker {} — skipping auto-install.", marker, e);
      return;
    }

    Path latch = server.getWorldPath(LevelResource.ROOT).resolve(LATCH_FILE);
    if (Files.exists(latch)) {
      return; // this world was already set up
    }

    armed = true;
    LOGGER.info("[Auto-Install] Marker present and world is fresh — install will run shortly after first join.");
  }

  private static void onTick(MinecraftServer server) {
    if (!armed) return;
    if (server.getPlayerList().getPlayers().isEmpty()) {
      ticksWithPlayer = 0;
      return;
    }
    if (++ticksWithPlayer < SETTLE_TICKS) return;
    armed = false;

    // Latch FIRST — even a partial run must never loop on every boot; install run is
    // idempotent and can always be re-run manually.
    writeLatch(server);

    CommandSourceStack src = server
      .createCommandSourceStack()
      .withPermission(4)
      .withSuppressedOutput();
    try {
      server.getCommands().performPrefixedCommand(src, "cobblemon-initiative install run");
      LOGGER.info("[Auto-Install] Dispatched 'cobblemon-initiative install run' on first join.");
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        p.sendSystemMessage(Component.literal(
          "§6[The Company, Inc.] §7This world has been provisioned. Welcome back to the ledger."
        ));
      }
    } catch (Exception e) {
      LOGGER.error("[Auto-Install] install run dispatch failed", e);
    }
  }

  private static void writeLatch(MinecraftServer server) {
    try {
      Path latch = server.getWorldPath(LevelResource.ROOT).resolve(LATCH_FILE);
      Files.createDirectories(latch.getParent());
      JsonObject json = new JsonObject();
      json.addProperty("done", true);
      json.addProperty("modVersion", FabricLoader.getInstance()
        .getModContainer("cobblemon-initiative")
        .map(c -> c.getMetadata().getVersion().getFriendlyString())
        .orElse("unknown"));
      try (Writer writer = Files.newBufferedWriter(latch)) {
        GSON.toJson(json, writer);
      }
    } catch (Exception e) {
      LOGGER.warn("[Auto-Install] Could not write the world latch — auto-install may re-run next boot.", e);
    }
  }
}

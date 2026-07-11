package com.thecompanyinc.cobblemoninitiative.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcPresetRefreshManager;
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
 * into the pack overrides. When the marker is present the auto-installer, a couple of seconds
 * after the first player joins, either:
 * <ul>
 *   <li>runs the full {@code /cobblemon-initiative install run} — on a world that has never
 *       been auto-installed (no world-dir latch) — finishing the pieces the build cannot
 *       pre-bake (zones → safe zones, Map Frontiers overlays, hardcore flip; the frontier
 *       bridge needs a live player as owner); or</li>
 *   <li>re-applies only the idempotent CONTENT refresh (NPC preset repaints + sight
 *       re-registration) — on a world that was already installed but at an OLDER mod/content
 *       version. This is how new gym-leader re-skins / new NPC bodies reach an existing save on
 *       join after a content update, WITHOUT re-running the full install
 *       (gamerules/hardcore/zones are once-per-world).</li>
 * </ul>
 *
 * <p>A bare-mod install (no mrpack) has no marker, so nothing ever auto-runs — the standalone
 * contract stays: {@code install run} is manual and optional. Both paths are idempotent, and on
 * a pre-baked world hardcore is already set, so no kick fires.
 *
 * <p>Map Frontiers overlays need no relog on the pack: build_mrpack pre-bakes install.json's
 * zones into the bundled world's {@code mapfrontiers/frontiers.dat}, which the very first join
 * handshake delivers; {@code install run} skips frontier creation when frontiers already exist
 * (see MapFrontiersBridge.hasExistingFrontiers), so nothing duplicates.
 */
public final class AutoInstall {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final String MARKER_FILE = "cobblemon-initiative-autoinstall.json";
  private static final String LATCH_FILE = "data/cobblemon_initiative_autoinstall.json";

  /** Ticks to wait after the first player joins before dispatching (~2s). */
  private static final int SETTLE_TICKS = 40;

  /** A never-installed world → dispatch the full install. */
  private static boolean armed;
  /** An installed world at an older content version → dispatch the content-only refresh. */
  private static boolean refreshArmed;
  private static int ticksWithPlayer;

  private AutoInstall() {}

  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(AutoInstall::onServerStarted);
    ServerTickEvents.END_SERVER_TICK.register(AutoInstall::onTick);
  }

  private static void onServerStarted(MinecraftServer server) {
    armed = false;
    refreshArmed = false;
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
    if (!Files.exists(latch)) {
      armed = true;
      LOGGER.info("[Auto-Install] Marker present and world is fresh — install will run shortly after first join.");
      return;
    }

    // Already installed once. Re-apply the idempotent content refresh if the version changed.
    String installed = readLatchVersion(latch);
    String current = currentModVersion();
    if (!current.equals(installed)) {
      refreshArmed = true;
      LOGGER.info("[Auto-Install] World installed at version {} but running {} — content refresh will run after first join.",
        installed, current);
    }
  }

  private static void onTick(MinecraftServer server) {
    if (!armed && !refreshArmed) return;
    if (server.getPlayerList().getPlayers().isEmpty()) {
      ticksWithPlayer = 0;
      return;
    }
    if (++ticksWithPlayer < SETTLE_TICKS) return;

    // Latch FIRST (records the current version) — even a partial run must never loop on every
    // boot; both paths are idempotent and can always be re-run manually.
    writeLatch(server);

    if (armed) {
      armed = false;
      dispatchFullInstall(server);
    } else {
      refreshArmed = false;
      dispatchContentRefresh(server);
    }
  }

  /** Fresh world: the full one-time provisioning. */
  private static void dispatchFullInstall(MinecraftServer server) {
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    try {
      server.getCommands().performPrefixedCommand(src, "cobblemon-initiative install run");
      LOGGER.info("[Auto-Install] Dispatched 'cobblemon-initiative install run' on first join.");
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        // Unattributed on purpose (tone rule 2026-07-06): the brand must not be the first
        // line of the run — the chill lands harder without a letterhead at minute zero.
        p.sendSystemMessage(Component.literal(
          "§7This world has been provisioned. Welcome back to the ledger."
        ));
      }
      // The opening flyover — the run's first frame. Skippable; suppressed output so a
      // missing scene (bare-mod worlds never reach this path) stays silent.
      ServerPlayer first = server.getPlayerList().getPlayers().stream().findFirst().orElse(null);
      if (first != null) {
        CommandSourceStack psrc = first.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        server.getCommands().performPrefixedCommand(psrc, "cutscene play opening");
      }
    } catch (Exception e) {
      LOGGER.error("[Auto-Install] install run dispatch failed", e);
    }
  }

  /** Already-installed world on a newer content version: re-apply only the idempotent NPC
   * preset repaint + sight re-registration (leader re-skins / new bodies), no full install. */
  private static void dispatchContentRefresh(MinecraftServer server) {
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    try {
      if (NpcPresetRefreshManager.hasBundledMap()) {
        NpcPresetRefreshManager.armFullRefresh(server);
      }
      // Repaint currently-loaded mapped NPCs now (the rest ride the armed chunk-load refresh),
      // and re-register the sight-driven NPCs. Both are idempotent.
      server.getCommands().performPrefixedCommand(src, "function cobblemon_initiative:update_npc_presets");
      server.getCommands().performPrefixedCommand(src, "function cobblemon_initiative:dialog/register_sight");
      LOGGER.info("[Auto-Install] Content refresh applied (NPC presets + sight) after a content-version change.");
    } catch (Exception e) {
      LOGGER.error("[Auto-Install] content refresh failed", e);
    }
  }

  private static void writeLatch(MinecraftServer server) {
    try {
      Path latch = server.getWorldPath(LevelResource.ROOT).resolve(LATCH_FILE);
      Files.createDirectories(latch.getParent());
      JsonObject json = new JsonObject();
      json.addProperty("done", true);
      json.addProperty("modVersion", currentModVersion());
      try (Writer writer = Files.newBufferedWriter(latch)) {
        GSON.toJson(json, writer);
      }
    } catch (Exception e) {
      LOGGER.warn("[Auto-Install] Could not write the world latch — auto-install may re-run next boot.", e);
    }
  }

  private static String readLatchVersion(Path latch) {
    try (Reader reader = Files.newBufferedReader(latch)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      if (json != null && json.has("modVersion")) {
        return json.get("modVersion").getAsString();
      }
    } catch (Exception e) {
      LOGGER.warn("[Auto-Install] Could not read latch version — treating the world as needing a refresh.", e);
    }
    return "unknown";
  }

  private static String currentModVersion() {
    return FabricLoader.getInstance()
      .getModContainer("cobblemon-initiative")
      .map(c -> c.getMetadata().getVersion().getFriendlyString())
      .orElse("unknown");
  }
}

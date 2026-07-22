package com.thecompanyinc.cobblemoninitiative.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads.InstallOverlayPayload;
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

  /** Ticks to wait after the first player joins before dispatching the install + opening
   * cutscene (~7s). Longer than a bare settle ON PURPOSE (bumped from 40/2s 2026-07-22): on a
   * fresh Sango spawn MapFrontiers fires its "Sango Town" region-enter TITLE, which was landing
   * ON TOP OF the opening cutscene's title card and overriding it. Holding the cutscene until
   * the region title has shown and faded lets the cutscene's title render last and stay. FLAG:
   * this races MapFrontiers' (configurable) title duration — tune up if it still gets clobbered. */
  private static final int SETTLE_TICKS = 140;

  /** Ticks the completed bar holds at 100% before the opening cutscene takes over — a beat so
   * the fill reads as "done", and a moment for install-spawned bodies to settle off-camera. */
  private static final int POST_INSTALL_HOLD = 16;

  /** A never-installed world → dispatch the full install. */
  private static boolean armed;
  /** An installed world at an older content version → dispatch the content-only refresh. */
  private static boolean refreshArmed;
  /** Marker {@code debug} flag: when false (the default) the install runs silently behind the
   * loading overlay — no [Initiative] repair chat, no "provisioned" line — so the cold open is
   * purely overlay → title → cutscene. Flip to true to surface the install log in chat. */
  private static boolean debug;
  private static int ticksWithPlayer;

  /** Fresh-install hand-off stage: after the silent provisioning we hold the full bar for a beat
   * ({@link #POST_INSTALL_HOLD}) before closing the overlay and playing the opening cutscene. */
  private enum Stage { IDLE, POST_INSTALL }
  private static Stage stage = Stage.IDLE;
  private static int postInstallTicks;

  private AutoInstall() {}

  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(AutoInstall::onServerStarted);
    ServerTickEvents.END_SERVER_TICK.register(AutoInstall::onTick);
  }

  private static void onServerStarted(MinecraftServer server) {
    armed = false;
    refreshArmed = false;
    debug = false;
    ticksWithPlayer = 0;
    stage = Stage.IDLE;
    postInstallTicks = 0;

    Path marker = FabricLoader.getInstance().getConfigDir().resolve(MARKER_FILE);
    if (!Files.exists(marker)) {
      return; // bare-mod install — never auto-run
    }
    try (Reader reader = Files.newBufferedReader(marker)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      if (json == null || !json.has("enabled") || !json.get("enabled").getAsBoolean()) {
        return;
      }
      // Opt-in verbose install: surfaces the [Initiative] repair chat + provisioned line.
      debug = json.has("debug") && json.get("debug").getAsBoolean();
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
    // Fresh-install hand-off: bar has filled, hold a beat, then reveal.
    if (stage == Stage.POST_INSTALL) {
      if (++postInstallTicks >= POST_INSTALL_HOLD) {
        stage = Stage.IDLE;
        closeOverlayAndReveal(server);
      }
      return;
    }

    if (!armed && !refreshArmed) return;
    if (server.getPlayerList().getPlayers().isEmpty()) {
      ticksWithPlayer = 0;
      return;
    }
    ticksWithPlayer++;

    if (armed) {
      // Fresh world: black the screen the instant the player is in, then provision behind it.
      if (ticksWithPlayer == 1) {
        ensureDebugScore(server);
        broadcastOverlay(server, InstallOverlayPayload.PHASE_OPEN, 0f);
        return;
      }
      if (ticksWithPlayer < SETTLE_TICKS) return; // let the bar animate + chunks stream
      // Latch FIRST (records the version) — even a partial run must never loop on every boot.
      writeLatch(server);
      armed = false;
      dispatchFullInstall(server); // silent unless debug; fills the bar via the "done" packet
      stage = Stage.POST_INSTALL;
      postInstallTicks = 0;
      return;
    }

    // Version-bump content refresh: idempotent repaint, no overlay/cutscene.
    if (ticksWithPlayer < SETTLE_TICKS) return;
    writeLatch(server);
    refreshArmed = false;
    ensureDebugScore(server);
    dispatchContentRefresh(server);
  }

  /** Fresh world: the full one-time provisioning, run silently behind the loading overlay. */
  private static void dispatchFullInstall(MinecraftServer server) {
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    try {
      server.getCommands().performPrefixedCommand(src, "cobblemon-initiative install run");
      LOGGER.info("[Auto-Install] Dispatched 'cobblemon-initiative install run' on first join.");
      if (debug) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
          // Unattributed on purpose (tone rule 2026-07-06): the brand must not be the first
          // line of the run — the chill lands harder without a letterhead at minute zero.
          p.sendSystemMessage(Component.literal(
            "§7This world has been provisioned. Welcome back to the ledger."
          ));
        }
      }
    } catch (Exception e) {
      LOGGER.error("[Auto-Install] install run dispatch failed", e);
    } finally {
      // Always fill the bar — even a failed/partial install must not strand the overlay.
      broadcastOverlay(server, InstallOverlayPayload.PHASE_DONE, 1f);
    }
  }

  /** Dismiss the overlay and play the opening flyover (its startTitle is the intended title
   * beat). Suppressed output so a missing scene (bare-mod worlds never reach here) stays silent. */
  private static void closeOverlayAndReveal(MinecraftServer server) {
    broadcastOverlay(server, InstallOverlayPayload.PHASE_CLOSE, 1f);
    ServerPlayer first = server.getPlayerList().getPlayers().stream().findFirst().orElse(null);
    if (first == null) return; // player left during the hold — nothing to reveal
    CommandSourceStack psrc = first.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(psrc, "cutscene play opening");
  }

  /** Ensure the ci_ambient objective exists and publish the debug flag onto {@code #debug} so
   * the install/repair mcfunctions can gate their {@code tellraw @a} flavor lines on it. */
  private static void ensureDebugScore(MinecraftServer server) {
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "scoreboard objectives add ci_ambient dummy");
    server.getCommands().performPrefixedCommand(src, "scoreboard players set #debug ci_ambient " + (debug ? 1 : 0));
  }

  private static void broadcastOverlay(MinecraftServer server, String phase, float progress) {
    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
      InitiativePayloads.sendInstallOverlay(p, phase, progress);
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
      // Also land any world-repair wave shipped with this update. Every wave is guarded by
      // its own #repair_aN flag, so on an already-repaired world only the NEW wave fires —
      // without this, a version-bump refresh re-imports presets but leaves e.g. the a13
      // duplicate-companion cleanup unrun until a manual install run.
      server.getCommands().performPrefixedCommand(src, "function cobblemon_initiative:install/repairs");
      LOGGER.info("[Auto-Install] Content refresh applied (NPC presets + sight + repairs) after a content-version change.");
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

package com.thecompanyinc.cobblemoninitiative.renderready;

import com.mojang.blaze3d.platform.InputConstants;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.HudConfig;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Join-hold driver: covers every world join with {@link BrandedHoldOverlay} (a real
 * {@code Minecraft#setOverlay} overlay — renders above everything, pauses nothing) until the
 * terrain around the camera has actually compiled, then fades out and reports "render ready"
 * to the server. On a fresh pack world the AutoInstall opening cutscene waits on that report,
 * so the cold open never plays over Sodium/BSL chunk pop-in; on ordinary sessions the hold is
 * purely client-side pop-in cover for the stream and the report is a server no-op.
 *
 * <h2>Readiness signal (bytecode-verified 2026-08-05, mojmap 1.21.1 + Sodium 0.6.13)</h2>
 * All three of the following, stable for {@link #STABLE_TICKS} consecutive client ticks, OR the
 * {@code overlayMaxSeconds} hard cap:
 * <ol>
 *   <li><b>Chunk data present</b> — every chunk column within {@code readyRadiusChunks} (clamped
 *       to the effective render distance) is in the client chunk cache
 *       ({@code ChunkSource.hasChunk}). Renderer-agnostic pure data arrival.</li>
 *   <li><b>Camera section compiled</b> — {@code LevelRenderer.isSectionCompiled(playerPos)}.
 *       Vanilla: ViewArea section non-null and {@code compiled != CompiledSection.UNCOMPILED}.
 *       Sodium 0.6.13 {@code @Overwrite}s it (core.render.world.LevelRendererMixin, intermediary
 *       {@code method_40050}) to {@code SodiumWorldRenderer.isSectionReady(x>>4,y>>4,z>>4)} →
 *       {@code RenderSectionManager.isSectionBuilt} → {@code RenderSection.built}. All-air
 *       sections flip {@code built} the moment their column loads (onSectionAdded →
 *       {@code LevelChunkSection.hasOnlyAir} → {@code BuiltSectionInfo.EMPTY} → setRenderState
 *       sets {@code built=true}), so a mid-air camera cannot wedge this probe.</li>
 *   <li><b>Build queue idle</b> — {@code LevelRenderer.hasRenderedAllSections()}. Vanilla:
 *       {@code SectionRenderDispatcher.isQueueEmpty}; Sodium overwrite ({@code method_3281}):
 *       {@code ChunkBuilder.isBuildQueueEmpty}.</li>
 * </ol>
 * A strict "every section in radius compiled" scan is NOT usable: both renderers only build
 * sections their occlusion graph can reach, so occluded/behind-terrain sections legitimately
 * never compile and a strict scan never passes. The queue-idle + N-tick-stability pair is the
 * defensible substitute (the queue oscillates empty↔non-empty while chunks stream in; stability
 * kills the transient-empty race). {@code countRenderedSections()} (Sodium: visible-chunk count)
 * is read only for the overlay's progress line, never for the gate.
 *
 * <h2>Why an Overlay needs babysitting (all bytecode-verified)</h2>
 * <ul>
 *   <li>{@code Overlay.isPauseScreen()} defaults to TRUE and {@code Minecraft.runTick} ORs it
 *       into the single-player pause flag — the overlay class MUST override it to false or the
 *       integrated server freezes the very install/cutscene this hold is waiting on.</li>
 *   <li>{@code KeyboardHandler.keyPress} still feeds {@code KeyMapping} state whenever
 *       {@code screen == null}, overlay or not — without the per-tick
 *       {@link KeyMapping#releaseAll()} the player walks blind behind the black.</li>
 *   <li>ESC calls {@code Minecraft.pauseGame} regardless of the overlay, opening a PauseScreen
 *       underneath → SP pause → wedge. The tick closes it and counts it as the skip-affordance
 *       press.</li>
 *   <li>{@code Minecraft.tick} still ticks {@code screen} under an overlay, so the vanilla
 *       level-loading screen ({@code ReceivingLevelScreen}) closes itself behind us, and
 *       {@code GameRenderer.render} still renders the level (gated only on
 *       {@code level != null}), so terrain keeps compiling behind the hold.</li>
 * </ul>
 *
 * <p>Fresh-connect definition (safety rule): the hold arms on
 * {@code ClientPlayConnectionEvents.JOIN} — fired once per connection, exactly like the phone
 * client's DISCONNECT counterpart — and shows on the first tick a {@code ClientLevel} exists.
 * Respawns and dimension changes reuse the connection and never re-fire JOIN, so they never
 * re-show the overlay.
 */
public final class RenderReadyClient {

  /** Consecutive ticks the three readiness gates must hold (kills queue-empty transients). */
  private static final int STABLE_TICKS = 10;
  /** Fade-out length once released — short, per the cold-open handoff (cutscene titles land
   * right after, so the reveal must not linger). */
  private static final int FADE_TICKS = 12;
  /** ESC / a click only reveals the Skip affordance after this long (10s) — the stream safety
   * valve should not be discoverable during a normal, healthy hold. */
  private static final int SKIP_REVEAL_TICKS = 10 * 20;
  /** Install-bar ease window, matched to the legacy LoadingOverlayScreen (≈ the AutoInstall
   * settle window) so the bar reads as real provisioning. */
  private static final int INSTALL_ANIM_TICKS = 130;
  private static final float INSTALL_HOLD_PROGRESS = 0.9f;

  private enum State { INACTIVE, HOLDING, FADING }

  private static State state = State.INACTIVE;
  /** Set on JOIN, consumed on the first tick with a live ClientLevel (the fresh-connect gate). */
  private static boolean joinPending;
  private static boolean readySent;
  /** The server's install overlay phases routed into this hold (never a second overlay):
   * installWait = OPEN seen and CLOSE not yet; installDone = DONE seen (bar fills). */
  private static boolean installWait;
  private static boolean installDone;
  private static boolean skipRevealed;
  private static boolean skipRequested;
  private static int holdTicks;
  private static int stableTicks;
  private static int fadeTicks;
  private static boolean prevEscDown;
  private static boolean prevMouseDown;
  // Progress readout for the overlay (display only — never part of the gate).
  private static int loadedChunks;
  private static int totalChunks = 1;
  private static int renderedSections;

  private RenderReadyClient() {}

  public static void init() {
    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> joinPending = true);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset(client));
    ClientTickEvents.END_CLIENT_TICK.register(RenderReadyClient::tick);
  }

  private static void tick(Minecraft mc) {
    if (joinPending && mc.level != null) {
      joinPending = false;
      if (HudConfig.get().holdOverlayOnJoin && mc.getOverlay() == null) {
        beginHold(mc);
      } else {
        // Hold disabled (or another overlay owns the frame): report ready at once — the
        // server's SETTLE_TICKS floor still paces the opening, we just don't add to it.
        sendReady(mc);
      }
    }
    switch (state) {
      case HOLDING -> tickHolding(mc);
      case FADING -> tickFading(mc);
      case INACTIVE -> {}
    }
  }

  private static void beginHold(Minecraft mc) {
    state = State.HOLDING;
    readySent = false;
    installWait = false;
    installDone = false;
    skipRevealed = false;
    skipRequested = false;
    holdTicks = 0;
    stableTicks = 0;
    fadeTicks = 0;
    prevEscDown = true;   // require a fresh press edge inside the hold
    prevMouseDown = true;
    loadedChunks = 0;
    totalChunks = 1;
    renderedSections = 0;
    // Race guard (never two covers): on a slow first frame the server's install PHASE_OPEN can
    // drain in the same client-tick task queue as handleLogin — BEFORE this hold arms — and
    // open the legacy LoadingOverlayScreen. Adopt its wait (the phases route here from now on)
    // and close it so the branded overlay is the only cover.
    if (mc.screen instanceof com.thecompanyinc.cobblemoninitiative.screen.LoadingOverlayScreen) {
      mc.setScreen(null);
      installWait = true;
    }
    mc.setOverlay(new BrandedHoldOverlay());
  }

  private static void tickHolding(Minecraft mc) {
    if (mc.level == null) { // connection died without a DISCONNECT reset yet
      reset(mc);
      return;
    }
    holdTicks++;

    // keyPress feeds KeyMappings whenever screen == null, overlay or not (verified) — keep
    // the player from walking/swinging blind behind the black.
    KeyMapping.releaseAll();

    // ESC opened the pause menu underneath (keyPress → pauseGame ignores the overlay) — close
    // it before the SP pause freezes the install we're covering, and count it as the press.
    boolean escEdge = false;
    if (mc.screen instanceof PauseScreen) {
      mc.setScreen(null);
      escEdge = true;
    }
    long window = mc.getWindow().getWindow();
    boolean escDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_ESCAPE);
    if (escDown && !prevEscDown) escEdge = true;
    prevEscDown = escDown;
    boolean mouseDown =
      GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    boolean clickEdge = mouseDown && !prevMouseDown;
    prevMouseDown = mouseDown;

    // Stream safety valve: after 10s, ESC or a click surfaces the small Skip button (and
    // frees the cursor so it can actually be clicked); a click ON the button skips.
    if (!skipRevealed && holdTicks >= SKIP_REVEAL_TICKS && (escEdge || clickEdge)) {
      skipRevealed = true;
      mc.mouseHandler.releaseMouse();
      clickEdge = false; // the reveal click must not immediately skip
    }
    if (skipRevealed && clickEdge && !skipRequested) {
      int[] r = BrandedHoldOverlay.skipRect(mc);
      double mx = guiMouseX(mc);
      double my = guiMouseY(mc);
      if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
        skipRequested = true;
        InitiativeInit.LOGGER.info("[RenderReady] Hold skipped by the showrunner after {} ticks.", holdTicks);
      }
    }

    boolean ready = probeReady(mc);
    int capTicks = Mth.clamp(HudConfig.get().overlayMaxSeconds, 5, 120) * 20;
    if (ready || skipRequested || holdTicks >= capTicks) {
      if (!readySent && holdTicks >= capTicks) {
        InitiativeInit.LOGGER.info(
          "[RenderReady] Hold cap ({}t) expired before the renderer settled — releasing.", capTicks);
      }
      sendReady(mc);
      // A fresh-install session keeps the cover up until the server's CLOSE phase (the
      // reveal must be the cutscene handoff, not a half-provisioned town).
      if (!installWait) beginFade(mc);
    }
  }

  /** The three-gate probe (see the class javadoc). Also refreshes the progress readout. */
  private static boolean probeReady(Minecraft mc) {
    if (mc.player == null || mc.level == null) {
      stableTicks = 0;
      return false;
    }
    int radius = Math.min(
      Mth.clamp(HudConfig.get().readyRadiusChunks, 2, 16),
      mc.options.getEffectiveRenderDistance());
    int cx = mc.player.chunkPosition().x;
    int cz = mc.player.chunkPosition().z;
    int total = 0;
    int loaded = 0;
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        total++;
        if (mc.level.getChunkSource().hasChunk(cx + dx, cz + dz)) loaded++;
      }
    }
    loadedChunks = loaded;
    totalChunks = total;
    renderedSections = mc.levelRenderer.countRenderedSections();

    boolean allChunks = loaded == total;
    boolean cameraCompiled = mc.levelRenderer.isSectionCompiled(mc.player.blockPosition());
    boolean queueIdle = mc.levelRenderer.hasRenderedAllSections();
    if (allChunks && cameraCompiled && queueIdle) {
      return ++stableTicks >= STABLE_TICKS;
    }
    stableTicks = 0;
    return false;
  }

  private static void tickFading(Minecraft mc) {
    if (++fadeTicks >= FADE_TICKS) {
      if (mc.getOverlay() instanceof BrandedHoldOverlay) mc.setOverlay(null);
      state = State.INACTIVE;
    }
  }

  private static void beginFade(Minecraft mc) {
    if (state == State.FADING) return;
    state = State.FADING;
    fadeTicks = 0;
    // If the skip affordance freed the cursor, hand it back to gameplay.
    if (skipRevealed && mc.screen == null) mc.mouseHandler.grabMouse();
  }

  private static void sendReady(Minecraft mc) {
    if (readySent || mc.getConnection() == null) return;
    readySent = true;
    ClientPlayNetworking.send(new InitiativePayloads.RenderReadyPayload());
  }

  /**
   * Route a server install-overlay phase into the active hold. Returns true when consumed —
   * the caller must NOT open the legacy {@code LoadingOverlayScreen} then (one overlay covers
   * both waits, never two stacked). Returns false when no hold owns the frame (hold disabled),
   * in which case the legacy screen path still applies. Client thread only.
   */
  public static boolean handleInstallPhase(String phase) {
    if (state == State.INACTIVE) return false;
    Minecraft mc = Minecraft.getInstance();
    switch (phase) {
      case InitiativePayloads.InstallOverlayPayload.PHASE_OPEN -> {
        installWait = true;
        installDone = false;
        if (state == State.FADING) { // raced our own release — re-cover for the install
          state = State.HOLDING;
          fadeTicks = 0;
        }
      }
      case InitiativePayloads.InstallOverlayPayload.PHASE_DONE -> installDone = true;
      case InitiativePayloads.InstallOverlayPayload.PHASE_CLOSE -> {
        // The server is revealing (cutscene starting) — release even if our own readiness
        // probe hasn't fired; holding longer would hide the opening itself.
        installWait = false;
        if (state == State.HOLDING) beginFade(mc);
      }
      default -> {}
    }
    return true;
  }

  private static void reset(Minecraft mc) {
    joinPending = false;
    state = State.INACTIVE;
    installWait = false;
    installDone = false;
    readySent = false;
    skipRevealed = false;
    skipRequested = false;
    if (mc.getOverlay() instanceof BrandedHoldOverlay) mc.setOverlay(null);
  }

  // ─── Overlay-facing state (render only) ───────────────────────────────────────────

  static float overlayAlpha(float partialTick) {
    if (state == State.FADING) {
      return Mth.clamp(1f - (fadeTicks + partialTick) / FADE_TICKS, 0f, 1f);
    }
    return 1f;
  }

  static int holdTicks() {
    return holdTicks;
  }

  static boolean installActive() {
    return installWait || installDone;
  }

  /** Eased provisioning-bar target, matched to the legacy screen's feel: creep to 90% over
   * the settle window, snap to 100% on the server's "done". */
  static float installBarTarget() {
    if (installDone) return 1f;
    float f = Math.min(1f, holdTicks / (float) INSTALL_ANIM_TICKS);
    return INSTALL_HOLD_PROGRESS * (1f - (float) Math.pow(1f - f, 3));
  }

  static boolean skipRevealed() {
    return skipRevealed;
  }

  static int terrainPercent() {
    return Mth.clamp(loadedChunks * 100 / Math.max(1, totalChunks), 0, 100);
  }

  static int sectionsOnline() {
    return renderedSections;
  }

  static double guiMouseX(Minecraft mc) {
    return mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth()
      / Math.max(1, mc.getWindow().getScreenWidth());
  }

  static double guiMouseY(Minecraft mc) {
    return mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight()
      / Math.max(1, mc.getWindow().getScreenHeight());
  }
}

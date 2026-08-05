package com.thecompanyinc.cobblemoninitiative.graphics;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side engine for the HIGH/LOW graphics toggle. A single {@link #applyMode} does all three
 * axes — Iris shaders (via {@link IrisBridge}), the 256x/32x texture pack (via {@link PackRepository}),
 * and the vanilla video sliders (via {@link Options}) — reading its values from {@link GraphicsPresetConfig}.
 *
 * <p>Three ways in, one converging state:
 * <ul>
 *   <li><b>{@code /gfx high|low|toggle|status}</b> and the (unbound) keybind — in-world, swap everything.
 *   <li><b>The FancyMenu main-menu button</b> — it only swaps the resource pack (its one rock-solid
 *       native action). A throttled tick here {@link #detectMode() detects} that swap (on the title
 *       screen or in-world) and syncs shaders + video to match. The active pack is the source of truth.
 * </ul>
 *
 * <p>Iris can't select a shaderpack by name through its public API, so "HIGH" just re-enables the
 * already-selected BSL and "LOW" turns shaders off — see {@link IrisBridge}.
 */
public final class GraphicsPresetManager {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  /** Poll cadence for external pack swaps. 20 ticks ≈ 1s — cheap, and the menu isn't latency-critical. */
  private static final int DETECT_INTERVAL_TICKS = 20;

  private static KeyMapping toggleKey;
  private static GraphicsMode lastKnownMode;
  private static int tickCounter;
  private static boolean nullReasserted;

  private GraphicsPresetManager() {}

  public static void init() {
    // Seed from the persisted choice so a matching pack at startup doesn't force a needless reload.
    lastKnownMode = GraphicsPresetConfig.get().savedMode();

    toggleKey = KeyBindingHelper.registerKeyBinding(
      new KeyMapping(
        "key.cobblemon-initiative.graphics_toggle",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN, // unbound by default; players can bind it in Controls
        "key.category.cobblemon-initiative"
      )
    );

    ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
      dispatcher.register(
        ClientCommandManager.literal("gfx")
          .then(ClientCommandManager.literal("high").executes(ctx -> {
            applyMode(GraphicsMode.HIGH, true, true);
            return 1;
          }))
          .then(ClientCommandManager.literal("low").executes(ctx -> {
            applyMode(GraphicsMode.LOW, true, true);
            return 1;
          }))
          .then(ClientCommandManager.literal("toggle").executes(ctx -> {
            GraphicsMode next = currentMode().other();
            applyMode(next, true, true);
            return 1;
          }))
          .then(ClientCommandManager.literal("status").executes(ctx -> {
            ctx.getSource().sendFeedback(Component.literal(
              "§bGraphics mode: §f" + currentMode().name() + " §7(" + shaderLabel(currentMode()) + ")"));
            return 1;
          }))
      ));

    ClientTickEvents.END_CLIENT_TICK.register(GraphicsPresetManager::onTick);

    LOGGER.info("[Graphics] High/Low toggle ready (mode={}, iris={}).",
      lastKnownMode, IrisBridge.isIrisLoaded());
  }

  private static void onTick(Minecraft client) {
    if (toggleKey != null) {
      while (toggleKey.consumeClick()) {
        applyMode(currentMode().other(), true, true);
      }
    }
    // Throttled watch for a pack swap done outside this class (FancyMenu button, pack screen).
    if (++tickCounter < DETECT_INTERVAL_TICKS) return;
    tickCounter = 0;

    GraphicsMode detected = detectMode(client);
    if (detected != null) {
      nullReasserted = false;
      if (detected != lastKnownMode) {
        LOGGER.info("[Graphics] Pack swap detected → syncing to {}.", detected);
        applyMode(detected, true, false); // normalise the selection + reload, sync shaders/video
      }
    } else if (!nullReasserted) {
      // No managed pack active — a fresh session, or the single toggle button turned the only pack
      // off because the 256x isn't installed (its TOGGLE was a no-op). Assert the saved mode; HIGH
      // auto-clamps to LOW without the 256x, so a player without the paid pack simply stays on LOW.
      nullReasserted = true;
      LOGGER.info("[Graphics] No managed pack active → asserting saved mode.");
      applyMode(GraphicsPresetConfig.get().savedMode(), true, false);
    }
  }

  /** The mode currently in effect: the active pack if recognisable, else the persisted choice. */
  public static GraphicsMode currentMode() {
    GraphicsMode detected = detectMode(Minecraft.getInstance());
    return detected != null ? detected : GraphicsPresetConfig.get().savedMode();
  }

  /**
   * Read the active resource pack to infer the mode; null if neither managed pack is enabled.
   * HD is checked first so that if both are momentarily enabled (the "High" button only enables
   * the 256x pack, it doesn't disable the 32x) the mode reads as HIGH and gets normalised.
   */
  private static GraphicsMode detectMode(Minecraft client) {
    if (client == null) return null;
    PackRepository repo = client.getResourcePackRepository();
    if (repo == null) return null;
    GraphicsPresetConfig cfg = GraphicsPresetConfig.get();
    Collection<String> selected = repo.getSelectedIds();
    if (selected.contains(cfg.hdPackId)) return GraphicsMode.HIGH;
    if (selected.contains(cfg.lowPackId)) return GraphicsMode.LOW;
    return null;
  }

  /** True if the (paid, may-be-absent) 256x HD pack is present in resourcepacks/. */
  private static boolean hdPackAvailable(Minecraft client, GraphicsPresetConfig cfg) {
    PackRepository repo = client.getResourcePackRepository();
    return repo != null && repo.getAvailableIds().contains(cfg.hdPackId);
  }

  /**
   * Apply a preset. Runs on the client thread.
   *
   * @param swapPacks when true, also normalise the texture-pack selection + reload; when false,
   *                  leave the packs as-is (used when reacting to an already-applied external swap —
   *                  though we still normalise for reliability).
   * @param announce  show an actionbar confirmation (in-world only).
   */
  public static void applyMode(GraphicsMode requested, boolean swapPacks, boolean announce) {
    Minecraft mc = Minecraft.getInstance();
    mc.execute(() -> {
      GraphicsPresetConfig cfg = GraphicsPresetConfig.get();
      PackRepository repo = mc.getResourcePackRepository();
      if (repo != null) repo.reload(); // discover a freshly-dropped pack (e.g. the paid 256x)

      // The 256x pack is a paid add-on that not every player owns. If it isn't installed, HIGH is
      // impossible — clamp to LOW so we never strand the player on a missing-texture "high" state.
      GraphicsMode mode = requested;
      if (mode == GraphicsMode.HIGH && !hdPackAvailable(mc, cfg)) {
        mode = GraphicsMode.LOW;
        if (announce && mc.player != null) {
          mc.player.displayClientMessage(Component.literal(
            "§eHigh-res (256x) pack not installed — staying on Low graphics."), true);
        }
        LOGGER.info("[Graphics] 256x pack absent → HIGH request downgraded to LOW.");
      }

      GraphicsPresetConfig.Preset p = cfg.preset(mode);
      IrisBridge.apply(p.shaders, cfg.highShaderPack, p.shaderOptions);
      if (cfg.applyVideoSettings) applyVideo(mc, p);
      if (swapPacks) swapResourcePack(mc, cfg, mode, repo);

      cfg.mode = mode.id();
      cfg.save();
      lastKnownMode = mode;

      if (announce && mc.player != null) {
        mc.player.displayClientMessage(
          Component.literal("§bGraphics: §f" + mode.name() + " §7(" + shaderLabel(mode) + ")"), true);
      }
      LOGGER.info("[Graphics] Applied {} preset.", mode);
    });
  }

  private static void applyVideo(Minecraft mc, GraphicsPresetConfig.Preset p) {
    Options o = mc.options;
    if (o == null) return;
    o.graphicsMode().set(parseGraphics(p.graphics));
    o.renderDistance().set(p.renderDistance);
    o.simulationDistance().set(p.simulationDistance);
    o.ambientOcclusion().set(p.ao);
    o.particles().set(parseParticles(p.particles));
    o.cloudStatus().set(parseClouds(p.clouds));
    o.entityDistanceScaling().set(p.entityDistanceScaling);
    o.biomeBlendRadius().set(p.biomeBlendRadius);
    o.mipmapLevels().set(p.mipmapLevels);
    o.save();
  }

  /** Rebuild the selected-pack list: keep everything except the two managed packs, append this mode's. */
  private static void swapResourcePack(Minecraft mc, GraphicsPresetConfig cfg, GraphicsMode mode,
                                       PackRepository repo) {
    if (repo == null) return;
    String target = (mode == GraphicsMode.HIGH) ? cfg.hdPackId : cfg.lowPackId;

    List<String> ids = new ArrayList<>(repo.getSelectedIds());
    ids.remove(cfg.hdPackId);
    ids.remove(cfg.lowPackId);
    if (repo.getAvailableIds().contains(target)) {
      ids.add(target); // last = highest priority, so its textures win
    } else {
      LOGGER.warn("[Graphics] Pack '{}' not found in resourcepacks/ — leaving textures unchanged.", target);
    }
    repo.setSelected(ids);

    // Mirror the selection into options.txt exactly as the vanilla pack screen does, then reload.
    Options o = mc.options;
    o.resourcePacks.clear();
    o.incompatibleResourcePacks.clear();
    for (Pack pack : repo.getSelectedPacks()) {
      if (!pack.isFixedPosition()) {
        o.resourcePacks.add(pack.getId());
        if (!pack.getCompatibility().isCompatible()) {
          o.incompatibleResourcePacks.add(pack.getId());
        }
      }
    }
    o.save();
    mc.reloadResourcePacks();
  }

  private static String shaderLabel(GraphicsMode mode) {
    GraphicsPresetConfig.Preset p = GraphicsPresetConfig.get().preset(mode);
    if (!p.shaders) return "shaders off";
    return mode == GraphicsMode.HIGH ? "BSL high" : "BSL performance";
  }

  private static GraphicsStatus parseGraphics(String s) {
    if (s == null) return GraphicsStatus.FANCY;
    return switch (s.trim().toLowerCase(java.util.Locale.ROOT)) {
      case "fast" -> GraphicsStatus.FAST;
      case "fabulous" -> GraphicsStatus.FABULOUS;
      default -> GraphicsStatus.FANCY;
    };
  }

  private static ParticleStatus parseParticles(String s) {
    if (s == null) return ParticleStatus.ALL;
    return switch (s.trim().toLowerCase(java.util.Locale.ROOT)) {
      case "minimal" -> ParticleStatus.MINIMAL;
      case "decreased" -> ParticleStatus.DECREASED;
      default -> ParticleStatus.ALL;
    };
  }

  private static CloudStatus parseClouds(String s) {
    if (s == null) return CloudStatus.FANCY;
    return switch (s.trim().toLowerCase(java.util.Locale.ROOT)) {
      case "off" -> CloudStatus.OFF;
      case "fast" -> CloudStatus.FAST;
      default -> CloudStatus.FANCY;
    };
  }
}

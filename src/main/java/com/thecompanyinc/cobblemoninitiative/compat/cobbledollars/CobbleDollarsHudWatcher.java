package com.thecompanyinc.cobblemoninitiative.compat.cobbledollars;

import com.thecompanyinc.cobblemoninitiative.config.HudConfig;
import fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt;
import java.math.BigInteger;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * Client-side only-on-change gate for the CobbleDollars balance HUD. Samples the
 * displayed balance every tick — {@code PlayerExtensionKt.getCobbleDollars} is the
 * exact read the overlay itself renders (bytecode: a duck-interface field get on the
 * player, no I/O) — and opens a show window on change; {@link CobbleDollarsHudMixin
 * the mixin} cancels the overlay draw while the window is closed.
 *
 * <p><b>Classload guard:</b> this class imports CobbleDollars types, so it is only
 * ever referenced from behind {@code FabricLoader.isModLoaded("cobbledollars")}
 * (NuzlockeClientInit) or from the mixin, which the mixin plugin skips when the mod
 * is absent — bare-mod dev without the jar never loads it.
 */
public final class CobbleDollarsHudWatcher {

  /** Last sampled balance; null = no baseline yet this session (next read = join read). */
  private static BigInteger lastBalance;

  /** Ticks left in the current show window; the HUD renders while &gt; 0. */
  private static int showTicks;

  private CobbleDollarsHudWatcher() {}

  public static void init() {
    ClientTickEvents.END_CLIENT_TICK.register(CobbleDollarsHudWatcher::tick);
    // Reset on disconnect so the next world gets its own join display instead of
    // diffing against a balance this server never owned (QuestTrackClient precedent).
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      lastBalance = null;
      showTicks = 0;
    });
  }

  private static void tick(Minecraft client) {
    if (client.player == null) return;
    BigInteger balance = PlayerExtensionKt.getCobbleDollars(client.player);
    if (!balance.equals(lastBalance)) {
      // Covers the first-join baseline too (lastBalance == null): the join read opens
      // one brief window, and the server's initial balance sync landing a tick or two
      // later just refreshes the SAME window — one continuous display, no second flash.
      lastBalance = balance;
      showTicks = HudConfig.get().cobbledollarsShowSeconds * 20;
    } else if (showTicks > 0) {
      showTicks--;
    }
  }

  /** True = the mixin cancels the overlay draw. Feature off = never hide (mod default). */
  public static boolean shouldHide() {
    return HudConfig.get().cobbledollarsOnChange && showTicks <= 0;
  }
}

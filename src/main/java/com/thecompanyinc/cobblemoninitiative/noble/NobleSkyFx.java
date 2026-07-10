package com.thecompanyinc.cobblemoninitiative.noble;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

/**
 * Per-player fake sky for the arena: weather and time are pure client-render state, so a
 * noble can fight under its own storm (Kyogre's downpour, Zapdos' thunderheads) or a harsh
 * drought noon (Groudon) without touching the real world. Fake WEATHER is sticky — vanilla
 * only re-broadcasts on a real weather change. Fake TIME is not: {@code MinecraftServer}
 * re-syncs every 20 ticks, so {@link #tickFakeTime} must run every encounter tick (it
 * re-sends on a 10-tick cadence). {@link #restore} is called from the manager's teardown,
 * which every exit path funnels through; a logout self-heals (vanilla re-sends both on join).
 */
public final class NobleSkyFx {

  /** The dayTime locked in while a DROUGHT arena is live (harsh clear noon). */
  private static final long DROUGHT_NOON = 6000L;

  private NobleSkyFx() {}

  /** Snap the player's rendered weather to the arena's ambient theme (one-shot, sticky). */
  public static void applyWeather(ServerPlayer player, AmbientTheme theme) {
    switch (theme) {
      case DOWNPOUR, BLIZZARD -> {
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0f));
      }
      case THUNDERSTORM -> {
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1.0f));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1.0f));
      }
      case DROUGHT -> player.connection.send(
        new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0f));
      default -> { /* SANDSTORM/GRAVITY/NONE: particles carry the vibe, real sky stays */ }
    }
  }

  /** Whether the theme locks the sky time (and therefore needs the per-tick resend). */
  public static boolean hasFakeTime(AmbientTheme theme) {
    return theme == AmbientTheme.DROUGHT;
  }

  /**
   * Re-assert the fake sky time. Must be called every encounter tick (INTRO, REALTIME,
   * STAGGERED and BATTLE) and sends every tick: vanilla re-broadcasts real time on its own
   * {@code tickCount % 20} cadence, which is offset from world game time by a fresh amount
   * each session — any sparser cadence here strobes the real sky through once per second.
   * The packet is 17 bytes and the encounter is single-player; per-tick is the robust choice.
   */
  public static void tickFakeTime(ServerPlayer player, ServerLevel level, AmbientTheme theme) {
    if (!hasFakeTime(theme)) return;
    player.connection.send(new ClientboundSetTimePacket(level.getGameTime(), DROUGHT_NOON, false));
  }

  /** Put the real sky back (weather + time). Safe to call on any exit path. */
  public static void restore(ServerPlayer player, ServerLevel level) {
    boolean raining = level.isRaining();
    player.connection.send(new ClientboundGameEventPacket(
      raining ? ClientboundGameEventPacket.START_RAINING : ClientboundGameEventPacket.STOP_RAINING, 0.0f));
    player.connection.send(new ClientboundGameEventPacket(
      ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, level.getRainLevel(1.0f)));
    player.connection.send(new ClientboundGameEventPacket(
      ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, level.getThunderLevel(1.0f)));
    player.connection.send(new ClientboundSetTimePacket(
      level.getGameTime(), level.getDayTime(),
      level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
  }
}

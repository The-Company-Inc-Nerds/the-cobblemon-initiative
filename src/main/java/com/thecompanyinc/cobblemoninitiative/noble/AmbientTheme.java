package com.thecompanyinc.cobblemoninitiative.noble;

import java.util.Locale;
import java.util.Random;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * A per-tick arena aura that sets the ring's vibe: a particle wash around the player plus a
 * mild, periodically-refreshed status effect (so it never fully fades). Purely atmospheric
 * pressure — the noble's attacks carry the real damage. Reuses the shrine idioms
 * (refreshed blindness = the dark-gauntlet trick; slowness/freeze = the ice trial feel).
 */
public enum AmbientTheme {
  NONE(null, 0),
  DROUGHT(ParticleTypes.FLAME, 0),
  DOWNPOUR(ParticleTypes.SPLASH, 80),
  BLIZZARD(ParticleTypes.SNOWFLAKE, 60),
  SANDSTORM(ParticleTypes.ASH, 60),
  THUNDERSTORM(ParticleTypes.ELECTRIC_SPARK, 0),
  GRAVITY(ParticleTypes.ENCHANT, 60);

  private static final Random RNG = new Random();

  private final ParticleOptions particle;
  /** Ticks between periodic effect refreshes; 0 = cosmetic only. */
  private final int periodTicks;

  AmbientTheme(ParticleOptions particle, int periodTicks) {
    this.particle = particle;
    this.periodTicks = periodTicks;
  }

  /**
   * Runs each REALTIME tick for a participating player. {@code escalation} is the rage
   * tier (0 = calm): themes may intensify as the noble weakens — BLIZZARD holds the
   * player's frost vignette (ticksFrozen) at a fraction of the freeze threshold, always
   * strictly below it so freeze damage can never tick (hardcore safety).
   */
  public void tick(ServerPlayer player, ServerLevel level, NobleEncounterState state, int escalation) {
    if (this == NONE) return;

    tickParticlesOnly(player, level);

    if (this == BLIZZARD && escalation > 0) {
      float hold = escalation >= 2 ? 0.7f : 0.4f;
      int target = (int) (player.getTicksRequiredToFreeze() * hold);
      if (player.getTicksFrozen() < target) player.setTicksFrozen(target);
    }

    if (periodTicks <= 0) return;
    int t = state.getAmbientTimer() + 1;
    if (t >= periodTicks) {
      applyPeriodic(player);
      t = 0;
    }
    state.setAmbientTimer(t);
  }

  /** The particle wash alone — used during the INTRO so no debuff can land before the fight. */
  public void tickParticlesOnly(ServerPlayer player, ServerLevel level) {
    if (this == NONE || particle == null) return;
    for (int i = 0; i < 3; i++) {
      double px = player.getX() + (RNG.nextDouble() - 0.5) * 10.0;
      double py = player.getY() + RNG.nextDouble() * 4.0;
      double pz = player.getZ() + (RNG.nextDouble() - 0.5) * 10.0;
      level.sendParticles(particle, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }

  private void applyPeriodic(ServerPlayer player) {
    switch (this) {
      case DOWNPOUR -> add(player, MobEffects.DIG_SLOWDOWN, 100, 0);
      case BLIZZARD -> {
        add(player, MobEffects.MOVEMENT_SLOWDOWN, 80, 0);
        player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze(), player.getTicksFrozen() + 40));
      }
      case SANDSTORM -> add(player, MobEffects.BLINDNESS, 80, 0);
      case GRAVITY -> add(player, MobEffects.MOVEMENT_SLOWDOWN, 80, 1);
      default -> { /* cosmetic only */ }
    }
  }

  private static void add(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int dur, int amp) {
    player.addEffect(new MobEffectInstance(effect, dur, amp, false, false));
  }

  /** Clear any effects this theme may have applied (teardown). */
  public void clear(ServerPlayer player) {
    player.removeEffect(MobEffects.DIG_SLOWDOWN);
    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    player.removeEffect(MobEffects.BLINDNESS);
    player.setTicksFrozen(0);
  }

  public static AmbientTheme resolve(String key) {
    if (key == null || key.isBlank()) return NONE;
    String k = key.trim().toUpperCase(Locale.ROOT);
    for (AmbientTheme t : values()) {
      if (t.name().equals(k)) return t;
    }
    return NONE;
  }
}

package com.thecompanyinc.cobblemoninitiative.noble;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.joml.Vector3f;

/**
 * The themed cosmetics + effects for an element typing. Each attack primitive resolves its
 * particles, telegraph color, damage source, on-hit effect, and cast sound from the noble's
 * {@code element} — so one generic primitive reads as lava for Groudon, a tidal wave for
 * Kyogre, a blizzard for Articuno. Cast sounds are resource-location strings resolved at
 * play time (see {@code NobleEncounterManager#playSoundId}) to sidestep the SoundEvent /
 * Holder split in the vanilla registry.
 */
public enum ElementTheme {
  FIRE(ParticleTypes.FLAME, ParticleTypes.SMOKE, new Vector3f(1.0f, 0.35f, 0.05f), "minecraft:entity.blaze.shoot",
    "minecraft:block.fire.ambient", "minecraft:entity.generic.explode"),
  WATER(ParticleTypes.SPLASH, ParticleTypes.DRIPPING_WATER, new Vector3f(0.2f, 0.45f, 0.95f), "minecraft:entity.player.splash",
    "minecraft:block.bubble_column.upwards_ambient", "minecraft:entity.player.splash.high_speed"),
  ICE(ParticleTypes.SNOWFLAKE, ParticleTypes.SNOWFLAKE, new Vector3f(0.6f, 0.85f, 1.0f), "minecraft:block.glass.break",
    "minecraft:block.glass.place", "minecraft:block.glass.break"),
  ELECTRIC(ParticleTypes.ELECTRIC_SPARK, ParticleTypes.ELECTRIC_SPARK, new Vector3f(1.0f, 0.95f, 0.35f), "minecraft:entity.lightning_bolt.thunder",
    "minecraft:block.sculk_sensor.clicking", "minecraft:entity.lightning_bolt.impact"),
  DRAGON(ParticleTypes.DRAGON_BREATH, ParticleTypes.DRAGON_BREATH, new Vector3f(0.55f, 0.2f, 0.75f), "minecraft:entity.ender_dragon.growl",
    "minecraft:entity.ender_dragon.ambient", "minecraft:entity.dragon_fireball.explode"),
  GROUND(ParticleTypes.LAVA, ParticleTypes.ASH, new Vector3f(0.6f, 0.42f, 0.2f), "minecraft:entity.generic.explode",
    "minecraft:block.gravel.break", "minecraft:entity.generic.explode"),
  ROCK(ParticleTypes.POOF, ParticleTypes.ASH, new Vector3f(0.7f, 0.65f, 0.5f), "minecraft:block.stone.break",
    "minecraft:block.stone.break", "minecraft:block.anvil.land"),
  FLYING(ParticleTypes.CLOUD, ParticleTypes.CLOUD, new Vector3f(0.9f, 0.95f, 1.0f), "minecraft:entity.phantom.swoop",
    "minecraft:entity.phantom.flap", "minecraft:entity.breeze.wind_burst"),
  DARK(ParticleTypes.SQUID_INK, ParticleTypes.SMOKE, new Vector3f(0.25f, 0.1f, 0.35f), "minecraft:entity.wither.shoot",
    "minecraft:entity.warden.listening", "minecraft:entity.wither.hurt"),
  STEEL(ParticleTypes.CRIT, ParticleTypes.SMOKE, new Vector3f(0.75f, 0.78f, 0.82f), "minecraft:block.anvil.land",
    "minecraft:block.chain.place", "minecraft:block.anvil.place"),
  PSYCHIC(ParticleTypes.ENCHANT, ParticleTypes.ENCHANT, new Vector3f(0.95f, 0.4f, 0.9f), "minecraft:block.amethyst_block.chime",
    "minecraft:block.amethyst_block.resonate", "minecraft:block.amethyst_block.chime");

  private final ParticleOptions impactParticle;
  private final ParticleOptions ambientParticle;
  private final Vector3f telegraphColor;
  private final String castSoundId;
  /** The accelerating telegraph pulse played while an attack arms. */
  private final String windupSoundId;
  /** The element's detonation voice (replaces the universal generic explode). */
  private final String impactSoundId;

  ElementTheme(ParticleOptions impact, ParticleOptions ambient, Vector3f telegraph, String castSound,
               String windupSound, String impactSound) {
    this.impactParticle = impact;
    this.ambientParticle = ambient;
    this.telegraphColor = telegraph;
    this.castSoundId = castSound;
    this.windupSoundId = windupSound;
    this.impactSoundId = impactSound;
  }

  public ParticleOptions impactParticle() { return impactParticle; }
  public ParticleOptions ambientParticle() { return ambientParticle; }
  public Vector3f telegraphColor() { return telegraphColor; }
  public String castSoundId() { return castSoundId; }
  public String windupSoundId() { return windupSoundId; }
  public String impactSoundId() { return impactSoundId; }

  /** The damage source themed attacks deal. */
  public DamageSource damageSource(ServerPlayer target) {
    var ds = target.damageSources();
    return switch (this) {
      case FIRE -> ds.inFire();
      case WATER -> ds.drown();
      case ICE -> ds.freeze();
      case ELECTRIC -> ds.lightningBolt();
      case DRAGON -> ds.dragonBreath();
      case DARK, PSYCHIC -> ds.magic();
      default -> ds.generic();
    };
  }

  /** Apply the themed on-hit status/effect to a player struck by a themed attack. */
  public void applyOnHit(ServerPlayer target, int durationTicks) {
    switch (this) {
      case FIRE -> target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), durationTicks));
      case WATER -> {
        addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 0);
        addEffect(target, MobEffects.DIG_SLOWDOWN, durationTicks, 0);
      }
      case ICE -> {
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze(), target.getTicksFrozen() + durationTicks));
        addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 1);
      }
      case ELECTRIC -> addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 2);
      case DRAGON -> addEffect(target, MobEffects.WEAKNESS, durationTicks, 0);
      case GROUND -> {
        addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 0);
        addEffect(target, MobEffects.CONFUSION, Math.min(durationTicks, 60), 0);
      }
      case ROCK -> {
        addEffect(target, MobEffects.BLINDNESS, Math.min(durationTicks, 40), 0);
        addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 0);
      }
      case FLYING -> addEffect(target, MobEffects.LEVITATION, Math.min(durationTicks, 20), 1);
      case DARK -> {
        addEffect(target, MobEffects.BLINDNESS, durationTicks, 0);
        addEffect(target, MobEffects.DARKNESS, durationTicks, 0);
      }
      case STEEL -> addEffect(target, MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 2);
      case PSYCHIC -> {
        addEffect(target, MobEffects.CONFUSION, Math.min(durationTicks, 60), 0);
        addEffect(target, MobEffects.LEVITATION, Math.min(durationTicks, 15), 1);
      }
    }
  }

  private static void addEffect(ServerPlayer target, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
    target.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
  }

  /** Resolve an element key (e.g. "fire", "ground_fire") to a theme; unknown → FIRE. */
  public static ElementTheme resolve(String key) {
    if (key == null || key.isBlank()) return FIRE;
    String k = key.trim().toUpperCase(java.util.Locale.ROOT);
    for (ElementTheme t : values()) {
      if (t.name().equals(k)) return t;
    }
    // Blend key like "GROUND_FIRE" → first recognized token.
    for (String part : k.split("_")) {
      for (ElementTheme t : values()) {
        if (t.name().equals(part)) return t;
      }
    }
    return FIRE;
  }
}

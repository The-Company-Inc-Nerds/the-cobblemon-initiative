package com.thecompanyinc.cobblemoninitiative.noble;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Shared server-side effects for noble encounters: particle rings/bursts, registry-resolved
 * sounds, and player knockback. Kept in one place so the manager, the attack registry, and
 * the themes all speak the same idiom (the shrine {@code sendParticles}/{@code playSound}
 * pattern, generalized).
 */
public final class NobleFx {

  private NobleFx() {}

  /** Play a vanilla sound by its resource-location id (sidesteps the SoundEvent/Holder split). */
  public static void playSoundId(ServerLevel level, double x, double y, double z, String soundId, float volume, float pitch) {
    if (soundId == null || soundId.isBlank()) return;
    ResourceLocation rl = ResourceLocation.tryParse(soundId);
    if (rl == null) return;
    BuiltInRegistries.SOUND_EVENT.getOptional(rl).ifPresent(se ->
      level.playSound(null, x, y, z, (SoundEvent) se, SoundSource.HOSTILE, volume, pitch)
    );
  }

  /** A burst of a particle at a point. */
  public static void burst(ServerLevel level, ParticleOptions particle, double x, double y, double z,
                           int count, double spread, double speed) {
    level.sendParticles(particle, x, y, z, count, spread, spread, spread, speed);
  }

  /** Draw a colored dust ring (the arena boundary / a telegraph circle). */
  public static void drawRing(ServerLevel level, double cx, double cy, double cz, double radius,
                              Vector3f color, double[] heights) {
    DustParticleOptions dust = new DustParticleOptions(color, 1.4f);
    int steps = Math.max(20, (int) (radius * 3));
    for (int i = 0; i < steps; i++) {
      double a = (Math.PI * 2.0 * i) / steps;
      double x = cx + radius * Math.cos(a);
      double z = cz + radius * Math.sin(a);
      for (double h : heights) {
        level.sendParticles(dust, x, cy + h, z, 1, 0.0, 0.0, 0.0, 0.0);
      }
    }
  }

  /** Knock a server player away from (fromX, fromZ) with an upward kick. */
  public static void knockback(ServerPlayer player, double fromX, double fromZ, double strength, double up) {
    double dx = player.getX() - fromX;
    double dz = player.getZ() - fromZ;
    double len = Math.sqrt(dx * dx + dz * dz);
    if (len < 1.0e-4) {
      // Player is on top of the source — pick a deterministic-ish direction from yaw.
      double yaw = Math.toRadians(player.getYRot());
      dx = -Math.sin(yaw);
      dz = Math.cos(yaw);
      len = 1.0;
    }
    Vec3 add = new Vec3((dx / len) * strength, up, (dz / len) * strength);
    player.setDeltaMovement(player.getDeltaMovement().add(add));
    player.hurtMarked = true; // force the velocity change to sync to the client
  }

  /** Horizontal squared distance (cylindrical), ignoring Y — the SafeZone.contains shape. */
  public static double horizontalDistSq(double x1, double z1, double x2, double z2) {
    double dx = x1 - x2;
    double dz = z1 - z2;
    return dx * dx + dz * dz;
  }
}

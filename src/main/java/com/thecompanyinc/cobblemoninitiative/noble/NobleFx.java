package com.thecompanyinc.cobblemoninitiative.noble;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
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

  /**
   * Play a sound by its resource-location id. Registered events resolve through the
   * registry; unregistered ids (e.g. Cobblemon's asset-only species cries,
   * {@code cobblemon:pokemon.<species>.cry}) fall back to a variable-range event — the
   * sound packet carries the holder directly, so the client resolves it from its own
   * assets. Variable-range events project {@code 16 * max(volume, 1)} blocks, so volume
   * above 1 legitimately carries farther.
   */
  public static void playSoundId(ServerLevel level, double x, double y, double z, String soundId, float volume, float pitch) {
    if (soundId == null || soundId.isBlank()) return;
    ResourceLocation rl = ResourceLocation.tryParse(soundId);
    if (rl == null) return;
    SoundEvent se = BuiltInRegistries.SOUND_EVENT.getOptional(rl)
      .orElseGet(() -> SoundEvent.createVariableRangeEvent(rl));
    level.playSound(null, x, y, z, se, SoundSource.HOSTILE, volume, pitch);
  }

  /** A burst of a particle at a point. */
  public static void burst(ServerLevel level, ParticleOptions particle, double x, double y, double z,
                           int count, double spread, double speed) {
    level.sendParticles(particle, x, y, z, count, spread, spread, spread, speed);
  }

  /** Draw a colored dust ring (the arena boundary / a telegraph circle). */
  public static void drawRing(ServerLevel level, double cx, double cy, double cz, double radius,
                              Vector3f color, double[] heights) {
    drawRing(level, cx, cy, cz, radius, color, heights, 1.4f);
  }

  /** Ring with an explicit dust size (telegraphs flash bigger + white in their final ticks). */
  public static void drawRing(ServerLevel level, double cx, double cy, double cz, double radius,
                              Vector3f color, double[] heights, float dustSize) {
    drawRing(level, cx, cy, cz, radius, (ParticleOptions) new DustParticleOptions(color, dustSize), heights);
  }

  /** Ring drawn with an arbitrary particle (a noble's authored {@code arena.boundaryParticle}). */
  public static void drawRing(ServerLevel level, double cx, double cy, double cz, double radius,
                              ParticleOptions particle, double[] heights) {
    int steps = Math.max(20, (int) (radius * 3));
    for (int i = 0; i < steps; i++) {
      double a = (Math.PI * 2.0 * i) / steps;
      double x = cx + radius * Math.cos(a);
      double z = cz + radius * Math.sin(a);
      for (double h : heights) {
        level.sendParticles(particle, x, cy + h, z, 1, 0.0, 0.0, 0.0, 0.0);
      }
    }
  }

  /**
   * Draw a localized vertical dust curtain arc on the ring — the "you are about to hit the
   * barrier" cue. {@code centerAngle} is the player's azimuth from the arena center;
   * {@code halfArc} the half-width in radians.
   */
  public static void drawArc(ServerLevel level, double cx, double cy, double cz, double radius,
                             double centerAngle, double halfArc, Vector3f color) {
    DustParticleOptions dust = new DustParticleOptions(color, 2.0f);
    int steps = Math.max(6, (int) (radius * halfArc));
    for (int i = 0; i <= steps; i++) {
      double a = centerAngle - halfArc + (2.0 * halfArc * i) / steps;
      double x = cx + radius * Math.cos(a);
      double z = cz + radius * Math.sin(a);
      for (double h = 0.0; h <= 3.0; h += 0.5) {
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

  /**
   * Directional hurt tilt: element damage sources carry no position, so vanilla's red tilt
   * is directionless. This override makes the camera tilt away from the attack origin
   * (cosmetic packet, no damage). Yaw convention matches vanilla hurtDir — no −90 offset.
   */
  public static void hurtTilt(ServerPlayer target, double fromX, double fromZ) {
    float yaw = (float) (Math.toDegrees(Math.atan2(fromZ - target.getZ(), fromX - target.getX())) - target.getYRot());
    target.connection.send(new ClientboundHurtAnimationPacket(target.getId(), yaw));
  }

  /** Horizontal squared distance (cylindrical), ignoring Y — the SafeZone.contains shape. */
  public static double horizontalDistSq(double x1, double z1, double x2, double z2) {
    double dx = x1 - x2;
    double dz = z1 - z2;
    return dx * dx + dz * dz;
  }
}

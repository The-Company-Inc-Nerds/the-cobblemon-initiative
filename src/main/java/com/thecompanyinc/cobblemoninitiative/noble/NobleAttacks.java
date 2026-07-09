package com.thecompanyinc.cobblemoninitiative.noble;

import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.NobleConfig;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterState.HazardZone;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterState.PendingBeam;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterState.PendingImpact;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterState.ProjectileBolt;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The attack primitive registry: one handler per JSON {@code type}, each themed by the
 * noble's {@link ElementTheme}. Handlers spawn transient combat objects (moving bolts,
 * telegraphed impacts, line beams, hazard zones) onto {@link NobleEncounterState}; the
 * manager advances them every REALTIME tick via {@link #tickTransients}.
 */
public final class NobleAttacks {

  private static final Random RNG = new Random();
  /** Default on-hit status duration (ticks). */
  private static final int EFFECT_TICKS = 60;
  private static final double[] IMPACT_RING_HEIGHTS = { 0.1, 0.5 };

  private NobleAttacks() {}

  /** Everything a handler needs to act. One participating player (single-player focus). */
  public static class Context {
    public final ServerLevel level;
    public final ServerPlayer target;
    public final LivingEntity body; // Phase-1 Easy NPC body; nullable if not resolved this tick
    public final NobleEncounterState state;
    public final NobleEncounterConfig config;
    public final ElementTheme element;
    public final double bodyX, bodyY, bodyZ;

    public Context(ServerLevel level, ServerPlayer target, LivingEntity body,
                   NobleEncounterState state, NobleEncounterConfig config, ElementTheme element) {
      this.level = level;
      this.target = target;
      this.body = body;
      this.state = state;
      this.config = config;
      this.element = element;
      if (body != null) {
        this.bodyX = body.getX(); this.bodyY = body.getY(); this.bodyZ = body.getZ();
      } else {
        this.bodyX = state.getArenaX(); this.bodyY = state.getArenaY(); this.bodyZ = state.getArenaZ();
      }
    }
  }

  // ── Dispatch ────────────────────────────────────────────────────────────────

  public static void runAttack(NobleEncounterConfig.Attack attack, Context ctx) {
    JsonObject p = attack.params;
    switch (attack.type == null ? "" : attack.type) {
      case "projectile" -> projectile(p, ctx);
      case "barrage_aoe" -> barrageAoe(p, ctx);
      case "beam" -> beam(p, ctx);
      case "hazard_zone" -> hazardZone(p, ctx);
      case "bolt_strike" -> boltStrike(p, ctx);
      case "dive_charge" -> diveCharge(p, ctx);
      case "stomp" -> stomp(p, ctx);
      default -> InitiativeInit.LOGGER.warn("[Noble] Unknown attack type: {}", attack.type);
    }
  }

  private static float mult() { return NobleConfig.get().getAttackDamageMultiplier(); }

  // ── Handlers ────────────────────────────────────────────────────────────────

  private static void projectile(JsonObject p, Context ctx) {
    int count = pInt(p, "count", 1);
    double speed = pDouble(p, "speed", 1.1);
    double spread = pDouble(p, "spread", 0.12);
    float dmg = (float) pDouble(p, "damage", 5.0);
    int life = pInt(p, "lifeTicks", 60);

    Vec3 from = new Vec3(ctx.bodyX, ctx.bodyY + 1.2, ctx.bodyZ);
    Vec3 dir = ctx.target.getEyePosition().subtract(from).normalize();
    NobleFx.burst(ctx.level, ctx.element.impactParticle(), from.x, from.y, from.z, 8, 0.2, 0.02);
    NobleFx.playSoundId(ctx.level, from.x, from.y, from.z, ctx.element.castSoundId(), 1.0f, 1.0f);

    for (int i = 0; i < count; i++) {
      Vec3 d = dir.add(rand(spread), rand(spread), rand(spread)).normalize().scale(speed);
      ProjectileBolt b = new ProjectileBolt();
      b.x = from.x; b.y = from.y; b.z = from.z;
      b.vx = d.x; b.vy = d.y; b.vz = d.z;
      b.ticksLeft = life;
      b.damage = dmg * mult();
      ctx.state.getBolts().add(b);
    }
  }

  private static void barrageAoe(JsonObject p, Context ctx) {
    int strikes = Math.max(1, pInt(p, "strikes", 1));
    String pattern = pStr(p, "pattern", "single");
    double radius = pDouble(p, "radius", 4.0);
    float dmg = (float) pDouble(p, "damage", 6.0);
    double kb = pDouble(p, "knockback", 1.0);
    int windup = pInt(p, "windupTicks", 20);
    int delay = pInt(p, "delayBetween", 6);
    boolean tracking = pBool(p, "tracking", false);

    for (int i = 0; i < strikes; i++) {
      Vec3 c = pickLocation(pattern, i, strikes, ctx, radius);
      PendingImpact im = new PendingImpact();
      im.x = c.x; im.y = c.y; im.z = c.z;
      im.radius = radius;
      im.totalWindup = windup;
      im.ticksLeft = windup + i * delay;
      im.damage = dmg * mult();
      im.knockback = kb;
      im.tracking = tracking && strikes == 1;
      ctx.state.getPendingImpacts().add(im);
    }
    NobleFx.playSoundId(ctx.level, ctx.bodyX, ctx.bodyY, ctx.bodyZ, ctx.element.castSoundId(), 0.9f, 0.8f);
  }

  private static void boltStrike(JsonObject p, Context ctx) {
    int count = Math.max(1, pInt(p, "count", 3));
    double radius = pDouble(p, "radius", 2.0);
    float dmg = (float) pDouble(p, "damage", 6.0);
    int windup = pInt(p, "windupTicks", 18);
    int delay = pInt(p, "delayBetween", 10);
    boolean tracking = pBool(p, "tracking", true);

    for (int i = 0; i < count; i++) {
      // Each successive strike re-snaps to the player's position at cast (spread-out storm).
      Vec3 c = new Vec3(
        ctx.target.getX() + rand(2.5),
        ctx.target.getY(),
        ctx.target.getZ() + rand(2.5)
      );
      PendingImpact im = new PendingImpact();
      im.x = c.x; im.y = c.y; im.z = c.z;
      im.radius = radius;
      im.totalWindup = windup;
      im.ticksLeft = windup + i * delay;
      im.damage = dmg * mult();
      im.knockback = 0.3;
      im.tracking = tracking && i == 0;
      im.column = true;
      ctx.state.getPendingImpacts().add(im);
    }
    NobleFx.playSoundId(ctx.level, ctx.bodyX, ctx.bodyY, ctx.bodyZ, ctx.element.castSoundId(), 1.0f, 1.2f);
  }

  private static void diveCharge(JsonObject p, Context ctx) {
    double radius = pDouble(p, "impactRadius", 5.0);
    float dmg = (float) pDouble(p, "damage", 9.0);
    double kb = pDouble(p, "knockback", 1.8);
    int windup = pInt(p, "windupTicks", 24);

    PendingImpact im = new PendingImpact();
    im.x = ctx.target.getX(); im.y = ctx.target.getY(); im.z = ctx.target.getZ();
    im.radius = radius;
    im.totalWindup = windup;
    im.ticksLeft = windup;
    im.damage = dmg * mult();
    im.knockback = kb;
    im.tracking = true; // slams where the player IS at impact
    ctx.state.getPendingImpacts().add(im);

    // Visual lunge: fling the body toward the target (manager re-pins it after).
    if (ctx.body != null) {
      Vec3 toward = ctx.target.position().subtract(ctx.body.position()).normalize().scale(0.8);
      ctx.body.setDeltaMovement(toward.x, 0.2, toward.z);
      ctx.body.hurtMarked = true;
    }
    NobleFx.playSoundId(ctx.level, ctx.bodyX, ctx.bodyY, ctx.bodyZ, ctx.element.castSoundId(), 1.0f, 0.7f);
  }

  private static void stomp(JsonObject p, Context ctx) {
    double range = pDouble(p, "range", 4.0);
    float dmg = (float) pDouble(p, "damage", 6.0);
    double kb = pDouble(p, "knockback", 1.5);
    int windup = pInt(p, "windupTicks", 8);

    PendingImpact im = new PendingImpact();
    im.x = ctx.bodyX; im.y = ctx.bodyY; im.z = ctx.bodyZ;
    im.radius = range;
    im.totalWindup = windup;
    im.ticksLeft = windup;
    im.damage = dmg * mult();
    im.knockback = kb;
    ctx.state.getPendingImpacts().add(im);
  }

  private static void beam(JsonObject p, Context ctx) {
    double length = pDouble(p, "length", 18.0);
    double width = pDouble(p, "width", 2.0);
    int windup = pInt(p, "windupTicks", 25);
    float dmg = (float) pDouble(p, "damage", 8.0);

    Vec3 from = new Vec3(ctx.bodyX, ctx.bodyY + 1.2, ctx.bodyZ);
    Vec3 dir = ctx.target.getEyePosition().subtract(from).normalize();
    PendingBeam bm = new PendingBeam();
    bm.sx = from.x; bm.sy = from.y; bm.sz = from.z;
    bm.dx = dir.x; bm.dy = dir.y; bm.dz = dir.z;
    bm.length = length; bm.width = width;
    bm.totalWindup = windup; bm.ticksLeft = windup;
    bm.damage = dmg * mult();
    ctx.state.getBeams().add(bm);
    NobleFx.playSoundId(ctx.level, from.x, from.y, from.z, ctx.element.castSoundId(), 1.0f, 0.9f);
  }

  private static void hazardZone(JsonObject p, Context ctx) {
    double radius = pDouble(p, "radius", 4.0);
    int duration = pInt(p, "durationTicks", 120);
    float tickDamage = (float) pDouble(p, "tickDamage", 2.0);
    String pull = pStr(p, "pull", "none");

    HazardZone z = new HazardZone();
    z.x = ctx.target.getX(); z.y = ctx.target.getY(); z.z = ctx.target.getZ();
    z.radius = radius;
    z.ticksLeft = duration;
    z.tickDamage = tickDamage * mult();
    z.pull = switch (pull) { case "toward" -> -1; case "away" -> 1; default -> 0; };
    ctx.state.getHazardZones().add(z);
    NobleFx.playSoundId(ctx.level, z.x, z.y, z.z, ctx.element.castSoundId(), 0.9f, 0.7f);
  }

  // ── Transient advancement (called every REALTIME tick) ───────────────────────

  public static void tickTransients(Context ctx) {
    tickBolts(ctx);
    tickImpacts(ctx);
    tickBeams(ctx);
    tickZones(ctx);
  }

  private static void tickBolts(Context ctx) {
    Iterator<ProjectileBolt> it = ctx.state.getBolts().iterator();
    while (it.hasNext()) {
      ProjectileBolt b = it.next();
      b.x += b.vx; b.y += b.vy; b.z += b.vz;
      b.ticksLeft--;
      NobleFx.burst(ctx.level, ctx.element.impactParticle(), b.x, b.y, b.z, 2, 0.02, 0.0);

      double dx = b.x - ctx.target.getX();
      double dy = b.y - (ctx.target.getY() + ctx.target.getBbHeight() * 0.5);
      double dz = b.z - ctx.target.getZ();
      if (dx * dx + dy * dy + dz * dz <= 1.4 * 1.4) {
        hit(ctx, b.damage, b.x, b.z, 0.4, 0.15);
        NobleFx.burst(ctx.level, ctx.element.impactParticle(), b.x, b.y, b.z, 10, 0.2, 0.03);
        it.remove();
        continue;
      }
      if (b.ticksLeft <= 0) it.remove();
    }
  }

  private static void tickImpacts(Context ctx) {
    Iterator<PendingImpact> it = ctx.state.getPendingImpacts().iterator();
    while (it.hasNext()) {
      PendingImpact im = it.next();
      if (im.tracking && im.ticksLeft > 0) {
        im.x = ctx.target.getX(); im.y = ctx.target.getY(); im.z = ctx.target.getZ();
      }
      im.ticksLeft--;

      if (im.ticksLeft > 0) {
        // Telegraph: pulsing warning ring (+ optional rising column).
        NobleFx.drawRing(ctx.level, im.x, im.y + 0.1, im.z, im.radius, ctx.element.telegraphColor(), IMPACT_RING_HEIGHTS);
        if (im.column) {
          for (double h = 0.5; h < 6.0; h += 0.8) {
            NobleFx.burst(ctx.level, ctx.element.impactParticle(), im.x, im.y + h, im.z, 1, 0.1, 0.0);
          }
        }
        continue;
      }

      // Impact.
      NobleFx.burst(ctx.level, ctx.element.impactParticle(), im.x, im.y + 0.3, im.z, 30, 0.6, 0.05);
      NobleFx.burst(ctx.level, ParticleTypes.EXPLOSION, im.x, im.y + 0.3, im.z, 1, 0.0, 0.0);
      NobleFx.playSoundId(ctx.level, im.x, im.y, im.z, "minecraft:entity.generic.explode", 0.8f, 0.9f);
      if (NobleFx.horizontalDistSq(ctx.target.getX(), ctx.target.getZ(), im.x, im.z) <= im.radius * im.radius) {
        hit(ctx, im.damage, im.x, im.z, im.knockback, 0.3);
      }
      it.remove();
    }
  }

  private static void tickBeams(Context ctx) {
    Iterator<PendingBeam> it = ctx.state.getBeams().iterator();
    while (it.hasNext()) {
      PendingBeam bm = it.next();
      bm.ticksLeft--;
      if (bm.ticksLeft > 0) {
        for (double d = 0; d <= bm.length; d += 0.8) {
          NobleFx.burst(ctx.level, ParticleTypes.SMOKE,
            bm.sx + bm.dx * d, bm.sy + bm.dy * d, bm.sz + bm.dz * d, 1, 0.05, 0.0);
        }
        continue;
      }
      // Fire: element particles along the segment + damage anyone within width.
      for (double d = 0; d <= bm.length; d += 0.5) {
        NobleFx.burst(ctx.level, ctx.element.impactParticle(),
          bm.sx + bm.dx * d, bm.sy + bm.dy * d, bm.sz + bm.dz * d, 2, 0.1, 0.0);
      }
      double dist = distanceToSegment(ctx.target.getX(), ctx.target.getY() + 0.9, ctx.target.getZ(), bm);
      if (dist <= bm.width * 0.5 + 0.5) {
        hit(ctx, bm.damage, ctx.target.getX() - bm.dx, ctx.target.getZ() - bm.dz, 0.6, 0.2);
      }
      NobleFx.playSoundId(ctx.level, bm.sx, bm.sy, bm.sz, "minecraft:entity.generic.explode", 0.7f, 1.3f);
      it.remove();
    }
  }

  private static void tickZones(Context ctx) {
    Iterator<HazardZone> it = ctx.state.getHazardZones().iterator();
    while (it.hasNext()) {
      HazardZone z = it.next();
      z.ticksLeft--;
      NobleFx.drawRing(ctx.level, z.x, z.y + 0.1, z.z, z.radius, ctx.element.telegraphColor(), new double[] { 0.1 });
      NobleFx.burst(ctx.level, ctx.element.ambientParticle(),
        z.x + rand(z.radius), z.y + 0.2, z.z + rand(z.radius), 1, 0.0, 0.0);

      boolean inside = NobleFx.horizontalDistSq(ctx.target.getX(), ctx.target.getZ(), z.x, z.z) <= z.radius * z.radius;
      if (inside) {
        if (z.pull == -1) NobleFx.knockback(ctx.target, z.x + (z.x - ctx.target.getX()), z.z + (z.z - ctx.target.getZ()), 0.25, 0.0);
        else if (z.pull == 1) NobleFx.knockback(ctx.target, z.x, z.z, 0.25, 0.05);
        if (z.ticksLeft % 10 == 0) {
          ctx.target.hurt(ctx.element.damageSource(ctx.target), z.tickDamage);
          ctx.element.applyOnHit(ctx.target, 40);
        }
      }
      if (z.ticksLeft <= 0) it.remove();
    }
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  /** Apply themed damage + on-hit effect + knockback away from (fromX, fromZ). */
  private static void hit(Context ctx, float damage, double fromX, double fromZ, double knockback, double up) {
    ctx.target.hurt(ctx.element.damageSource(ctx.target), damage);
    ctx.element.applyOnHit(ctx.target, EFFECT_TICKS);
    if (knockback > 0) NobleFx.knockback(ctx.target, fromX, fromZ, knockback, up);
  }

  private static Vec3 pickLocation(String pattern, int i, int strikes, Context ctx, double radius) {
    double ty = ctx.target.getY();
    return switch (pattern) {
      case "random" -> new Vec3(
        ctx.state.getArenaX() + rand(ctx.state.getArenaRadius() - 2),
        ty,
        ctx.state.getArenaZ() + rand(ctx.state.getArenaRadius() - 2));
      case "circle" -> {
        double a = (Math.PI * 2.0 * i) / Math.max(1, strikes);
        yield new Vec3(ctx.target.getX() + Math.cos(a) * radius, ty, ctx.target.getZ() + Math.sin(a) * radius);
      }
      case "line" -> {
        Vec3 dir = ctx.target.position().subtract(new Vec3(ctx.bodyX, ty, ctx.bodyZ));
        if (dir.lengthSqr() < 1.0e-3) dir = new Vec3(1, 0, 0);
        dir = dir.normalize();
        double step = (i + 1) * (radius + 1.0);
        yield new Vec3(ctx.bodyX + dir.x * step, ty, ctx.bodyZ + dir.z * step);
      }
      default -> new Vec3(ctx.target.getX(), ty, ctx.target.getZ());
    };
  }

  private static double distanceToSegment(double px, double py, double pz, PendingBeam bm) {
    // Project P-S onto the (unit) direction, clamp to [0, length], measure the residual.
    double vx = px - bm.sx, vy = py - bm.sy, vz = pz - bm.sz;
    double t = vx * bm.dx + vy * bm.dy + vz * bm.dz;
    t = Math.max(0, Math.min(bm.length, t));
    double cx = bm.sx + bm.dx * t, cy = bm.sy + bm.dy * t, cz = bm.sz + bm.dz * t;
    double dx = px - cx, dy = py - cy, dz = pz - cz;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  private static double rand(double half) { return (RNG.nextDouble() - 0.5) * 2.0 * half; }

  // ── JSON param readers ──────────────────────────────────────────────────────

  private static int pInt(JsonObject p, String k, int def) {
    return (p != null && p.has(k) && !p.get(k).isJsonNull()) ? p.get(k).getAsInt() : def;
  }
  private static double pDouble(JsonObject p, String k, double def) {
    return (p != null && p.has(k) && !p.get(k).isJsonNull()) ? p.get(k).getAsDouble() : def;
  }
  private static boolean pBool(JsonObject p, String k, boolean def) {
    return (p != null && p.has(k) && !p.get(k).isJsonNull()) ? p.get(k).getAsBoolean() : def;
  }
  private static String pStr(JsonObject p, String k, String def) {
    return (p != null && p.has(k) && !p.get(k).isJsonNull()) ? p.get(k).getAsString() : def;
  }
}

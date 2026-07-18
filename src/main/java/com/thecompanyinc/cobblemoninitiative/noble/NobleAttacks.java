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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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
    /** Cast/windup pitch multiplier that rises as the body weakens — the fight audibly sharpens. */
    public final float ragePitch;

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
      this.ragePitch = (body != null && body.getMaxHealth() > 0)
        ? 1.0f + 0.35f * (1.0f - body.getHealth() / body.getMaxHealth())
        : 1.0f;
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

  /**
   * Play an attack's cast voice: JSON {@code castSound}/{@code castVolume}/{@code castPitch}
   * params override the element default, and the rage multiplier sharpens the pitch as the
   * body weakens. Pure-JSON per-attack voices — mini-nobles compose signature movesets
   * without touching Java.
   */
  private static void castCue(JsonObject p, Context ctx, double x, double y, double z,
                              float defVolume, float defPitch) {
    String sound = pStr(p, "castSound", ctx.element.castSoundId());
    float volume = (float) pDouble(p, "castVolume", defVolume);
    float pitch = Mth.clamp((float) pDouble(p, "castPitch", defPitch) * ctx.ragePitch, 0.5f, 2.0f);
    NobleFx.playSoundId(ctx.level, x, y, z, sound, volume, pitch);
  }

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
    castCue(p, ctx, from.x, from.y, from.z, 1.0f, 1.0f);

    for (int i = 0; i < count; i++) {
      Vec3 d = dir.add(rand(spread), rand(spread), rand(spread)).normalize().scale(speed);
      ProjectileBolt b = new ProjectileBolt();
      b.x = from.x; b.y = from.y; b.z = from.z;
      b.vx = d.x; b.vy = d.y; b.vz = d.z;
      b.ticksLeft = life;
      b.damage = dmg * mult();
      b.impactSoundId = pStr(p, "impactSound", null);
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
      im.impactSoundId = pStr(p, "impactSound", null);
      ctx.state.getPendingImpacts().add(im);
    }
    castCue(p, ctx, ctx.bodyX, ctx.bodyY, ctx.bodyZ, 0.9f, 0.8f);
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
      im.impactSoundId = pStr(p, "impactSound", null);
      ctx.state.getPendingImpacts().add(im);
    }
    castCue(p, ctx, ctx.bodyX, ctx.bodyY, ctx.bodyZ, 1.0f, 1.2f);
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
    im.impactSoundId = pStr(p, "impactSound", null);
    ctx.state.getPendingImpacts().add(im);

    // Visual lunge: fling the body toward the target (manager re-pins it after).
    if (ctx.body != null) {
      Vec3 toward = ctx.target.position().subtract(ctx.body.position()).normalize().scale(0.8);
      ctx.body.setDeltaMovement(toward.x, 0.2, toward.z);
      ctx.body.hurtMarked = true;
    }
    castCue(p, ctx, ctx.bodyX, ctx.bodyY, ctx.bodyZ, 1.0f, 0.7f);
  }

  private static void stomp(JsonObject p, Context ctx) {
    double range = pDouble(p, "range", 4.0);
    float dmg = (float) pDouble(p, "damage", 6.0);
    double kb = pDouble(p, "knockback", 1.5);
    int windup = pInt(p, "windupTicks", 8);

    PendingImpact im = new PendingImpact();
    // The shockwave lives on the floor even if the body is airborne (the damage check is
    // horizontal-only — a hover-height telegraph would be an invisible ground AoE).
    im.x = ctx.bodyX; im.y = Math.min(ctx.bodyY, ctx.state.getArenaY()); im.z = ctx.bodyZ;
    im.radius = range;
    im.totalWindup = windup;
    im.ticksLeft = windup;
    im.damage = dmg * mult();
    im.knockback = kb;
    im.impactSoundId = pStr(p, "impactSound", null);
    ctx.state.getPendingImpacts().add(im);

    // Rear-up hop (the diveCharge lunge idiom) — the windup finally has a visible tell.
    // Skipped while a flyer is pinned airborne (the pin re-zeroes velocity every tick).
    if (ctx.body != null && !(ctx.config.isFlyer() && ctx.state.isAirborne())) {
      ctx.body.setDeltaMovement(0, 0.35, 0);
      ctx.body.hurtMarked = true;
    }
    castCue(p, ctx, ctx.bodyX, ctx.bodyY, ctx.bodyZ, 1.0f, 0.9f);
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
    bm.impactSoundId = pStr(p, "impactSound", null);
    ctx.state.getBeams().add(bm);
    castCue(p, ctx, from.x, from.y, from.z, 1.0f, 0.9f);
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
    castCue(p, ctx, z.x, z.y, z.z, 0.9f, 0.7f);
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
        NobleFx.playSoundId(ctx.level, b.x, b.y, b.z,
          b.impactSoundId != null ? b.impactSoundId : ctx.element.impactSoundId(), 0.6f, 1.2f);
        it.remove();
        continue;
      }
      if (b.ticksLeft <= 0) it.remove();
    }
  }

  private static void tickImpacts(Context ctx) {
    // Metronome only the soonest-landing impact — overlapping barrage windups would
    // otherwise stack into noise instead of a readable countdown.
    PendingImpact soonest = null;
    for (PendingImpact im : ctx.state.getPendingImpacts()) {
      if (im.ticksLeft > 0 && (soonest == null || im.ticksLeft < soonest.ticksLeft)) soonest = im;
    }

    Iterator<PendingImpact> it = ctx.state.getPendingImpacts().iterator();
    while (it.hasNext()) {
      PendingImpact im = it.next();
      // Tracking locks in with radius-scaled lead time (~radius / sprint-speed ticks): a
      // strike that re-aims through its final tick is geometrically undodgeable, which has
      // no place in a hardcore run.
      if (im.tracking && im.ticksLeft > Math.max(5, (int) (im.radius * 4))) {
        im.x = ctx.target.getX(); im.y = ctx.target.getY(); im.z = ctx.target.getZ();
      }
      im.ticksLeft--;

      if (im.ticksLeft > 0) {
        // Telegraph: pulsing warning ring (+ optional rising column). The last 5 ticks
        // flip white-hot and bigger — the screen-readable "MOVE NOW" cue.
        boolean armed = im.ticksLeft <= 5;
        Vector3f ringColor = armed ? new Vector3f(1f, 1f, 1f) : ctx.element.telegraphColor();
        NobleFx.drawRing(ctx.level, im.x, im.y + 0.1, im.z, im.radius, ringColor,
          IMPACT_RING_HEIGHTS, armed ? 2.2f : 1.4f);
        if (im.column) {
          for (double h = 0.5; h < 6.0; h += 0.8) {
            NobleFx.burst(ctx.level, ctx.element.impactParticle(), im.x, im.y + h, im.z, 1, 0.1, 0.0);
          }
        }
        // Accelerating metronome tick (barrage strikes queue beyond totalWindup — clamp).
        if (im == soonest && im.ticksLeft <= im.totalWindup) {
          double progress = Mth.clamp(1.0 - (double) im.ticksLeft / Math.max(1, im.totalWindup), 0.0, 1.0);
          int interval = Math.max(2, 8 - (int) (6 * progress));
          if (im.ticksLeft % interval == 0) {
            NobleFx.playSoundId(ctx.level, im.x, im.y, im.z, ctx.element.windupSoundId(),
              0.6f, Mth.clamp((0.8f + 0.8f * (float) progress) * ctx.ragePitch, 0.5f, 2.0f));
          }
        }
        continue;
      }

      // Impact.
      NobleFx.burst(ctx.level, ctx.element.impactParticle(), im.x, im.y + 0.3, im.z, 30, 0.6, 0.05);
      NobleFx.burst(ctx.level, ParticleTypes.EXPLOSION, im.x, im.y + 0.3, im.z, 1, 0.0, 0.0);
      NobleFx.playSoundId(ctx.level, im.x, im.y, im.z,
        im.impactSoundId != null ? im.impactSoundId : ctx.element.impactSoundId(), 0.9f, 0.9f);
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
        // Warning line — sparks white-hot for the final 5 ticks; charge hum rises in pitch.
        ParticleOptions warn = bm.ticksLeft <= 5 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SMOKE;
        for (double d = 0; d <= bm.length; d += 0.8) {
          NobleFx.burst(ctx.level, warn,
            bm.sx + bm.dx * d, bm.sy + bm.dy * d, bm.sz + bm.dz * d, 1, 0.05, 0.0);
        }
        if (bm.ticksLeft % 5 == 0) {
          double progress = Mth.clamp(1.0 - (double) bm.ticksLeft / Math.max(1, bm.totalWindup), 0.0, 1.0);
          NobleFx.playSoundId(ctx.level, bm.sx, bm.sy, bm.sz, "minecraft:block.beacon.deactivate",
            0.5f, Mth.clamp((0.6f + 0.8f * (float) progress) * ctx.ragePitch, 0.5f, 2.0f));
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
      NobleFx.playSoundId(ctx.level, bm.sx, bm.sy, bm.sz,
        bm.impactSoundId != null ? bm.impactSoundId : ctx.element.impactSoundId(), 0.7f, 1.3f);
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
        // knockback() pushes AWAY from its origin — pulling toward the center means the
        // origin is the center mirrored ACROSS THE PLAYER (2P − C), not across the center.
        if (z.pull == -1) NobleFx.knockback(ctx.target,
          ctx.target.getX() + (ctx.target.getX() - z.x), ctx.target.getZ() + (ctx.target.getZ() - z.z), 0.25, 0.0);
        else if (z.pull == 1) NobleFx.knockback(ctx.target, z.x, z.z, 0.25, 0.05);
        if (z.ticksLeft % 10 == 0) {
          hurtCapped(ctx.target, ctx.element.damageSource(ctx.target), z.tickDamage);
          ctx.element.applyOnHit(ctx.target, 40);
          NobleFx.hurtTilt(ctx.target, z.x, z.z);
          // Standing in the lava pool / whirlpool audibly burns.
          NobleFx.playSoundId(ctx.level, z.x, z.y, z.z, ctx.element.windupSoundId(), 0.4f, 0.7f);
        }
      }
      if (z.ticksLeft <= 0) it.remove();
    }
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  /**
   * A noble is never LETHAL (Legends-Arceus model, showrunner 2026-07-17): losing knocks you
   * out and retreats you, it never kills — critical in hardcore, and doubly so now the birds
   * always attack their towns. This caps every noble hit so it can't drop the player below
   * {@link #KNOCKOUT_FLOOR}; the manager tick sees the floor and ends the fight as a retreat.
   */
  public static final float KNOCKOUT_FLOOR = 4.0f;

  private static void hurtCapped(ServerPlayer target, DamageSource src, float damage) {
    // Lethal mode (default): full damage — a noble can kill you. Knockout mode: cap so it can't
    // drop you below the floor (the manager then ends the fight as a retreat).
    if (com.thecompanyinc.cobblemoninitiative.config.NobleConfig.get().isLethalNobleFights()) {
      target.hurt(src, damage);
      return;
    }
    float allowed = Math.max(0f, target.getHealth() - KNOCKOUT_FLOOR);
    float capped = Math.min(damage, allowed);
    if (capped > 0f) target.hurt(src, capped);
  }

  /** Apply themed damage + on-hit effect + knockback away from (fromX, fromZ). */
  private static void hit(Context ctx, float damage, double fromX, double fromZ, double knockback, double up) {
    hurtCapped(ctx.target, ctx.element.damageSource(ctx.target), damage);
    ctx.element.applyOnHit(ctx.target, EFFECT_TICKS);
    if (knockback > 0) NobleFx.knockback(ctx.target, fromX, fromZ, knockback, up);
    NobleFx.hurtTilt(ctx.target, fromX, fromZ);
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

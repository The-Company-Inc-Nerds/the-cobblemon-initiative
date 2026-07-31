package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.config.CyclopsConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * Drives the giant mushroom-island cyclops (see {@link CyclopsConfig}). The BODY (3x humanoid, 50 HP,
 * hostile AI, water-avoid) is an Easy NPC preset baked from dialog-src; its native goals (ATTACK_MOB /
 * ATTACK_PLAYER / MELEE_ATTACK) already do targeting + chase + the mob-smash. This manager adds the two
 * things vanilla goals can't: (1) a water-avoidance PUSH while chasing (native attack pathing walks
 * straight through water), and (2) the signature player attack — GRAB (freeze + hold the player at the
 * cyclops's fist), SQUEEZE (periodic damage), then THROW (outward+up velocity, synced to the client).
 *
 * <p>Static manager (mirrors {@code DojoDifficultyManager}): {@link #init()} from InitiativeInit registers
 * the ENTITY_LOAD scale-apply + the END_SERVER_TICK driver. Bodies are summon-only — {@link #spawnAll}
 * import_new's the preset at {@link CyclopsConfig#spawnPoints} ({@code /cobblemon-initiative cyclops spawn}).
 */
public final class CyclopsManager {

  private CyclopsManager() {}

  private static final String SCALED_TAG = "cyclops_scaled";
  /** Known live cyclops bodies (add on load, prune on death). */
  private static final Set<UUID> bodies = new LinkedHashSet<>();
  /** Two-phase scale-apply queue (ENTITY_LOAD fires before attributes finish importing). */
  private static final Set<UUID> incoming = new LinkedHashSet<>();
  private static final Set<UUID> ready = new LinkedHashSet<>();
  /** Active grabs: cyclops uuid -> the held player + elapsed ticks. */
  private static final Map<UUID, Grab> grabs = new HashMap<>();
  /** Players currently held by ANY cyclops — so two adjacent cyclops can't both grab one player
   *  (which would double-squeeze and ping-pong the thrown player straight back to the other's fist). */
  private static final Set<UUID> grabbedPlayers = new HashSet<>();
  /** Post-throw re-grab cooldown: cyclops uuid -> gameTime it can grab again. */
  private static final Map<UUID, Long> cooldownUntil = new HashMap<>();

  private static boolean initialized;

  private static final class Grab { UUID player; int ticks; }

  public static void init() {
    if (initialized) return;
    initialized = true;
    ServerEntityEvents.ENTITY_LOAD.register(CyclopsManager::onEntityLoad);
    ServerTickEvents.END_SERVER_TICK.register(CyclopsManager::tick);
  }

  private static void onEntityLoad(Entity entity, ServerLevel level) {
    if (!(entity instanceof LivingEntity)) return;
    if (!entity.getTags().contains(CyclopsConfig.get().bodyTag)) return;
    bodies.add(entity.getUUID());
    if (!entity.getTags().contains(SCALED_TAG)) incoming.add(entity.getUUID());
  }

  // ── spawn / clear (op commands) ──────────────────────────────────────────────────

  /** {@code /cobblemon-initiative cyclops spawn} — import_new one body at each config spawn point. */
  public static int spawnAll(MinecraftServer server) {
    CyclopsConfig cfg = CyclopsConfig.get();
    if (!cfg.enabled || cfg.spawnPoints == null || cfg.spawnPoints.isEmpty()) return 0;
    ServerLevel level = levelFor(server, cfg);
    if (level == null) return 0;
    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    int n = 0;
    for (CyclopsConfig.Pos p : cfg.spawnPoints) {
      server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
        "easy_npc preset import_new data %s %.2f %.2f %.2f", cfg.bodyPreset, p.x, p.y, p.z));
      n++;
    }
    return n;
  }

  /** {@code /cobblemon-initiative cyclops clear} — remove every cyclops body. */
  public static int clearAll(MinecraftServer server) {
    var src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + CyclopsConfig.get().bodyTag + "]");
    bodies.clear();
    grabs.clear();
    grabbedPlayers.clear();
    cooldownUntil.clear();
    return 1;
  }

  // ── tick ─────────────────────────────────────────────────────────────────────────

  private static void tick(MinecraftServer server) {
    CyclopsConfig cfg = CyclopsConfig.get();

    // (A) Two-phase scale apply — one tick after load, so the preset attributes are in.
    if (!ready.isEmpty()) {
      for (UUID uuid : ready) {
        Entity e = resolve(server, uuid);
        if (e instanceof LivingEntity le && le.getTags().contains(cfg.bodyTag)
            && !le.getTags().contains(SCALED_TAG)) {
          applyScale(le, cfg);
        }
      }
      ready.clear();
    }
    if (!incoming.isEmpty()) { ready.addAll(incoming); incoming.clear(); }

    if (!cfg.enabled || bodies.isEmpty()) return;

    // (B) Drive each live cyclops.
    for (UUID uuid : new HashSet<>(bodies)) {
      Entity e = resolve(server, uuid);
      if (!(e instanceof Mob cy) || !cy.isAlive()) {
        bodies.remove(uuid);
        Grab dead = grabs.remove(uuid);
        if (dead != null && dead.player != null) grabbedPlayers.remove(dead.player);
        cooldownUntil.remove(uuid);
        continue;
      }
      driveCyclops(server, cy, cfg);
    }
  }

  private static void applyScale(LivingEntity le, CyclopsConfig cfg) {
    AttributeInstance maxHealth = le.getAttribute(Attributes.MAX_HEALTH);
    if (maxHealth != null && cfg.getHealthMultiplier() != 1.0f) {
      maxHealth.setBaseValue(maxHealth.getBaseValue() * cfg.getHealthMultiplier());
    }
    AttributeInstance attackDamage = le.getAttribute(Attributes.ATTACK_DAMAGE);
    if (attackDamage != null && cfg.getDamageMultiplier() != 1.0f) {
      attackDamage.setBaseValue(attackDamage.getBaseValue() * cfg.getDamageMultiplier());
    }
    le.setHealth(le.getMaxHealth());
    le.addTag(SCALED_TAG);
  }

  private static void driveCyclops(MinecraftServer server, Mob cy, CyclopsConfig cfg) {
    long now = cy.level().getGameTime();

    // (1) Water avoidance while chasing — native attack pathing ignores water, so shove it back out.
    if (cy.isInWaterOrBubble()) {
      Vec3 v = cy.getDeltaMovement();
      cy.setDeltaMovement(v.x * -0.6, Math.max(v.y, 0.3), v.z * -0.6);
      cy.hurtMarked = true;
    }

    // (2) Active grab → hold + squeeze, then throw.
    Grab g = grabs.get(cy.getUUID());
    if (g != null) {
      ServerPlayer p = server.getPlayerList().getPlayer(g.player);
      if (p == null || !p.isAlive() || p.isSpectator() || cy.distanceToSqr(p) > 900.0) {
        releaseGrab(cy, cfg, now);
        return;
      }
      g.ticks++;
      if (g.ticks >= cfg.getGrabDurationTicks()) {
        throwPlayer(p, cy, cfg);
        releaseGrab(cy, cfg, now);
        return;
      }
      holdGrabbed(p, cy);
      if (g.ticks % cfg.getSqueezeIntervalTicks() == 0) {
        p.hurt(cy.damageSources().mobAttack(cy), cfg.getSqueezeDamage());
        cy.level().playSound(null, cy.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG,
          SoundSource.HOSTILE, 1.0f, 0.55f);
      }
      return;
    }

    // (3) Initiate a grab: the goal-selected target is a reachable, non-creative player in range.
    Long cd = cooldownUntil.get(cy.getUUID());
    if (cd != null && now < cd) return;
    LivingEntity target = cy.getTarget();
    if (target instanceof ServerPlayer sp && sp.isAlive() && !sp.isSpectator()
        && !sp.getAbilities().instabuild
        && !grabbedPlayers.contains(sp.getUUID()) // not already held by another cyclops
        && cy.distanceToSqr(sp) <= cfg.getGrabRange() * cfg.getGrabRange()) {
      startGrab(cy, sp);
    }
  }

  private static void startGrab(Mob cy, ServerPlayer p) {
    Grab g = new Grab();
    g.player = p.getUUID();
    g.ticks = 0;
    grabs.put(cy.getUUID(), g);
    grabbedPlayers.add(p.getUUID());
    cy.setNoAi(true); // freeze while it holds the player
    cy.getNavigation().stop();
    cy.level().playSound(null, cy.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK,
      SoundSource.HOSTILE, 1.3f, 0.65f);
    p.displayClientMessage(Component.literal("§cThe cyclops seizes you in its fist!"), true);
  }

  /** Hard positional lock at the cyclops's fist (in front, at ~chest height of the 3x body). */
  private static void holdGrabbed(ServerPlayer p, Mob cy) {
    float yaw = cy.getYRot();
    double fx = -Math.sin(Math.toRadians(yaw));
    double fz = Math.cos(Math.toRadians(yaw));
    double hx = cy.getX() + fx * 1.8;
    double hz = cy.getZ() + fz * 1.8;
    double hy = cy.getY() + cy.getBbHeight() * 0.6;
    p.setDeltaMovement(Vec3.ZERO);
    p.resetFallDistance();
    p.connection.teleport(hx, hy, hz, cy.getYRot(), 15.0f);
    p.hurtMarked = true;
  }

  private static void throwPlayer(ServerPlayer p, Mob cy, CyclopsConfig cfg) {
    double dx = p.getX() - cy.getX();
    double dz = p.getZ() - cy.getZ();
    double len = Math.sqrt(dx * dx + dz * dz);
    if (len < 1.0e-4) { // player centred on the cyclops — throw along its facing
      double yaw = Math.toRadians(cy.getYRot());
      dx = -Math.sin(yaw); dz = Math.cos(yaw); len = 1.0;
    }
    Vec3 v = new Vec3((dx / len) * cfg.getThrowHorizontal(), cfg.getThrowVertical(),
      (dz / len) * cfg.getThrowHorizontal());
    p.setDeltaMovement(v);
    p.hurtMarked = true; // sync the server velocity to the client
    p.connection.send(new ClientboundSetEntityMotionPacket(p.getId(), v)); // guarantee it lands this tick
    p.hurt(cy.damageSources().mobAttack(cy), cfg.getThrowImpactDamage());
    // Deliberately NOT resetFallDistance — the player takes fall damage on landing.
    cy.level().playSound(null, cy.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
      SoundSource.HOSTILE, 1.3f, 0.6f);
    p.displayClientMessage(Component.literal("§cThe cyclops hurls you away!"), true);
  }

  private static void releaseGrab(Mob cy, CyclopsConfig cfg, long now) {
    Grab g = grabs.remove(cy.getUUID());
    if (g != null && g.player != null) grabbedPlayers.remove(g.player);
    cy.setNoAi(false);
    cy.setTarget(null); // brief disengage so it doesn't re-lunge instantly
    cooldownUntil.put(cy.getUUID(), now + cfg.getGrabCooldownTicks());
  }

  // ── helpers ──────────────────────────────────────────────────────────────────────

  private static ServerLevel levelFor(MinecraftServer server, CyclopsConfig cfg) {
    for (ServerLevel l : server.getAllLevels()) {
      if (l.dimension().location().toString().equals(cfg.dimension)) return l;
    }
    return server.overworld();
  }

  private static Entity resolve(MinecraftServer server, UUID uuid) {
    for (ServerLevel level : server.getAllLevels()) {
      Entity e = level.getEntity(uuid);
      if (e != null) return e;
    }
    return null;
  }
}

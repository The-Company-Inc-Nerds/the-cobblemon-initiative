package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.config.KalaharConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Drives the Kalahar Reach (gym 6) mirage hunt (see {@link KalaharConfig}). Design:
 *
 * <ul>
 *   <li>The six gym students (Jr. Apprentice Dune, Apprentice Terra, and the four dune trainers)
 *       are ordinary placement-latched talkable bodies — their existing battle blocks + weakening
 *       ladder are untouched; each carries a per-student {@code ci_kal_*} tag and an {@code on_win}
 *       that teleports it back to the gym hollow when beaten ("returns to the gym").</li>
 *   <li>When a player nears the gym guide ({@link KalaharConfig#guidePos}) the hunt "starts": for
 *       every student not yet defeated, {@code count - 1} heat-shimmer FAKE decoys ({@code ci_mirage_fake})
 *       are import_new'd across the town scatter pool. Fakes share the students' skins, so the player
 *       cannot tell a decoy from the real trainer without reaching out.</li>
 *   <li>Reaching out to a fake (its dialog runs {@code /cobblemon-initiative kalahar reach}) rolls
 *       {@link KalaharConfig#getDopplerChance()}: it either POOFS (tagged {@code ci_mirage_popped},
 *       swept by ambient/tick FX) or collapses into a hostile low-HP DOPPLER that attacks the player
 *       and must be killed.</li>
 * </ul>
 *
 * <p>Static manager (mirrors {@link CyclopsManager}): {@link #init()} from InitiativeInit registers
 * the ENTITY_LOAD Doppler-stat apply and the END_SERVER_TICK driver (two-phase stat apply + the
 * guide "on sight" auto-start). No auto-seed on server start — the hunt is player-triggered.
 * {@code /cobblemon-initiative kalahar start|reach|clear|reload}.
 */
public final class KalaharManager {

  private KalaharManager() {}

  /** One gym student: its rctmod trainer id, the skin-matched fake preset, and whether it gates the leader. */
  private record Student(String trainerId, String fakePreset, boolean apprentice) {}

  private static final String P_APP    = "easy_npc:preset/humanoid/kalahar_mirage_apprentice.npc.snbt";
  private static final String P_HIKER  = "easy_npc:preset/humanoid/kalahar_mirage_hiker.npc.snbt";
  private static final String P_DIGGER = "easy_npc:preset/humanoid/kalahar_mirage_digger.npc.snbt";

  /** Skins: Dune + Terra share single/kalahar_apprentice; Boulder(t1) + Juno(t3) share trainer_1;
   *  Dustin(t2) + Vince(t4) share trainer_2 — so three fake presets cover all six students. */
  private static final List<Student> STUDENTS = List.of(
    new Student("kalahar_jr_apprentice", P_APP,    true),
    new Student("kalahar_apprentice",    P_APP,    true),
    new Student("kalahar_trainer_1",     P_HIKER,  false),
    new Student("kalahar_trainer_2",     P_DIGGER, false),
    new Student("kalahar_trainer_3",     P_HIKER,  false),
    new Student("kalahar_trainer_4",     P_DIGGER, false)
  );

  private static final String DOPPLER_SCALED = "ci_doppler_scaled";
  /** Per-world "hunt has scattered" flag (scoreboard) so a reboot never re-scatters. */
  private static final String FLAG_OBJ = "ci_kalahar_hunt";
  private static final String POPPED_TAG = "ci_mirage_popped"; // reuses ambient/tick poof FX + kill

  /** Two-phase stat-apply queue (ENTITY_LOAD fires before the preset attributes finish importing). */
  private static final Set<UUID> incoming = new LinkedHashSet<>();
  private static final Set<UUID> ready = new LinkedHashSet<>();

  private static final Random RNG = new Random();
  private static boolean initialized;

  public static void init() {
    if (initialized) return;
    initialized = true;
    ServerEntityEvents.ENTITY_LOAD.register(KalaharManager::onEntityLoad);
    ServerTickEvents.END_SERVER_TICK.register(KalaharManager::tick);
  }

  private static void onEntityLoad(Entity entity, ServerLevel level) {
    if (!(entity instanceof LivingEntity)) return;
    if (!entity.getTags().contains(KalaharConfig.get().dopplerTag)) return;
    if (!entity.getTags().contains(DOPPLER_SCALED)) incoming.add(entity.getUUID());
  }

  // ── hunt start (guide "on sight" or /... kalahar start) ─────────────────────────

  /** Scatter the fake mirages ONCE per world. {@code trigger} is the player who set it off (null for
   *  the op command) — used to skip decoys for students they have already beaten, and to narrate. */
  public static int start(MinecraftServer server, ServerPlayer trigger) {
    KalaharConfig cfg = KalaharConfig.get();
    if (!cfg.enabled) return 0;
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(FLAG_OBJ);
    if (obj == null) {
      obj = sb.addObjective(FLAG_OBJ, ObjectiveCriteria.DUMMY,
        Component.literal("Kalahar Hunt"), ObjectiveCriteria.RenderType.INTEGER, false, null);
    }
    ScoreAccess flag = sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#started"), obj);
    if (flag.get() >= 1) return 0; // already scattered this world
    // Do not scatter if the gym is already cleared (e.g. a post-badge revisit walks past the guide).
    if (trigger != null && trigger.getTags().contains("defeated_kalahar_leader")) { flag.set(1); return 0; }

    ServerLevel level = levelFor(server, cfg);
    // Do NOT latch #started when there is nowhere to scatter — leave it un-armed so a reconfigured
    // pool retries on the next approach instead of marking the hunt permanently started with 0 fakes.
    if (level == null || cfg.scatterSpots == null || cfg.scatterSpots.isEmpty()) return 0;
    flag.set(1);

    List<KalaharConfig.Pos> pool = new ArrayList<>(cfg.scatterSpots);
    Collections.shuffle(pool, RNG);
    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    int idx = 0, spawned = 0;
    for (Student s : STUDENTS) {
      if (trigger != null && trigger.getTags().contains("defeated_" + s.trainerId())) continue;
      int fakes = (s.apprentice() ? cfg.getApprenticeMirageCount() : cfg.getTrainerMirageCount()) - 1;
      for (int k = 0; k < fakes && idx < pool.size(); k++, idx++) {
        KalaharConfig.Pos p = pool.get(idx);
        level.getChunk(((int) Math.floor(p.x)) >> 4, ((int) Math.floor(p.z)) >> 4); // load + persist target chunk
        server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
          "easy_npc preset import_new data %s %.2f %.2f %.2f", s.fakePreset(), p.x, p.y, p.z));
        spawned++;
      }
    }
    if (trigger != null) {
      trigger.displayClientMessage(Component.literal(
        "§eThe gym guide calls out: §7The Reach has scattered — Gaia's students hid among their own "
        + "mirages. Reach out to find the true ones. A mirage casts no shadow."), false);
    }
    return spawned;
  }

  // ── reach out (fake dialog button → /... kalahar reach) ─────────────────────────

  /** Resolve the fake the player is reaching for: 50/50 poof-or-Doppler. */
  public static int reach(ServerPlayer player) {
    if (player == null) return 0;
    KalaharConfig cfg = KalaharConfig.get();
    ServerLevel level = player.serverLevel();
    AABB box = player.getBoundingBox().inflate(3.0);
    Entity nearest = null;
    double best = Double.MAX_VALUE;
    for (Entity e : level.getEntities(player, box, e -> e.getTags().contains(cfg.fakeTag))) {
      double d = e.distanceToSqr(player);
      if (d < best) { best = d; nearest = e; }
    }
    if (nearest == null) return 0;

    double x = nearest.getX(), y = nearest.getY(), z = nearest.getZ();
    BlockPos bp = nearest.blockPosition();
    if (RNG.nextDouble() < cfg.getDopplerChance()) {
      nearest.discard(); // collapse the passive decoy
      level.sendParticles(ParticleTypes.CLOUD, x, y + 1.0, z, 30, 0.3, 0.6, 0.3, 0.02);
      level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 12, 0.25, 0.4, 0.25, 0.01);
      level.playSound(null, bp, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 1.0f, 0.7f);
      spawnDoppler(level.getServer(), level, x, y, z);
      player.displayClientMessage(
        Component.literal("§cThe mirage twists into something that lunges at you!"), true);
    } else {
      nearest.addTag(POPPED_TAG); // ambient/tick sweeps the poof FX + kill next tick
      player.displayClientMessage(Component.literal("§7The figure dissolves into drifting sand."), true);
    }
    return 1;
  }

  private static void spawnDoppler(MinecraftServer server, ServerLevel level, double x, double y, double z) {
    if (server == null) return;
    KalaharConfig cfg = KalaharConfig.get();
    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
      "easy_npc preset import_new data %s %.2f %.2f %.2f", cfg.dopplerPreset, x, y, z));
  }

  // ── clear / dev ─────────────────────────────────────────────────────────────────

  /** {@code /cobblemon-initiative kalahar clear} — remove every fake + Doppler and re-arm the hunt. */
  public static int clear(MinecraftServer server) {
    cleanupDecoys(server);
    Objective obj = server.getScoreboard().getObjective(FLAG_OBJ);
    if (obj != null) {
      server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("#started"), obj).set(0);
      server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("#cleaned"), obj).set(0);
    }
    incoming.clear();
    ready.clear();
    return 1;
  }

  /** Kill every scattered fake + Doppler (no flag reset). Shared by {@link #clear} and the
   *  post-victory dissipation in {@link #tick}. */
  private static void cleanupDecoys(MinecraftServer server) {
    KalaharConfig cfg = KalaharConfig.get();
    var src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + cfg.fakeTag + "]");
    server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + cfg.dopplerTag + "]");
  }

  // ── tick ─────────────────────────────────────────────────────────────────────────

  private static void tick(MinecraftServer server) {
    KalaharConfig cfg = KalaharConfig.get();

    // (A) Two-phase Doppler stat apply — one tick after load, so the preset attributes are in.
    if (!ready.isEmpty()) {
      for (UUID uuid : ready) {
        Entity e = resolve(server, uuid);
        if (e instanceof LivingEntity le && le.getTags().contains(cfg.dopplerTag)
            && !le.getTags().contains(DOPPLER_SCALED)) {
          applyDoppler(le, cfg);
        }
      }
      ready.clear();
    }
    if (!incoming.isEmpty()) { ready.addAll(incoming); incoming.clear(); }

    // (B) Guide "on sight" — scatter once when a player comes within range of the gym guide.
    if (cfg.enabled && !huntStarted(server)) {
      ServerLevel level = levelFor(server, cfg);
      if (level != null) {
        double r2 = cfg.guideTriggerRadius * cfg.guideTriggerRadius;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
          if (p.level() != level || p.isSpectator()) continue;
          double dx = p.getX() - cfg.guidePos.x, dy = p.getY() - cfg.guidePos.y, dz = p.getZ() - cfg.guidePos.z;
          if (dx * dx + dy * dy + dz * dz <= r2) { start(server, p); break; }
        }
      }
    }

    // (C) Once Leader Gaia falls, the illusion dissipates — clear any leftover fakes/Dopplers once.
    if (huntStarted(server) && !cleaned(server)) {
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        if (p.getTags().contains("defeated_kalahar_leader")) {
          cleanupDecoys(server);
          markCleaned(server);
          break;
        }
      }
    }
  }

  private static void applyDoppler(LivingEntity le, KalaharConfig cfg) {
    AttributeInstance hp = le.getAttribute(Attributes.MAX_HEALTH);
    if (hp != null) hp.setBaseValue(cfg.getDopplerHealth());
    AttributeInstance atk = le.getAttribute(Attributes.ATTACK_DAMAGE);
    if (atk != null) atk.setBaseValue(cfg.getDopplerDamage());
    le.setHealth(le.getMaxHealth());
    le.addTag(DOPPLER_SCALED);
  }

  // ── helpers ──────────────────────────────────────────────────────────────────────

  private static boolean huntStarted(MinecraftServer server) {
    Objective obj = server.getScoreboard().getObjective(FLAG_OBJ);
    if (obj == null) return false;
    return server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("#started"), obj).get() >= 1;
  }

  private static boolean cleaned(MinecraftServer server) {
    Objective obj = server.getScoreboard().getObjective(FLAG_OBJ);
    if (obj == null) return false;
    return server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("#cleaned"), obj).get() >= 1;
  }

  private static void markCleaned(MinecraftServer server) {
    Objective obj = server.getScoreboard().getObjective(FLAG_OBJ);
    if (obj != null) {
      server.getScoreboard().getOrCreatePlayerScore(ScoreHolder.forNameOnly("#cleaned"), obj).set(1);
    }
  }

  private static ServerLevel levelFor(MinecraftServer server, KalaharConfig cfg) {
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

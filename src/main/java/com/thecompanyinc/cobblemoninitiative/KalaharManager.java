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
 *       are SUMMON-ONLY (a19): {@link #start} deals each real onto a random spot of the SAME
 *       31-pin scatter pool as its mirages, so neither text, name, skin, nor POSITION tells them
 *       apart, and nothing stands in town before Tarek's button fires the scatter. Each carries a
 *       per-student {@code ci_kal_*} tag; being FOUND teleports it to its fixed gym station where
 *       its untouched battle ladder plays out.</li>
 *   <li>The hunt "starts" from Tarek Ramessu's dialog button ({@code kalahar start} — a19, playtest
 *       2026-08-04 N1; the old proximity auto-start is gone): for every student not yet defeated or
 *       found, {@code count - 1} FAKE decoys ({@code ci_mirage_fake}) are import_new'd across the
 *       town scatter pool — each fake carries its real's NAME, skin, and dialog text, so nothing
 *       tells a decoy from the real trainer without reaching out.</li>
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

  /**
   * One gym student (a18 rework, playtest N23): its rctmod trainer id, display name (actionbar), the
   * per-student fake preset (SAME name + skin + text as the real — indistinguishable), whether it
   * gates the leader, its {@code ci_kal_*} real-body tag, the gym-hollow spot it teleports to when
   * FOUND, and the recorded found-cutscene id. {@code foundTag()} = the player tag that flips the
   * real body's dialog from mirage-text to its battle ladder.
   */
  private record Student(String trainerId, String name, String fakePreset, boolean apprentice,
                         String realTag, double hx, double hy, double hz, String cutscene,
                         String foundTag) {}

  private static String fake(String s) { return "easy_npc:preset/humanoid/kalahar_mirage_" + s + ".npc.snbt"; }

  /** GYM STATIONS = the in-world pins from the 2026-08-03 playtest — CORRECTED 2026-08-04 note 1:
   *  those N11–N16 coords are the positions the reals TELEPORT TO after being found (the recorded
   *  kalahar_found_* cutscenes each end looking at exactly one of them), NOT their town hiding
   *  spots (the char placements revert to the a17 town coords). Gaia stands at ~1978.5/131/4092.5
   *  in the hollow below; found students face her from their stations. foundTag is the FULL
   *  literal (not derived) so dialog_lint's Java string-literal grant scan sees the tag granted
   *  here — a constructed "found_" + id reads as granted-nowhere (ORPHAN). */
  private static final List<Student> STUDENTS = List.of(
    new Student("kalahar_jr_apprentice", "Jr. Apprentice Dune", fake("dune"),    true,  "ci_kal_dune",    1978.5, 136, 4142.5, "kalahar_found_dune",    "found_kalahar_jr_apprentice"),
    new Student("kalahar_apprentice",    "Apprentice Terra",    fake("terra"),   true,  "ci_kal_terra",   1978.5, 136, 4032.5, "kalahar_found_terra",   "found_kalahar_apprentice"),
    new Student("kalahar_trainer_1",     "Hiker Boulder",       fake("boulder"), false, "ci_kal_boulder", 1934.5, 136, 4043.5, "kalahar_found_boulder", "found_kalahar_trainer_1"),
    new Student("kalahar_trainer_2",     "Ruin Maniac Dustin",  fake("dustin"),  false, "ci_kal_dustin",  2022.5, 136, 4043.5, "kalahar_found_dustin",  "found_kalahar_trainer_2"),
    new Student("kalahar_trainer_3",     "Archaeologist Juno",  fake("juno"),    false, "ci_kal_juno",    1934.5, 136, 4131.5, "kalahar_found_juno",    "found_kalahar_trainer_3"),
    new Student("kalahar_trainer_4",     "Prospector Vince",    fake("vince"),   false, "ci_kal_vince",   2022.5, 136, 4131.5, "kalahar_found_vince",   "found_kalahar_trainer_4")
  );

  /** Gaia's spot in the hollow — a found student teleports in FACING her. */
  private static final double GAIA_X = 1978.5, GAIA_Z = 4092.5;

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
      // Skip students the trigger player already beat OR already found (a found real is standing at
      // its gym station — its decoys would be unresolvable leftovers).
      if (trigger != null && (trigger.getTags().contains("defeated_" + s.trainerId())
          || trigger.getTags().contains(s.foundTag()))) continue;
      // a19 (playtest 2026-08-04 follow-up): the REAL is dealt onto the SAME shuffled pool as its
      // mirages — reals are summon-only now (no placement latch), so NOTHING stands in town until
      // Tarek's button fires this scatter, and position gives nothing away. Belt: sweep any loose
      // unfound real of this student first (a dev clear+restart would otherwise double-spawn it).
      server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + s.realTag() + "]");
      int copies = s.apprentice() ? cfg.getApprenticeMirageCount() : cfg.getTrainerMirageCount();
      for (int k = 0; k < copies && idx < pool.size(); k++, idx++) {
        KalaharConfig.Pos p = pool.get(idx);
        level.getChunk(((int) Math.floor(p.x)) >> 4, ((int) Math.floor(p.z)) >> 4); // load + persist target chunk
        String preset = (k == 0)
          ? "easy_npc:preset/humanoid/" + s.trainerId() + ".npc.snbt" // the real (k=0 of a SHUFFLED pool = a random spot)
          : s.fakePreset();
        server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
          "easy_npc preset import_new data %s %.2f %.2f %.2f", preset, p.x, p.y, p.z));
        spawned++;
      }
    }
    if (trigger != null) {
      // a19: the hunt fires from Tarek's dialog button (playtest 2026-08-04 N1) — no cutscene here
      // (the GYM6introcutscene recording turned out to be Gaia's leader intro, note 4).
      trigger.displayClientMessage(Component.literal(
        "§eTarek leans over the empty spice trays: §7There — feel it? The Reach just scattered. "
        + "Gaia's students hid among their own mirages. Walk up to every familiar face and reach "
        + "out; the desert only keeps the honest ones solid."), false);
    }
    return spawned;
  }

  // ── reach out (mirage dialog button → /... kalahar reach, fakes AND reals) ──────

  /**
   * Resolve the figure the player is reaching for. A FAKE ({@code ci_mirage_fake}) rolls the classic
   * 50/50 poof-or-Doppler. A REAL student ({@code ci_kal_*}, not yet found) is the a18 FOUND beat
   * (playtest N23): firework + found tag + the body streams back to the gym hollow (facing Gaia) +
   * the per-student recorded cutscene — the battle then happens IN THE GYM.
   */
  public static int reach(ServerPlayer player) {
    if (player == null) return 0;
    KalaharConfig cfg = KalaharConfig.get();
    ServerLevel level = player.serverLevel();
    AABB box = player.getBoundingBox().inflate(3.0);

    // Fakes take precedence (a real never stands on a scatter spot, but be deterministic).
    Entity nearest = null;
    double best = Double.MAX_VALUE;
    for (Entity e : level.getEntities(player, box, e -> e.getTags().contains(cfg.fakeTag))) {
      double d = e.distanceToSqr(player);
      if (d < best) { best = d; nearest = e; }
    }
    if (nearest != null) {
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

    // No fake in range — is the player reaching for a REAL student?
    Student found = null;
    Entity realBody = null;
    best = Double.MAX_VALUE;
    for (Student s : STUDENTS) {
      if (player.getTags().contains(s.foundTag())) continue;
      for (Entity e : level.getEntities(player, box, e -> e.getTags().contains(s.realTag()))) {
        double d = e.distanceToSqr(player);
        if (d < best) { best = d; found = s; realBody = e; }
      }
    }
    if (found == null || realBody == null) return 0;

    if (!huntStarted(level.getServer())) {
      // Pre-hunt nudge: the beat belongs to the guide — send the player to Tarek first.
      player.displayClientMessage(Component.literal(
        "§7The figure wavers under your hand but holds. Something is wrong across the Reach — "
        + "the spice-seller by the gym has been shouting about it."), true);
      return 0;
    }

    player.addTag(found.foundTag());
    double x = realBody.getX(), y = realBody.getY(), z = realBody.getZ();
    // The FOUND beat: a firework over the true one, a sand-burst, and the body streams home.
    launchFirework(level, x, y + 1.5, z);
    level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
        ParticleTypes.FALLING_DUST, net.minecraft.world.level.block.Blocks.SAND.defaultBlockState()),
      x, y + 1.0, z, 40, 0.4, 0.8, 0.4, 0.02);
    level.playSound(null, realBody.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);

    // Teleport the real body to its gym-hollow spot, facing Gaia (chunk-load first — the hollow is
    // likely unloaded while the player hunts in town; the loaded chunk also persists the move).
    level.getChunk(((int) Math.floor(found.hx())) >> 4, ((int) Math.floor(found.hz())) >> 4);
    float yaw = (float) (Math.toDegrees(Math.atan2(GAIA_Z - found.hz(), GAIA_X - found.hx())) - 90.0);
    realBody.teleportTo(level, found.hx(), found.hy(), found.hz(),
      java.util.Set.of(), yaw, 0.0f);

    // The recorded found-dive (cosmetic — the tp above already happened, so a skip loses nothing).
    runAsPlayer(player, "cutscene play " + found.cutscene());
    player.displayClientMessage(Component.literal(
      "§6Real! §e" + found.name() + "§7 streams back to the Reach — face them in the gym."), false);
    return 1;
  }

  /** Gold desert firework over a found real (LARGE_BALL, trail + twinkle, short fuse). */
  private static void launchFirework(ServerLevel level, double x, double y, double z) {
    net.minecraft.world.item.ItemStack rocket =
      new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET);
    rocket.set(net.minecraft.core.component.DataComponents.FIREWORKS,
      new net.minecraft.world.item.component.Fireworks(1, List.of(
        new net.minecraft.world.item.component.FireworkExplosion(
          net.minecraft.world.item.component.FireworkExplosion.Shape.LARGE_BALL,
          it.unimi.dsi.fastutil.ints.IntList.of(0xE9A13B, 0xC9A227),
          it.unimi.dsi.fastutil.ints.IntList.of(0xFFF3C9),
          true, true))));
    level.addFreshEntity(new net.minecraft.world.entity.projectile.FireworkRocketEntity(level, x, y, z, rocket));
  }

  private static void runAsPlayer(ServerPlayer player, String command) {
    MinecraftServer server = player.getServer();
    if (server == null) return;
    server.getCommands().performPrefixedCommand(
      player.createCommandSourceStack().withPermission(2).withSuppressedOutput(), command);
  }

  private static void spawnDoppler(MinecraftServer server, ServerLevel level, double x, double y, double z) {
    if (server == null) return;
    KalaharConfig cfg = KalaharConfig.get();
    var src = server.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, String.format(Locale.ROOT,
      "easy_npc preset import_new data %s %.2f %.2f %.2f", cfg.dopplerPreset, x, y, z));
  }

  // ── clear / dev ─────────────────────────────────────────────────────────────────

  /** {@code /cobblemon-initiative kalahar clear} — remove every fake, Doppler AND real body, then
   *  re-arm the hunt (a19: reals are summon-only, so a full reset must sweep them too; the next
   *  Tarek-button start re-deals everything — beaten students stay beaten via their tags, only
   *  their cosmetic station bodies vacate). */
  public static int clear(MinecraftServer server) {
    cleanupDecoys(server);
    var src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    for (Student s : STUDENTS) {
      server.getCommands().performPrefixedCommand(src, "kill @e[tag=" + s.realTag() + "]");
    }
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
   *  post-victory dissipation in {@link #tick}. Chunk-loads the scatter pool first (review-found):
   *  the leader falls in the gym hollow while pool spots sit 200+ blocks out — {@code kill @e}
   *  only reaches LOADED entities, and tick(C) latches #cleaned after ONE pass, so an unloaded
   *  fake would otherwise survive forever. Dopplers spawn at fake positions, so the same pass
   *  covers them. */
  private static void cleanupDecoys(MinecraftServer server) {
    KalaharConfig cfg = KalaharConfig.get();
    ServerLevel level = levelFor(server, cfg);
    if (level != null && cfg.scatterSpots != null) {
      for (KalaharConfig.Pos p : cfg.scatterSpots) {
        level.getChunk(((int) Math.floor(p.x)) >> 4, ((int) Math.floor(p.z)) >> 4);
      }
    }
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

    // (B) — REMOVED a19 (playtest 2026-08-04 N1): the hunt no longer auto-starts on guide
    // proximity; it fires from Tarek Ramessu's dialog button (`kalahar start`, idempotent via
    // the #started flag). KalaharConfig.guidePos/guideTriggerRadius are retained for config-file
    // compatibility but nothing reads them anymore.

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

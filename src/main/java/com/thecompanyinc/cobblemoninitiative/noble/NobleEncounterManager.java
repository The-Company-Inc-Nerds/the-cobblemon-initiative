package com.thecompanyinc.cobblemoninitiative.noble;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.NobleConfig;
import com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterState.Phase;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Drives noble boss encounters — the shrine "director" pattern applied to a real-time boss.
 * Phase 1 is an Easy NPC body (native melee + real health); this manager adds the ranged/AoE
 * attacks, arena ring, and stagger boss bar. At stagger it body-swaps to a real Cobblemon and
 * opens a wild battle for the catch. Mirrors
 * {@link com.thecompanyinc.cobblemoninitiative.shrine.ShrineChallengeManager}.
 */
public class NobleEncounterManager {

  private static final Gson GSON = new Gson();

  /** Shipped nobles (extend as new JSON configs are added). */
  public static final String[] NOBLE_IDS = {
    "groudon", "kyogre", "rayquaza", "articuno", "zapdos", "moltres",
    "mew",
  };

  /** Gap (ticks) between successive attacks so telegraphs never overlap. */
  private static final int ATTACK_GAP_TICKS = 25;
  private static final double[] RING_HEIGHTS = { 0.3, 1.4, 2.5 };

  private final Map<UUID, NobleEncounterState> activeStates = new HashMap<>();
  private final Map<String, NobleEncounterConfig> nobles = new HashMap<>();

  // ── Loading ─────────────────────────────────────────────────────────────────

  public void loadNobles() {
    nobles.clear();
    for (String id : NOBLE_IDS) {
      String path = "data/cobblemon_initiative/noble_encounters/" + id + ".json";
      try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
        if (in == null) {
          InitiativeInit.LOGGER.warn("Noble encounter config not found: {}", path);
          continue;
        }
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
          NobleEncounterConfig cfg = GSON.fromJson(reader, NobleEncounterConfig.class);
          nobles.put(id, cfg);
          InitiativeInit.LOGGER.info("Loaded noble encounter: {} ({})", id, cfg.getDisplayName());
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("Failed to load noble encounter: {}", id, e);
      }
    }
  }

  // ── Query ───────────────────────────────────────────────────────────────────

  public NobleEncounterConfig getNoble(String id) { return nobles.get(id); }
  public String[] getNobleIds() { return NOBLE_IDS; }
  public boolean hasActive(UUID playerId) { return activeStates.containsKey(playerId); }

  // ── Command entry points ─────────────────────────────────────────────────────

  /** Called by /noble start <id>. */
  public boolean start(ServerPlayer player, String nobleId) {
    if (!NobleConfig.get().isNoblesEnabled()) {
      player.sendSystemMessage(Component.literal("§cNoble encounters are disabled."));
      return false;
    }
    NobleEncounterConfig cfg = nobles.get(nobleId);
    if (cfg == null) {
      player.sendSystemMessage(Component.literal("§cUnknown noble: " + nobleId));
      return false;
    }
    MinecraftServer server = player.getServer();
    if (server == null) return false;

    // Clear any pre-existing encounter (no penalty).
    NobleEncounterState existing = activeStates.get(player.getUUID());
    if (existing != null) {
      teardown(server, existing);
      activeStates.remove(player.getUUID());
    }

    NobleEncounterState state = new NobleEncounterState(player.getUUID(), nobleId);

    // Arena: config center if present, else the player's feet (dev convenience).
    NobleEncounterConfig.Arena arena = cfg.getArena();
    double cx, cy, cz;
    if (arena.center != null && arena.center.length == 3
        && !(arena.center[0] == 0 && arena.center[1] == 0 && arena.center[2] == 0)) {
      cx = arena.center[0] + 0.5; cy = arena.center[1]; cz = arena.center[2] + 0.5;
    } else {
      cx = player.getX(); cy = player.getY(); cz = player.getZ();
    }
    state.setArena(cx, cy, cz, arena.radius, arena.dimension);

    ServerLevel level = resolveLevel(server, arena.dimension);
    if (level == null) {
      player.sendSystemMessage(Component.literal("§cNoble arena dimension not loaded: " + arena.dimension));
      return false;
    }

    // Boss bar (full during the intro; mirrors body health once REALTIME begins).
    if (NobleConfig.get().isBossBarEnabled()) {
      ServerBossEvent bar = new ServerBossEvent(
        Component.literal(cfg.getDisplayName()),
        parseColor(cfg.getStagger().bossBarColor),
        parseOverlay(cfg.getStagger().bossBarOverlay)
      );
      bar.setProgress(1.0f);
      bar.addPlayer(player);
      state.setBossBar(bar);
    }

    // Spawn the Phase-1 Easy NPC body; it's discovered by tag in tickIntro.
    spawnBody(server, cfg, state);

    activeStates.put(player.getUUID(), state);

    sendTitle(player, cfg.getStartTitle(), cfg.getStartSubtitle(), 10, 60, 20);
    NobleFx.playSoundId(level, cx, cy, cz, cfg.getSounds().start, NobleConfig.get().getSfxVolume(), NobleConfig.get().getSfxPitch());
    InitiativeInit.LOGGER.info("Player {} started noble encounter {}", player.getName().getString(), nobleId);
    return true;
  }

  /** Player-facing /noble-abort. */
  public void abort(ServerPlayer player) {
    NobleEncounterState state = activeStates.get(player.getUUID());
    if (state == null) {
      player.sendSystemMessage(Component.literal("§7You have no active noble encounter."));
      return;
    }
    MinecraftServer server = player.getServer();
    fail(server, player, state, "§7You withdrew from the noble.");
  }

  // ── Server lifecycle ─────────────────────────────────────────────────────────

  public void onServerStarted(MinecraftServer server) {
    // Sessions never survive a restart — belt-and-braces (the map is already empty on a fresh JVM).
    activeStates.clear();
  }

  public void onServerStopping(MinecraftServer server) {
    for (NobleEncounterState state : new ArrayList<>(activeStates.values())) {
      teardown(server, state);
    }
    activeStates.clear();
  }

  // ── Tick ────────────────────────────────────────────────────────────────────

  public void tick(MinecraftServer server) {
    if (activeStates.isEmpty()) return;
    for (NobleEncounterState state : new ArrayList<>(activeStates.values())) {
      ServerPlayer player = server.getPlayerList().getPlayer(state.getPlayerId());
      if (player == null) { // logout
        teardown(server, state);
        activeStates.remove(state.getPlayerId());
        continue;
      }
      NobleEncounterConfig cfg = nobles.get(state.getNobleId());
      if (cfg == null) {
        teardown(server, state);
        activeStates.remove(state.getPlayerId());
        continue;
      }
      if (player.isDeadOrDying()) {
        fail(server, player, state, "§cThe noble bested you.");
        continue;
      }
      switch (state.getPhase()) {
        case INTRO -> tickIntro(server, player, state, cfg);
        case REALTIME -> tickRealtime(server, player, state, cfg);
        case BATTLE -> tickBattle(server, player, state, cfg);
        default -> { /* COMPLETE / FAILED already removed */ }
      }
    }
  }

  private void tickIntro(MinecraftServer server, ServerPlayer player, NobleEncounterState state, NobleEncounterConfig cfg) {
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    if (level == null) return;

    if (state.getBodyUuid() == null) {
      Entity body = findBodyByTag(level, cfg.getBodyTag());
      if (body != null) {
        state.setBodyUuid(body.getUUID());
        // Ensure the body opens at full health regardless of preset attribute-apply order,
        // so it never spawns below the stagger threshold and skips Phase 1.
        if (body instanceof LivingEntity le) le.setHealth(le.getMaxHealth());
      }
    }

    drawArenaRing(level, state, cfg);

    if (state.getPhaseElapsedMs() >= cfg.getIntroSeconds() * 1000L) {
      if (state.getBodyUuid() == null) {
        fail(server, player, state, "§cThe noble failed to manifest (body preset missing?).");
        return;
      }
      state.setPhase(Phase.REALTIME);
    }
  }

  private void tickRealtime(MinecraftServer server, ServerPlayer player, NobleEncounterState state, NobleEncounterConfig cfg) {
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    if (level == null) return;

    if ("chase".equals(cfg.getType())) {
      tickChase(server, player, state, cfg, level);
      return;
    }

    LivingEntity body = resolveBody(server, state);
    if (body == null) {
      // The player killed the frenzied body (or it despawned) — treat as subdued → Phase 2.
      enterStaggerAndSwap(server, player, state, cfg, level);
      return;
    }

    // Boss bar mirrors the body's real health.
    ServerBossEvent bar = state.getBossBar();
    if (bar != null && body.getMaxHealth() > 0) {
      bar.setProgress(Mth.clamp(body.getHealth() / body.getMaxHealth(), 0f, 1f));
    }

    // Stagger threshold.
    if (body.getHealth() <= body.getMaxHealth() * cfg.getStagger().staggerAtHealthFraction) {
      enterStaggerAndSwap(server, player, state, cfg, level);
      return;
    }

    enforceRing(player, state);
    drawArenaRing(level, state, cfg);

    AmbientTheme.resolve(cfg.getAmbientTheme()).tick(player, level, state);

    if (cfg.isFlyer()) tickFlyer(body, state, cfg, player);
    else tetherBody(body, state);

    // Combat: advance transient objects, then maybe fire a new attack.
    ElementTheme element = ElementTheme.resolve(cfg.getElement());
    NobleAttacks.Context ctx = new NobleAttacks.Context(level, player, body, state, cfg, element);
    NobleAttacks.tickTransients(ctx);
    fireAttacks(state, cfg, ctx);
  }

  private void tickBattle(MinecraftServer server, ServerPlayer player, NobleEncounterState state, NobleEncounterConfig cfg) {
    // Completion is event-driven (onBattleVictory / onPokemonCaptured). Safety net for a
    // battle that ended some other way (fled): after a short grace, if nothing is battling, end it.
    if (state.getPhaseElapsedMs() < 3000L) return;
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    Entity gr = (level != null && state.getBattleEntityUuid() != null) ? level.getEntity(state.getBattleEntityUuid()) : null;
    if (gr == null) {
      fail(server, player, state, "§7The noble slipped away.");
    } else if (gr instanceof PokemonEntity pe && !pe.isBattling()) {
      fail(server, player, state, "§7The noble slipped away.");
    }
  }

  /**
   * Friendly "chase" task (Mew): the invulnerable body flees; the player tags it
   * {@code tagsRequired} times to tire it out, then it becomes the catchable wild Cobblemon.
   */
  private void tickChase(MinecraftServer server, ServerPlayer player, NobleEncounterState state,
                         NobleEncounterConfig cfg, ServerLevel level) {
    LivingEntity body = resolveBody(server, state);
    if (body == null) { // invulnerable, so shouldn't vanish — but if it does, treat as caught
      enterStaggerAndSwap(server, player, state, cfg, level);
      return;
    }
    NobleEncounterConfig.Chase c = cfg.getChase() != null ? cfg.getChase() : new NobleEncounterConfig.Chase();
    ElementTheme element = ElementTheme.resolve(cfg.getElement());

    ServerBossEvent bar = state.getBossBar();
    if (bar != null) bar.setProgress(Mth.clamp((float) state.getTaskProgress() / Math.max(1, c.tagsRequired), 0f, 1f));

    enforceRing(player, state);
    drawArenaRing(level, state, cfg);
    AmbientTheme.resolve(cfg.getAmbientTheme()).tick(player, level, state);

    double hoverY = state.getArenaY() + c.hoverHeight;
    body.setNoGravity(true);
    if (state.getTaskCooldown() > 0) state.setTaskCooldown(state.getTaskCooldown() - 1);

    double dx = body.getX() - player.getX();
    double dz = body.getZ() - player.getZ();
    double dist = Math.sqrt(dx * dx + dz * dz);

    // Tag!
    if (dist <= c.touchRadius && state.getTaskCooldown() <= 0) {
      state.setTaskProgress(state.getTaskProgress() + 1);
      state.setTaskCooldown(c.tagCooldownTicks);
      NobleFx.burst(level, element.impactParticle(), body.getX(), body.getY() + 0.6, body.getZ(), 24, 0.4, 0.06);
      NobleFx.burst(level, ParticleTypes.HEART, body.getX(), body.getY() + 1.1, body.getZ(), 6, 0.35, 0.0);
      NobleFx.playSoundId(level, body.getX(), body.getY(), body.getZ(), element.castSoundId(), 1.0f, 1.6f);
      player.displayClientMessage(Component.literal("§dTagged " + cfg.getDisplayName().replaceAll("§.", "")
        + "! §7(" + state.getTaskProgress() + "/" + c.tagsRequired + ")"), true);
      if (state.getTaskProgress() >= c.tagsRequired) {
        enterStaggerAndSwap(server, player, state, cfg, level);
        return;
      }
      blinkBody(level, body, state, Math.max(c.blinkRange, c.fleeRadius + 2), hoverY, element);
      return;
    }

    // Flee / idle.
    if (dist <= c.fleeRadius) {
      if (Math.random() < c.blinkChance) {
        blinkBody(level, body, state, c.blinkRange, hoverY, element);
      } else {
        double nx = dist < 1.0e-3 ? 1 : dx / dist;
        double nz = dist < 1.0e-3 ? 0 : dz / dist;
        double tx = body.getX() + nx * c.fleeSpeed * 4.0;
        double tz = body.getZ() + nz * c.fleeSpeed * 4.0;
        double maxR = Math.max(2.0, state.getArenaRadius() - 2);
        double ox = tx - state.getArenaX(), oz = tz - state.getArenaZ();
        double od = Math.sqrt(ox * ox + oz * oz);
        if (od > maxR) { tx = state.getArenaX() + ox / od * maxR; tz = state.getArenaZ() + oz / od * maxR; }
        faceMoveTo(body, tx, hoverY, tz, player);
        NobleFx.burst(level, element.ambientParticle(), body.getX(), body.getY() + 0.4, body.getZ(), 2, 0.1, 0.0);
      }
    } else {
      faceMoveTo(body, body.getX(), hoverY, body.getZ(), player);
    }
  }

  private static void blinkBody(ServerLevel level, LivingEntity body, NobleEncounterState state,
                                double minHop, double hoverY, ElementTheme element) {
    NobleFx.burst(level, element.impactParticle(), body.getX(), body.getY() + 0.5, body.getZ(), 12, 0.3, 0.05);
    double maxR = Math.max(2.0, state.getArenaRadius() - 2);
    for (int i = 0; i < 8; i++) {
      double a = Math.random() * Math.PI * 2;
      double r = maxR * (0.4 + Math.random() * 0.6);
      double x = state.getArenaX() + Math.cos(a) * r;
      double z = state.getArenaZ() + Math.sin(a) * r;
      if (NobleFx.horizontalDistSq(x, z, body.getX(), body.getZ()) >= minHop * minHop) {
        body.moveTo(x, hoverY, z, body.getYRot(), 0f);
        body.setDeltaMovement(0, 0, 0);
        NobleFx.burst(level, element.impactParticle(), x, hoverY + 0.5, z, 12, 0.3, 0.05);
        return;
      }
    }
  }

  private static void faceMoveTo(LivingEntity body, double x, double y, double z, ServerPlayer player) {
    float yaw = (float) (Math.toDegrees(Math.atan2(player.getZ() - z, player.getX() - x)) - 90.0);
    body.moveTo(x, y, z, yaw, 0f);
    body.setDeltaMovement(0, 0, 0);
    body.hurtMarked = true;
  }

  // ── Transitions ──────────────────────────────────────────────────────────────

  private void enterStaggerAndSwap(MinecraftServer server, ServerPlayer player, NobleEncounterState state,
                                   NobleEncounterConfig cfg, ServerLevel level) {
    // Despawn the frenzied Easy NPC body.
    despawnBody(server, state);

    // Tear down the boss bar + lingering combat objects/effects (Cobblemon shows its own battle UI).
    ServerBossEvent bar = state.getBossBar();
    if (bar != null) { bar.removeAllPlayers(); state.setBossBar(null); }
    state.getBolts().clear();
    state.getPendingImpacts().clear();
    state.getBeams().clear();
    state.getHazardZones().clear();
    AmbientTheme.resolve(cfg.getAmbientTheme()).clear(player);

    String stTitle = cfg.getStaggerTitle() != null ? cfg.getStaggerTitle() : "§e§lSTAGGERED";
    String stSub = cfg.getStaggerSubtitle() != null ? cfg.getStaggerSubtitle() : "§7" + cfg.getDisplayName();
    sendTitle(player, stTitle, stSub, 5, 40, 15);
    NobleFx.playSoundId(level, state.getArenaX(), state.getArenaY(), state.getArenaZ(),
      cfg.getSounds().stagger, NobleConfig.get().getSfxVolume(), 1.0f);

    // Spawn the REAL Cobblemon at the same spot and open a wild (catchable) battle.
    try {
      String species = cfg.getBattleSpecies() != null ? cfg.getBattleSpecies() : cfg.getId();
      PokemonProperties props = PokemonProperties.Companion.parse(species);
      PokemonEntity gr = props.createEntity(level);
      float yaw = (float) (Math.toDegrees(Math.atan2(player.getZ() - state.getArenaZ(), player.getX() - state.getArenaX())) - 90.0);
      gr.moveTo(state.getArenaX(), state.getArenaY(), state.getArenaZ(), yaw, 0f);
      gr.setPersistenceRequired();
      level.addFreshEntity(gr);
      state.setBattleEntityUuid(gr.getUUID());
      state.setBattlePokemonUuid(gr.getPokemon().getUuid());

      if (cfg.getPhase2().healPartyBeforeBattle) healParty(player);

      BattleBuilder.INSTANCE.pve(player, gr);
      state.setBattleId(gr.getBattleId()); // raw UUID, may be null if the battle didn't start
      state.setPhase(Phase.BATTLE);
      InitiativeInit.LOGGER.info("Noble {} staggered — wild battle opened for {}", state.getNobleId(), player.getName().getString());
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to open noble Phase-2 battle for {}", state.getNobleId(), e);
      fail(server, player, state, "§cThe noble could not be engaged.");
    }
  }

  private void complete(MinecraftServer server, ServerPlayer player, NobleEncounterState state) {
    NobleEncounterConfig cfg = nobles.get(state.getNobleId());
    if (cfg != null) {
      grantRewards(server, player, cfg);
      sendTitle(player, cfg.getCompleteTitle(), cfg.getCompleteSubtitle(), 10, 70, 30);
      ServerLevel level = resolveLevel(server, state.getArenaDimension());
      if (level != null) {
        NobleFx.playSoundId(level, player.getX(), player.getY(), player.getZ(),
          cfg.getSounds().complete, NobleConfig.get().getSfxVolume(), 1.0f);
      }
      player.sendSystemMessage(Component.literal("§6§l[Noble] §r§e" + cfg.getDisplayName() + " §6subdued!"));
    }
    teardown(server, state);
    activeStates.remove(state.getPlayerId());
    InitiativeInit.LOGGER.info("Player {} completed noble encounter {}", player.getName().getString(), state.getNobleId());
  }

  private void fail(MinecraftServer server, ServerPlayer player, NobleEncounterState state, String reason) {
    player.sendSystemMessage(Component.literal(reason + " §7The noble encounter has ended."));
    teardown(server, state);
    activeStates.remove(state.getPlayerId());
    InitiativeInit.LOGGER.info("Player {} noble encounter {} ended: {}", player.getName().getString(), state.getNobleId(), reason);
  }

  /** Idempotent cleanup: boss bar, live bodies, and lingering effects. */
  private void teardown(MinecraftServer server, NobleEncounterState state) {
    ServerBossEvent bar = state.getBossBar();
    if (bar != null) { bar.removeAllPlayers(); state.setBossBar(null); }

    despawnBody(server, state);

    // A still-standing Phase-2 wild entity (abort mid-battle) is discarded; a captured one
    // isn't alive in the world, and a defeated one is already gone.
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    if (level != null && state.getBattleEntityUuid() != null) {
      Entity gr = level.getEntity(state.getBattleEntityUuid());
      if (gr != null && gr.isAlive() && !(gr instanceof PokemonEntity pe && pe.isBattling())) gr.discard();
    }

    ServerPlayer player = server != null ? server.getPlayerList().getPlayer(state.getPlayerId()) : null;
    if (player != null) {
      NobleEncounterConfig cfg = nobles.get(state.getNobleId());
      if (cfg != null) AmbientTheme.resolve(cfg.getAmbientTheme()).clear(player);
    }
  }

  // ── Cobblemon event hooks (subscribed in NobleEncounterInit) ─────────────────

  public void onBattleVictory(BattleVictoryEvent event) {
    PokemonBattle battle = event.getBattle();
    UUID bid = battle.getBattleId();
    if (bid == null) return;

    List<BattleActor> everyone = new ArrayList<>();
    everyone.addAll(event.getWinners());
    everyone.addAll(event.getLosers());

    for (BattleActor a : everyone) {
      if (a instanceof PlayerBattleActor pba && pba.getEntity() instanceof ServerPlayer sp) {
        NobleEncounterState st = activeStates.get(sp.getUUID());
        if (st == null || st.getPhase() != Phase.BATTLE || !bid.equals(st.getBattleId())) continue;
        boolean playerWon = event.getWinners().stream().anyMatch(
          w -> w instanceof PlayerBattleActor p && p.getEntity() == sp);
        if (playerWon) complete(sp.getServer(), sp, st);
        else fail(sp.getServer(), sp, st, "§cThe noble bested you.");
        return;
      }
    }
  }

  public void onPokemonCaptured(PokemonCapturedEvent event) {
    ServerPlayer player = event.getPlayer();
    if (player == null) return;
    Pokemon captured = event.getPokemon();
    NobleEncounterState st = activeStates.get(player.getUUID());
    if (st != null && st.getPhase() == Phase.BATTLE
        && st.getBattlePokemonUuid() != null
        && st.getBattlePokemonUuid().equals(captured.getUuid())) {
      complete(player.getServer(), player, st);
    }
  }

  // ── Combat helpers ───────────────────────────────────────────────────────────

  private void fireAttacks(NobleEncounterState state, NobleEncounterConfig cfg, NobleAttacks.Context ctx) {
    if (state.getGlobalAttackGap() > 0) { state.setGlobalAttackGap(state.getGlobalAttackGap() - 1); }

    Map<Integer, Integer> cds = state.getAttackCooldowns();
    List<NobleEncounterConfig.Attack> attacks = cfg.getPhase1().attacks;
    if (attacks == null || attacks.isEmpty()) return;

    // Decrement cooldowns.
    for (int i = 0; i < attacks.size(); i++) {
      int cd = cds.getOrDefault(i, 0);
      if (cd > 0) cds.put(i, cd - 1);
    }
    if (state.getGlobalAttackGap() > 0) return;

    // Gather ready attacks and fire one at random.
    List<Integer> ready = new ArrayList<>();
    for (int i = 0; i < attacks.size(); i++) {
      if (cds.getOrDefault(i, 0) <= 0) ready.add(i);
    }
    if (ready.isEmpty()) return;
    int idx = ready.get((int) (Math.random() * ready.size()));
    NobleEncounterConfig.Attack attack = attacks.get(idx);
    NobleAttacks.runAttack(attack, ctx);
    cds.put(idx, attack.cooldownTicks);
    state.setGlobalAttackGap(ATTACK_GAP_TICKS);
  }

  private void enforceRing(ServerPlayer player, NobleEncounterState state) {
    double r = state.getArenaRadius();
    double distSq = NobleFx.horizontalDistSq(player.getX(), player.getZ(), state.getArenaX(), state.getArenaZ());
    if (distSq <= r * r) return;

    double d = Math.sqrt(distSq);
    double push = NobleConfig.get().getRingPushback();
    double nx = state.getArenaX() + (player.getX() - state.getArenaX()) / d * (r - push);
    double nz = state.getArenaZ() + (player.getZ() - state.getArenaZ()) / d * (r - push);
    player.setDeltaMovement(0, 0, 0);
    player.resetFallDistance();
    player.connection.teleport(nx, player.getY(), nz, player.getYRot(), player.getXRot());
    player.displayClientMessage(Component.literal("§cThe arena barrier repels you!"), true);
    NobleFx.playSoundId(player.serverLevel(), player.getX(), player.getY(), player.getZ(),
      "minecraft:block.note_block.bass", 0.7f, 0.6f);
  }

  private void drawArenaRing(ServerLevel level, NobleEncounterState state, NobleEncounterConfig cfg) {
    NobleFx.drawRing(level, state.getArenaX(), state.getArenaY() + 0.2, state.getArenaZ(),
      state.getArenaRadius(), ElementTheme.resolve(cfg.getElement()).telegraphColor(), RING_HEIGHTS);
  }

  private void tetherBody(LivingEntity body, NobleEncounterState state) {
    double r = state.getArenaRadius();
    double distSq = NobleFx.horizontalDistSq(body.getX(), body.getZ(), state.getArenaX(), state.getArenaZ());
    if (distSq > (r - 1) * (r - 1)) {
      Vec3 toward = new Vec3(state.getArenaX() - body.getX(), 0, state.getArenaZ() - body.getZ()).normalize().scale(0.4);
      body.setDeltaMovement(toward.x, body.getDeltaMovement().y, toward.z);
      body.hurtMarked = true;
    }
  }

  private void tickFlyer(LivingEntity body, NobleEncounterState state, NobleEncounterConfig cfg, ServerPlayer player) {
    NobleEncounterConfig.Flyer f = cfg.getFlyer();
    int t = state.getFlyerTimer() + 1;
    if (state.isAirborne()) {
      // Pin above arena center, out of melee reach; face the player.
      float yaw = (float) (Math.toDegrees(Math.atan2(player.getZ() - body.getZ(), player.getX() - body.getX())) - 90.0);
      body.moveTo(state.getArenaX(), state.getArenaY() + f.hoverHeight, state.getArenaZ(), yaw, 0f);
      body.setDeltaMovement(0, 0, 0);
      body.setNoGravity(true);
      if (t >= f.airTicks) { state.setAirborne(false); t = 0; }
    } else {
      // Grounded window: let its native AI chase + be meleeable.
      body.setNoGravity(false);
      tetherBody(body, state);
      if (t >= f.groundedWindowTicks) { state.setAirborne(true); t = 0; }
    }
    state.setFlyerTimer(t);
  }

  // ── Easy NPC body spawn / find / despawn ─────────────────────────────────────

  private void spawnBody(MinecraftServer server, NobleEncounterConfig cfg, NobleEncounterState state) {
    if (cfg.getBodyPreset() == null) return;
    String cmd = String.format(java.util.Locale.ROOT,
      "easy_npc preset import_new data %s %.2f %.2f %.2f",
      cfg.getBodyPreset(), state.getArenaX(), state.getArenaY(), state.getArenaZ());
    runServerCommand(server, cmd);
  }

  private void despawnBody(MinecraftServer server, NobleEncounterState state) {
    if (server == null || state.getBodyUuid() == null) return;
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    Entity body = (level != null) ? level.getEntity(state.getBodyUuid()) : null;
    // Prefer Easy NPC's clean deregister; fall back to a discard if it's still around.
    runServerCommand(server, "easy_npc delete " + state.getBodyUuid());
    if (body != null && body.isAlive()) body.discard();
    state.setBodyUuid(null);
  }

  private static Entity findBodyByTag(ServerLevel level, String tag) {
    if (tag == null || tag.isBlank()) return null;
    for (Entity e : level.getAllEntities()) {
      if (e.isAlive() && e.getTags().contains(tag)) return e;
    }
    return null;
  }

  private LivingEntity resolveBody(MinecraftServer server, NobleEncounterState state) {
    if (state.getBodyUuid() == null) return null;
    ServerLevel level = resolveLevel(server, state.getArenaDimension());
    if (level == null) return null;
    Entity e = level.getEntity(state.getBodyUuid());
    return (e instanceof LivingEntity le && le.isAlive()) ? le : null;
  }

  // ── Rewards ──────────────────────────────────────────────────────────────────

  private void grantRewards(MinecraftServer server, ServerPlayer player, NobleEncounterConfig cfg) {
    NobleEncounterConfig.Rewards rewards = cfg.getRewards();
    if (rewards == null) return;
    CommandSourceStack playerSrc = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();

    if (rewards.storyFlag != null && rewards.storyFlag.objective != null) {
      NobleEncounterConfig.StoryFlag sf = rewards.storyFlag;
      runCommand(server, playerSrc, "scoreboard objectives add " + sf.objective + " dummy");
      runCommand(server, playerSrc, "scoreboard players set " + sf.holder + " " + sf.objective + " " + sf.value);
    }
    if (rewards.achievement != null) {
      runServerCommand(server, "advancement grant " + player.getName().getString() + " only " + rewards.achievement);
    }
    if (rewards.commands != null) {
      for (String raw : rewards.commands) {
        String cmd = raw.replace("{player}", player.getName().getString()).replace("{uuid}", player.getUUID().toString());
        runCommand(server, playerSrc, cmd);
      }
    }
  }

  private void healParty(ServerPlayer player) {
    try {
      PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
      for (int i = 0; i < party.size(); i++) {
        Pokemon p = party.get(i);
        if (p != null) p.heal();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to heal party before noble battle", e);
    }
  }

  // ── Small helpers ────────────────────────────────────────────────────────────

  private static void runServerCommand(MinecraftServer server, String cmd) {
    if (server == null) return;
    runCommand(server, server.createCommandSourceStack().withPermission(4).withSuppressedOutput(), cmd);
  }

  private static void runCommand(MinecraftServer server, CommandSourceStack src, String cmd) {
    if (server == null) return;
    server.getCommands().performPrefixedCommand(src, cmd);
  }

  private static ServerLevel resolveLevel(MinecraftServer server, String dimension) {
    ResourceLocation rl = ResourceLocation.tryParse(dimension);
    if (rl == null) return server.overworld();
    return server.getLevel(ResourceKey.create(Registries.DIMENSION, rl));
  }

  private void sendTitle(ServerPlayer player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title == null ? "" : title)));
    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle == null ? "" : subtitle)));
  }

  private static BossEvent.BossBarColor parseColor(String name) {
    try { return BossEvent.BossBarColor.valueOf(name.toUpperCase(java.util.Locale.ROOT)); }
    catch (Exception e) { return BossEvent.BossBarColor.RED; }
  }

  private static BossEvent.BossBarOverlay parseOverlay(String name) {
    try { return BossEvent.BossBarOverlay.valueOf(name.toUpperCase(java.util.Locale.ROOT)); }
    catch (Exception e) { return BossEvent.BossBarOverlay.PROGRESS; }
  }
}

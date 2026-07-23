package com.thecompanyinc.cobblemoninitiative.cutscene;

import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Reusable cutscene director. A {@link CutsceneScript} (JSON, loaded lazily by id) drops the
 * player into SPECTATOR, drops an optional body-double "you" in frame, flies an invisible
 * marker "camera rig" along the keyframes with the player's camera locked to it, fires timed
 * cues (titles / sounds / commands), then restores the player to their exact pre-scene
 * transform + game mode when the last keyframe completes (or on skip / logout / server stop).
 *
 * <p>Server-authoritative and single-player-safe, modelled on {@code NobleEncounterManager}
 * (per-player session state, a tick() loop, and an idempotent teardown every exit funnels
 * through). Hardcore-safe: a live SPECTATOR swap has no hardcore guard, and a cutscene never
 * touches the Nuzlocke faint/whiteout path, so it cannot trip the death screen.
 */
public class CutsceneManager {

  private static final Gson GSON = new Gson();

  /** Body-double preset MUST bake {@code Tags:["ci_cutscene_double"]} so teardown can find it. */
  private static final String DOUBLE_TAG = "ci_cutscene_double";
  /** The player-skinned double ALSO bakes this so the runtime skin patch can target it and
   * not a sibling figure (the Takehara scene stands a "you" double AND a watcher double). */
  private static final String PLAYER_DOUBLE_TAG = "ci_cutscene_playerdouble";
  private static final String RIG_TAG = "ci_cutscene_rig";

  /** Hard safety cap: no scene runs longer than this many ticks regardless of the script. */
  private static final int MAX_SCENE_TICKS = 20 * 60 * 5; // 5 minutes

  /** Radius (in chunks) of the entity-ticking ticket that pins the scene's SUBJECT (the gym /
   * shrine leader the end-command battles) loaded for the whole scene. The spectator camera drags
   * the player's own chunk ticket onto the flying rig, and {@code endBack} restores the player
   * AWAY from the leader — so a leader a couple of chunks off can unload before {@code end()} runs
   * its {@code tbcs attach} / {@code tbcs battle}, which then throws "…is not attached to an
   * entity". Radius 4 → ticket level 29 (≤31 = entity-ticking), covering ~72 blocks around the
   * click position; the leader is always within a few blocks of it. Released in {@code end()}. */
  private static final int SUBJECT_TICKET_RADIUS = 4;

  private final Map<UUID, CutsceneState> active = new HashMap<>();
  private final Map<String, CutsceneScript> scripts = new HashMap<>();

  /** Deferred double-sweeps: an instant skip can end a scene BEFORE the deferred
   * `import_new` lands its body — the immediate sweep then finds nothing and the
   * clone would persist into the hardcore save. Each entry re-sweeps a dimension a
   * few ticks after its scene ended. [0]=due game time; [1]=dimension id. */
  private final List<Object[]> pendingSweeps = new ArrayList<>();

  // ── Loading (lazy, by id — "add a JSON, no code") ────────────────────────────

  /** Where live-authored/override scenes live: {@code config/cobblemon-initiative/cutscenes/}.
   * Checked BEFORE the bundled jar resources, so a scene can be recorded/tweaked in-game
   * ({@code /cutscene record …}, or edit the file + {@code /cutscene reload}) without a
   * rebuild — and the file can later be promoted into src/main/resources verbatim. */
  public static java.nio.file.Path overrideDir() {
    return net.fabricmc.loader.api.FabricLoader.getInstance()
      .getConfigDir().resolve("cobblemon-initiative").resolve("cutscenes");
  }

  private CutsceneScript getScript(String id) {
    CutsceneScript cached = scripts.get(id);
    if (cached != null) return cached;

    // 1. Filesystem override (live authoring / per-pack tweaks).
    java.nio.file.Path override = overrideDir().resolve(id + ".json");
    if (java.nio.file.Files.isRegularFile(override)) {
      try (Reader r = java.nio.file.Files.newBufferedReader(override, StandardCharsets.UTF_8)) {
        CutsceneScript s = GSON.fromJson(r, CutsceneScript.class);
        if (s != null) { s.id = id; scripts.put(id, s); return s; }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("Failed to load override cutscene: {}", override, e);
        // fall through to the bundled resource
      }
    }

    // 2. Bundled jar resource.
    String path = "data/cobblemon_initiative/cutscenes/" + id + ".json";
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
      if (in == null) return null;
      try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        CutsceneScript s = GSON.fromJson(r, CutsceneScript.class);
        if (s != null) { s.id = id; scripts.put(id, s); }
        return s;
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load cutscene: {}", id, e);
      return null;
    }
  }

  /** Drop the script cache so edited override files are re-read on next play. */
  public int reloadScripts() {
    int n = scripts.size();
    scripts.clear();
    return n;
  }

  public boolean hasActive(UUID playerId) { return active.containsKey(playerId); }
  public List<String> getLoadedIds() { return new ArrayList<>(scripts.keySet()); }

  // ── Entry points ─────────────────────────────────────────────────────────────

  /** Start a scene for the player. Returns false if it could not begin. */
  public boolean play(ServerPlayer player, String scriptId) {
    return play(player, scriptId, null);
  }

  /** Start a scene with a command to run AS the player when it ends or is skipped —
   * the chain the gym-leader intros use ({@code cutscene play leader_intro function
   * …:gym/engage/<trainer>} — the engage function opens the battle from wherever the
   * scene left the player, i.e. the endBack stage position). */
  public boolean play(ServerPlayer player, String scriptId, String endCommand) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    if (player.isDeadOrDying()) return false; // never hijack a death/whiteout in progress

    CutsceneScript script = getScript(scriptId);
    if (script == null) {
      player.sendSystemMessage(Component.literal("§cUnknown cutscene: " + scriptId));
      return false;
    }
    if (script.keyframes == null || script.keyframes.isEmpty()) {
      InitiativeInit.LOGGER.warn("Cutscene {} has no keyframes; ignoring.", scriptId);
      return false;
    }

    // Restart cleanly if one is already playing for this player.
    CutsceneState existing = active.get(player.getUUID());
    if (existing != null) { end(server, existing, player); }

    // A scene always plays in the player's current level (a cross-dimension camera cannot
    // spectate an entity in another level); the dimension id is stored for the double sweep.
    ServerLevel level = player.serverLevel();
    String dimId = level.dimension().location().toString();

    // Capture restore transform BEFORE any swap.
    GameType restoreMode = player.gameMode.getGameModeForPlayer();
    double eyeX = player.getX(), eyeY = player.getEyeY(), eyeZ = player.getZ();
    float startYaw = player.getYRot(), startPitch = player.getXRot();

    // endBack: restore the player STRAIGHT BACK from where they stood (opposite the
    // captured facing, horizontal, same Y) — the pre-boss stage-back. Yaw/pitch keep
    // facing the leader. No ground probe: use on flat arenas.
    double restoreX = player.getX(), restoreZ = player.getZ();
    if (script.endBack > 0) {
      double yawRad = Math.toRadians(startYaw);
      restoreX += Math.sin(yawRad) * script.endBack;  // -forward.x * endBack
      restoreZ -= Math.cos(yawRad) * script.endBack;  // -forward.z * endBack
    }

    // Optional body-double(s) (each must bake the DOUBLE_TAG in its preset for cleanup).
    List<CutsceneScript.DoubleSpec> doubleSpecs = resolveDoubles(script);
    boolean hasDouble = spawnDoubles(server, level, player, doubleSpecs);

    // The camera rig: an invisible, weightless armor stand the client tracks + spectates.
    ArmorStand rig = new ArmorStand(level, eyeX, eyeY, eyeZ);
    rig.setInvisible(true);
    rig.setNoGravity(true);
    rig.setInvulnerable(true);
    rig.setSilent(true);
    rig.setNoBasePlate(true);
    rig.moveTo(eyeX, eyeY, eyeZ, startYaw, startPitch);
    rig.setYHeadRot(startYaw);
    rig.setDeltaMovement(Vec3.ZERO);
    rig.addTag(RIG_TAG);
    level.addFreshEntity(rig);

    CutsceneState state = new CutsceneState(
      player.getUUID(), scriptId, dimId, script.skippable, hasDouble,
      restoreX, player.getY(), restoreZ, startYaw, startPitch, restoreMode,
      eyeX, eyeY, eyeZ, startYaw, startPitch);
    state.setRig(rig);
    state.setBase(eyeX, eyeY, eyeZ, script.relative);
    if (script.relative && script.facingFrame) state.setFacingFrame(startYaw, startPitch);
    if (endCommand != null && !endCommand.isBlank()) state.setEndCommand(endCommand);
    // Only patch a double to the player's live skin when a spec asks for it (the "you"
    // double). Other doubles — e.g. the all-black victory watcher (watcher_shadow_dark) —
    // must keep their own baked skin; patching them would clobber it (the note-20 blocker).
    boolean skinPending = false;
    for (CutsceneScript.DoubleSpec spec : doubleSpecs) {
      if (spec.playerSkin) { skinPending = true; state.setPlayerDoubleYaw(spec.yaw); break; }
    }
    if (hasDouble && skinPending) {
      state.setDoubleSkinPending(true);
    }
    active.put(player.getUUID(), state);

    // Keep the scene subject (the leader the end-command battles) entity-loaded for the whole
    // scene, so the engage function's `execute if entity <leader>` still resolves at end() even
    // after the camera pan + endBack restore drifted the player's own ticket off it. Anchored on
    // the click position (the leader is within dialog range of it); released in end().
    BlockPos subjectPos = BlockPos.containing(eyeX, eyeY, eyeZ);
    ChunkPos subjectChunk = new ChunkPos(subjectPos);
    level.getChunkSource().addRegionTicket(TicketType.PORTAL, subjectChunk, SUBJECT_TICKET_RADIUS, subjectPos);
    state.setSubjectAnchor(subjectChunk, subjectPos);

    // Spectator FIRST, then attach the camera (the vanilla /spectate order; a live swap is
    // hardcore-safe — no guard on changeGameModeForPlayer).
    player.setGameMode(GameType.SPECTATOR);
    player.setCamera(rig);

    applyWeather(player, script.ambientWeather);
    if (script.startTitle != null || script.startSubtitle != null) {
      int fadeIn = script.startTitleFadeIn != null ? script.startTitleFadeIn : 10;
      int stay = script.startTitleStay != null ? script.startTitleStay : 70;
      int fadeOut = script.startTitleFadeOut != null ? script.startTitleFadeOut : 20;
      sendTitle(player, orEmpty(script.startTitle), orEmpty(script.startSubtitle), fadeIn, stay, fadeOut);
    }
    InitiativeInit.LOGGER.info("Player {} started cutscene {}", player.getName().getString(), scriptId);
    return true;
  }

  /** Player-facing skip (bound key / command). No-op if the scene is not skippable. */
  public void skip(ServerPlayer player) {
    CutsceneState state = active.get(player.getUUID());
    if (state == null) return;
    if (!state.isSkippable()) {
      player.sendSystemMessage(Component.literal("§7This scene cannot be skipped."));
      return;
    }
    end(player.getServer(), state, player);
  }

  // ── Lifecycle hooks (wired in CutsceneInit) ──────────────────────────────────

  public void tick(MinecraftServer server) {
    // Drain due double-sweeps FIRST — they must run even with no active scene (the
    // whole point is catching a body that spawned after its scene already ended).
    if (!pendingSweeps.isEmpty()) {
      long now = server.overworld().getGameTime();
      Iterator<Object[]> it = pendingSweeps.iterator();
      while (it.hasNext()) {
        Object[] sweep = it.next();
        if (now >= (Long) sweep[0]) {
          sweepDouble(server, (String) sweep[1]);
          it.remove();
        }
      }
    }
    if (active.isEmpty()) return;
    for (CutsceneState state : new ArrayList<>(active.values())) {
      ServerPlayer player = server.getPlayerList().getPlayer(state.getPlayerId());
      if (player == null) { // logout the DISCONNECT hook missed — just clean up handles
        end(server, state, null);
        continue;
      }
      if (player.isDeadOrDying()) { end(server, state, player); continue; }
      advance(server, state, player);
    }
  }

  /** Restore a player's game mode the instant they disconnect, so a mid-scene logout can
   * never persist SPECTATOR into their save data (they would rejoin stuck as a spectator). */
  public void onDisconnect(ServerPlayer player) {
    if (player == null) return;
    CutsceneState state = active.get(player.getUUID());
    if (state != null) end(player.getServer(), state, player);
  }

  public void onServerStopping(MinecraftServer server) {
    for (CutsceneState state : new ArrayList<>(active.values())) {
      end(server, state, server.getPlayerList().getPlayer(state.getPlayerId()));
    }
    active.clear();
  }

  // ── Playback ─────────────────────────────────────────────────────────────────

  private void advance(MinecraftServer server, CutsceneState state, ServerPlayer player) {
    CutsceneScript script = getScript(state.getScriptId());
    if (script == null) { end(server, state, player); return; }

    // Patch the double's skin to the player once it's discoverable by tag (import_new is
    // deferred and hands back no UUID). Give up after ~40 ticks so a bad preset degrades to
    // the default skin rather than retrying forever.
    if (state.isDoubleSkinPending()) {
      if (applyPlayerDoublePatch(server, state, player) || state.getTickCount() > 40) {
        state.setDoubleSkinPending(false);
      }
    }

    // Fire cues scheduled for this tick.
    if (script.cues != null) {
      for (CutsceneScript.Cue cue : script.cues) {
        if (cue.tick == state.getTickCount()) fireCue(server, player, state, cue);
      }
    }

    // Ease the rig toward the current keyframe.
    List<CutsceneScript.Keyframe> kfs = script.keyframes;
    int idx = state.getKeyframeIndex();
    if (idx >= kfs.size() || state.getTickCount() >= MAX_SCENE_TICKS) { end(server, state, player); return; }
    CutsceneScript.Keyframe kf = kfs.get(idx);
    int dur = Math.max(1, kf.ticks);
    state.incTicksIntoKeyframe();
    float t = Math.min(1f, state.getTicksIntoKeyframe() / (float) dur);

    // Resolve the keyframe target (absolute, or offset from the captured eye when
    // relative; facingFrame additionally rotates offsets into the captured look
    // direction — x=right, z=forward — and treats yaw/pitch as offsets, so one scene
    // plays correctly at any arena orientation).
    double kx, ky, kz;
    float targetYaw = kf.yaw, targetPitch = kf.pitch;
    if (state.isRelative() && state.isFacingFrame()) {
      double yawRad = Math.toRadians(state.getBaseYaw());
      double fwdX = -Math.sin(yawRad), fwdZ = Math.cos(yawRad);   // facing
      double rightX = -fwdZ, rightZ = fwdX;                       // 90° clockwise
      kx = state.getBaseX() + rightX * kf.x + fwdX * kf.z;
      ky = state.getBaseY() + kf.y;
      kz = state.getBaseZ() + rightZ * kf.x + fwdZ * kf.z;
      targetYaw = state.getBaseYaw() + kf.yaw;
      targetPitch = Mth.clamp(state.getBasePitch() + kf.pitch, -90f, 90f);
    } else if (state.isRelative()) {
      kx = state.getBaseX() + kf.x;
      ky = state.getBaseY() + kf.y;
      kz = state.getBaseZ() + kf.z;
    } else {
      kx = kf.x; ky = kf.y; kz = kf.z;
    }

    double cx = Mth.lerp(t, state.getPrevX(), kx);
    double cy = Mth.lerp(t, state.getPrevY(), ky);
    double cz = Mth.lerp(t, state.getPrevZ(), kz);
    float cyaw = Mth.rotLerp(t, state.getPrevYaw(), targetYaw);
    float cpitch = state.getPrevPitch() + (targetPitch - state.getPrevPitch()) * t;

    ArmorStand rig = state.getRig();
    if (rig == null || rig.isRemoved()) { end(server, state, player); return; }
    rig.moveTo(cx, cy, cz, cyaw, cpitch);
    rig.setYRot(cyaw);
    rig.setXRot(cpitch);
    rig.setYHeadRot(cyaw);
    rig.setDeltaMovement(Vec3.ZERO);

    // Keep the hidden player body co-located with the camera: loads chunks around the rig on
    // long pans and makes restore deterministic regardless of any spectator input drift.
    player.connection.teleport(cx, cy, cz, cyaw, cpitch);

    if (state.getTicksIntoKeyframe() >= dur) {
      state.advanceKeyframe(kx, ky, kz, targetYaw, targetPitch);
      if (state.getKeyframeIndex() >= kfs.size()) { end(server, state, player); return; }
    }
    state.incTick();
  }

  private void fireCue(MinecraftServer server, ServerPlayer player, CutsceneState state, CutsceneScript.Cue cue) {
    if (cue.title != null || cue.subtitle != null) {
      sendTitle(player, orEmpty(cue.title), orEmpty(cue.subtitle), 8, 50, 15);
    }
    if (cue.sound != null && !cue.sound.isBlank()) {
      float vol = cue.volume != null ? cue.volume : 1.0f;
      float pitch = cue.pitch != null ? cue.pitch : 1.0f;
      runPlayerCommand(server, player, String.format(Locale.ROOT,
        "playsound %s master %s %.2f %.2f %.2f %.2f %.2f",
        cue.sound, player.getName().getString(), player.getX(), player.getY(), player.getZ(), vol, pitch));
    }
    if (cue.command != null && !cue.command.isBlank()) {
      runPlayerCommand(server, player, cue.command);
    }
  }

  // ── Teardown (the single exit funnel: completion, skip, logout, stop) ────────

  private void end(MinecraftServer server, CutsceneState state, ServerPlayer player) {
    if (player != null) {
      player.setCamera(player);                       // detach from the rig
      player.setGameMode(state.getRestoreMode());     // internally resets the camera too
      player.connection.teleport(state.getRestoreX(), state.getRestoreY(), state.getRestoreZ(),
        state.getRestoreYaw(), state.getRestorePitch());
      // Only undo weather the SCENE applied — a scene with no ambientWeather must not
      // clobber fake sky some other system owns (e.g. the rift dragon's storm).
      CutsceneScript script = getScript(state.getScriptId());
      if (script != null && script.ambientWeather != null && !script.ambientWeather.isBlank()) {
        restoreWeather(player, player.serverLevel());
      }
      // Chain the end command (leader engage, etc.) AFTER the restore, so it fires
      // from the endBack stage position. Runs on skip too — a skipped intro must
      // still open the battle. Never for a dead player (TBCS would refuse anyway).
      if (state.getEndCommand() != null && !player.isDeadOrDying()) {
        runPlayerCommand(server, player, state.getEndCommand());
      }
    }
    ArmorStand rig = state.getRig();
    if (rig != null && !rig.isRemoved()) rig.discard();
    state.setRig(null);
    if (state.hasDouble()) {
      sweepDouble(server, state.getDimension());
      // Re-sweep shortly after: an instant skip can outrun the deferred import_new,
      // leaving the body to spawn AFTER this immediate sweep found nothing.
      if (server != null) {
        pendingSweeps.add(new Object[] { server.overworld().getGameTime() + 20L, state.getDimension() });
      }
    }
    // Release the subject forceload (added in play(), after the end command has already dispatched
    // and resolved the leader): the player is now back in survival beside the leader, so their own
    // ticket keeps it loaded — ours would only pin extra chunks. Self-times-out anyway if missed.
    if (state.getSubjectChunk() != null && server != null) {
      ServerLevel sub = resolveLevel(server, state.getDimension());
      if (sub != null) {
        sub.getChunkSource().removeRegionTicket(
          TicketType.PORTAL, state.getSubjectChunk(), SUBJECT_TICKET_RADIUS, state.getSubjectPos());
      }
    }
    active.remove(state.getPlayerId());
  }

  // ── Body double (optional) ───────────────────────────────────────────────────

  /** Normalise a script's double declaration to a spec list: the {@code doubles} array when
   * present (multi-figure scenes), else a single spec synthesised from {@code doublePreset}/
   * {@code doublePos} (the pre-existing single-double scenes — shadow_watcher, boss intros). */
  private static List<CutsceneScript.DoubleSpec> resolveDoubles(CutsceneScript script) {
    if (script.doubles != null && !script.doubles.isEmpty()) return script.doubles;
    if (script.doublePreset != null && !script.doublePreset.isBlank()) {
      CutsceneScript.DoubleSpec spec = new CutsceneScript.DoubleSpec();
      spec.preset = script.doublePreset;
      spec.pos = script.doublePos;
      spec.playerSkin = script.doublePreset.contains("player_double");
      return List.of(spec);
    }
    return List.of();
  }

  /** Spawn every declared double. Returns whether any were spawned (drives the teardown sweep). */
  private boolean spawnDoubles(MinecraftServer server, ServerLevel level, ServerPlayer player,
                               List<CutsceneScript.DoubleSpec> specs) {
    boolean any = false;
    for (CutsceneScript.DoubleSpec spec : specs) {
      if (spec.preset == null || spec.preset.isBlank()) continue;
      double[] dp = spec.pos;
      double dx = (dp != null && dp.length == 3) ? dp[0] : player.getX();
      double dy = (dp != null && dp.length == 3) ? dp[1] : player.getY();
      double dz = (dp != null && dp.length == 3) ? dp[2] : player.getZ();
      // Facing is NOT set on the source: easy_npc's import_new IGNORES the command-source
      // rotation (bytecode-verified against the pinned Easy NPC jar — the placement functions
      // use `import` onto an EXISTING body, which keeps its own yaw). The player-skinned double
      // is turned to
      // spec.yaw by a data-modify in applyPlayerDoublePatch once it exists; other doubles keep
      // their preset/spawn facing (bake a Rotation into the preset if one needs a set yaw).
      CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
      if (level != null) src = src.withLevel(level).withPosition(new Vec3(dx, dy, dz));
      runCommand(server, src, String.format(Locale.ROOT,
        "easy_npc preset import_new data %s %.2f %.2f %.2f", spec.preset, dx, dy, dz));
      any = true;
    }
    return any;
  }

  /**
   * Patch the discovered player-skinned double: render the player's skin (a /data write into the
   * Easy NPC SkinData compound — there is no skin command for player skins) AND turn it to the
   * scripted facing (import_new ignores the spawn rotation, so the yaw is written here too). The
   * skin resolves from the stored UUID against Mojang's session server, so a normal online client
   * renders "you" with nothing baked in; offline it falls back to the default variant. Returns
   * false (retry next tick) until the tagged body is spawned + discoverable.
   */
  private boolean applyPlayerDoublePatch(MinecraftServer server, CutsceneState state, ServerPlayer player) {
    ServerLevel level = resolveLevel(server, state.getDimension());
    if (level == null) return true; // nothing to patch; stop retrying
    Entity body = null;
    for (Entity e : level.getAllEntities()) {
      // PLAYER_DOUBLE_TAG (not DOUBLE_TAG): a scene may stand several doubles, but only the
      // player-skinned "you" carries this tag — patching a sibling (e.g. the watcher) would
      // clobber its baked skin.
      if (e.isAlive() && e.getTags().contains(PLAYER_DOUBLE_TAG)) { body = e; break; }
    }
    if (body == null) return false; // not spawned yet — retry
    int[] u = UUIDUtil.uuidToIntArray(player.getUUID());
    String name = player.getGameProfile().getName();
    String cmd = String.format(Locale.ROOT,
      "data modify entity %s SkinData set value {Type:\"PLAYER_SKIN\",Name:\"%s\",URL:\"\",UUID:[I;%d,%d,%d,%d],DisableLayers:0b,Content:\"\",Timestamp:0L}",
      body.getUUID(), name, u[0], u[1], u[2], u[3]);
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput().withLevel(level);
    runCommand(server, src, cmd);
    // Face the scripted yaw (import_new spawned it facing default south). Rotation is the base
    // entity [yaw, pitch] list; we only turn it horizontally.
    Float yaw = state.getPlayerDoubleYaw();
    if (yaw != null) {
      runCommand(server, src, String.format(Locale.ROOT,
        "data modify entity %s Rotation set value [%.1ff,0.0f]", body.getUUID(), yaw));
    }
    return true;
  }

  private void sweepDouble(MinecraftServer server, String dimension) {
    ServerLevel level = resolveLevel(server, dimension);
    if (level == null) return;
    for (Entity e : level.getAllEntities()) {
      if (e.isAlive() && e.getTags().contains(DOUBLE_TAG)) {
        runServerCommand(server, "easy_npc delete " + e.getUUID());
        if (e.isAlive()) e.discard();
      }
    }
  }

  // ── Self-contained helpers (no cross-package coupling) ───────────────────────

  private static void applyWeather(ServerPlayer p, String weather) {
    if (weather == null || weather.isBlank()) return;
    switch (weather.toUpperCase(Locale.ROOT)) {
      case "DOWNPOUR", "RAIN" -> {
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0f));
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1f));
      }
      case "THUNDERSTORM", "STORM" -> {
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0f));
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1f));
        p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1f));
      }
      case "CLEAR" -> p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0f));
      default -> { /* unknown: leave the real sky */ }
    }
  }

  private static void restoreWeather(ServerPlayer p, ServerLevel level) {
    boolean raining = level.isRaining();
    p.connection.send(new ClientboundGameEventPacket(
      raining ? ClientboundGameEventPacket.START_RAINING : ClientboundGameEventPacket.STOP_RAINING, 0f));
    p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, level.getRainLevel(1f)));
    p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, level.getThunderLevel(1f)));
  }

  private void sendTitle(ServerPlayer player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
  }

  private static void runPlayerCommand(MinecraftServer server, ServerPlayer player, String cmd) {
    if (server == null || player == null || cmd == null || cmd.isBlank()) return;
    CommandSourceStack src = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(src, cmd);
  }

  private static void runServerCommand(MinecraftServer server, String cmd) {
    if (server == null) return;
    runCommand(server, server.createCommandSourceStack().withPermission(4).withSuppressedOutput(), cmd);
  }

  private static void runCommand(MinecraftServer server, CommandSourceStack src, String cmd) {
    if (server == null) return;
    server.getCommands().performPrefixedCommand(src, cmd);
  }

  private static ServerLevel resolveLevel(MinecraftServer server, String dimension) {
    if (dimension == null) return server.overworld();
    ResourceLocation rl = ResourceLocation.tryParse(dimension);
    if (rl == null) return server.overworld();
    return server.getLevel(ResourceKey.create(Registries.DIMENSION, rl));
  }

  private static String orEmpty(String s) { return s == null ? "" : s; }
}

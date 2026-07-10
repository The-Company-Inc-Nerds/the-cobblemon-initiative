package com.thecompanyinc.cobblemoninitiative.cutscene;

import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
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
  private static final String RIG_TAG = "ci_cutscene_rig";

  /** Hard safety cap: no scene runs longer than this many ticks regardless of the script. */
  private static final int MAX_SCENE_TICKS = 20 * 60 * 5; // 5 minutes

  private final Map<UUID, CutsceneState> active = new HashMap<>();
  private final Map<String, CutsceneScript> scripts = new HashMap<>();

  // ── Loading (lazy, by id — "add a JSON, no code") ────────────────────────────

  private CutsceneScript getScript(String id) {
    CutsceneScript cached = scripts.get(id);
    if (cached != null) return cached;
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

  public boolean hasActive(UUID playerId) { return active.containsKey(playerId); }
  public List<String> getLoadedIds() { return new ArrayList<>(scripts.keySet()); }

  // ── Entry points ─────────────────────────────────────────────────────────────

  /** Start a scene for the player. Returns false if it could not begin. */
  public boolean play(ServerPlayer player, String scriptId) {
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

    // Optional body-double (must bake the DOUBLE_TAG in its preset for cleanup).
    boolean hasDouble = spawnDouble(server, level, player, script);

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
      player.getX(), player.getY(), player.getZ(), startYaw, startPitch, restoreMode,
      eyeX, eyeY, eyeZ, startYaw, startPitch);
    state.setRig(rig);
    state.setBase(eyeX, eyeY, eyeZ, script.relative);
    if (hasDouble) state.setDoubleSkinPending(true);
    active.put(player.getUUID(), state);

    // Spectator FIRST, then attach the camera (the vanilla /spectate order; a live swap is
    // hardcore-safe — no guard on changeGameModeForPlayer).
    player.setGameMode(GameType.SPECTATOR);
    player.setCamera(rig);

    applyWeather(player, script.ambientWeather);
    if (script.startTitle != null || script.startSubtitle != null) {
      sendTitle(player, orEmpty(script.startTitle), orEmpty(script.startSubtitle), 10, 70, 20);
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
      if (applyDoubleSkin(server, state, player) || state.getTickCount() > 40) {
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

    // Resolve the keyframe target (absolute, or offset from the captured eye when relative).
    double kx = state.isRelative() ? state.getBaseX() + kf.x : kf.x;
    double ky = state.isRelative() ? state.getBaseY() + kf.y : kf.y;
    double kz = state.isRelative() ? state.getBaseZ() + kf.z : kf.z;

    double cx = Mth.lerp(t, state.getPrevX(), kx);
    double cy = Mth.lerp(t, state.getPrevY(), ky);
    double cz = Mth.lerp(t, state.getPrevZ(), kz);
    float cyaw = Mth.rotLerp(t, state.getPrevYaw(), kf.yaw);
    float cpitch = state.getPrevPitch() + (kf.pitch - state.getPrevPitch()) * t;

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
      state.advanceKeyframe(kx, ky, kz, kf.yaw, kf.pitch);
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
      restoreWeather(player, player.serverLevel());
    }
    ArmorStand rig = state.getRig();
    if (rig != null && !rig.isRemoved()) rig.discard();
    state.setRig(null);
    if (state.hasDouble()) sweepDouble(server, state);
    active.remove(state.getPlayerId());
  }

  // ── Body double (optional) ───────────────────────────────────────────────────

  private boolean spawnDouble(MinecraftServer server, ServerLevel level, ServerPlayer player, CutsceneScript script) {
    if (script.doublePreset == null || script.doublePreset.isBlank()) return false;
    double[] dp = script.doublePos;
    double dx = (dp != null && dp.length == 3) ? dp[0] : player.getX();
    double dy = (dp != null && dp.length == 3) ? dp[1] : player.getY();
    double dz = (dp != null && dp.length == 3) ? dp[2] : player.getZ();
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    if (level != null) src = src.withLevel(level).withPosition(new Vec3(dx, dy, dz));
    runCommand(server, src, String.format(Locale.ROOT,
      "easy_npc preset import_new data %s %.2f %.2f %.2f", script.doublePreset, dx, dy, dz));
    return true;
  }

  /**
   * Patch the discovered double to render the player's skin, via a /data write into the Easy
   * NPC SkinData compound (there is no skin command for player skins). The skin resolves from
   * the stored UUID against Mojang's session server, so on a normal online client the live
   * player's UUID renders "you" with nothing baked in; offline it falls back to the default
   * variant. Returns false (retry next tick) until the tagged body is spawned + discoverable.
   */
  private boolean applyDoubleSkin(MinecraftServer server, CutsceneState state, ServerPlayer player) {
    ServerLevel level = resolveLevel(server, state.getDimension());
    if (level == null) return true; // nothing to patch; stop retrying
    Entity body = null;
    for (Entity e : level.getAllEntities()) {
      if (e.isAlive() && e.getTags().contains(DOUBLE_TAG)) { body = e; break; }
    }
    if (body == null) return false; // not spawned yet — retry
    int[] u = UUIDUtil.uuidToIntArray(player.getUUID());
    String name = player.getGameProfile().getName();
    String cmd = String.format(Locale.ROOT,
      "data modify entity %s SkinData set value {Type:\"PLAYER_SKIN\",Name:\"%s\",URL:\"\",UUID:[I;%d,%d,%d,%d],DisableLayers:0b,Content:\"\",Timestamp:0L}",
      body.getUUID(), name, u[0], u[1], u[2], u[3]);
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput().withLevel(level);
    runCommand(server, src, cmd);
    return true;
  }

  private void sweepDouble(MinecraftServer server, CutsceneState state) {
    ServerLevel level = resolveLevel(server, state.getDimension());
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

package com.thecompanyinc.cobblemoninitiative.noble;

import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * The Ryujin Keep rift: a vanilla Ender Dragon boss gating the Dragon gym leader. A rift
 * tears open above the town, the dragon emerges and circles the keep; end crystals feed it
 * (vanilla entity-local heal, +2 HP/s from the nearest within 32 blocks) so the loop is
 * "destroy the crystals, then bring it down". Killing it grants the {@code dragon_slain}
 * player tag that Leader Ryujin's challenge dialog gates on.
 *
 * <p>Engine facts this build relies on (bytecode-verified): an overworld dragon flies fine
 * with a null EndDragonFight, but its flight anchor ({@code fightOrigin}) defaults to 0,0
 * and is NOT persisted — so it is set here on discovery and re-asserted every tick.
 * mobGriefing=false (already in the pack's gamerules) fully disables its only block-destroy
 * path, and an overworld death spawns no portal/egg. {@code /kill} is a clean teardown.
 *
 * <p>Hardcore guardrails: the fight is tuned via {@code healthMultiplier}, is fully
 * retryable (teardown on logout/death/abort re-arms the rift button — the gate tag is only
 * granted on a kill), and {@code /riftdragon stop} is a no-shame exit.
 */
public class RiftDragonManager {

  private static final Gson GSON = new Gson();
  private static final String CONFIG_PATH = "data/cobblemon_initiative/rift_dragon.json";
  private static final String DRAGON_TAG = "ci_rift_dragon";
  private static final String CRYSTAL_TAG = "ci_rift_crystal";

  /** JSON config — data/cobblemon_initiative/rift_dragon.json. The fight always plays in
   * the CHALLENGER's level (there is no dimension field — a configured dimension the
   * player is not in would split the camera from the fight). */
  public static class Config {
    public double[] riftAnchor = {2267, 343, 963};
    public double[] fightOrigin = {2267, 215, 963};
    public double[][] crystals = {};
    public float healthMultiplier = 0.5f;
    public String gateTag = "dragon_slain";
    public String startTitle = "§5§lA RIFT TEARS OPEN";
    public String startSubtitle = "§7Something old answers the keep";
    public String slainTitle = "§5The rift closes";
    public String slainSubtitle = "§7The keep will hear you now";
  }

  private static class RiftState {
    final UUID playerId;
    UUID dragonUuid;
    boolean tuned;
    /** Set once the dragon enters its death animation — the ONLY path to victory.
     * A dragon that disappears without this (chunk unload, dimension change) is an
     * escape, not a kill; the fight tears down and re-arms instead. */
    boolean dying;
    ServerBossEvent bar;
    int graceTicks; // ticks waited for the summon to be discoverable

    RiftState(UUID playerId) { this.playerId = playerId; }
  }

  private Config config = new Config();
  private final Map<UUID, RiftState> active = new HashMap<>();

  public void loadConfig() {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH)) {
      if (in == null) {
        InitiativeInit.LOGGER.warn("Rift dragon config not found: {} (using defaults)", CONFIG_PATH);
        return;
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        Config cfg = GSON.fromJson(reader, Config.class);
        if (cfg != null) config = cfg;
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load rift dragon config", e);
    }
  }

  public boolean hasActive() { return !active.isEmpty(); }

  /** /riftdragon start (also reached from Leader Ryujin's rift button via gym/rift_start). */
  public boolean start(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return false;
    if (player.getTags().contains(config.gateTag)) {
      player.sendSystemMessage(Component.literal("§7The rift is already closed. The keep remembers."));
      return false;
    }
    if (active.containsKey(player.getUUID())) {
      player.sendSystemMessage(Component.literal("§7The rift already rages. Bring it down — or withdraw with /riftdragon stop."));
      return false;
    }

    ServerLevel level = player.serverLevel();
    RiftState state = new RiftState(player.getUUID());

    // The rift opens high above town; the dragon emerges there and flies to circle the
    // fight origin (set on discovery — the summon NBT cannot carry it).
    double[] rift = config.riftAnchor;
    runServerCommand(server, level, String.format(Locale.ROOT,
      "summon minecraft:ender_dragon %.1f %.1f %.1f {Tags:[\"%s\"]}",
      rift[0], rift[1], rift[2], DRAGON_TAG));
    for (double[] c : config.crystals) {
      if (c == null || c.length != 3) continue;
      runServerCommand(server, level, String.format(Locale.ROOT,
        "summon minecraft:end_crystal %.1f %.1f %.1f {ShowBottom:0b,Tags:[\"%s\"]}",
        c[0], c[1], c[2], CRYSTAL_TAG));
    }

    state.bar = new ServerBossEvent(
      Component.literal("The Rift Dragon"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    state.bar.setProgress(1.0f);
    state.bar.addPlayer(player);
    active.put(player.getUUID(), state);

    sendTitle(player, config.startTitle, config.startSubtitle);
    NobleSkyFx.applyWeather(player, AmbientTheme.THUNDERSTORM);
    runServerCommand(server, level, String.format(Locale.ROOT,
      "playsound minecraft:entity.ender_dragon.growl master @a %.1f %.1f %.1f 3 0.8",
      rift[0], rift[1], rift[2]));
    InitiativeInit.LOGGER.info("Player {} opened the rift dragon fight", player.getName().getString());
    return true;
  }

  /** /riftdragon stop — withdraw; the rift can be reopened (no gate tag granted). */
  public void abort(ServerPlayer player) {
    RiftState state = active.get(player.getUUID());
    if (state == null) {
      player.sendSystemMessage(Component.literal("§7The rift is quiet."));
      return;
    }
    teardown(player.getServer(), state, false);
    player.sendSystemMessage(Component.literal("§7You withdraw. The rift seethes on over the keep."));
  }

  public void tick(MinecraftServer server) {
    if (active.isEmpty()) return;
    for (RiftState state : new ArrayList<>(active.values())) {
      ServerPlayer player = server.getPlayerList().getPlayer(state.playerId);
      if (player == null || player.isDeadOrDying()) { // logout or a hardcore death
        teardown(server, state, false);
        continue;
      }
      ServerLevel level = player.serverLevel();

      // Discover the summoned dragon by tag (the summon command hands back no UUID).
      EnderDragon dragon = resolveDragon(level, state);
      if (dragon == null) {
        if (state.dragonUuid == null) {
          if (++state.graceTicks > 100) { // summon never landed — fail cleanly
            player.sendSystemMessage(Component.literal("§cThe rift failed to open (see log)."));
            InitiativeInit.LOGGER.warn("Rift dragon never spawned/discovered — tearing down.");
            teardown(server, state, false);
          }
          continue;
        }
        if (state.dying) {
          // The death animation finished and the entity was removed — a real kill.
          complete(server, player, state);
        } else {
          // Gone WITHOUT dying: chunk unload / dimension change / fled out of range.
          // Never a victory — tear down (removes the persisted boss) and re-arm.
          player.sendSystemMessage(Component.literal(
            "§7The rift loses sight of you. It seethes on over the keep."));
          teardown(server, state, false);
        }
        continue;
      }

      if (!state.tuned) {
        AttributeInstance maxHealth = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && config.healthMultiplier != 1.0f) {
          maxHealth.setBaseValue(maxHealth.getBaseValue() * config.healthMultiplier);
        }
        dragon.setHealth(dragon.getMaxHealth());
        state.tuned = true;
      }

      // The flight anchor is not persisted and defaults to world 0,0 — re-assert every
      // tick so the dragon always circles the keep, across chunk reloads too.
      double[] o = config.fightOrigin;
      dragon.setFightOrigin(new BlockPos((int) o[0], (int) o[1], (int) o[2]));

      if (dragon.isDeadOrDying()) {
        // Death animation running: latch the ONLY victory path and freeze the bar;
        // complete() fires when the entity is fully removed (the vanilla horn + burst
        // play on their own).
        state.dying = true;
        state.bar.setProgress(0.0f);
        continue;
      }

      int crystals = countCrystals(level);
      state.bar.setProgress(Math.max(0f, Math.min(1f, dragon.getHealth() / dragon.getMaxHealth())));
      state.bar.setName(crystals > 0
        ? Component.literal("The Rift Dragon §5— §d" + crystals + " crystal(s) feed it")
        : Component.literal("The Rift Dragon"));
      state.bar.setColor(crystals > 0 ? BossEvent.BossBarColor.PINK : BossEvent.BossBarColor.PURPLE);
    }
  }

  public void onServerStopping(MinecraftServer server) {
    for (RiftState state : new ArrayList<>(active.values())) teardown(server, state, false);
    active.clear();
  }

  // ── Internals ────────────────────────────────────────────────────────────────

  private void complete(MinecraftServer server, ServerPlayer player, RiftState state) {
    player.addTag(config.gateTag);
    sendTitle(player, config.slainTitle, config.slainSubtitle);
    teardown(server, state, true);
    InitiativeInit.LOGGER.info("Player {} slew the rift dragon ({} granted)",
      player.getName().getString(), config.gateTag);
  }

  private void teardown(MinecraftServer server, RiftState state, boolean victory) {
    // The kills must run on EVERY exit path — including logout (player == null this
    // tick) and server stop — or a persisted 200-HP boss roams the hardcore save
    // forever (checkDespawn is a no-op). An unrestricted @e searches all loaded
    // levels; on a same-tick logout the entities are still loaded.
    if (!victory) {
      runServerCommand(server, null, "kill @e[type=minecraft:ender_dragon,tag=" + DRAGON_TAG + "]");
    }
    runServerCommand(server, null, "kill @e[type=minecraft:end_crystal,tag=" + CRYSTAL_TAG + "]");

    ServerPlayer player = server != null ? server.getPlayerList().getPlayer(state.playerId) : null;
    if (player != null) {
      NobleSkyFx.restore(player, player.serverLevel());
    }
    if (state.bar != null) { state.bar.removeAllPlayers(); state.bar = null; }
    active.remove(state.playerId);
  }

  private EnderDragon resolveDragon(ServerLevel level, RiftState state) {
    if (state.dragonUuid != null) {
      // NO isAlive() filter here: a dying dragon (health 0, death animation running)
      // must keep resolving so tick() can latch state.dying — the only victory proof.
      Entity e = level.getEntity(state.dragonUuid);
      return e instanceof EnderDragon d ? d : null;
    }
    for (Entity e : level.getAllEntities()) {
      if (e instanceof EnderDragon d && e.isAlive() && e.getTags().contains(DRAGON_TAG)) {
        state.dragonUuid = e.getUUID();
        return d;
      }
    }
    return null;
  }

  private static int countCrystals(ServerLevel level) {
    int n = 0;
    for (Entity e : level.getAllEntities()) {
      if (e.isAlive() && e.getTags().contains(CRYSTAL_TAG)) n++;
    }
    return n;
  }

  private static void sendTitle(ServerPlayer player, String title, String subtitle) {
    player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title == null ? "" : title)));
    player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle == null ? "" : subtitle)));
  }

  private static void runServerCommand(MinecraftServer server, ServerLevel level, String cmd) {
    if (server == null) return;
    CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    if (level != null) src = src.withLevel(level);
    server.getCommands().performPrefixedCommand(src, cmd);
  }
}

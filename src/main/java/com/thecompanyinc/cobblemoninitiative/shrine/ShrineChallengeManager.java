package com.thecompanyinc.cobblemoninitiative.shrine;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.ShrineConfig;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;

public class ShrineChallengeManager {

  private static final Gson GSON = new Gson();

  public static final String[] SHRINE_IDS = {
    "dragon",
    "fairy",
    "ice",
    "fire",
    "ground",
  };

  private final Map<UUID, ShrineChallengeState> activeStates = new HashMap<>();
  private final Map<String, ShrineChallengeConfig> challenges = new HashMap<>();

  /** Per-world recorded safe-path positions (writable; ships with the map save). */
  private final ShrinePathStorage pathStorage = new ShrinePathStorage();
  /** shrineId -> baked safe positions from the config JSON (packed BlockPos). */
  private final Map<String, Set<Long>> bakedSafe = new HashMap<>();
  /** Players currently recording a safe path (dev). playerId -> shrineId. */
  private final Map<UUID, String> recordingPlayers = new HashMap<>();
  /** Periodic-flush bookkeeping for active recording (bounds crash-loss). */
  private int recordFlushTicks = 0;
  private boolean recordDirty = false;

  // ── Loading ───────────────────────────────────────────────────────────────────

  public void loadChallenges() {
    // Clear so a live reload doesn't leave stale challenge / baked-path entries.
    // (Does NOT touch activeStates or recordingPlayers — live player state.)
    challenges.clear();
    bakedSafe.clear();
    for (String shrine : SHRINE_IDS) {
      String path =
        "data/cobblemon_initiative/shrine_challenges/" + shrine + ".json";
      try (
        InputStream in = getClass().getClassLoader().getResourceAsStream(path)
      ) {
        if (in == null) {
          InitiativeInit.LOGGER.warn(
            "Shrine challenge config not found: {}",
            path
          );
          continue;
        }
        try (
          Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)
        ) {
          ShrineChallengeConfig config = GSON.fromJson(
            reader,
            ShrineChallengeConfig.class
          );
          challenges.put(shrine, config);

          // Pre-pack any baked safe-path positions for O(1) tick lookups.
          int[][] baked = config.getSafePositions();
          if (baked != null && baked.length > 0) {
            Set<Long> set = new HashSet<>();
            for (int[] xyz : baked) {
              if (xyz.length == 3) {
                set.add(BlockPos.asLong(xyz[0], xyz[1], xyz[2]));
              }
            }
            bakedSafe.put(shrine, set);
          }

          InitiativeInit.LOGGER.info(
            "Loaded shrine challenge: {} ({})",
            shrine,
            config.getDisplayName()
          );
        }
      } catch (IOException e) {
        InitiativeInit.LOGGER.error(
          "Failed to load shrine challenge: {}",
          shrine,
          e
        );
      }
    }
  }

  /** Load recorded safe-path positions from the world directory (SERVER_STARTED). */
  public void loadPaths(MinecraftServer server) {
    pathStorage.load(server);
  }

  /** Persist recorded safe-path positions to the world directory (SERVER_STOPPING). */
  public void savePaths() {
    pathStorage.save();
  }

  // ── Command entry points ─────────────────────────────────────────────────────

  /** Called by /cobblemon-initiative shrine <id> start */
  public boolean startChallenge(ServerPlayer player, String shrineId) {
    ShrineChallengeConfig config = challenges.get(shrineId);
    if (config == null) {
      player.sendSystemMessage(
        Component.literal("§cUnknown shrine challenge: " + shrineId)
      );
      return false;
    }

    // Clear any pre-existing challenge (no penalty — they can restart freely)
    if (activeStates.containsKey(player.getUUID())) {
      clearChallengeEffects(player);
    }

    ShrineChallengeState state = new ShrineChallengeState(
      player.getUUID(),
      shrineId
    );
    activeStates.put(player.getUUID(), state);

    // Ice floor: capture the point we yank the player back to on a misstep.
    // Use the pinned start if the config provides one, else where they began.
    if (config.isIceFloorEnabled()) {
      ShrineChallengeConfig.Vec s = config.getStart();
      if (s != null) {
        state.setResetPoint(s.x, s.y, s.z, s.yaw, s.pitch);
      } else {
        state.setResetPoint(
          player.getX(),
          player.getY(),
          player.getZ(),
          player.getYRot(),
          player.getXRot()
        );
      }
    }

    // Title splash
    sendTitle(
      player,
      config.getStartTitle(),
      config.getStartSubtitle(),
      10,
      80,
      20
    );

    // Type-specific start behaviour
    switch (config.getType()) {
      case "hydra_gauntlet" -> {
        player.sendSystemMessage(
          Component.literal(
            "§5§l[Hydra Gauntlet] §r§7Stage 1 — Face the first head!"
          )
        );
      }
      case "fairy_tests" -> {
        player.sendSystemMessage(
          Component.literal(
            "§d§l[Five Tests] §r§7Test 1: §dBond of Friendship"
          )
        );
        player.sendSystemMessage(
          Component.literal(
            "§7Bring your first Pokémon to the shrine altar and use /cobblemon-initiative shrine fairy test"
          )
        );
      }
      case "timed_parkour" -> {
        player.sendSystemMessage(
          Component.literal(
            "§e§l[" +
              config.getDisplayName() +
              "] §r§7Timer started — §e" +
              config.getTimeLimitSeconds() +
              " seconds. §7Reach the finish!"
          )
        );
        if (iceFloorActive(config)) {
          player.sendSystemMessage(
            Component.literal(
              "§b❄ §7Stay on the safe path — touching the wrong ice will hurt you and send you back to the start!"
            )
          );
        }
      }
      case "dark_gauntlet" -> {
        // Reduced starting health (fraction configurable in ModMenu)
        float maxHp = player.getMaxHealth();
        player.setHealth(maxHp * ShrineConfig.get().getDarkGauntletStartHealthFraction());
        // Immediate blindness
        applyBlindness(player);
        player.sendSystemMessage(
          Component.literal(
            "§8§l[The Buried Maze] §r§8Sight dims. Strength wavers. Find the High Priest."
          )
        );
      }
    }

    InitiativeInit.LOGGER.info(
      "Player {} started {} shrine challenge",
      player.getName().getString(),
      shrineId
    );
    return true;
  }

  /**
   * Abort an active challenge. No penalty — challenge state and all effects are
   * cleared. The player can restart whenever they like.
   * Called by /shrine-abort (no permission required) or the shrine's abort NPC.
   */
  public void stopChallenge(ServerPlayer player) {
    if (!activeStates.containsKey(player.getUUID())) {
      player.sendSystemMessage(
        Component.literal("§7You have no active shrine challenge.")
      );
      return;
    }

    clearChallengeEffects(player);
    activeStates.remove(player.getUUID());
    player.sendSystemMessage(
      Component.literal(
        "§7Shrine challenge ended. Return to try again whenever you're ready."
      )
    );
    InitiativeInit.LOGGER.info(
      "Player {} aborted shrine challenge",
      player.getName().getString()
    );
  }

  /**
   * Run the current Fairy shrine test on the player's lead Pokémon.
   * Called by /cobblemon-initiative shrine fairy test
   */
  /**
   * testName: "friendship" | "fullness" | "nickname" | "shiny" | "resolve"
   *
   * The first four are independent stat-check commands — feedback only, no gates.
   * "resolve" checks ALL four conditions plus solo party, then registers the
   * Pokémon and sends the player to battle the cult leader.
   * Called by /cobblemon-initiative shrine fairy test <testName>
   */
  public boolean runFairyTest(ServerPlayer player, String testName) {
    ShrineChallengeState state = activeStates.get(player.getUUID());

    if (state == null || !"fairy".equals(state.getShrineId())) {
      player.sendSystemMessage(
        Component.literal("§cYou don't have an active Fairy shrine challenge.")
      );
      return false;
    }

    if (state.getFairyTestPokemonUuid() != null) {
      player.sendSystemMessage(Component.literal(
        "§cThe Trial of Resolve is already underway — defeat the High Priestess!"
      ));
      return false;
    }

    ShrineChallengeConfig config = challenges.get("fairy");
    if (config == null) return false;

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon lead = party.get(0);
    if (lead == null) {
      player.sendSystemMessage(Component.literal("§cYour first party slot is empty!"));
      return false;
    }

    String leadName = lead.getDisplayName(true).getString();

    return switch (testName) {
      case "friendship" -> {
        int friendship = lead.getFriendship();
        boolean passed = friendship >= config.getFriendshipThreshold();
        player.sendSystemMessage(Component.literal("§d§lTest — Bond of Friendship"));
        player.sendSystemMessage(Component.literal(passed
          ? "§a✔ " + leadName + "'s friendship: " + friendship + "/" + config.getFriendshipThreshold() + " — Your bond shines true!"
          : "§c✘ " + leadName + "'s friendship: " + friendship + "/" + config.getFriendshipThreshold() + " — Grow closer before returning."
        ));
        yield passed;
      }
      case "fullness" -> {
        int fullness = lead.getCurrentFullness();
        boolean passed = fullness >= config.getFullnessThreshold();
        player.sendSystemMessage(Component.literal("§d§lTest — Nourishment"));
        player.sendSystemMessage(Component.literal(passed
          ? "§a✔ " + leadName + "'s fullness: " + fullness + "/" + config.getFullnessThreshold() + " — Well nourished and ready!"
          : "§c✘ " + leadName + "'s fullness: " + fullness + "/" + config.getFullnessThreshold() + " — Feed your Pokémon first."
        ));
        yield passed;
      }
      case "nickname" -> {
        var nickname = lead.getNickname();
        boolean passed = nickname != null && !nickname.getString().isBlank();
        player.sendSystemMessage(Component.literal("§d§lTest — Name of the Heart"));
        player.sendSystemMessage(Component.literal(passed
          ? "§a✔ \"" + nickname.getString() + "\" — You know each other by name!"
          : "§c✘ " + leadName + " has no nickname — Give them a name that comes from the heart."
        ));
        yield passed;
      }
      case "shiny" -> {
        boolean passed = lead.getShiny();
        player.sendSystemMessage(Component.literal("§d§lTest — Radiance"));
        player.sendSystemMessage(Component.literal(passed
          ? "§a✔ Shiny! — A rare light that cannot be faked!"
          : "§c✘ " + leadName + " does not shine — The shrine demands radiance."
        ));
        yield passed;
      }
      case "resolve" -> {
        int friendship = lead.getFriendship();
        int fullness = lead.getCurrentFullness();
        var nickname = lead.getNickname();
        boolean hasNickname = nickname != null && !nickname.getString().isBlank();
        boolean isShiny = lead.getShiny();

        int partySize = 0;
        for (int i = 0; i < 6; i++) {
          if (party.get(i) != null) partySize++;
        }

        player.sendSystemMessage(Component.literal("§d§l— Trial of Resolve —"));

        boolean allPass = true;
        if (friendship < config.getFriendshipThreshold()) {
          player.sendSystemMessage(Component.literal("§c✘ Friendship: " + friendship + "/" + config.getFriendshipThreshold()));
          allPass = false;
        } else {
          player.sendSystemMessage(Component.literal("§a✔ Friendship: " + friendship + "/" + config.getFriendshipThreshold()));
        }
        if (fullness < config.getFullnessThreshold()) {
          player.sendSystemMessage(Component.literal("§c✘ Fullness: " + fullness + "/" + config.getFullnessThreshold()));
          allPass = false;
        } else {
          player.sendSystemMessage(Component.literal("§a✔ Fullness: " + fullness + "/" + config.getFullnessThreshold()));
        }
        if (!hasNickname) {
          player.sendSystemMessage(Component.literal("§c✘ No nickname"));
          allPass = false;
        } else {
          player.sendSystemMessage(Component.literal("§a✔ Nickname: \"" + nickname.getString() + "\""));
        }
        if (!isShiny) {
          player.sendSystemMessage(Component.literal("§c✘ Not shiny"));
          allPass = false;
        } else {
          player.sendSystemMessage(Component.literal("§a✔ Shiny"));
        }
        if (partySize != 1) {
          player.sendSystemMessage(Component.literal("§c✘ " + leadName + " must be your only Pokémon (" + partySize + " in party)"));
          allPass = false;
        } else {
          player.sendSystemMessage(Component.literal("§a✔ " + leadName + " stands alone"));
        }

        if (!allPass) {
          player.sendSystemMessage(Component.literal("§7Resolve the issues above and return when " + leadName + " is ready."));
          yield false;
        }

        state.setFairyTestPokemonUuid(lead.getUuid());
        player.sendSystemMessage(Component.literal(
          "§d" + leadName + " §7is worthy. Seek out the §dHigh Priestess§7 and defeat her — " +
          "this is your only Pokémon, and your bond is all you have."
        ));
        yield true;
      }
      default -> {
        player.sendSystemMessage(Component.literal(
          "§cUnknown test: " + testName + ". Use: friendship, fullness, nickname, shiny, resolve"
        ));
        yield false;
      }
    };
  }

  /**
   * Called when a player reaches the parkour finish line.
   * Triggered by a command block / pressure plate:
   *   execute as @a[distance=..3] run cobblemon-initiative shrine <id> complete
   */
  public boolean completeParkour(ServerPlayer player, String shrineId) {
    ShrineChallengeState state = activeStates.get(player.getUUID());

    if (state == null || !shrineId.equals(state.getShrineId())) {
      return false;
    }

    ShrineChallengeConfig config = challenges.get(shrineId);
    if (config == null || !"timed_parkour".equals(config.getType())) {
      return false;
    }

    long elapsedSec =
      (System.currentTimeMillis() - state.getStartTimeMs()) / 1000L;
    long remaining = config.getTimeLimitSeconds() - elapsedSec;

    if (remaining < 0) {
      player.sendSystemMessage(
        Component.literal(
          "§c§lTime expired! §r§7The challenge has been reset."
        )
      );
      stopChallenge(player);
      return false;
    }

    long minutes = remaining / 60;
    long seconds = remaining % 60;
    String timeStr = minutes > 0
      ? minutes + "m " + seconds + "s"
      : seconds + "s";

    player.sendSystemMessage(
      Component.literal(
        "§a§l[" +
          config.getDisplayName() +
          " Complete!] §r§7Finished with §a" +
          timeStr +
          " §7to spare."
      )
    );
    completeChallenge(player, state);
    return true;
  }

  // ── Called by PlayerProgressManager ──────────────────────────────────────────

  /**
   * Notified whenever the player defeats a trainer. If the trainer matches the
   * current challenge stage, advance (and heal if needed).
   */
  public void onTrainerDefeated(ServerPlayer player, String trainerId) {
    ShrineChallengeState state = activeStates.get(player.getUUID());
    if (state == null) return;

    ShrineChallengeConfig config = challenges.get(state.getShrineId());
    if (config == null) return;

    switch (config.getType()) {
      case "hydra_gauntlet" -> {
        List<String> stages = config.getStageTrainerIds();
        int current = state.getCurrentStageIndex();
        if (current < stages.size() && stages.get(current).equals(trainerId)) {
          int next = current + 1;
          state.setCurrentStageIndex(next);

          if (next >= stages.size()) {
            completeChallenge(player, state);
          } else {
            healParty(player);
            player.sendSystemMessage(
              Component.literal(
                "§5§l[Hydra Gauntlet] §r§7Stage " +
                  (current + 1) +
                  " defeated! §aYour party is healed. §7Advance to Stage " +
                  (next + 1) +
                  "!"
              )
            );
          }
        }
      }
      case "dark_gauntlet" -> {
        if (trainerId.equals(config.getTargetTrainerId())) {
          completeChallenge(player, state);
        }
      }
      case "fairy_tests" -> {
        // Complete the challenge when the cult leader is defeated with the
        // registered Pokémon still in party slot 0.
        UUID registeredUuid = state.getFairyTestPokemonUuid();
        if (registeredUuid == null) return; // resolve not yet run
        if (!trainerId.equals(config.getCultistLeaderTrainerId())) return;

        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon lead = party.get(0);

        if (lead == null || !lead.getUuid().equals(registeredUuid)) {
          player.sendSystemMessage(Component.literal(
            "§c✘ Trial of Resolve failed — your registered Pokémon was not leading " +
            "the party when the battle ended."
          ));
          player.sendSystemMessage(Component.literal(
            "§7Run §d/cobblemon-initiative shrine fairy test resolve §7again to retry."
          ));
          state.setFairyTestPokemonUuid(null);
          return;
        }

        completeChallenge(player, state);
      }
      // timed_parkour doesn't use trainer defeat for advancement
    }
  }

  // ── Server tick ───────────────────────────────────────────────────────────────

  /** Called once per server tick from InitiativeInit. */
  public void tick(MinecraftServer server) {
    List<UUID> expired = new ArrayList<>();

    for (Map.Entry<UUID, ShrineChallengeState> entry : activeStates.entrySet()) {
      UUID uuid = entry.getKey();
      ShrineChallengeState state = entry.getValue();

      ServerPlayer player = server.getPlayerList().getPlayer(uuid);
      if (player == null) {
        // Player logged off — clear state silently
        expired.add(uuid);
        continue;
      }

      ShrineChallengeConfig config = challenges.get(state.getShrineId());
      if (config == null) {
        expired.add(uuid);
        continue;
      }

      switch (config.getType()) {
        case "timed_parkour" -> tickParkour(player, state, config, expired);
        case "dark_gauntlet" -> tickDarkGauntlet(
          player,
          state,
          config,
          server
        );
      }
    }

    expired.forEach(activeStates::remove);

    tickRecording(server);
  }

  /** Captures every block a recording dev walks over as a safe-path position. */
  private void tickRecording(MinecraftServer server) {
    if (recordingPlayers.isEmpty()) return;

    Iterator<Map.Entry<UUID, String>> it = recordingPlayers
      .entrySet()
      .iterator();
    while (it.hasNext()) {
      Map.Entry<UUID, String> entry = it.next();
      ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
      if (player == null) {
        it.remove();
        continue;
      }
      BlockPos foot = footBlock(player);
      if (pathStorage.addDeferred(entry.getValue(), foot)) {
        recordDirty = true;
        // New block captured — flash a marker so the dev sees the trail grow.
        player.serverLevel().sendParticles(
          ParticleTypes.HAPPY_VILLAGER,
          foot.getX() + 0.5,
          foot.getY() + 1.05,
          foot.getZ() + 0.5,
          4,
          0.3,
          0.1,
          0.3,
          0.0
        );
      }
    }

    // Flush at most every 5s while recording, so a crash loses only seconds of
    // route rather than rewriting the whole file on every footstep.
    if (recordDirty && ++recordFlushTicks >= 100) {
      pathStorage.save();
      recordFlushTicks = 0;
      recordDirty = false;
    }
  }

  private void tickParkour(
    ServerPlayer player,
    ShrineChallengeState state,
    ShrineChallengeConfig config,
    List<UUID> expired
  ) {
    long elapsedSec =
      (System.currentTimeMillis() - state.getStartTimeMs()) / 1000L;
    long remaining = config.getTimeLimitSeconds() - elapsedSec;

    if (remaining <= 0) {
      player.sendSystemMessage(
        Component.literal(
          "§c§l" +
            config.getDisplayName() +
            " — Time's up! §r§7The challenge has reset. Try again."
        )
      );
      clearChallengeEffects(player);
      expired.add(player.getUUID());
      return;
    }

    // Countdown warnings — fire exactly once per threshold second
    if (
      (remaining == 60 || remaining == 30 || remaining == 10 ||
       remaining == 5 || remaining == 3 || remaining == 2 || remaining == 1)
      && state.getLastAnnouncedSecond() != remaining
    ) {
      state.setLastAnnouncedSecond(remaining);
      String colour = remaining <= 10 ? "§c" : "§e";
      player.sendSystemMessage(
        Component.literal(colour + remaining + " seconds remaining!")
      );
    }

    // Optional floor hazard layered on top of the timer (e.g. the ice trial).
    if (iceFloorActive(config)) {
      tickIceFloor(player, state, config);
    }
  }

  /**
   * The floor hazard is live only when the shrine is structurally a floor trial
   * (per-shrine JSON) AND the global master toggle is on (ModMenu / ShrineConfig).
   */
  private boolean iceFloorActive(ShrineChallengeConfig config) {
    return config.isIceFloorEnabled() && ShrineConfig.get().isIceFloorEnabled();
  }

  /**
   * Floor hazard: if the player is standing on a hazard block that was NOT
   * recorded as part of the safe path, deal damage and yank them to the start.
   */
  private void tickIceFloor(
    ServerPlayer player,
    ShrineChallengeState state,
    ShrineChallengeConfig config
  ) {
    if (state.getIceHitCooldown() > 0) {
      state.decrementIceHitCooldown();
      return;
    }

    BlockPos foot = footBlock(player);

    // The start/reset tile is always implicitly safe. Otherwise, if the captured
    // start happens to sit on hazard ice, the player would be re-punished every
    // cooldown cycle after each teleport — a death loop (run-ending in hardcore).
    BlockPos resetFoot = BlockPos.containing(
      state.getResetX(),
      state.getResetY() - 0.05,
      state.getResetZ()
    );
    if (foot.equals(resetFoot)) return;

    ServerLevel level = player.serverLevel();
    BlockState below = level.getBlockState(foot);
    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(below.getBlock());

    // Not a hazard block → safe to stand here.
    if (!config.getIceHazardBlocks().contains(blockId.toString())) return;

    // A recorded (or baked) safe block → no penalty.
    if (isSafe(state.getShrineId(), foot)) return;

    punishIce(player, state, config);
  }

  private void punishIce(
    ServerPlayer player,
    ShrineChallengeState state,
    ShrineChallengeConfig config
  ) {
    ShrineConfig tuning = ShrineConfig.get();
    player.hurt(player.damageSources().freeze(), tuning.getIceFloorDamage());

    // Halt momentum so the player doesn't keep sliding/falling after the yank.
    player.setDeltaMovement(0, 0, 0);
    player.resetFallDistance();
    player.connection.teleport(
      state.getResetX(),
      state.getResetY(),
      state.getResetZ(),
      state.getResetYaw(),
      state.getResetPitch()
    );

    player.serverLevel().playSound(
      null,
      player.blockPosition(),
      SoundEvents.GLASS_BREAK,
      SoundSource.BLOCKS,
      tuning.getIceCrackSoundVolume(),
      tuning.getIceCrackSoundPitch()
    );
    player.sendSystemMessage(
      Component.literal("§b❄ §7The ice cracks beneath you — back to the start!")
    );

    state.setIceHitCooldown(tuning.getIceFloorHitCooldownTicks());
  }

  private void tickDarkGauntlet(
    ServerPlayer player,
    ShrineChallengeState state,
    ShrineChallengeConfig config,
    MinecraftServer server
  ) {
    // Re-apply blindness every 5 seconds (100 ticks) so it never fades
    state.incrementBlindnessTicks();
    if (state.getBlindnessRefreshTicks() >= ShrineConfig.get().getDarkGauntletBlindnessRefreshTicks()) {
      applyBlindness(player);
      state.setBlindnessRefreshTicks(0);
    }

    // Earthquake timer
    state.incrementEarthquakeTicks();
    int intervalTicks = config.getEarthquakeIntervalSeconds() * 20;
    if (state.getEarthquakeTickCounter() >= intervalTicks) {
      triggerEarthquake(player, config.getEarthquakeRadius());
      state.setEarthquakeTickCounter(0);
    }
  }

  // ── Private helpers ───────────────────────────────────────────────────────────

  private void completeChallenge(
    ServerPlayer player,
    ShrineChallengeState state
  ) {
    String shrineId = state.getShrineId();
    ShrineChallengeConfig config = challenges.get(shrineId);
    String displayName = config != null ? config.getDisplayName() : shrineId;

    clearChallengeEffects(player);
    activeStates.remove(player.getUUID());

    // Persist a completion tag so the ladder + leader can gate on the trial. Previously this
    // method only showed a title and latched NOTHING — the whole trial→keeper chain was dead
    // (nothing downstream could read that the trial was cleared). Easy NPC dialog gates read
    // player tags, so this is the enforcement hook: the shrine unit gates the leader's battle
    // button on `<type>_shrine_trial_clear` (e.g. ice_shrine_trial_clear). Entity tags persist
    // in player NBT, so the clear is relog-safe.
    player.addTag(shrineId + "_shrine_trial_clear");

    sendTitle(player, "§6§lChallenge Complete!", "§7" + displayName, 10, 80, 30);
    player.sendSystemMessage(
      Component.literal(
        "§6§l[Shrine Challenge] §r§e" + displayName + " §6completed!"
      )
    );

    InitiativeInit.LOGGER.info(
      "Player {} completed {} shrine challenge",
      player.getName().getString(),
      shrineId
    );
  }

  /** Removes any lingering effects applied by the challenge. */
  private void clearChallengeEffects(ServerPlayer player) {
    UUID uuid = player.getUUID();
    ShrineChallengeState state = activeStates.get(uuid);
    if (state == null) return;

    ShrineChallengeConfig config = challenges.get(state.getShrineId());
    if (config == null) return;

    if ("dark_gauntlet".equals(config.getType())) {
      player.removeEffect(MobEffects.BLINDNESS);
      player.removeEffect(MobEffects.CONFUSION);
    }
  }

  private void sendTitle(
    ServerPlayer player,
    String title,
    String subtitle,
    int fadeIn,
    int stay,
    int fadeOut
  ) {
    player.connection.send(
      new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut)
    );
    player.connection.send(
      new ClientboundSetTitleTextPacket(Component.literal(title))
    );
    player.connection.send(
      new ClientboundSetSubtitleTextPacket(Component.literal(subtitle))
    );
  }

  /** Heals every Pokémon in the player's party fully. */
  private void healParty(ServerPlayer player) {
    try {
      PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
      for (int i = 0; i < party.size(); i++) {
        Pokemon pokemon = party.get(i);
        if (pokemon != null) {
          pokemon.heal();
        }
      }
      player.sendSystemMessage(
        Component.literal("§a✚ Your party has been fully healed!")
      );
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to heal party for shrine challenge", e);
    }
  }

  private void applyBlindness(ServerPlayer player) {
    // Duration configurable; refreshed on an interval so it never fades.
    player.addEffect(
      new MobEffectInstance(
        MobEffects.BLINDNESS,
        ShrineConfig.get().getDarkGauntletBlindnessDurationTicks(),
        0,
        false,
        false
      )
    );
  }

  private void triggerEarthquake(ServerPlayer player, int radius) {
    ShrineConfig cfg = ShrineConfig.get();

    // Sound
    player.serverLevel().playSound(
      null,
      player.blockPosition(),
      SoundEvents.GENERIC_EXPLODE.value(),
      SoundSource.BLOCKS,
      cfg.getEarthquakeSoundVolume(),
      cfg.getEarthquakeSoundPitch()
    );

    // Brief nausea to sell the disorientation (0 disables)
    int nauseaTicks = cfg.getEarthquakeNauseaTicks();
    if (nauseaTicks > 0) {
      player.addEffect(
        new MobEffectInstance(MobEffects.CONFUSION, nauseaTicks, 0, false, false)
      );
    }

    // Random displacement
    Random rng = new Random();
    double dx = (rng.nextDouble() - 0.5) * 2.0 * radius;
    double dz = (rng.nextDouble() - 0.5) * 2.0 * radius;
    float newYaw = rng.nextFloat() * 360f - 180f;

    // Teleport (connection.teleport preserves relative flags more cleanly)
    player.connection.teleport(
      player.getX() + dx,
      player.getY(),
      player.getZ() + dz,
      newYaw,
      player.getXRot()
    );

    player.sendSystemMessage(
      Component.literal("§6§lThe earth trembles — you are thrown!")
    );
  }

  // ── Safe-path authoring (dev) ─────────────────────────────────────────────────

  /**
   * Toggle continuous safe-path recording for a shrine. While active, every
   * block the player walks over is saved as a safe position.
   * @return true if recording is now ON, false if it was just turned OFF.
   */
  public boolean toggleRecording(ServerPlayer player, String shrineId) {
    UUID id = player.getUUID();
    if (shrineId.equals(recordingPlayers.get(id))) {
      recordingPlayers.remove(id);
      // Persist immediately on stop (the recording loop only flushes periodically).
      pathStorage.save();
      recordDirty = false;
      player.sendSystemMessage(
        Component.literal(
          "§e[Path] §7Recording stopped for §e" + shrineId + "§7 — " +
            pathStorage.count(shrineId) + " safe blocks saved."
        )
      );
      return false;
    }
    recordingPlayers.put(id, shrineId);
    player.sendSystemMessage(
      Component.literal(
        "§a[Path] §7Recording the §a" + shrineId + "§7 safe path — walk the route. " +
          "Every block you step on is saved. Run the command again to stop."
      )
    );
    return true;
  }

  /** Add the single block under the player's feet to the safe path. */
  public void recordHere(ServerPlayer player, String shrineId) {
    BlockPos foot = footBlock(player);
    boolean added = pathStorage.add(shrineId, foot);
    player.sendSystemMessage(
      Component.literal(
        added
          ? "§a[Path] §7Added safe block §a" + foot.getX() + " " + foot.getY() +
            " " + foot.getZ() + " §7(" + pathStorage.count(shrineId) + " total)."
          : "§7[Path] That block is already part of the " + shrineId + " safe path."
      )
    );
  }

  /** Wipe all recorded safe positions for a shrine and stop recording. */
  public void clearPath(ServerPlayer player, String shrineId) {
    recordingPlayers.remove(player.getUUID());
    int removed = pathStorage.clear(shrineId);
    player.sendSystemMessage(
      Component.literal(
        "§c[Path] §7Cleared §c" + removed + "§7 safe blocks for §e" + shrineId + "§7."
      )
    );
  }

  /** Particle-highlight nearby safe blocks (recorded + baked) for the player. */
  public void showPath(ServerPlayer player, String shrineId) {
    Set<Long> all = new HashSet<>(pathStorage.get(shrineId));
    Set<Long> baked = bakedSafe.get(shrineId);
    if (baked != null) all.addAll(baked);

    ServerLevel level = player.serverLevel();
    BlockPos origin = player.blockPosition();
    int shown = 0;
    for (long packed : all) {
      BlockPos pos = BlockPos.of(packed);
      if (pos.distSqr(origin) > 64 * 64) continue; // nearby only
      level.sendParticles(
        ParticleTypes.END_ROD,
        pos.getX() + 0.5,
        pos.getY() + 1.05,
        pos.getZ() + 0.5,
        6,
        0.2,
        0.05,
        0.2,
        0.0
      );
      shown++;
    }
    player.sendSystemMessage(
      Component.literal(
        "§b[Path] §7Highlighted §b" + shown + "§7 nearby safe blocks (of " +
          all.size() + " total)."
      )
    );
  }

  /** Print the recorded safe path as a {@code "safePositions"} JSON snippet. */
  public void exportPath(ServerPlayer player, String shrineId) {
    // Union recorded (world) positions with any already baked into the config,
    // so re-exporting after a partial commit never drops earlier positions.
    Set<Long> union = new HashSet<>(pathStorage.get(shrineId));
    Set<Long> baked = bakedSafe.get(shrineId);
    if (baked != null) union.addAll(baked);

    String json = ShrinePathStorage.toJsonString(union);
    int count = union.size();
    player.sendSystemMessage(
      Component.literal(
        "§6[Path] §7Exported §6" + count + "§7 safe blocks for §e" + shrineId + "§7."
      )
    );
    player.sendSystemMessage(
      Component.literal(
        "§7Paste into §f" + shrineId + ".json§7 as §f\"safePositions\"§7 " +
          "(also written to the server log):"
      )
    );
    player.sendSystemMessage(Component.literal("§8\"safePositions\": " + json));
    InitiativeInit.LOGGER.info(
      "[Shrine Path] Export for {} -> \"safePositions\": {}",
      shrineId,
      json
    );
  }

  /** The block directly supporting the player's feet (bottom of the bounding box). */
  private static BlockPos footBlock(ServerPlayer player) {
    return BlockPos.containing(
      player.getX(),
      player.getBoundingBox().minY - 0.05,
      player.getZ()
    );
  }

  /** True if the position is a safe block (recorded in the world or baked in config). */
  private boolean isSafe(String shrineId, BlockPos pos) {
    if (pathStorage.isSafe(shrineId, pos)) return true;
    Set<Long> baked = bakedSafe.get(shrineId);
    return baked != null && baked.contains(pos.asLong());
  }

  // ── Query helpers ─────────────────────────────────────────────────────────────

  public boolean hasActiveChallenge(UUID playerId) {
    return activeStates.containsKey(playerId);
  }

  public ShrineChallengeState getState(UUID playerId) {
    return activeStates.get(playerId);
  }

  public ShrineChallengeConfig getChallenge(String shrineId) {
    return challenges.get(shrineId);
  }

  public String[] getShrineIds() {
    return SHRINE_IDS;
  }
}

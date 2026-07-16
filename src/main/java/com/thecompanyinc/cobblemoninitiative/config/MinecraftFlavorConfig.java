package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Make it feel Minecraft" datapack-facing toggles (docs/MINECRAFT_FLAVOR.md §8 +
 * docs/PHONE_AND_CARE.md §4). Unlike {@link HomesteadConfig} / MomCareConfig — which hold
 * numbers Java reads directly — these are booleans that gate DATAPACK behaviour, so they are
 * mirrored into a {@code ci_flavor} scoreboard the same way NpcSight/Dex/Safari publish state
 * (scoreboard-as-IPC). {@code milestone_loot} and {@code daycare_independent} are read in Java
 * so they carry no scoreboard holder.
 *
 * File style follows {@link com.thecompanyinc.cobblemoninitiative.daycare.DaycareConfig}: a flat
 * JSON at config/cobblemon-initiative-flavor.json, written with defaults on first run so the
 * showrunner can tune without a rebuild.
 */
public class MinecraftFlavorConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-flavor.json");

  public static final String OBJECTIVE = "ci_flavor";

  // ── PokéPhone (PHONE_AND_CARE §4) ──────────────────────────────────────────────
  /** Master switch for the PokéPhone remote-call system. */
  private boolean phoneEnabled = true;
  /** true = the ring auto-opens the call dialog; false = it persists (answer-by-sneak, v2). */
  private boolean phoneAutoOpen = true;

  // ── Per-gym Minecraft requirements (MINECRAFT_FLAVOR §6) ─────────────────────────
  /**
   * Minecraft achievements are REQUIRED to challenge each gym (default true). When on, the per-gym
   * mirror advancements fire (set {@code mc_gym<n>_done} + flavor) AND each leader's challenge is
   * gated on that tag (a "prove yourself in the world first" locked dialog entry). When off, Java
   * marks every player {@code cfg_gym_gate_off} so the locked entries never show — the gyms open
   * and the flavor tasks stop firing. Gym 8 (Ryujin) is always gated on the Ender Dragon regardless.
   */
  private boolean gymMcAchievementsRequired = true;

  /** Per-player tag Java sets when {@link #gymMcAchievementsRequired} is OFF (opens all gym gates). */
  public static final String GATE_OFF_TAG = "cfg_gym_gate_off";

  // ── Java-side toggles (no scoreboard holder — read directly) ─────────────────────
  /** Iconic Minecraft loot at story beats: diamond/badge, beacon/Champion, netherite/Board. */
  private boolean milestoneLootEnabled = true;
  /**
   * The daycare keeper's "we are not Company" independence line. NOTE: the line is authored
   * into the compiled keeper dialog, so flipping this false hides its RUNTIME resale/care cues
   * but does not retract the shipped say-line (that needs a content recompile) — kept here for
   * completeness + parity with the other knobs.
   */
  private boolean daycareIndependentFlavor = true;

  public MinecraftFlavorConfig() {}

  public static MinecraftFlavorConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          MinecraftFlavorConfig config = GSON.fromJson(reader, MinecraftFlavorConfig.class);
          if (config != null) {
            return config;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading flavor config, using defaults: {}", e.getMessage());
    }

    MinecraftFlavorConfig config = new MinecraftFlavorConfig();
    config.save();
    return config;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      LOGGER.error("Error saving flavor config: {}", e.getMessage());
    }
  }

  /**
   * Publish the boolean toggles into the {@code ci_flavor} scoreboard so datapack functions can
   * gate on them. Called on SERVER_STARTED and whenever the ModMenu screen applies a change.
   * Objective is created here (Java) so gates work on any world without a datapack load edit.
   */
  public void pushToScoreboard(MinecraftServer server) {
    if (server == null) return;
    Scoreboard sb = server.getScoreboard();
    Objective obj = sb.getObjective(OBJECTIVE);
    if (obj == null) {
      obj = sb.addObjective(
        OBJECTIVE,
        ObjectiveCriteria.DUMMY,
        Component.literal("Minecraft Flavor"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null);
    }
    set(sb, obj, "#phone", phoneEnabled);
    set(sb, obj, "#phone_auto_open", phoneAutoOpen);
    // The per-gym mirror advancements gate on #gym_mc_req — they fire (and can be earned) only
    // when the requirement is on. When off, the tags are irrelevant (the gate is opened by tag).
    set(sb, obj, "#gym_mc_req", gymMcAchievementsRequired);
  }

  private static void set(Scoreboard sb, Objective obj, String holder, boolean on) {
    sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(holder), obj).set(on ? 1 : 0);
  }

  /**
   * Apply the gym-gate control tag to every online player: set {@link #GATE_OFF_TAG} when the
   * requirement is OFF (opening every leader's challenge), clear it when ON (the mc_gym tags do
   * the gating). Called on SERVER_STARTED, on player join, and on a ModMenu apply.
   */
  public void applyGymGateTags(MinecraftServer server) {
    if (server == null) return;
    for (net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
      applyGymGateTag(p);
    }
  }

  /** Single-player variant (the join handler). */
  public void applyGymGateTag(net.minecraft.server.level.ServerPlayer player) {
    if (player == null) return;
    if (gymMcAchievementsRequired) {
      player.removeTag(GATE_OFF_TAG);
    } else {
      player.addTag(GATE_OFF_TAG);
    }
  }

  // ── Getters ──────────────────────────────────────────────────────────────────
  public boolean isPhoneEnabled() { return phoneEnabled; }
  public boolean isPhoneAutoOpen() { return phoneAutoOpen; }
  public boolean isGymMcAchievementsRequired() { return gymMcAchievementsRequired; }
  public boolean isMilestoneLootEnabled() { return milestoneLootEnabled; }
  public boolean isDaycareIndependentFlavor() { return daycareIndependentFlavor; }

  // ── Setters (ModMenu screen writes these, then save() + pushToScoreboard()) ──────
  public void setPhoneEnabled(boolean v) { this.phoneEnabled = v; }
  public void setPhoneAutoOpen(boolean v) { this.phoneAutoOpen = v; }
  public void setGymMcAchievementsRequired(boolean v) { this.gymMcAchievementsRequired = v; }
  public void setMilestoneLootEnabled(boolean v) { this.milestoneLootEnabled = v; }
  public void setDaycareIndependentFlavor(boolean v) { this.daycareIndependentFlavor = v; }
}

package com.thecompanyinc.cobblemoninitiative.achievement;

import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import net.minecraft.server.level.ServerPlayer;

/**
 * A one-shot snapshot of every derived/global value the achievement predicates read,
 * computed ONCE per evaluation pass (see {@link AchievementManager#buildContext}). Predicates
 * are then pure functions of this object — no repeated scoreboard/Pokédex reads while walking
 * the manifest.
 *
 * <p>This is the heart of the "global achievement" idea: these fields are keyed on
 * global/derived state (badge count, dex count, fields liberated, roster size, run deaths)
 * rather than a single live event, so the moment the state qualifies the achievement can be
 * auto-granted — no dedicated player action or hand-wired grant site per achievement.
 */
public final class AchievementContext {

  public final ServerPlayer player;
  public final PlayerProgress progress;

  /** Gym badges earned (defeated gym leaders). */
  public final int badges;
  /** Elemental shrines cleared (defeated shrine keepers). */
  public final int shrines;
  /** Battle Frontier halls cleared (of 8). */
  public final int frontierHalls;
  /** Noble Pokémon befriended (completed noble advancements). */
  public final int nobles;
  /** Wheat War fields liberated (the {@code fields_liberated} score). */
  public final int fields;
  /** Unique species registered as caught in the Pokédex. */
  public final int dexCaught;
  /** Permanent Pokémon losses this run (0 = a still-flawless run). */
  public final int deaths;
  /** Whether {@link #deaths} is a trustworthy count (see PlayerProgress.deathsKnown). The
   *  deathless/flawless predicates require this so they never grant on an unverifiable count. */
  public final boolean deathsKnown;
  /** Story quests completed. */
  public final int quests;

  AchievementContext(
    ServerPlayer player, PlayerProgress progress,
    int badges, int shrines, int frontierHalls, int nobles,
    int fields, int dexCaught, int deaths, boolean deathsKnown, int quests
  ) {
    this.player = player;
    this.progress = progress;
    this.badges = badges;
    this.shrines = shrines;
    this.frontierHalls = frontierHalls;
    this.nobles = nobles;
    this.fields = fields;
    this.dexCaught = dexCaught;
    this.deaths = deaths;
    this.deathsKnown = deathsKnown;
    this.quests = quests;
  }

  /** True if the given Battle Frontier hall has been cleared (FrontierManager's clear tag). */
  public boolean hasFrontierHall(String hall) {
    return player.getTags().contains("frontier_" + hall + "_cleared");
  }
}

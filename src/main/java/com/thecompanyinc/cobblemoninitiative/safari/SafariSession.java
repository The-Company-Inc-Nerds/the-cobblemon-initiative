package com.thecompanyinc.cobblemoninitiative.safari;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerBossEvent;

/**
 * One live safari round. Sessions are deliberately NOT persisted — crash-safety lives in
 * the CUSTODY record ({@link SafariManager}), which is written through to disk BEFORE the
 * player is mutated and restored on join/respawn. Lifetime stats persist separately.
 */
public class SafariSession {

  /** Round mode — the split happens at round end (keep catches vs appraise+release). */
  public enum Mode {
    CAPTURE,
    CONTEST
  }

  /** A scatter waiting out its suspense window. */
  public static class PendingScatter {

    public final String baitType;
    public final double x;
    public final double y;
    public final double z;
    /** Spot key (BlockPos.asLong of the scatter block) for the warm-spot map. */
    public final long spotKey;
    public final int warmth;
    public int ticksUntilSpawn;

    public PendingScatter(
      String baitType,
      double x,
      double y,
      double z,
      long spotKey,
      int warmth,
      int ticksUntilSpawn
    ) {
      this.baitType = baitType;
      this.x = x;
      this.y = y;
      this.z = z;
      this.spotKey = spotKey;
      this.warmth = warmth;
      this.ticksUntilSpawn = ticksUntilSpawn;
    }
  }

  /** A lured Pokémon standing in its catchable window. */
  public static class ActiveLure {

    public final UUID entityUuid;
    public final UUID pokemonUuid;
    public final long spotKey;
    /** Lure-table rarity at roll time (common | uncommon | rare) — contest scoring reads it. */
    public final String rarity;
    public int ticksRemaining;
    /** Befriended via a bait offering — never runs detection, never spooks. */
    public boolean friendly;
    /** Stealth alert level (0..alertChecks); decays while unseen. */
    public int alert;
    /** >0 = spooked and fleeing; poofs when it reaches 0. */
    public int fleeTicksLeft;
    /**
     * Snowball staggers landed (0..staggerMax) — the push-your-luck dial: each stack
     * buys catch rate but raises the odds of a bolt and thins the alert threshold.
     */
    public int stagger;

    public ActiveLure(
      UUID entityUuid,
      UUID pokemonUuid,
      long spotKey,
      String rarity,
      int ticksRemaining
    ) {
      this.entityUuid = entityUuid;
      this.pokemonUuid = pokemonUuid;
      this.spotKey = spotKey;
      this.rarity = rarity;
      this.ticksRemaining = ticksRemaining;
    }
  }

  /** One catch on this round's ledger (rarity+friendly captured at catch time for contest). */
  public static class CatchRecord {

    public final String species;
    public final int level;
    public final UUID pokemonUuid;
    public final String rarity;
    public final boolean friendly;

    public CatchRecord(String species, int level, UUID pokemonUuid, String rarity, boolean friendly) {
      this.species = species;
      this.level = level;
      this.pokemonUuid = pokemonUuid;
      this.rarity = rarity;
      this.friendly = friendly;
    }
  }

  private final UUID playerId;
  private final Mode mode;

  // Entry position — the round-clock escort returns here (verified-safe by construction).
  private final String dimension;
  private final double entryX;
  private final double entryY;
  private final double entryZ;
  private final float entryYaw;
  private final float entryPitch;

  private int ticksRemaining;
  private ServerBossEvent bossBar;
  /** Catch-chance readout for the nearest lure (GREEN, under the round clock); null while no target. */
  private ServerBossEvent catchBar;

  private final List<PendingScatter> pendingScatters = new ArrayList<>();
  private final List<ActiveLure> lures = new ArrayList<>();
  private final List<CatchRecord> catches = new ArrayList<>();

  /** Warm-spot map for this round: scatter-block key → warmth tier (0..2). */
  private final Map<Long, Integer> warmSpots = new HashMap<>();

  private boolean warned60;
  private boolean warned10;

  /** Out-of-balls wrap-up countdown; -1 = not running. */
  private int ballsGraceTicks = -1;

  /** Boundary return countdown; -1 = inside the Preserve / not running. */
  private int boundaryGraceTicks = -1;

  /** Ticks a clock/boundary end has been held for an in-flight capture (capped). */
  private int endBusyHoldTicks = 0;

  public SafariSession(
    UUID playerId,
    Mode mode,
    String dimension,
    double entryX,
    double entryY,
    double entryZ,
    float entryYaw,
    float entryPitch,
    int ticksRemaining
  ) {
    this.playerId = playerId;
    this.mode = mode;
    this.dimension = dimension;
    this.entryX = entryX;
    this.entryY = entryY;
    this.entryZ = entryZ;
    this.entryYaw = entryYaw;
    this.entryPitch = entryPitch;
    this.ticksRemaining = ticksRemaining;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public Mode getMode() {
    return mode;
  }

  public String getDimension() {
    return dimension;
  }

  public double getEntryX() {
    return entryX;
  }

  public double getEntryY() {
    return entryY;
  }

  public double getEntryZ() {
    return entryZ;
  }

  public float getEntryYaw() {
    return entryYaw;
  }

  public float getEntryPitch() {
    return entryPitch;
  }

  public int getTicksRemaining() {
    return ticksRemaining;
  }

  public void setTicksRemaining(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }

  public ServerBossEvent getBossBar() {
    return bossBar;
  }

  public void setBossBar(ServerBossEvent bossBar) {
    this.bossBar = bossBar;
  }

  public ServerBossEvent getCatchBar() {
    return catchBar;
  }

  public void setCatchBar(ServerBossEvent catchBar) {
    this.catchBar = catchBar;
  }

  public List<PendingScatter> getPendingScatters() {
    return pendingScatters;
  }

  public List<ActiveLure> getLures() {
    return lures;
  }

  public List<CatchRecord> getCatches() {
    return catches;
  }

  public int getWarmth(long spotKey) {
    return warmSpots.getOrDefault(spotKey, 0);
  }

  public void bumpWarmth(long spotKey) {
    warmSpots.merge(spotKey, 1, (a, b) -> Math.min(2, a + b));
  }

  public Map<Long, Integer> getWarmSpots() {
    return warmSpots;
  }

  public boolean isWarned60() {
    return warned60;
  }

  public void setWarned60(boolean warned60) {
    this.warned60 = warned60;
  }

  public boolean isWarned10() {
    return warned10;
  }

  public void setWarned10(boolean warned10) {
    this.warned10 = warned10;
  }

  public int getBallsGraceTicks() {
    return ballsGraceTicks;
  }

  public void setBallsGraceTicks(int ballsGraceTicks) {
    this.ballsGraceTicks = ballsGraceTicks;
  }

  public int getBoundaryGraceTicks() {
    return boundaryGraceTicks;
  }

  public void setBoundaryGraceTicks(int boundaryGraceTicks) {
    this.boundaryGraceTicks = boundaryGraceTicks;
  }

  public int getEndBusyHoldTicks() {
    return endBusyHoldTicks;
  }

  public void setEndBusyHoldTicks(int endBusyHoldTicks) {
    this.endBusyHoldTicks = endBusyHoldTicks;
  }
}

package com.thecompanyinc.cobblemoninitiative.safari;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerBossEvent;

/**
 * One live Day Permit. Sessions are deliberately NOT persisted: on single-player a
 * logout stops the integrated server, so "logout forfeits the visit (no refund)" falls
 * out of session state simply never being written to disk. Lifetime stats persist
 * separately in {@link SafariManager}.
 */
public class SafariSession {

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
    public int ticksRemaining;

    public ActiveLure(UUID entityUuid, UUID pokemonUuid, long spotKey, int ticksRemaining) {
      this.entityUuid = entityUuid;
      this.pokemonUuid = pokemonUuid;
      this.spotKey = spotKey;
      this.ticksRemaining = ticksRemaining;
    }
  }

  /** One catch on this visit's ledger (printed at exit for the chat vote). */
  public static class CatchRecord {

    public final String species;
    public final int level;

    public CatchRecord(String species, int level) {
      this.species = species;
      this.level = level;
    }
  }

  private final UUID playerId;

  // Entry position — the eject escort returns here (verified-safe by construction).
  private final String dimension;
  private final double entryX;
  private final double entryY;
  private final double entryZ;
  private final float entryYaw;
  private final float entryPitch;

  private int ticksRemaining;
  private ServerBossEvent bossBar;

  private final List<PendingScatter> pendingScatters = new ArrayList<>();
  private final List<ActiveLure> lures = new ArrayList<>();
  private final List<CatchRecord> catches = new ArrayList<>();

  /** Warm-spot map for this visit: scatter-block key → warmth tier (0..2). */
  private final Map<Long, Integer> warmSpots = new HashMap<>();

  private boolean warned60;
  private boolean warned10;

  public SafariSession(
    UUID playerId,
    String dimension,
    double entryX,
    double entryY,
    double entryZ,
    float entryYaw,
    float entryPitch,
    int ticksRemaining
  ) {
    this.playerId = playerId;
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
}

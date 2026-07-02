package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuzlockeConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative.json");

  private boolean scaleDamageByPartySize = true;
  private boolean useMaxHealth = true;
  private float minimumDamagePercent = 0.0f;
  private boolean applyInWildBattles = true;
  private boolean applyInTrainerBattles = true;
  private String damageMessage = "§c%pokemon% fainted! You take damage!";
  private boolean removeFaintedPokemon = true;
  private boolean sacrificeOnFlee = true;
  private boolean mysterySacrifice = false;
  private boolean sendCaughtToPC = true;
  private boolean setCaughtToZeroHP = true;
  private DuplicateHandling duplicateHandling = DuplicateHandling.OFF;
  private Set<String> caughtSpecies = new HashSet<>();
  private boolean enableSafeZones = true;
  private List<SafeZone> safeZones = new ArrayList<>();
  private boolean enableAreaAnnouncements = true;
  private AnnouncementStyle announcementStyle = AnnouncementStyle.TITLE;
  private int announcementFadeIn = 20;
  private int announcementStay = 70;
  private int announcementFadeOut = 20;
  private boolean announceOnExit = false;
  /** If true, announces "the wild" when the player leaves a named zone into undefined territory. */
  private boolean announceWilderness = true;
  /** Display name for undefined territory. */
  private String wildernessName = "Wilderness";
  /** Optional subtitle shown beneath the wilderness name (TITLE style only). */
  private String wildernessSubtitle = "";
  /** Hex color for the wilderness announcement, e.g. "#88AA88". */
  private String wildernessColor = "#88AA88";

  // -------------------------------------------------------------------------
  // Dark Urge whispers (intrusive shadow-self lines on Pokémon faint)
  // -------------------------------------------------------------------------
  /** Master toggle for the Dark Urge whisper-on-faint flavour. */
  private boolean enableDarkUrgeWhispers = true;
  /** Chance (0..1) that an eligible faint fires a whisper. */
  private float darkUrgeChance = 0.12f;
  /** Per-player cooldown between whispers, in game ticks (6000 = 5 min). */
  private int darkUrgeCooldownTicks = 6000;
  /**
   * Whisper pool indexed by escalation tier 0..3. Tier rises with the player's level
   * cap, so tier 3 (the founder speaking plainly) only appears post-gym-8 — after the
   * gym-7 "charter" memory fragment has already landed.
   */
  private List<List<String>> darkUrgeMessages = defaultDarkUrgeMessages();

  // Level-cap breakpoints that raise the Dark Urge whisper escalation tier.
  private int darkUrgeTier1LevelCap = 30;
  private int darkUrgeTier2LevelCap = 52;
  private int darkUrgeTier3LevelCap = 73;

  /** How often (ticks) zone-transition announcements are polled. 20 = 1s. */
  private int zoneCheckCadenceTicks = 20;

  public enum DuplicateHandling {
    OFF,
    RELEASE_IF_OWNED,
    RELEASE_IF_EVER_CAUGHT,
  }

  public enum AnnouncementStyle {
    TITLE,
    ACTIONBAR,
    CHAT
  }

  public static class SafeZone {

    public String name;
    public String dimension;
    public int centerX;
    public int centerY;
    public int centerZ;
    public int radius;
    public boolean preventHostileOnly;
    public boolean cylindrical;
    /** If true, this zone does not suppress mob spawning (mobs spawn normally). */
    public boolean mobsSpawn = false;
    /** Optional liberation gate — see InstallZone.activeWhenObjective. Blank objective = always active. */
    public String activeWhenObjective = null;
    public String activeWhenHolder = null;
    public int activeWhenMin = 1;
    /** If true, fires an area announcement when the player enters this zone. */
    public boolean announce = false;
    /** Optional subtitle shown beneath the zone name (TITLE style only). */
    public String subtitle = "";
    /** Hex color for map labelling, e.g. "#7AAAD0". */
    public String color = "";

    public SafeZone() {}

    public SafeZone(
      String name,
      String dimension,
      int x,
      int y,
      int z,
      int radius,
      boolean hostileOnly,
      boolean cylindrical
    ) {
      this.name = name;
      this.dimension = dimension;
      this.centerX = x;
      this.centerY = y;
      this.centerZ = z;
      this.radius = radius;
      this.preventHostileOnly = hostileOnly;
      this.cylindrical = cylindrical;
    }

    public boolean contains(String dim, int x, int y, int z) {
      if (!dimension.equals(dim)) return false;

      int dx = x - centerX;
      int dz = z - centerZ;
      int distSq = dx * dx + dz * dz;

      if (!cylindrical) {
        int dy = y - centerY;
        distSq += dy * dy;
      }

      return distSq <= (radius * radius);
    }
  }

  public NuzlockeConfig() {}

  public static NuzlockeConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          NuzlockeConfig config = GSON.fromJson(reader, NuzlockeConfig.class);
          if (config != null) {
            if (config.caughtSpecies == null) config.caughtSpecies = new HashSet<>();
            if (config.safeZones == null) config.safeZones = new ArrayList<>();
            if (config.darkUrgeMessages == null || config.darkUrgeMessages.isEmpty())
              config.darkUrgeMessages = defaultDarkUrgeMessages();
            return config;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading Nuzlocke config, using defaults: {}", e.getMessage());
    }

    NuzlockeConfig config = new NuzlockeConfig();
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
      LOGGER.error("Error saving Nuzlocke config: {}", e.getMessage());
    }
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  public boolean isScaleDamageByPartySize() { return scaleDamageByPartySize; }
  public boolean isUseMaxHealth() { return useMaxHealth; }
  public float getMinimumDamagePercent() { return minimumDamagePercent; }
  public boolean isApplyInWildBattles() { return applyInWildBattles; }
  public boolean isApplyInTrainerBattles() { return applyInTrainerBattles; }
  public String getDamageMessage() { return damageMessage; }
  public boolean isRemoveFaintedPokemon() { return removeFaintedPokemon; }
  public boolean isSacrificeOnFlee() { return sacrificeOnFlee; }
  public boolean isMysterySacrifice() { return mysterySacrifice; }
  public boolean isSendCaughtToPC() { return sendCaughtToPC; }
  public boolean isSetCaughtToZeroHP() { return setCaughtToZeroHP; }
  public DuplicateHandling getDuplicateHandling() { return duplicateHandling; }
  public Set<String> getCaughtSpecies() { return caughtSpecies; }
  public boolean isEnableSafeZones() { return enableSafeZones; }
  public List<SafeZone> getSafeZones() { return safeZones; }
  public boolean isEnableAreaAnnouncements() { return enableAreaAnnouncements; }
  public AnnouncementStyle getAnnouncementStyle() { return announcementStyle; }
  public int getAnnouncementFadeIn() { return announcementFadeIn; }
  public int getAnnouncementStay() { return announcementStay; }
  public int getAnnouncementFadeOut() { return announcementFadeOut; }
  public boolean isAnnounceOnExit() { return announceOnExit; }
  public boolean isAnnounceWilderness() { return announceWilderness; }
  public String getWildernessName() { return wildernessName; }
  public String getWildernessSubtitle() { return wildernessSubtitle; }
  public String getWildernessColor() { return wildernessColor; }
  public boolean isEnableDarkUrgeWhispers() { return enableDarkUrgeWhispers; }
  public float getDarkUrgeChance() { return darkUrgeChance; }
  public int getDarkUrgeCooldownTicks() { return darkUrgeCooldownTicks; }
  public List<List<String>> getDarkUrgeMessages() { return darkUrgeMessages; }
  public int getDarkUrgeTier1LevelCap() { return darkUrgeTier1LevelCap; }
  public int getDarkUrgeTier2LevelCap() { return darkUrgeTier2LevelCap; }
  public int getDarkUrgeTier3LevelCap() { return darkUrgeTier3LevelCap; }
  public int getZoneCheckCadenceTicks() { return zoneCheckCadenceTicks; }

  // -------------------------------------------------------------------------
  // Setters
  // -------------------------------------------------------------------------

  public void setScaleDamageByPartySize(boolean v) { this.scaleDamageByPartySize = v; }
  public void setUseMaxHealth(boolean v) { this.useMaxHealth = v; }
  public void setMinimumDamagePercent(float v) { this.minimumDamagePercent = v; }
  public void setApplyInWildBattles(boolean v) { this.applyInWildBattles = v; }
  public void setApplyInTrainerBattles(boolean v) { this.applyInTrainerBattles = v; }
  public void setDamageMessage(String v) { this.damageMessage = v; }
  public void setRemoveFaintedPokemon(boolean v) { this.removeFaintedPokemon = v; }
  public void setSacrificeOnFlee(boolean v) { this.sacrificeOnFlee = v; }
  public void setMysterySacrifice(boolean v) { this.mysterySacrifice = v; }
  public void setSendCaughtToPC(boolean v) { this.sendCaughtToPC = v; }
  public void setSetCaughtToZeroHP(boolean v) { this.setCaughtToZeroHP = v; }
  public void setDuplicateHandling(DuplicateHandling v) { this.duplicateHandling = v; }
  public void setEnableSafeZones(boolean v) { this.enableSafeZones = v; }
  public void setSafeZones(List<SafeZone> v) { this.safeZones = v; }
  public void setEnableAreaAnnouncements(boolean v) { this.enableAreaAnnouncements = v; }
  public void setAnnouncementStyle(AnnouncementStyle v) { this.announcementStyle = v; }
  public void setAnnouncementFadeIn(int v) { this.announcementFadeIn = v; }
  public void setAnnouncementStay(int v) { this.announcementStay = v; }
  public void setAnnouncementFadeOut(int v) { this.announcementFadeOut = v; }
  public void setAnnounceOnExit(boolean v) { this.announceOnExit = v; }
  public void setAnnounceWilderness(boolean v) { this.announceWilderness = v; }
  public void setWildernessName(String v) { this.wildernessName = v; }
  public void setWildernessSubtitle(String v) { this.wildernessSubtitle = v; }
  public void setWildernessColor(String v) { this.wildernessColor = v; }
  public void setEnableDarkUrgeWhispers(boolean v) { this.enableDarkUrgeWhispers = v; }
  public void setDarkUrgeChance(float v) { this.darkUrgeChance = v; }
  public void setDarkUrgeCooldownTicks(int v) { this.darkUrgeCooldownTicks = v; }
  public void setDarkUrgeTier1LevelCap(int v) { this.darkUrgeTier1LevelCap = v; }
  public void setDarkUrgeTier2LevelCap(int v) { this.darkUrgeTier2LevelCap = v; }
  public void setDarkUrgeTier3LevelCap(int v) { this.darkUrgeTier3LevelCap = v; }
  public void setZoneCheckCadenceTicks(int v) { this.zoneCheckCadenceTicks = v; }

  // -------------------------------------------------------------------------
  // Utility
  // -------------------------------------------------------------------------

  /**
   * Default Dark Urge whisper pool, indexed by escalation tier 0..3. Written in the
   * voice of the protagonist's shadow self — the founder who built the CobbleDollar
   * ledger and treats loss as a line item. Keep lines free of double-quotes.
   */
  private static List<List<String>> defaultDarkUrgeMessages() {
    List<List<String>> tiers = new ArrayList<>();
    // Tier 0 — pre-first-badge: formless unease.
    tiers.add(Arrays.asList(
      "...that one mattered to you. Curious.",
      "You flinched. I never used to flinch.",
      "Something in you keeps score. It is almost done counting."
    ));
    // Tier 1 — gyms 1-3: the cold logic starts to speak.
    tiers.add(Arrays.asList(
      "Assets fail. You replace them. You know this.",
      "Sentiment is a line item, and you always balanced the books.",
      "It served its purpose. So did the last one. So will the next.",
      "You grieve like someone learning the word for the first time."
    ));
    // Tier 2 — gyms 4-7: unmistakably the founder's voice.
    tiers.add(Arrays.asList(
      "We do not mourn inventory. We audit it.",
      "Every empire stands on something expendable. You taught me that.",
      "They trusted you to verify. You verified them into the ledger.",
      "One more entry in red. You were always good with red.",
      "You built a machine that runs on loss. Why be surprised it took one?"
    ));
    // Tier 3 — gym 8+: the shadow stops pretending the two of you are different.
    tiers.add(Arrays.asList(
      "There it is. The face I wore the day I signed them away.",
      "You and I keep the same books. You only forgot whose name is on them.",
      "I never lost a thing I could not write off. Neither did you.",
      "Hold the grief if you like. It does not move the balance.",
      "Every name you bury brings you one step closer to your own.",
      "Welcome back. The Company missed your hand on the pen."
    ));
    return tiers;
  }

  public void addCaughtSpecies(String species) {
    caughtSpecies.add(species.toLowerCase());
    save();
  }

  public boolean hasEverCaught(String species) {
    return caughtSpecies.contains(species.toLowerCase());
  }

  public void addSafeZone(SafeZone zone) {
    safeZones.add(zone);
    save();
  }

  public boolean removeSafeZone(String name) {
    boolean removed = safeZones.removeIf(z -> z.name.equalsIgnoreCase(name));
    if (removed) save();
    return removed;
  }

  public boolean isInSafeZone(String dimension, int x, int y, int z) {
    return isInSafeZone(dimension, x, y, z, null);
  }

  public boolean isInSafeZone(String dimension, int x, int y, int z, MinecraftServer server) {
    if (!enableSafeZones) return false;
    for (SafeZone zone : safeZones) {
      if (zone.contains(dimension, x, y, z) && isZoneActive(zone, server)) return true;
    }
    return false;
  }

  public SafeZone getSafeZoneAt(String dimension, int x, int y, int z) {
    return getSafeZoneAt(dimension, x, y, z, null);
  }

  public SafeZone getSafeZoneAt(String dimension, int x, int y, int z, MinecraftServer server) {
    for (SafeZone zone : safeZones) {
      if (zone.contains(dimension, x, y, z) && isZoneActive(zone, server)) return zone;
    }
    return null;
  }

  /** Returns the first announce-enabled zone containing the position, or null. */
  public SafeZone getAnnouncedZoneAt(String dimension, int x, int y, int z) {
    return getAnnouncedZoneAt(dimension, x, y, z, null);
  }

  public SafeZone getAnnouncedZoneAt(String dimension, int x, int y, int z, MinecraftServer server) {
    for (SafeZone zone : safeZones) {
      if (zone.announce && zone.contains(dimension, x, y, z) && isZoneActive(zone, server)) return zone;
    }
    return null;
  }

  /**
   * A zone with an {@code activeWhenObjective} only counts as active once that world
   * scoreboard (holder {@code activeWhenHolder}, default the zone name) reaches
   * {@code activeWhenMin}. Ungated zones are always active. Fails open (active) when the
   * server/scoreboard is unavailable so normal zones are never accidentally disabled; a
   * gated zone whose objective doesn't exist yet reads as inactive (not-yet-liberated).
   */
  public boolean isZoneActive(SafeZone zone, MinecraftServer server) {
    if (zone.activeWhenObjective == null || zone.activeWhenObjective.isEmpty()) return true;
    if (server == null) return true;
    try {
      Scoreboard sb = server.getScoreboard();
      Objective obj = sb.getObjective(zone.activeWhenObjective);
      if (obj == null) return false;
      String holder = (zone.activeWhenHolder != null && !zone.activeWhenHolder.isEmpty())
        ? zone.activeWhenHolder
        : zone.name;
      ReadOnlyScoreInfo info = sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(holder), obj);
      return info != null && info.value() >= zone.activeWhenMin;
    } catch (Exception e) {
      return true;
    }
  }
}

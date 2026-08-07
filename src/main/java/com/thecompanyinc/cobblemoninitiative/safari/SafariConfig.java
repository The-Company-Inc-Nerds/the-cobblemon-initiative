package com.thecompanyinc.cobblemoninitiative.safari;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Tunables for the Ridgewatch Preserve safari rounds — loaded from the jar resource
 * {@code data/cobblemon_initiative/safari/safari.json} (noble/quest-track pattern:
 * showrunner numbers live in data, code carries the defaults as a fallback).
 *
 * <p>All fees are FLAT and printed before commitment (randomness invariant: committed
 * amounts are never rolled).
 */
public class SafariConfig {

  private static final String RESOURCE_PATH =
    "data/cobblemon_initiative/safari/safari.json";
  /** Writable override (ModMenu-editable). When present it wins over the bundled resource. */
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-safari.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /** Round fee in CobbleDollars — the pay-probe amount. */
  public int permitFee = 500;

  /** Preserve Safari Balls issued per round (marked; handed back at the gate). */
  public int balls = 10;

  /** Marked snowballs issued per round (the weaken tool — the un-humane path, kept generous). */
  public int snowballs = 32;

  /** Units of EACH standard bait table issued per round (executive_blend excluded). */
  public int baitPerTable = 2;

  /** Round clock, in seconds (tick-based — the ESC pause freezes it; say so on stream). */
  public int roundSeconds = 180;

  /** How long lured Pokémon linger before wandering off, in seconds. */
  public int windowSeconds = 75;

  /** Gym badges required before Maya starts a round (post-gym-6 leg, lure band 40-50). */
  public int gateBadges = 6;

  /**
   * Safari-exclusive roster: table species never spawn naturally anywhere — the Preserve
   * is their only wild source; bait lures bypass the guard by construction (addFreshEntity
   * never emits ENTITY_SPAWN). Enforced worldwide by NaturalSpawnGuard.
   */
  public boolean exclusiveSpecies = true;

  /** Kiosk bait price per unit for the standard tables (0 = free issue). */
  public int baitFee = 60;

  /** Kiosk bait price per unit for the premium executive_blend table. */
  public int baitFeeExecutive = 250;

  /** Suspense window between a scatter and the spawn roll, in seconds (min..max). */
  public int suspenseMinSeconds = 5;

  public int suspenseMaxSeconds = 15;

  /** Spawns rolled per scatter (min..max, inclusive). */
  public int spawnsMin = 1;

  public int spawnsMax = 3;

  /** Stealth: how close a lure must be to a player to run a detection check (blocks). */
  public double detectRange = 7.0;

  /** Consecutive "seen" detection checks (one per 10t) before a lure spooks. */
  public int alertChecks = 5;

  /** Ticks a spooked lure flees directly away before it poofs. */
  public int fleeTicks = 30;

  /** Fraction of max HP a Preserve snowball shaves off (floors at 1 HP). */
  public double weakenFraction = 0.25;

  /**
   * Catch-rate bonus per snowball stagger: the real throw (POKEMON_CATCH_RATE hook)
   * and the catch-chance bar both apply rate × (1 + bonus × staggers).
   */
  public double staggerCatchBonus = 0.35;

  /**
   * Bolt roll on each snowball hit that lands a stagger: flee chance = this × the
   * NEW stagger count (the push-your-luck cost of stacking staggers).
   */
  public double staggerFleeChance = 0.07;

  /** Staggers that stack (further snowballs still weaken HP but add no more). */
  public int staggerMax = 4;

  // ── Trust meter (the HUMANE catch path) ─────────────────────────────────────────

  /**
   * Trust needed before a lure is befriended. Each treat (a bait offered by hand to a
   * present lure) raises trust; at this threshold the lure flips {@code friendly}:
   * it never spooks, its window extends, its capture is easier, and a befriended catch
   * comes out at {@link #befriendedFriendship}. Per-round — trust lives on the in-round
   * lure (never persisted).
   */
  public int trustThreshold = 3;

  /**
   * Trust gained per treat when the offered bait MATCHES the table that lured the mon
   * (its favourite). A mismatched bait still earns +1 (see SafariManager.onUseEntity).
   */
  public int trustPerTreat = 1;

  /**
   * Catch-rate bonus for a fully-befriended (friendly) lure: the real throw
   * (POKEMON_CATCH_RATE hook) and the catch-chance bar both apply
   * rate × (1 + friendlyCatchBonus). Stacks additively with the stagger bonus, so the
   * gentle path reaches the same odds as staggering without spooking the mon.
   */
  public double friendlyCatchBonus = 0.6;

  /**
   * Friendship stamped on a Pokémon caught after it was befriended (the humane payoff —
   * a bond it keeps). Clamped to Cobblemon's max friendship on apply. 0 = leave friendship
   * as caught.
   */
  public int befriendedFriendship = 220;

  /** Catch-window extension for a befriended lure, in seconds. */
  public int friendlyBonusSeconds = 30;

  /** Seconds to step back inside the Preserve after a boundary warning. */
  public int boundaryGraceSeconds = 10;

  /** Contest appraisal points per catch, by lure-table rarity. */
  public int pointsCommon = 1;

  public int pointsUncommon = 3;

  public int pointsRare = 6;

  /** Contest bonus points for a catch whose lure was befriended first (the humane premium). */
  public int friendlyPoint = 3;

  /**
   * Eject pad. All-zero = unset → the round-clock escort returns the player to the exact
   * position where they entered (recorded at round start — verified-safe ground by
   * construction: they stood on it).
   */
  public int ejectX = 0;

  public int ejectY = 0;

  public int ejectZ = 0;

  public boolean hasEjectPad() {
    return ejectX != 0 || ejectY != 0 || ejectZ != 0;
  }

  public static SafariConfig load() {
    // Writable ModMenu override wins over the bundled resource default.
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        SafariConfig cfg = GSON.fromJson(reader, SafariConfig.class);
        if (cfg != null) {
          return cfg;
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error(
          "Failed to read {} — falling back to the bundled default.", CONFIG_FILE, e);
      }
    }
    try (
      InputStream in = SafariConfig.class.getClassLoader()
        .getResourceAsStream(RESOURCE_PATH)
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn(
          "Safari config resource missing ({}); using built-in defaults.",
          RESOURCE_PATH
        );
        return new SafariConfig();
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        SafariConfig cfg = new Gson().fromJson(reader, SafariConfig.class);
        return cfg != null ? cfg : new SafariConfig();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load safari config — using defaults.", e);
      return new SafariConfig();
    }
  }

  /** Write the current values to the config override (ModMenu save path). */
  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Error saving safari config: {}", e.getMessage());
    }
  }
}

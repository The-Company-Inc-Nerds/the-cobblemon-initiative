package com.thecompanyinc.cobblemoninitiative.homestead;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Homestead-beacon economy tuning (docs/MINECRAFT_FLAVOR.md §1 + §8). A liberated field pays
 * the player a daily CobbleDollars trickle scaled by the beacon they raise on it: the pyramid's
 * TIER (1–4) picks a base rate, its MATERIAL mix applies a quality multiplier, and a nether star
 * unlocks the top tier (fork (c) — the pyramid is read directly, so income works before any
 * vanilla beacon activation). Per-field and aggregate caps keep it a trickle, never a faucet, in
 * a hardcore economy.
 *
 * File style follows DaycareConfig: config/cobblemon-initiative-homestead.json, defaults on first
 * run. Beacon PURCHASE (Suzune's resale) is priced here but charged through the datapack pay-probe
 * rail; income PAYOUT is done in Java (cobbledollars give).
 */
public class HomesteadConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File("config/cobblemon-initiative-homestead.json");

  private boolean enabled = true;

  /** Global scale on ALL homestead income. */
  private double incomeMultiplier = 1.0;

  /** Base CD/day per pyramid tier (index 0 = T1 … index 3 = T4). */
  private int[] beaconYieldTier = { 25, 50, 100, 175 };

  /** Quality multiplier weighted by the pyramid's block mix. */
  private double materialMultIron = 1.0;
  private double materialMultGold = 1.25;
  private double materialMultDiamond = 1.6;
  private double materialMultEmerald = 1.8;
  private double materialMultNetherite = 2.5;

  /** Max CD/day from any single homestead. */
  private int fieldCap = 450;
  /** Max CD/day across ALL of a player's homesteads combined. */
  private int totalCap = 1500;

  /** Suzune's beacon resale: first beacon is a gift, then priceBase × growth^(bought). */
  private int priceBase = 2000;
  private double priceGrowth = 1.5;

  /** fork (c): a nether star unlocks T4 output (and lights the vanilla beam). */
  private boolean starForTopTier = true;

  /** Liberated fields become no-death build zones (handled by the liberation datapack). */
  private boolean homesteadSafeZone = true;

  /** Radius (blocks) searched for a beacon when the player runs `homestead claim`. */
  private int claimRadius = 16;

  public HomesteadConfig() {}

  public static HomesteadConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          HomesteadConfig config = GSON.fromJson(reader, HomesteadConfig.class);
          if (config != null && config.beaconYieldTier != null && config.beaconYieldTier.length == 4) {
            return config;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading homestead config, using defaults: {}", e.getMessage());
    }

    HomesteadConfig config = new HomesteadConfig();
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
      LOGGER.error("Error saving homestead config: {}", e.getMessage());
    }
  }

  /** Base CD/day for a completed pyramid tier (0 → nothing; 1..4 → the table). */
  public int baseForTier(int tier) {
    if (tier <= 0) return 0;
    int idx = Math.min(tier, 4) - 1;
    return beaconYieldTier[idx];
  }

  /** Suzune's price for the player's next beacon after {@code alreadyBought} purchases. */
  public int priceForNext(int alreadyBought) {
    return (int) Math.round(priceBase * Math.pow(priceGrowth, Math.max(0, alreadyBought)));
  }

  // ── Getters ──────────────────────────────────────────────────────────────────
  public boolean isEnabled() { return enabled; }
  public double getIncomeMultiplier() { return incomeMultiplier; }
  public int[] getBeaconYieldTier() { return beaconYieldTier; }
  public double getMaterialMultIron() { return materialMultIron; }
  public double getMaterialMultGold() { return materialMultGold; }
  public double getMaterialMultDiamond() { return materialMultDiamond; }
  public double getMaterialMultEmerald() { return materialMultEmerald; }
  public double getMaterialMultNetherite() { return materialMultNetherite; }
  public int getFieldCap() { return fieldCap; }
  public int getTotalCap() { return totalCap; }
  public int getPriceBase() { return priceBase; }
  public double getPriceGrowth() { return priceGrowth; }
  public boolean isStarForTopTier() { return starForTopTier; }
  public boolean isHomesteadSafeZone() { return homesteadSafeZone; }
  public int getClaimRadius() { return claimRadius; }

  // ── Setters (ModMenu screen) ─────────────────────────────────────────────────
  public void setEnabled(boolean v) { this.enabled = v; }
  public void setIncomeMultiplier(double v) { this.incomeMultiplier = v; }
  public void setBeaconYieldTier(int idx, int v) {
    if (beaconYieldTier != null && idx >= 0 && idx < beaconYieldTier.length) beaconYieldTier[idx] = v;
  }
  public void setMaterialMultIron(double v) { this.materialMultIron = v; }
  public void setMaterialMultGold(double v) { this.materialMultGold = v; }
  public void setMaterialMultDiamond(double v) { this.materialMultDiamond = v; }
  public void setMaterialMultEmerald(double v) { this.materialMultEmerald = v; }
  public void setMaterialMultNetherite(double v) { this.materialMultNetherite = v; }
  public void setFieldCap(int v) { this.fieldCap = v; }
  public void setTotalCap(int v) { this.totalCap = v; }
  public void setPriceBase(int v) { this.priceBase = v; }
  public void setPriceGrowth(double v) { this.priceGrowth = v; }
  public void setStarForTopTier(boolean v) { this.starForTopTier = v; }
  public void setHomesteadSafeZone(boolean v) { this.homesteadSafeZone = v; }
  public void setClaimRadius(int v) { this.claimRadius = v; }
}

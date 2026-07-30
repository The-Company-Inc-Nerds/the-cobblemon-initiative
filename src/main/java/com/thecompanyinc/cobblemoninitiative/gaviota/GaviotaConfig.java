package com.thecompanyinc.cobblemoninitiative.gaviota;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config for the two Gaviota Port set-pieces (0.7.0-alpha.11), both coord-driven so the showrunner
 * fills the geometry in later. Lives at {@code config/cobblemon-initiative-gaviota.json} (a default
 * with PLACEHOLDER coords is written on first load — edit it, then {@code /cobblemon-initiative
 * gaviota reload}). Singleton like {@link com.thecompanyinc.cobblemoninitiative.config.DojoConfig}.
 *
 * <p><b>(A) The drainable water gym.</b> {@link Drain#region} is flooded (a {@code fill … water})
 * by {@code /cobblemon-initiative gaviota flood}. The gym has "valve" operator NPCs (each carrying a
 * unique entity tag in {@link Drain#operatorTags}); the correct order is RANDOMISED per world. Talking
 * to the right next operator drains one stage — the water level lowers smoothly (a tick-driven layer
 * fill) and, if {@link Drain#drainCutscene} is set, a cutscene frames it. Any gym NPC below the
 * current water surface cannot be interacted with until the water drops past it.
 *
 * <p><b>(B) The donation aquarium.</b> Donate a caught species from {@link Aquarium#donatableSpecies}
 * at the curator (each species once): the mon leaves your party and appears in the matching
 * {@link Aquarium.Tank} with a random name from {@link Aquarium#fishNames}. Three tiers by distinct
 * donation count: {@link Aquarium#tier1Count} tags {@code aquarium_tier1_done} (unlocks the leader),
 * {@link Aquarium#tier2Count} gives bait, and donating ALL of them gives the enchanted Master Rod.
 */
public class GaviotaConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-gaviota.json"
  );

  private static GaviotaConfig instance;

  public boolean enabled = true;
  public String dimension = "minecraft:overworld";
  public Drain drain = new Drain();
  public Aquarium aquarium = new Aquarium();

  /** The flooded gym arena + its pump-pair drain puzzle (playtest coords, 2026-07-30). */
  public static class Drain {
    /** The water box (inclusive), from the playtest water-level corners P12/P13. */
    public Box region = new Box(570, 87, 3632, 611, 92, 3661);
    /** Starting (full) water surface Y. */
    public int waterTopY = 92;
    /** Y the water bottoms out at when fully drained. */
    public int waterBottomY = 87;
    /** The pump-bot positions (playtest P15-P26). Randomly paired at flood; activate BOTH pumps of a
     *  matched pair to drain a level. Activating two that are NOT a pair resets the whole gym (water
     *  refills, trainers re-arm). Each pump glows its pair colour so the pairs are readable. */
    public List<Pos> pumps = new ArrayList<>(List.of(
      new Pos(561.6, 103, 3633.5), new Pos(577.6, 103, 3617.7), new Pos(603.4, 103, 3617.5),
      new Pos(619.6, 103, 3633.3), new Pos(619.6, 103, 3659.7), new Pos(603.7, 103, 3675.5),
      new Pos(577.6, 103, 3675.4), new Pos(561.5, 103, 3659.5), new Pos(570.8, 92, 3660.1),
      new Pos(610.8, 92, 3632.5), new Pos(570.2, 92, 3632.2), new Pos(610.7, 92, 3660.8)));
    /** Easy NPC preset spawned at each pump on flood (tagged gaviota_pump + gaviota_gym_npc). */
    public String pumpPreset = "easy_npc:preset/humanoid/gaviota_pump.npc.snbt";
    /** Entity tag every gym NPC (pumps + trainers) carries, so the interaction gate water-locks any
     *  body below the current surface. */
    public String gymNpcTag = "gaviota_gym_npc";
    /** Gym-trainer defeat tags REVOKED on a mismatch reset — the eyesight battles re-arm. ONLY the
     *  four CREW are sight/pursue trainers (index-keyed defeated_gaviota_trainer_1..4); the Jr.
     *  Apprentice, Apprentice, and Leader are talk-to-battle gated behind {@code gaviota_drained}
     *  (battleable only once the arena is fully drained), so a reset re-locks them automatically via
     *  that tag and they are NOT listed here. Must match the crew's real battle {@code defeat_tag}s. */
    public List<String> gymTrainerDefeatedTags = new ArrayList<>(List.of(
      "defeated_gaviota_trainer_1", "defeated_gaviota_trainer_2",
      "defeated_gaviota_trainer_3", "defeated_gaviota_trainer_4"));
    /** Ticks between each 1-block water layer move during the smooth drain/raise (lower = faster). */
    public int animPeriodTicks = 4;
    /** Cutscene id played when the water drains a level (P27 spot; blank = none). */
    public String drainCutscene = "gaviota_water";
    /** Cutscene id played when the water raises/resets (blank = none). */
    public String raiseCutscene = "gaviota_water";
  }

  public static class Pos {
    public double x, y, z;
    public Pos() {}
    public Pos(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
  }

  /** The donation aquarium: tanks, the donatable roster, tiers, and the tank-fish name pool. */
  public static class Aquarium {
    /** Entity tag the curator NPC carries (the donation dialog button runs the donate command). */
    public String curatorTag = "gaviota_curator";
    /** Distinct donations for tier 1 (unlocks the leader via aquarium_tier1_done). */
    public int tier1Count = 3;
    /** Distinct donations for tier 2 (the bait reward). */
    public int tier2Count = 6;
    /** Bait item + count given at tier 2. */
    public String tier2BaitItem = "cobblemon:poke_bait";
    public int tier2BaitCount = 16;
    /** Cobblemon fish SPECIES donatable from the party (each once). Curated to WARM-OCEAN species the
     *  Poke Rod actually reels on a badlands coast (magikarp/krabby broad ocean; horsea/corsola/luvdisc
     *  warm ocean). Dropped: goldeen (river-only), shellder (cold-ocean), feebas (has NO water-column
     *  spawn — not in the rod pool). If Gaviota's water is a LANDLOCKED badlands lake rather than open
     *  warm ocean, these will not bite there — add a fishing spawn override or lean on the vanilla fish. */
    public List<String> donatablePokemon = new ArrayList<>(List.of(
      "magikarp", "krabby", "horsea", "corsola", "luvdisc"));
    /** Vanilla Minecraft FISH ITEMS donatable from the inventory (each once). {@code entity} is the
     *  mob spawned in the tank as its display. */
    public List<FishItem> donatableFishItems = new ArrayList<>(List.of(
      new FishItem("minecraft:cod", "minecraft:cod"),
      new FishItem("minecraft:salmon", "minecraft:salmon"),
      new FishItem("minecraft:tropical_fish", "minecraft:tropical_fish"),
      new FishItem("minecraft:pufferfish", "minecraft:pufferfish")));
    /** The display tanks (playtest P2-P11, "aquarium spot 1-10"). A donation drops into the tank whose
     *  {@code accepts} lists its id, else round-robins by its roster index across these tanks. */
    public List<Tank> tanks = new ArrayList<>(List.of(
      new Tank(501.3, 72, 3487.7), new Tank(506.2, 72, 3488.6), new Tank(513.4, 74.6, 3484.8),
      new Tank(510.7, 78.4, 3500), new Tank(496.9, 77.2, 3504.6), new Tank(493.5, 73.5, 3512.8),
      new Tank(501.6, 73.5, 3512.9), new Tank(510.4, 72, 3513.1), new Tank(492.7, 74.2, 3509.4),
      new Tank(495.9, 73.5, 3493.2)));
    /** How many display fish a single donation spawns — a small swimming school dropped into one
     *  randomly-chosen tank (they keep light AI so they mill/swim; PersistenceRequired so they stay). */
    public int schoolSize = 4;
    /** Random names given to donated tank fish. */
    public List<String> fishNames = new ArrayList<>(List.of(
      "Bubbles", "Finny", "Gil", "Splashington", "Captain Nibbles", "Wanda",
      "Sir Fin", "Marlin", "Coral", "Pebble", "Squirt", "Dory", "Nemo", "Bloop"));

    /** Total distinct donations = the full roster (tier 3 completion). */
    public int totalDonatable() {
      return (donatablePokemon == null ? 0 : donatablePokemon.size())
        + (donatableFishItems == null ? 0 : donatableFishItems.size());
    }

    public static class FishItem {
      public String item = "";
      public String entity = "";
      public FishItem() {}
      public FishItem(String item, String entity) { this.item = item; this.entity = entity; }
    }

    public static class Tank {
      public double x = 0, y = 0, z = 0;
      /** Ids (species names and/or fish-item paths like "cod") this tank accepts; empty = any. */
      public List<String> accepts = new ArrayList<>();
      public Tank() {}
      public Tank(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    }
  }

  public static class Box {
    public int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
    public Box() {}
    public Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX; this.minY = minY; this.minZ = minZ;
      this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
    }
    public boolean isPlaceholder() { return minX == 0 && maxX == 0 && minZ == 0 && maxZ == 0; }
  }

  // ── singleton / lifecycle ──────────────────────────────────────────────────────

  public static GaviotaConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() { instance = load(); }

  public static GaviotaConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader r = new FileReader(CONFIG_FILE)) {
          GaviotaConfig cfg = GSON.fromJson(r, GaviotaConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("[Gaviota] Error loading config, using defaults: {}", e.getMessage());
    }
    GaviotaConfig cfg = new GaviotaConfig();
    cfg.save();
    return cfg;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter w = new FileWriter(CONFIG_FILE)) { GSON.toJson(this, w); }
    } catch (IOException e) {
      LOGGER.error("[Gaviota] Error saving config: {}", e.getMessage());
    }
  }
}

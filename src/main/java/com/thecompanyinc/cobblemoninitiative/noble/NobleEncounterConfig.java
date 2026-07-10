package com.thecompanyinc.cobblemoninitiative.noble;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;

/**
 * A single noble-encounter definition, deserialized from
 * {@code data/cobblemon_initiative/noble_encounters/<id>.json}.
 *
 * <p>Pure data (mirrors {@link com.thecompanyinc.cobblemoninitiative.shrine.ShrineChallengeConfig}).
 * The reusable engine reads these fields; a new noble is a new JSON file + an Easy NPC
 * preset, with no code change. Per-attack parameters ride an opaque {@link JsonObject} so
 * new attack types need no schema change here — the attack handler pulls out the keys it
 * needs.
 */
public class NobleEncounterConfig {

  private String id;
  private String displayName = "";
  /** Encounter type: "boss" (combat wear-down) or "chase" (friendly flee-and-tag task). */
  private String type = "boss";
  /** Element theme key (see {@link ElementTheme}) — drives particles/damage/effect/sound. */
  private String element = "fire";
  /** Easy NPC preset location for the Phase-1 body, e.g. {@code easy_npc:preset/cobblemon/noble_groudon.npc.snbt}. */
  private String bodyPreset;
  /** Unique vanilla tag baked into the preset so the manager can find the spawned body. */
  private String bodyTag;
  /** {@code PokemonProperties} string for the REAL Cobblemon spawned in Phase 2 (the catch battle). */
  private String battleSpecies;

  private String startTitle = "";
  private String startSubtitle = "";
  private int introSeconds = 4;

  private Arena arena = new Arena();
  /** Ambient arena theme key (see {@link AmbientTheme}); nullable = none. */
  private String ambientTheme;
  /** Flyer profile; null = grounded (always meleeable). */
  private Flyer flyer;
  private Stagger stagger = new Stagger();
  private Phase1 phase1 = new Phase1();
  /** Present only for {@code type == "chase"}: the flee-and-tag task tuning. */
  private Chase chase;
  private Phase2 phase2 = new Phase2();
  private Rewards rewards;

  /** Optional Phase-1→Phase-2 transition title (defaults to "§e§lSTAGGERED"). */
  private String staggerTitle;
  private String staggerSubtitle;
  private String completeTitle = "";
  private String completeSubtitle = "";
  private Sounds sounds = new Sounds();

  // ── Nested holders ────────────────────────────────────────────────────────────

  public static class Arena {
    /** Ring center [x, y, z]. */
    public int[] center;
    public int radius = 20;
    public String dimension = "minecraft:overworld";
    /** Optional ring particle override (a simple particle id, e.g. {@code minecraft:enchant});
     * null = the element's colored dust. */
    public String boundaryParticle;
  }

  /** Present only for flying nobles: hover + descend-to-meleeable rhythm. */
  public static class Flyer {
    /** Blocks above arena floor the noble hovers while airborne. */
    public double hoverHeight = 8.0;
    /** Ticks the noble stays low + meleeable (the punish window). */
    public int groundedWindowTicks = 120;
    /** Ticks the noble stays airborne between grounded windows. */
    public int airTicks = 200;
  }

  public static class Stagger {
    /** Phase 2 triggers when body health drops to this fraction of its max. */
    public float staggerAtHealthFraction = 0.15f;
    /** {@code net.minecraft.world.BossEvent.BossBarColor} name. */
    public String bossBarColor = "RED";
    /** {@code net.minecraft.world.BossEvent.BossBarOverlay} name. */
    public String bossBarOverlay = "NOTCHED_10";
    /**
     * Stagger cinematic variant: null = default collapse cocoon; "rebirth" = the Moltres
     * ember-collapse fake-out; "gotcha" = the friendly chase freeze-frame (auto for
     * {@code type == "chase"}).
     */
    public String script;
  }

  public static class Phase1 {
    public List<Attack> attacks = Collections.emptyList();
    /** Ticks between any two attacks (the global telegraph-spacing gap). */
    public int attackGapTicks = 25;
    /**
     * Body-health fractions (descending) at which the noble enrages — roar, boss-bar tint,
     * tighter attack cadence. Null = the engine default {0.6, 0.3}.
     */
    public double[] rageThresholds;
  }

  /** Friendly "chase Mew" task: the noble flees; the player must tag it {@code tagsRequired} times. */
  public static class Chase {
    /** Tags needed to tire it out → Phase 2. */
    public int tagsRequired = 5;
    /** It flees when the player is within this horizontal radius. */
    public double fleeRadius = 5.0;
    /** A tag registers when the player reaches within this radius. */
    public double touchRadius = 2.0;
    /** Flee step per tick (blocks). */
    public double fleeSpeed = 0.32;
    /** Ticks of grace after a tag before another can register. */
    public int tagCooldownTicks = 25;
    /** Blocks above the arena floor it hovers. */
    public double hoverHeight = 2.0;
    /** Per-tick chance to blink-teleport while fleeing (Mew's signature warp). */
    public double blinkChance = 0.02;
    /** Blink hop distance. */
    public double blinkRange = 7.0;
  }

  /** One entry in the Phase-1 attack rotation. */
  public static class Attack {
    /** Primitive id: projectile | barrage_aoe | beam | hazard_zone | bolt_strike | dive_charge | stomp. */
    public String type;
    /** Ticks between uses of this attack. */
    public int cooldownTicks = 60;
    /** Opaque per-attack parameters (read by the primitive handler). */
    public JsonObject params;
  }

  public static class Phase2 {
    /** Full-heal the player's party the instant the catch battle opens. */
    public boolean healPartyBeforeBattle = true;
  }

  public static class Rewards {
    public StoryFlag storyFlag;
    /** Advancement path granted on completion, e.g. {@code cobblemon_initiative:nobles/groudon}. */
    public String achievement;
    /** Commands run on completion, with {@code {player}}/{@code {uuid}} substitution. */
    public List<String> commands;
    /** Outcome-specific overrides: run INSTEAD of {@code commands} when present. A caught
     * noble can pay differently from a knocked-out one (capture-or-lose-it wager). */
    public List<String> commandsOnCapture;
    public List<String> commandsOnDefeat;
  }

  public static class StoryFlag {
    public String objective;
    public String holder = "@s";
    public int value = 1;
  }

  public static class Sounds {
    public String start;
    public String stagger;
    public String complete;
    /** Phase-1 boss-music loop (a long vanilla track id, played on HOSTILE). Null = none. */
    public String loop;
    /** Declared duration of {@code loop} in seconds — the server cannot read ogg length. */
    public float loopSeconds;
    /** Species-cry override; default derives {@code cobblemon:pokemon.<species>.cry}
     * from the first battleSpecies token. */
    public String cry;
    /** Pre-encounter approach-cue override (used once arenas are placed). */
    public String approach;
    /** Play the global wither-spawn horn when Phase 1 goes live (weather-trio gravitas). */
    public boolean hornOnStart;
    /** Nullable per-cue overrides, multiplied into the global sfxVolume/sfxPitch knobs. */
    public Float startVolume;
    public Float startPitch;
    public Float staggerPitch;
    public Float completePitch;
  }

  // ── Getters ─────────────────────────────────────────────────────────────────

  public String getId() { return id; }
  public String getDisplayName() { return displayName; }
  public String getType() { return type == null ? "boss" : type; }
  public String getElement() { return element; }
  public String getBodyPreset() { return bodyPreset; }
  public String getBodyTag() { return bodyTag; }
  public String getBattleSpecies() { return battleSpecies; }
  public String getStartTitle() { return startTitle; }
  public String getStartSubtitle() { return startSubtitle; }
  public int getIntroSeconds() { return introSeconds; }
  public Arena getArena() { return arena; }
  public String getAmbientTheme() { return ambientTheme; }
  public Flyer getFlyer() { return flyer; }
  public boolean isFlyer() { return flyer != null; }
  public Stagger getStagger() { return stagger; }
  public Phase1 getPhase1() { return phase1; }
  public Chase getChase() { return chase; }
  public Phase2 getPhase2() { return phase2; }
  public Rewards getRewards() { return rewards; }
  public String getStaggerTitle() { return staggerTitle; }
  public String getStaggerSubtitle() { return staggerSubtitle; }
  public String getCompleteTitle() { return completeTitle; }
  public String getCompleteSubtitle() { return completeSubtitle; }
  public Sounds getSounds() { return sounds; }
}

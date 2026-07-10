package com.thecompanyinc.cobblemoninitiative.cutscene;

import java.util.List;

/**
 * A data-driven cutscene, loaded (lazily, by id) from
 * {@code data/cobblemon_initiative/cutscenes/<id>.json}. A scene is a scripted spectator
 * camera path plus timed cues; the player is dropped into spectator, a marker "camera rig"
 * is flown along the keyframes, and the player is restored to survival at their exact
 * pre-scene transform when it ends (or is skipped). Adding a new scene is a JSON file — no
 * code — the same "add a JSON" ergonomics as trainers and noble encounters.
 *
 * Field names are the JSON keys (Gson, camelCase — matching {@code NobleEncounterConfig}).
 */
public class CutsceneScript {

  /** Filled from the file id at load; JSON may omit it. */
  public String id;

  /** Dimension the scene plays in. Cutscenes are single-dimension (rig + double live in it). */
  public String dimension = "minecraft:overworld";

  /** Whether {@code /cutscene skip} (or the bound key) may cut the scene short. */
  public boolean skippable = true;

  /** If true, keyframe {@code x,y,z} are OFFSETS from the player's eye at play time (yaw/pitch
   * stay absolute) — lets a scene (or a portable demo) be authored relative to the player. */
  public boolean relative = false;

  /**
   * Optional Easy NPC humanoid preset for a body-double stand-in dropped at the player's
   * spot so the scene has a "you" in frame. Null = no double (most scenes — rifts, boss
   * intros — just pan the camera). For a player-skinned double in this single-player mod,
   * author the preset with {@code SkinType:"PLAYER_SKIN"} + the player's Name baked in.
   */
  public String doublePreset;

  /** Optional [x,y,z] for the double; null = the player's position at play time. */
  public double[] doublePos;

  /** Optional fake weather for the scene: DOWNPOUR | THUNDERSTORM | CLEAR (else real sky). */
  public String ambientWeather;

  /** Optional title card shown on the first tick. */
  public String startTitle;
  public String startSubtitle;

  /** The camera path. Each keyframe is a target the rig eases TO from the previous point
   * (the first eases from the player's eye) over {@code ticks} server ticks. */
  public List<Keyframe> keyframes;

  /** Timed events fired when {@code tickCount == tick} (title/sound/command). */
  public List<Cue> cues;

  /** A camera waypoint. */
  public static class Keyframe {
    public double x, y, z;
    public float yaw, pitch;
    /** Ticks to ease from the previous keyframe to this one (min 1). */
    public int ticks = 20;
  }

  /** A scheduled beat. Any subset of {title, sound, command} may be set. */
  public static class Cue {
    public int tick;
    public String title;
    public String subtitle;
    /** A sound id, e.g. {@code minecraft:entity.ender_dragon.growl}. */
    public String sound;
    public Float volume;
    public Float pitch;
    /** A command run as the player (perm 4) — e.g. set a tag, summon, start the fight. */
    public String command;
  }
}

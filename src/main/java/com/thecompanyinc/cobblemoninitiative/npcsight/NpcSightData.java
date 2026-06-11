package com.thecompanyinc.cobblemoninitiative.npcsight;

import java.util.UUID;

public class NpcSightData {

  public UUID uuid;

  /**
   * Per-entity sight range in blocks. -1 means fall back to the global
   * default configured in {@link NpcSightConfig}.
   */
  public int sightRange = -1;

  /**
   * Easy NPC dialog identifier to open when the NPC sees a player within
   * {@link NpcSightConfig#getDialogRange()} blocks. {@code null} or blank
   * disables the dialog trigger entirely.
   */
  public String dialogName = null;

  /**
   * Behaviour when the NPC sees a player:
   * <ul>
   *   <li>{@code DIALOG} (default) — open {@link #dialogName} once per "seen session"
   *       (the original behaviour).</li>
   *   <li>{@code PURSUE} — walk toward the player while it can see them (sight-gated
   *       follow); the actual battle is driven by the preset's {@code ON_DISTANCE_TOUCH}
   *       action. Used for trainers and forced villains.</li>
   *   <li>{@code APPROACH_ONCE} — the first time it ever sees the player, walk up and open
   *       {@link #dialogName} once, then never auto-approach again (persisted via
   *       {@link #fired}). Used for one-time story characters (e.g. Mom).</li>
   * </ul>
   */
  public String mode = MODE_DIALOG;

  public static final String MODE_DIALOG = "DIALOG";
  public static final String MODE_PURSUE = "PURSUE";
  public static final String MODE_APPROACH_ONCE = "APPROACH_ONCE";

  /**
   * Persistent one-shot latch for {@link #MODE_APPROACH_ONCE}: set true once the NPC has
   * completed its single approach-and-talk, so it never auto-approaches again.
   */
  public boolean fired = false;

  /**
   * Optional player tag added to the player when an {@link #MODE_APPROACH_ONCE} completes
   * (e.g. {@code met_mom}), so the preset can switch to post-meeting dialog via a native
   * {@code PLAYER_TAG} dialog condition. {@code null}/blank disables.
   */
  public String meetTag = null;

  /**
   * Optional "stand-down" player tag: when the nearest player carries this tag the NPC will
   * not pursue or engage (e.g. {@code defeated_<trainer_id>} so a beaten trainer stops
   * chasing). {@code null}/blank disables.
   */
  public String stopTag = null;

  // ----- transient runtime state (not persisted) -----

  /** True when the NPC can see at least one player this tick cycle. */
  public transient boolean canSeePlayer = false;

  /**
   * True once the dialog has been triggered for the current "seen session".
   * Resets to false when the NPC loses sight, allowing one trigger per session.
   */
  public transient boolean dialogFiredThisSession = false;

  /**
   * True while a {@code set follow player} objective is currently asserted on the NPC, so the
   * manager only issues the set/remove commands on sight transitions rather than every tick.
   * Transient: a follow left set across a reload self-corrects after the next sight cycle.
   */
  public transient boolean pursuing = false;

  public NpcSightData() {}

  public NpcSightData(UUID uuid, int sightRange, String dialogName) {
    this.uuid = uuid;
    this.sightRange = sightRange;
    this.dialogName = dialogName;
  }

  /**
   * Returns the effective sight range, substituting the global default when
   * this entity has no override.
   */
  public int getEffectiveSightRange(int globalDefault) {
    return sightRange > 0 ? sightRange : globalDefault;
  }

  public boolean hasDialog() {
    return dialogName != null && !dialogName.isBlank();
  }

  /** Effective mode, upper-cased and defaulting to {@link #MODE_DIALOG} when unset/blank. */
  public String effectiveMode() {
    return (mode == null || mode.isBlank()) ? MODE_DIALOG : mode.toUpperCase(java.util.Locale.ROOT);
  }

  public boolean hasMeetTag() {
    return meetTag != null && !meetTag.isBlank();
  }

  public boolean hasStopTag() {
    return stopTag != null && !stopTag.isBlank();
  }
}

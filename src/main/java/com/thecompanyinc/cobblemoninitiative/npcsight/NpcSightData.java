package com.thecompanyinc.cobblemoninitiative.npcsight;

import java.util.UUID;

public class NpcSightData {

  public UUID uuid;

  /** How this NPC behaves regarding player detection. */
  public SightMode mode = SightMode.STATIONARY;

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

  // ----- transient runtime state (not persisted) -----

  /** True when the NPC can see at least one player this tick cycle. */
  public transient boolean canSeePlayer = false;

  /** Epoch-ms timestamp of the last dialog open, used for cooldown. */
  public transient long lastDialogTriggerMs = 0L;

  public enum SightMode {
    /** NPC looks in a fixed direction; sight detection uses its current yaw. */
    STATIONARY,
    /** NPC head rotates to track the nearest in-range player. */
    TRACKING
  }

  public NpcSightData() {}

  public NpcSightData(UUID uuid, SightMode mode, int sightRange, String dialogName) {
    this.uuid = uuid;
    this.mode = mode;
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
}

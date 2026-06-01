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

  // ----- transient runtime state (not persisted) -----

  /** True when the NPC can see at least one player this tick cycle. */
  public transient boolean canSeePlayer = false;

  /**
   * True once the dialog has been triggered for the current "seen session".
   * Resets to false when the NPC loses sight, allowing one trigger per session.
   */
  public transient boolean dialogFiredThisSession = false;

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
}

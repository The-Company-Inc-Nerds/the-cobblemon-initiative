package com.thecompanyinc.cobblemoninitiative.npcsight;

import java.util.UUID;

/**
 * A TAG-keyed sight registration (round 13e) — the automatic alternative to the
 * uuid-keyed {@link NpcSightData}. Placement-spawned NPCs get a random uuid at spawn, so
 * they cannot be pre-registered by uuid; instead the compiler emits one profile per sight
 * tag (from characters that have a {@code sight} block + {@code entity_tags} but no uuid).
 * At runtime {@link NpcSightManager} discovers any live entity carrying {@link #tag} and
 * processes it with a session {@link NpcSightData} built from this template — no manual
 * {@code npcsight add} step per world.
 *
 * <p>Loaded from the jar resource {@code data/cobblemon_initiative/npcsight_profiles.json}.
 */
public class NpcSightProfile {

  /** Entity scoreboard tag every matching NPC carries (e.g. takehara_sentry, auditor). */
  public String tag;
  /** Sight range in blocks; -1 = the global default. */
  public int range = -1;
  /** Behaviour mode: PASSIVE (scoreboard only), DIALOG, PURSUE, APPROACH_ONCE. */
  public String mode = NpcSightData.MODE_PASSIVE;
  /** Dialog to open (DIALOG/APPROACH_ONCE modes only). */
  public String dialog = null;
  /** Player tag added when an APPROACH_ONCE meeting completes. */
  public String meetTag = null;
  /** Player tag that stands the NPC down (stops PURSUE / re-approach). */
  public String stopTag = null;

  public NpcSightProfile() {}

  /** Build a per-entity session data object from this template. */
  public NpcSightData toData(UUID uuid) {
    NpcSightData d = new NpcSightData();
    d.uuid = uuid;
    d.sightRange = range;
    d.mode = mode;
    d.dialogName = dialog;
    d.meetTag = meetTag;
    d.stopTag = stopTag;
    return d;
  }
}

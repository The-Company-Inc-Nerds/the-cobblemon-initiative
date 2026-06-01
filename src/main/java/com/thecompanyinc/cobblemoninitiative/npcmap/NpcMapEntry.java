package com.thecompanyinc.cobblemoninitiative.npcmap;

import java.util.UUID;

public class NpcMapEntry {

  public UUID uuid;
  public String preset;   // full resource location: "cobblemon_initiative:humanoid/cyber_leader"
  public String label;    // optional human-readable label for the generated function comments

  public NpcMapEntry() {}

  public NpcMapEntry(UUID uuid, String preset, String label) {
    this.uuid   = uuid;
    this.preset = preset;
    this.label  = label != null ? label : "";
  }
}

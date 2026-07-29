# repairs wave a23 — arm: 0.7.0-alpha.6 playtest wave.
# Scope:
#   * Gaviota Center/podium swap: Nurse Marina's uuid tp'd from the old Center (560/3540) onto the
#     podium (585/3435) — the a22 civilian Lucia that stood there is killed + re-latched to the docks
#     (474/3552) FIRST, order matters (a21 nurse precedent).
#   * Daycare: Alessia (pen 2) + Paolo (pen 1) re-roled to pen keepers + made stationary — their uuid
#     bodies are tp'd onto their pens (were ambient_wander).
#   * Zwiggo re-couple: the a22 quay-latch swampert (now summon-only) is killed (unless already
#     recruited); Deckhand Rocco re-latched to the new spot (405/3499).
#   * (Renato -> gaviota_guide is a uuid re-cast — refreshes via update_npc_presets, no repairs.)
scoreboard players set #repair_a23 ci_ambient 1
forceload add 555 3430 592 3546
forceload add 545 3470 560 3510
forceload add 400 3495 412 3504
forceload add 470 3548 480 3556
schedule function cobblemon_initiative:install/repairs_a23_apply 3s

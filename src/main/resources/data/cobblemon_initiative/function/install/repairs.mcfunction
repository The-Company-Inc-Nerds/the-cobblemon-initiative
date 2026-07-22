# One-shot world repairs for content that moved AFTER a world already latched it.
# Dispatched by `/cobblemon-initiative install run` (and safe to run by hand); each
# wave guards itself with a #repair_* flag in ci_ambient so it applies exactly once
# per world. Pattern per wave: arm (forceload the affected sites + schedule) → apply
# (kill the stale bodies with chunks live, reset their latches, unload). Latches then
# respawn the bodies at the CURRENT authored coords the next time a player visits.

# ── wave a2 (0.6.0-alpha.2): Takehara greenhouse cast, Sango auditor leashes,
#    mew-wisp giver + Oasis pump crew (all moved; old bodies stale or buried)
execute unless score #repair_a2 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a2_arm

# ── wave a5 (0.6.0-alpha.5): noble monuments moved to their (alpha.4-relocated)
#    arenas — kyogre buoy → Mystic Island, rayquaza altar → Sky Ring, groudon stone
#    → the real south rim (old site ~200 blocks inside the volcano); plus a sweep of
#    phase-1 noble bodies leaked by the failed distant-arena starts
execute unless score #repair_a5 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a5_arm

# ── wave a6 (0.6.0-alpha.6): gyms-3-7 spec-cast ground-probe repositioning (44
# NPCs; see repairs_a6_arm for scope) ──
execute unless score #repair_a6 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a6_arm

# ── wave a7 (0.6.0-alpha.6): shrine cultist + noble-giver ground-probe repositioning ──
execute unless score #repair_a7 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a7_arm

# ── wave a8 (0.6.0-alpha.7): shrine cultists retired (structure ruling) ──
execute unless score #repair_a8 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a8_arm

# ── wave a9 (0.6.0-alpha.9): skin dress pass repaint — 99 latch-placed civilians/props
#    gained authored skins (12 new trainer_textures); stale undressed bodies killed +
#    latches re-armed so they re-spawn dressed (coords unchanged) ──
execute unless score #repair_a9 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a9_arm

# ── wave a10 (0.6.0-alpha.10): dialog cohesion pass — latch-placed cast re-latched so
#    punched-up dialog reaches already-spawned bodies (uuid'd NPCs refresh via preset hash) ──
execute unless score #repair_a10 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a10_arm

# ── wave a13 (0.6.0-alpha.13): duplicate cobblemon-model companions — a10 killed the six
#    Cobblemon-model companions (Mimi/Jackpot/Coins/Bobber/Cloud/Pip) with the WRONG
#    easy_npc type (humanoid, not cobblemon_npc) so the kill no-opped while the latch
#    reset → a second body spawned. Re-kill by the correct type (name + entity-tag) and
#    re-latch so each respawns as a single body ──
execute unless score #repair_a13 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a13_arm

# ── wave a14 (0.6.0-alpha.14): companion dupes STILL live — a13's `name="Mimi"` kills
#    no-opped (Easy NPC names are JSON components, not bare strings), so stale bodies
#    survived and a13's re-arm added another. Re-clear by PROXIMITY + type (name-agnostic,
#    catches 1/2/3 copies) and re-latch to a single body ──
execute unless score #repair_a14 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a14_arm

# ── wave a15 (0.6.0-alpha.15): companion dupes, FINAL sweep — a14 was one-shot and its
#    3s-scheduled kill no-opped in saves where the home chunk was still unloaded, so two
#    Mimis / two Jackpots persist. Re-sweep by proximity+type under a fresh guard; ships
#    with the recompiled placement latches whose new `unless entity` tick guard +
#    kill-before-import place fn make a re-dupe impossible (the last companion repair) ──
execute unless score #repair_a15 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a15_arm

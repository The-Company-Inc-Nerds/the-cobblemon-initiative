# repairs wave a5 — arm: mark done, load every affected site, schedule the apply
# pass 3s out (forceloaded chunks need a tick or two before their ENTITIES exist).
# Scope: the three noble monument NPCs moved to their (relocated) arenas in alpha.5
# — the alpha.4 arena moves left the monuments 1,037b (kyogre) / 3,969b (rayquaza)
# from their arenas and groudon's whole site ~200 blocks inside the volcano — plus a
# defensive sweep for phase-1 noble bodies leaked into unloaded chunks by failed
# starts (spawnBody imports at the arena center; teardown only sweeps loaded chunks).
scoreboard players set #repair_a5 ci_ambient 1

# old kyogre buoy (Gullwing pier) + old kyogre arena (pre-alpha.4 center 700,62,3330)
forceload add 640 3285 670 3315
forceload add 685 3315 715 3345
# old rayquaza altar on the Ryujin spire (== the old arena center)
forceload add 2141 869 2171 899
# old groudon monument + old arena (same column, y110/y70 — both inside the mountain)
forceload add 3790 3731 3820 3761
# NEW kyogre + rayquaza arena centers (failed alpha.4 starts may have left bodies here)
forceload add 231 2332 261 2362
forceload add 717 4574 747 4604

schedule function cobblemon_initiative:install/repairs_a5_apply 60t
tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Applying one-time world repairs (noble monuments)…","color":"gray"}]

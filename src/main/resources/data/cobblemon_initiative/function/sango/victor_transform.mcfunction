# Victor's reveal — the silent apprentice was Victini all along.
# Called from ambient/tick, executed POSITIONED AT the humanoid Victor (tag
# victor_apprentice) the tick after a qualified player tags themselves
# victor_transformed. Spawn the Victini form at his exact spot, flourish, then
# despawn the humanoid. Guarded upstream so this fires exactly once.
easy_npc preset import_new data easy_npc:preset/humanoid/victor_victini.npc.snbt ~ ~ ~
particle minecraft:end_rod ~ ~1 ~ 0.3 0.7 0.3 0.04 80 force
particle minecraft:flame ~ ~0.6 ~ 0.4 0.4 0.4 0.02 40 force
playsound minecraft:block.beacon.activate master @a[distance=..40] ~ ~ ~ 1 1.4
playsound minecraft:entity.player.levelup master @a[distance=..40] ~ ~ ~ 1 1.6
kill @e[tag=victor_apprentice,type=!minecraft:player]

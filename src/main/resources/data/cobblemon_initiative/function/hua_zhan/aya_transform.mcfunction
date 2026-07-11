# Aya's reveal — the groundskeeper by the west stair was the gym leader all along.
# Called from ambient/tick, executed AS the challenger and POSITIONED AT the
# groundskeeper body (tag aya_groundskeeper) the tick after a challenger who cleared
# all four garden wardens tags themselves aya_transformed via her dialog. Spawn
# Leader Blossom at her exact spot, flourish in bloom, then despawn the groundskeeper.
# Guarded upstream so this fires exactly once (unless entity hz_leader_body).
# The reveal cutscene locks the camera on the spot while the swap happens this tick
# (@s = the player; a skipped scene changes nothing — the swap already ran).
cutscene play blossom_reveal
easy_npc preset import_new data easy_npc:preset/humanoid/hua_zhan_leader.npc.snbt ~ ~ ~
particle minecraft:happy_villager ~ ~1 ~ 0.4 0.8 0.4 0.2 80 force
particle minecraft:composter ~ ~0.6 ~ 0.5 0.5 0.5 0.1 60 force
playsound minecraft:block.beacon.activate master @a[distance=..40] ~ ~ ~ 1 1.2
playsound minecraft:block.grass.break master @a[distance=..40] ~ ~ ~ 1 0.8
kill @e[tag=aya_groundskeeper,type=!minecraft:player]

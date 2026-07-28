# Aya's reveal — the groundskeeper by the west stair was the gym leader all along.
# Called from ambient/tick, executed AS the challenger and POSITIONED AT the
# groundskeeper body (tag aya_groundskeeper) the tick after a challenger who cleared
# all four garden wardens tags themselves aya_transformed via her dialog. Spawn
# Leader Blossom at her battle spot, flourish in bloom, then despawn the groundskeeper.
# Guarded upstream so this fires exactly once (unless entity hz_leader_body).
# alpha.26 playtest P1: Blossom now spawns ABSOLUTE at her battle spot 1381.2 93 2047.4
# (was ~ ~ ~ = the groundskeeper spot 1381.5 93 2060) so the engage battle opens where
# the re-aimed cutscenes frame her. That spot is ~13 blocks (-z) from the executor at the
# groundskeeper body — well inside the cutscene subject forceload radius (4 chunks), so
# the chunk-hold guarantee below still covers her.
# Spawn Leader Blossom and despawn the groundskeeper FIRST, while the executor is still
# positioned here — BEFORE `cutscene play` drops the player to spectator and teleports
# them off. Running the swap first means her body lands at the stable spot in a held
# chunk (not drifting), so her `hz_leader_body` tag resolves for the later engage;
# spawning after the camera moved left her in the wrong place / an unloaded chunk = the
# "Leader Blossom is not attached to an entity" bug. The reveal then pans onto the standing leader.
easy_npc preset import_new data easy_npc:preset/humanoid/hua_zhan_leader.npc.snbt 1381.2 93 2047.4
kill @e[tag=aya_groundskeeper,type=!minecraft:player]
particle minecraft:happy_villager ~ ~1 ~ 0.4 0.8 0.4 0.2 80 force
particle minecraft:composter ~ ~0.6 ~ 0.5 0.5 0.5 0.1 60 force
playsound minecraft:block.beacon.activate master @a[distance=..40] ~ ~ ~ 1 1.2
playsound minecraft:block.grass.break master @a[distance=..40] ~ ~ ~ 1 0.8
cutscene play blossom_reveal

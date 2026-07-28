# Deepcore City gym ladder — per-player count of defeated gym trainers. Registered in
# #minecraft:tick (tags/function/tick.json), objective added in gym/load. Recomputed
# from scratch every tick (band_tags precedent — cheap, tag reads only) from the
# defeated_deepcore_trainer_1..4 battle-onwin tags. The compiler lowers the dialog score
# gates {score: deepcore_tower gte 1|2|4} to band tags deepcore_tower_gte_1/2/4 in
# dialog/band_tags, which weaken Jr. Apprentice / Apprentice / Leader respectively.
scoreboard players set @a deepcore_tower 0
execute as @a[tag=defeated_deepcore_trainer_1] run scoreboard players add @s deepcore_tower 1
execute as @a[tag=defeated_deepcore_trainer_2] run scoreboard players add @s deepcore_tower 1
execute as @a[tag=defeated_deepcore_trainer_3] run scoreboard players add @s deepcore_tower 1
execute as @a[tag=defeated_deepcore_trainer_4] run scoreboard players add @s deepcore_tower 1
# ALPHA.27 floor stand-down (Bruno two-track): dc_floor_N_down = the player has
# defeated floor master N OR chose the pit-only route (dc_track_pit from Bruno's
# choice entry). The floor trainers' sight.stop_tag points at these derived tags
# (npcsight stopTag is a SINGLE player tag — the OR lives here, not in the already
# 4-condition forced-battle gates). Recomputed from scratch every tick, same style
# as the tower count above.
tag @a remove dc_floor_1_down
tag @a remove dc_floor_2_down
tag @a remove dc_floor_3_down
tag @a remove dc_floor_4_down
execute as @a[tag=defeated_deepcore_trainer_1] run tag @s add dc_floor_1_down
execute as @a[tag=defeated_deepcore_trainer_2] run tag @s add dc_floor_2_down
execute as @a[tag=defeated_deepcore_trainer_3] run tag @s add dc_floor_3_down
execute as @a[tag=defeated_deepcore_trainer_4] run tag @s add dc_floor_4_down
execute as @a[tag=dc_track_pit] run tag @s add dc_floor_1_down
execute as @a[tag=dc_track_pit] run tag @s add dc_floor_2_down
execute as @a[tag=dc_track_pit] run tag @s add dc_floor_3_down
execute as @a[tag=dc_track_pit] run tag @s add dc_floor_4_down

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
# a22 PVP (playtest N5-N10): dc_pit_ready = the challenger may face the pit now. TRUE if they
# took the pit-only route (dc_track_pit), OR the whole dojo (dc_track_full) AND all four floor
# masters are down (deepcore_tower gte 4). gym/deepcore_pvp_tick reads this single derived tag to
# raise Striker on approach, so the OR stays out of the raise line. Recomputed each tick, same
# style as the tower count above. (The alpha.27 dc_floor_N_down stand-down is GONE: the floor
# masters no longer pursue/force a battle — they are passive flavor bodies until the whole-dojo
# gauntlet spawns the hostile duel bodies, so nothing needs standing down for the pit route.)
tag @a remove dc_pit_ready
execute as @a[tag=dc_track_pit] run tag @s add dc_pit_ready
execute as @a[tag=dc_track_full] if score @s deepcore_tower matches 4.. run tag @s add dc_pit_ready

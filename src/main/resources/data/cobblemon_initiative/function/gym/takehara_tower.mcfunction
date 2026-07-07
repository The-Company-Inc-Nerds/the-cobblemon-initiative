# Takehara tower ladder — per-player count of defeated tower trainers. Registered in
# #minecraft:tick (tags/function/tick.json), objective added in gym/load. Recomputed
# from scratch every tick (band_tags precedent — cheap, tag reads only) from the
# defeated_takehara_trainer_1..4 battle-onwin tags (Koji/Yuki/Shin/Taro). Sora's dialog
# gates on {score: takehara_tower gte 2}, which the compiler lowers to the
# takehara_tower_gte_2 band tag in dialog/band_tags — that reads this objective.
scoreboard players set @a takehara_tower 0
execute as @a[tag=defeated_takehara_trainer_1] run scoreboard players add @s takehara_tower 1
execute as @a[tag=defeated_takehara_trainer_2] run scoreboard players add @s takehara_tower 1
execute as @a[tag=defeated_takehara_trainer_3] run scoreboard players add @s takehara_tower 1
execute as @a[tag=defeated_takehara_trainer_4] run scoreboard players add @s takehara_tower 1

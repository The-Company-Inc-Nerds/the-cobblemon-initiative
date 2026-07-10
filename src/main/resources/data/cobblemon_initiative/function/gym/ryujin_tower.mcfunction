# Ryujin Keep gym ladder — per-player count of defeated gym trainers. Registered in
# #minecraft:tick (tags/function/tick.json), objective added in gym/load. Recomputed
# from scratch every tick (band_tags precedent — cheap, tag reads only) from the
# defeated_ryujin_trainer_1..4 battle-onwin tags. The compiler lowers the dialog score
# gates {score: ryujin_tower gte 1|2|4} to band tags ryujin_tower_gte_1/2/4 in
# dialog/band_tags, which weaken Jr. Apprentice / Apprentice / Leader respectively.
scoreboard players set @a ryujin_tower 0
execute as @a[tag=defeated_ryujin_trainer_1] run scoreboard players add @s ryujin_tower 1
execute as @a[tag=defeated_ryujin_trainer_2] run scoreboard players add @s ryujin_tower 1
execute as @a[tag=defeated_ryujin_trainer_3] run scoreboard players add @s ryujin_tower 1
execute as @a[tag=defeated_ryujin_trainer_4] run scoreboard players add @s ryujin_tower 1

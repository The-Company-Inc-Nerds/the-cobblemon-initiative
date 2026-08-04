# Hand the recovered deed to Ossa. Run as @s = the player (report button). Consume-probe the
# token (custom_data-tagged paper), retire the site, pay the FIXED records rate (350 CD via
# economy/payout — REPEATABLE, so no training pack, randomness only on the dig loot itself).
# FIRST recovery carries the two-seal founder beat that used to live on boundary stone 2 —
# the beat survives the quest swap (+ the one-time npc_gift/kalahar_ground that filing paid).
execute store result score #cache_t ci_cache run clear @s minecraft:paper[minecraft:custom_data~{ci_reach_cache:1b}] 0
execute if score #cache_t ci_cache matches ..0 run title @s actionbar [{"text":"You have not brought back a cache deed. The shimmer marks the dig.","color":"red"}]
execute if score #cache_t ci_cache matches ..0 run return 0
clear @s minecraft:paper[minecraft:custom_data~{ci_reach_cache:1b}] 1
function cobblemon_initiative:sidequest/reach_cache/clean with storage cobblemon_initiative:cache active
scoreboard players set #cache_site ci_cache 0
tag @s remove reach_cache_active
scoreboard players add @s ci_cache 1
function cobblemon_initiative:economy/payout {amount:350}
execute if entity @s[tag=!reach_cache_first_done] run loot give @s loot cobblemon_initiative:npc_gift/kalahar_ground
execute if entity @s[tag=!reach_cache_first_done] run title @s title [{"text":"TWO SEALS","color":"gold"}]
execute if entity @s[tag=!reach_cache_first_done] run title @s subtitle [{"text":"the Company mark - and an older hand beneath it","color":"gray"}]
execute if entity @s[tag=!reach_cache_first_done] run tellraw @s [{"text":"The deed carries two seals. The Company stamp - and beneath it a personal mark that makes Ossa go very quiet. She files the deed face down and does not meet your eye.","color":"gray"}]
execute if entity @s[tag=!reach_cache_first_done] run playsound minecraft:block.amethyst_block.resonate master @s ~ ~ ~ 0.8 0.6
tag @s add reach_cache_first_done

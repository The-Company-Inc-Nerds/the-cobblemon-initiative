# Quest reward — STANDARD tier, scaled by badge count (memory_fragment). Run AS the player.
execute if score @s memory_fragment matches ..2 run give @s cobblemon:exp_candy_s 2
execute if score @s memory_fragment matches ..2 run give @s cobblemon:exp_candy_m 1
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:exp_candy_m 2
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:exp_candy_l 1
execute if score @s memory_fragment matches 6..8 run give @s cobblemon:exp_candy_l 2
execute if score @s memory_fragment matches 9.. run give @s cobblemon:exp_candy_l 3

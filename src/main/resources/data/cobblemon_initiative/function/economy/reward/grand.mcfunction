# Quest reward — GRAND tier (finale), scaled by badge count (memory_fragment). Run AS the player.
execute if score @s memory_fragment matches ..2 run give @s cobblemon:rare_candy 1
execute if score @s memory_fragment matches ..2 run give @s cobblemon:exp_candy_l 1
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:rare_candy 1
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:exp_candy_xl 1
execute if score @s memory_fragment matches 6..8 run give @s cobblemon:rare_candy 2
execute if score @s memory_fragment matches 6..8 run give @s cobblemon:exp_candy_xl 1
execute if score @s memory_fragment matches 9.. run give @s cobblemon:rare_candy 3
execute if score @s memory_fragment matches 9.. run give @s cobblemon:exp_candy_xl 1

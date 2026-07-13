# Quest reward — MAJOR tier, scaled by badge count (memory_fragment). Run AS the player.
# Always includes one random vitamin (npc_gift/vitamin_random).
execute if score @s memory_fragment matches ..2 run give @s cobblemon:exp_candy_l 1
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:exp_candy_l 2
execute if score @s memory_fragment matches 6..8 run give @s cobblemon:exp_candy_xl 1
execute if score @s memory_fragment matches 9.. run give @s cobblemon:exp_candy_xl 2
loot give @s loot cobblemon_initiative:npc_gift/vitamin_random

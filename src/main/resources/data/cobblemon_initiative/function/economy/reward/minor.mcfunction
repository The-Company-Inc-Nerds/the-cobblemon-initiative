# Quest reward — MINOR tier, scaled by badge count (memory_fragment 0-10). Run AS the
# player. Candy QUALITY + count rise with era so a "small errand" pack stays useful at
# the higher level caps (an XS candy is dead weight by cap 80). Band 1 = today's contents.
execute if score @s memory_fragment matches ..2 run give @s cobblemon:exp_candy_xs 3
execute if score @s memory_fragment matches 3..5 run give @s cobblemon:exp_candy_s 3
execute if score @s memory_fragment matches 6..8 run give @s cobblemon:exp_candy_m 3
execute if score @s memory_fragment matches 9.. run give @s cobblemon:exp_candy_l 2

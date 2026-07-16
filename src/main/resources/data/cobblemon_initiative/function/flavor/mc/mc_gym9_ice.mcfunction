# §6 gym-9 Minecraft requirement — obtain blue/packed ice. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym9_done] run return 0
tag @s add flavor_mc_gym9_done
tag @s add mc_gym9_done
tellraw @s [{"text":"Ice that won't melt in your hand. ","color":"gold"},{"text":"Nifl's whiteout will not surprise someone who carries the cold.","color":"gray","italic":true}]

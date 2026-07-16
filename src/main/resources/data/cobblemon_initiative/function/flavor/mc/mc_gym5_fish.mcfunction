# §6 gym-5 Minecraft requirement — hook with fishing rod. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym5_done] run return 0
tag @s add flavor_mc_gym5_done
tag @s add mc_gym5_done
tellraw @s [{"text":"The harbor takes to you. ","color":"gold"},{"text":"A line in the water is its own kind of patience.","color":"gray","italic":true}]

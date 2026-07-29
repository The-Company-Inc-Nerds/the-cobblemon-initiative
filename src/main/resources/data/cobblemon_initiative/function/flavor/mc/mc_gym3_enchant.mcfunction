# §6 gym-3 Minecraft requirement — make mushroom stew (a marsh offering). Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym3_done] run return 0
tag @s add flavor_mc_gym3_done
tag @s add mc_gym3_done
tellraw @s [{"text":"The marsh smells the stew on you. ","color":"gold"},{"text":"The Fairy leader will accept your challenge now.","color":"gray","italic":true}]

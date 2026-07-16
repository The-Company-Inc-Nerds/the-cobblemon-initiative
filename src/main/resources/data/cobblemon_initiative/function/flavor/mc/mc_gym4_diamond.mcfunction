# §6 gym-4 Minecraft requirement — obtain diamond. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym4_done] run return 0
tag @s add flavor_mc_gym4_done
tag @s add mc_gym4_done
tellraw @s [{"text":"Deepcore only respects what you dug for. ","color":"gold"},{"text":"You dug.","color":"gray","italic":true}]

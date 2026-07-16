# §6 gym-7 Minecraft requirement — obtain lightning rod. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym7_done] run return 0
tag @s add flavor_mc_gym7_done
tag @s add mc_gym7_done
tellraw @s [{"text":"Current wants a path. You built it one. ","color":"gold"},{"text":"Cyber City runs on exactly this.","color":"gray","italic":true}]

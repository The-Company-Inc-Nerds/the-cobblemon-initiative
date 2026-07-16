# §6 gym-10 Minecraft requirement — obtain ancient debris/netherite scrap. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym10_done] run return 0
tag @s add flavor_mc_gym10_done
tag @s add mc_gym10_done
tellraw @s [{"text":"Debris from the deep hot dark. ","color":"gold"},{"text":"Scorchspire's banked coals are nothing next to where this came from.","color":"gray","italic":true}]

# §6 gym-6 Minecraft requirement — obtain obsidian. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym6_done] run return 0
tag @s add flavor_mc_gym6_done
tag @s add mc_gym6_done
tellraw @s [{"text":"Cooled deep-stone. The ground gave it up slowly. ","color":"gold"},{"text":"Something in Kalahar's sand remembers the heat.","color":"gray","italic":true}]

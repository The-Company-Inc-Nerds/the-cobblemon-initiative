# §6 gym-2 Minecraft requirement — obtain wheat. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym2_done] run return 0
tag @s add flavor_mc_gym2_done
tag @s add mc_gym2_done
tellraw @s [{"text":"You grew this yourself. The Company grows it to own you. ","color":"gold"},{"text":"Remember the difference.","color":"gray","italic":true}]

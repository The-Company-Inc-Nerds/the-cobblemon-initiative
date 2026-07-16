# §6 gym-1 Minecraft requirement — obtain honey bottle. Soft: acknowledge + tag.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym1_done] run return 0
tag @s add flavor_mc_gym1_done
tag @s add mc_gym1_done
tellraw @s [{"text":"The hive gives if you ask gently. ","color":"gold"},{"text":"Takehara's bugs respect a careful hand.","color":"gray","italic":true}]

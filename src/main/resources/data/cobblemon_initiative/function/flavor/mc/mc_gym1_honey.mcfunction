# §6 gym-1 Minecraft requirement — obtain a Glow Berry + a Spore Blossom (falls cave, mouth 2125 136 2703).
# Soft gate: fires only when both are held (advancement requirements-AND) and the gate config is on.
execute unless score #gym_mc_req ci_flavor matches 1.. run return 0
execute if entity @s[tag=flavor_mc_gym1_done] run return 0
tag @s add flavor_mc_gym1_done
tag @s add mc_gym1_done
tellraw @s [{"text":"Glow Berry and Spore Blossom, both from the deep of the falls cave. ","color":"gold"},{"text":"Takehara's woods reward a patient forager.","color":"gray","italic":true}]

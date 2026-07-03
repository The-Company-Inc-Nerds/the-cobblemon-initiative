# THE CUTTING contract reward: 250 CD (skew-aware) + 3x exp_candy_xs. Run as the player.
clear @s minecraft:coal 12
tag @s add work_mine_done
function cobblemon_initiative:economy/payout {amount:250}
give @s cobblemon:exp_candy_xs 3
playsound minecraft:entity.experience_orb.pickup master @s ~ ~ ~ 1 1
tellraw @s [{"text":"The Cutting closed. ","color":"green"},{"text":"250 CD and three candies from the crew stores. The lanterns burn another season.","color":"gray"}]

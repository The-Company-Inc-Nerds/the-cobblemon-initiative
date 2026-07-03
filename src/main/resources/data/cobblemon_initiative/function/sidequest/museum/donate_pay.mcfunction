# Museum bone donation — verified payout branch (player has 6+ bones). Run as the player.
clear @s minecraft:bone 6
function cobblemon_initiative:economy/payout {amount:400}
give @s cobblemon:poke_ball 2
tag @s add sq_museum_donation_done
tellraw @s [{"text":"The comparative anatomy case is complete. ","color":"gold"},{"text":"400 CD and two Poke Balls — the museum pays its finders fees.","color":"gray"}]
execute at @s run playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 1 1

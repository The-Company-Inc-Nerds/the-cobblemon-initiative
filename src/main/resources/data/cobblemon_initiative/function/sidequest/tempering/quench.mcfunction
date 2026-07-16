# SQ2 The Tempering - Hollis quenches the keepsake and pays. Run as the player (guarded by
# turn_in: count 8.. and not already done). Consume the 8 iron ingots, pay 400 via the skewed
# payout + a standard training gift + a tempered keepsake held item (cobblemon:expert_belt -
# jar-validated, used elsewhere in the pack). Then temper_blade_done arms the optional wager.
clear @s minecraft:iron_ingot 8
function cobblemon_initiative:economy/payout {amount:400}
function cobblemon_initiative:economy/reward/standard
give @s cobblemon:expert_belt 1
tag @s add temper_blade_done
title @s actionbar [{"text":"Tempered. ","color":"gold"},{"text":"Ready for the strike.","color":"gray"}]

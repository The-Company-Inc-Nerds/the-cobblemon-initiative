# Deep Restructuring - Kang reads the halved vein out loud and pays the union rate.
# Run as the player (guarded by turn_in_pages: count 6.. and not already done). Face 900 via
# the skewed payout (feels short at idx 32, per plot) + a standard training gift + a Black Belt
# held item (cobblemon:black_belt - jar-validated). No cd_instability change: the mine is not a
# field, only field liberation moves the meter; this quest REVEALS the plot, it does not free it.
clear @s minecraft:paper 6
function cobblemon_initiative:economy/payout {amount:900}
function cobblemon_initiative:economy/reward/standard
give @s cobblemon:black_belt 1
tag @s add deepcore_restructure_done
title @s actionbar [{"text":"RESERVE FRAUD FILED. ","color":"gold"},{"text":"The vein was halved on paper. The pit has the pages now.","color":"gray"}]

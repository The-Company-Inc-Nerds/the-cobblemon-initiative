# Mending the Deep Nets - Rui restrings two deep-sea nets, keeps one for the fleet, wards
# the other line for you. Run as the player (guarded by turn_in_net: count 8.. and not already
# done). 300 CD via the skewed payout + a standard training gift (the reward bundle; no bespoke
# net item id is jar-validated, so reuse economy/reward/standard like the Genji rod - Open Q1).
clear @s minecraft:string 8
function cobblemon_initiative:economy/payout {amount:300}
function cobblemon_initiative:economy/reward/standard
tag @s add bosun_net_done
title @s actionbar [{"text":"Restrung. ","color":"gold"},{"text":"The deep nets remember the fleet - and so, apparently, do you.","color":"gray"}]

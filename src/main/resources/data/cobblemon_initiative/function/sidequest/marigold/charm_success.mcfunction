# Wisps in the Reeds - Marigold restrings both charms, keeps one, wards the other for you.
# Run as the player (guarded by turn_in_charm: count 8.. and not already done). Face 300 via
# the skewed payout + a standard training gift (the "kept charm" is the reward bundle; no
# bespoke charm item id is jar-validated, so reuse economy/reward/standard like the Genji rod).
clear @s minecraft:string 8
function cobblemon_initiative:economy/payout {amount:300}
function cobblemon_initiative:economy/reward/standard
tag @s add mm_charms_done
title @s actionbar [{"text":"Restrung. ","color":"gold"},{"text":"A little marsh-luck, bottled - it laughs at a hard hit.","color":"gray"}]

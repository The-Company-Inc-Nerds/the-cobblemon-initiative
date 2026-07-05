# Out of Office - Genji restrings two rods, keeps one, hands the player the other.
# Run as the player. cobblemon:poke_rod id is UNVERIFIED in 1.7.3 - jar-check before ship
# (the rod is craftable this era, so it is convenience, not sequence-breaking).
clear @s minecraft:string 8
give @s cobblemon:poke_rod 1
function cobblemon_initiative:economy/payout {amount:300}
loot give @s loot cobblemon_initiative:npc_gift/training_standard
tag @s add genji_rod_done
title @s actionbar [{"text":"Restrung. ","color":"gold"},{"text":"An old rod, and a whole new way to lose a hardcore run.","color":"gray"}]

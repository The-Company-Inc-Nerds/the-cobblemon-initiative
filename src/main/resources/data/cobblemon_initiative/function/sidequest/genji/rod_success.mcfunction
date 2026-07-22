# Out of Office - Genji trades the player his bait stash for 8x string.
# Run as the player. cobblemon:poke_bait is jar-verified and currently unused (16x share).
clear @s minecraft:string 8
give @s cobblemon:poke_bait 16
function cobblemon_initiative:economy/payout {amount:300}
function cobblemon_initiative:economy/reward/standard
tag @s add genji_rod_done
title @s actionbar [{"text":"Stash split. ","color":"gold"},{"text":"His bait, your string. The river never restates its earnings.","color":"gray"}]

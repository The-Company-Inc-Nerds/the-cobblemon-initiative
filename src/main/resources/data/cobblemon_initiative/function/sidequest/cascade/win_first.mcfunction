# Cascade Ascent — one-time first-clear reward (doc reward fork B).
# economy/payout is the verified skew-aware entry point (computes rate/raw for pay_macro).
function cobblemon_initiative:economy/payout {amount:500}
give @s cobblemon:super_potion 2
give @s minecraft:emerald 1
tag @s add sq_cascade_done
title @s title [{"text":"ASCENT CLEARED","color":"aqua","bold":true}]
title @s subtitle [{"text":"The record board takes a new name","color":"gray"}]
tellraw @s [{"text":"First ascent! ","color":"aqua","bold":true},{"text":"500 CD, two super potions, and one emerald — the color of the Takehara badge.","color":"gray"}]

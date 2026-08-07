# Augmented Ring Run — one-time first-clear reward. economy/payout is the verified skew-aware entry
# point; economy/reward/major is the sanctioned training pack. Mirrors the trident race (400 CD + pack).
# The augment is an EFFECT, not an item, so there is no keepsake to grant — repeat laps are their own
# reward (no payout on a rematch; see win.mcfunction).
function cobblemon_initiative:economy/payout {amount:400}
function cobblemon_initiative:economy/reward/major
tag @s add sq_augmented_race_done
title @s title [{"text":"RINGS CLEARED","color":"light_purple","bold":true}]
title @s subtitle [{"text":"The rig logs your run","color":"gray"}]
tellraw @s [{"text":"First clean run! ","color":"light_purple","bold":true},{"text":"400 CD and a training pack - Arlo says the grid finally has something to be proud of.","color":"gray"}]

# Trident Tide-Race — one-time first-clear reward. economy/payout is the verified skew-aware entry
# point; economy/reward/major is the sanctioned training pack. The Riptide trident granted at start
# is the keepsake (the sea gives things back — Gianna's lore), so no item is re-granted here.
function cobblemon_initiative:economy/payout {amount:400}
function cobblemon_initiative:economy/reward/major
tag @s add sq_trident_race_done
title @s title [{"text":"TIDE-RINGS CLEARED","color":"aqua","bold":true}]
title @s subtitle [{"text":"The sea gives things back","color":"gray"}]
tellraw @s [{"text":"First clean run! ","color":"aqua","bold":true},{"text":"400 CD and a training pack - and the Tide-Caller's Trident is yours to keep.","color":"gray"}]

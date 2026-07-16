# The First Oath (Ryujin SQ4) - thank Skywatcher Rei for the oath. Run AS the player
# (dialog take_oath button). Lore-completion beat, no battle: 300 CD at full face value +
# 2x rare candy (cobblemon:rare_candy, jar-valid 1.7.3) + the ryujin_oath_told latch.
function cobblemon_initiative:economy/payout {amount:300}
give @s cobblemon:rare_candy 2
tag @s add ryujin_oath_told
title @s actionbar [{"text":"The First Oath. ","color":"gold"},{"text":"Raised, not bought.","color":"gray"}]

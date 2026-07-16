# The Long Memory (Nifl SQ3, lantern 4) - the far lantern, the founder-adjacent whisper. Battle-free.
# Run AS the player from the lantern_four dialog button. Guarded once with unless nifl_lanterns_done.
# Sets nifl_lantern_4 + nifl_lanterns_done, pays 300 via the skewed payout (~282 landed at idx25).
# Keepsake: minecraft:snowball x4 (cobblemon:pretty_feather does NOT exist in the 1.7.3 jar - swapped
# per the spec note). No Pokemon gift, no nether_star (nether stars back the currency - never a drop).
execute if entity @s[tag=nifl_lanterns_done] run return 0
tag @s add nifl_lantern_4
tag @s add nifl_lanterns_done
function cobblemon_initiative:economy/payout {amount:300}
give @s minecraft:snowball 4
title @s actionbar [{"text":"The far lantern holds one more memory for you than for anyone in town.","color":"gray"}]

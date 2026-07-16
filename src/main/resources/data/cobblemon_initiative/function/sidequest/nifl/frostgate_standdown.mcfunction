# Stand Down at the Frostgate (Nifl SQ2, branch B) - the veteran salutes and walks into the snow,
# the first on-screen defector. Run AS the player from the stand-down dialog button. Guarded once
# with unless nifl_frostgate_clear so it grants a single time (and never double-fires against the
# fight or paid-decline branches, which set the same clear tag). Grants the Frostgate token keepsake
# (ice_stone), pays 260 via the skewed payout (~244 landed at idx25), and throws the STOOD DOWN card.
# The body is left in place for the placement pass to relocate or remove.
execute if entity @s[tag=nifl_frostgate_clear] run return 0
tag @s add nifl_warrant_stood_down
tag @s add nifl_frostgate_clear
give @s cobblemon:ice_stone 1
function cobblemon_initiative:economy/payout {amount:260}
title @s title [{"text":"STOOD DOWN","color":"gold"}]
title @s subtitle [{"text":"A veteran salutes and walks into the snow.","color":"gray"}]

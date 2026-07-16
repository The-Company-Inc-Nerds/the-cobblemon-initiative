# Cold Storage (Nifl SQ1) - the portrait read, the frag_9 payoff. Run AS the player from the
# core_three portrait button (gated tag=nifl_archive_open, not_tag=nifl_archive_read in the dialog).
# Guarded once with unless tag=nifl_archive_read so it pays a single time. Face 600 via the skewed
# payout (~564 landed at idx25) + a badge-scaled training gift + rare_candy x5. NO Pokemon gift
# (endgame budget). No nether_star (nether stars back the currency - never a quest drop).
execute if entity @s[tag=nifl_archive_read] run return 0
function cobblemon_initiative:economy/payout {amount:600}
function cobblemon_initiative:economy/reward/standard
give @s cobblemon:rare_candy 5
tag @s add nifl_archive_read
title @s actionbar [{"text":"They emptied you. ","color":"gold"},{"text":"The cold kept the shape of what they took.","color":"gray"}]

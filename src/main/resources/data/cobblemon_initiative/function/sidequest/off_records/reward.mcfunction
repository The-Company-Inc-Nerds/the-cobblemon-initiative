# OFF THE RECORDS - payout. Run as the player (guarded by turn_in: all three pages, not done).
# Skewed payout (economy/payout) + a standard training gift. Latches ci_file_done so the quest ends.
function cobblemon_initiative:economy/payout {amount:1200}
function cobblemon_initiative:economy/reward/standard
tag @s add ci_file_done
title @s title [{"text":"ASSET RECLAIMED","color":"gold","bold":true}]
title @s subtitle [{"text":"one page they do not get to burn.","color":"gray"}]
function cobblemon_initiative:quest/refresh

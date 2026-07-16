# SIGNAL INTEGRITY - payout. Run as the player (guarded by turn_in: all three boards, not done).
# Skewed civic payout (economy/payout) + a standard training gift. Latches ci_signal_done.
function cobblemon_initiative:economy/payout {amount:700}
function cobblemon_initiative:economy/reward/standard
tag @s add ci_signal_done
title @s title [{"text":"FEED SCRUBBED","color":"gold","bold":true}]
title @s subtitle [{"text":"the cover-up leaks slower now.","color":"gray"}]
function cobblemon_initiative:quest/refresh

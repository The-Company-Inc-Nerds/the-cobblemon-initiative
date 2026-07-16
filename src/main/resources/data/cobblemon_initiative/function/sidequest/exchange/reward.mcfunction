# EXCHANGE RATE - payout. Run as the player (guarded by turn_in: all three tags present, not done).
# Skewed COMPANY-branded rate (payout_company) so the peak shortfall reads on camera - an
# attribution receipt - plus a standard training gift. Latches ci_reserves_done so the quest ends.
function cobblemon_initiative:economy/payout_company {amount:900}
function cobblemon_initiative:economy/reward/standard
tag @s add ci_reserves_done
title @s title [{"text":"ADJUSTMENT","color":"gold","bold":true}]
title @s subtitle [{"text":"rounding, in the Company favor.","color":"gray"}]
function cobblemon_initiative:quest/refresh

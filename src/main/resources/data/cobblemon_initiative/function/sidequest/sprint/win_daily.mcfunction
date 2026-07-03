# Quarterly Sprint — daily rematch win: 150 CD courier rate (below the cheapest grunt
# prize, so it never becomes a money printer). Run as the player.
tag @s remove ci_sprint_daily
tag @s add race_daily_claimed
function cobblemon_initiative:economy/payout {amount:150}
title @s title [{"text":"DELIVERED","color":"yellow"}]
tellraw @s [{"text":"Courier rate paid: ","color":"gray"},{"text":"150 CD. ","color":"gold"},{"text":"Same bell tomorrow.","color":"dark_gray","italic":true}]

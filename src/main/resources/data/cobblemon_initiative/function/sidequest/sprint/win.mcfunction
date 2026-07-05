# Quarterly Sprint — FIRST win: 500 CD (skew-aware) + the insured parcel. Run as the player.
tag @s add race_won
tag @s add race_daily_claimed
function cobblemon_initiative:economy/payout {amount:500}
give @s cobblemon:exp_candy_xs 2
give @s cobblemon:heal_ball 1
loot give @s loot cobblemon_initiative:npc_gift/training_major
title @s title [{"text":"DELIVERED","color":"gold","bold":true}]
title @s subtitle [{"text":"The bell rang with time to spare","color":"yellow"}]
tellraw @s [{"text":"Insured parcel rate paid: ","color":"gray"},{"text":"500 CD, candies, one Heal Ball — and a courier's training bonus. ","color":"gold"},{"text":"Mio will want a rematch every morning.","color":"dark_gray","italic":true}]

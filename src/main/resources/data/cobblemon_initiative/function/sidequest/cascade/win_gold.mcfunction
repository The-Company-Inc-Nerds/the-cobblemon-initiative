# Cascade Ascent — repeatable gold-time clear (post-badge-1 rematch). 300 CD paid once
# per Minecraft day (cascade_gold_claimed, dawn-cleared by tick.mcfunction — the board
# only certifies one record a day); repeat golds still get the title, zero pay.
execute unless entity @s[tag=cascade_gold_claimed] run function cobblemon_initiative:economy/payout {amount:300}
tag @s add cascade_gold_claimed
title @s title [{"text":"GOLD TIME","color":"gold","bold":true}]
title @s subtitle [{"text":"Shou pretends he is not impressed","color":"gray"}]

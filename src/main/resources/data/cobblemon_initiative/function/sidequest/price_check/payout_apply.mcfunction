# ADJUSTED RETAIL — the payout. Run as the player (via payout.mcfunction guard).
# 260 CD face value through the skew-aware rail: payout_company prints the BRANDED
# Company Verified Rate actionbar (rate = 100 - min(idx/4, 25)), so the shortfall
# lands on camera right after the player logged everyone else getting shorted —
# this quest is the attribution moment, one of the four deliberately-branded receipts.
# The dialog reaction (paid_out entry) points at that yellow line — no rate digits in text.
function cobblemon_initiative:economy/payout_company {amount:260}
function cobblemon_initiative:economy/reward/minor
loot give @s loot cobblemon_initiative:npc_gift/price_check
tag @s add hz_prices_done
title @s title [{"text":"ADJUSTED FOR RETAIL","color":"gold","bold":true}]
title @s subtitle [{"text":"The verified index, checked against the street","color":"gray"}]
tellraw @s [{"text":"Kaito flips the VERIFIED sign face-down and slides it under the counter.","color":"gray","italic":true}]
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.6 1.2
function cobblemon_initiative:quest/refresh

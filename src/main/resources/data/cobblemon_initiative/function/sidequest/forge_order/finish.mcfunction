# SQ1 The Last Forge Order - Sena reforges the blanked charter into a nameless ingot.
# Run as the player (guarded by the burn_charter button: forge_order_agent_clear OR
# declined_sq_recovery_agent, plus not already done). Face 600 via the skewed payout
# (idx 25 pays the floor rate, the last echo of the destabilisation) + a major training
# gift + charcoal x8 keepsake (minecraft:charcoal, vanilla, jar-safe).
tag @s add forge_order_done
function cobblemon_initiative:economy/payout {amount:600}
function cobblemon_initiative:economy/reward/major
give @s minecraft:charcoal 8
title @s actionbar [{"text":"THE LAST ORDER BURNS. ","color":"gold"},{"text":"A signature you can hold instead of read.","color":"gray"}]

# Five Keepers capstone (14_shrines Q4) — one-time payout for clearing all five elemental
# shrines. Run AS the player from the Last Pilgrim claim button (gated: all five
# defeated_<type>_shrine_leader tags held, and not_tag five_keepers_paid on the button).
# The single shrine CD faucet: 5000 skewed via economy/payout (the Company-skims-everything
# haircut). Keepsake = a Master Ball (the one no living trainer holds) + a stack of rare
# candy. Macro-safe: no apostrophes, no percent, no double-quotes in delivered strings.
tag @s add five_keepers_paid
function cobblemon_initiative:economy/payout {amount:5000}
give @s cobblemon:master_ball 1
give @s cobblemon:rare_candy 10

# Streamable title card.
title @s times 10 90 20
title @s subtitle [{"text":"The crystals are yours","color":"gray","italic":true}]
title @s title [{"text":"FIVE KEEPERS ANSWER","color":"#7A5CA8","bold":true}]

# Triumphant chime.
playsound minecraft:block.beacon.activate master @s ~ ~ ~ 1 1.2
playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 0.8 0.7

tellraw @s [{"text":"[The Last Pilgrim] ","color":"aqua","bold":true},{"text":"Five elements, five keepers, five crystals - all answering to one hand. Be worthy of that.","color":"gray","italic":true}]

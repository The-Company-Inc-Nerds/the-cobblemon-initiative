# Sango Classic — take the entry fee and start the two-minute quarter. Run as the player.
# NOTE: cobbledollars remove with an empty wallet is UNVERIFIED — no balance pre-check
# primitive exists; doc fallback is free entry with a smaller purse (see quest doc).
cobbledollars remove @s 150
tag @s add classic_active
# Baseline snapshot: only fish caught DURING the quarter count (round-13 exploit fix —
# pre-carried fish previously auto-won at hand-in, bypassing the timer entirely).
execute store result score @s ci_fish_base_cod run clear @s minecraft:cod 0
execute store result score @s ci_fish_base_salmon run clear @s minecraft:salmon 0
execute store result score @s ci_fish_base_puffer run clear @s minecraft:pufferfish 0
execute store result score @s ci_fish_base_tropical run clear @s minecraft:tropical_fish 0
scoreboard players set #on ci_classic 1
scoreboard players set #time ci_classic 120
bossbar set cobblemon_initiative:sango_classic value 120
bossbar set cobblemon_initiative:sango_classic players @s
bossbar set cobblemon_initiative:sango_classic visible true
title @s title [{"text":"THE SANGO CLASSIC","color":"aqua","bold":true}]
title @s subtitle [{"text":"Three fish off the pier before the bar empties","color":"gray"}]
# RECORD SPECIES OF THE QUARTER (review B6): chalkboard roll at entry — 1 cod / 2 salmon
# / 3 pufferfish / 4 tropical fish. Land one among your three and win_common pays a
# +75 CD skew-aware bonus. Bonus-only variance, always announced (stream-visible roll);
# the entry fee and purses never move (randomness invariants, ENGINE_FINDINGS §3).
execute store result score #species ci_classic run random value 1..4
execute if score #species ci_classic matches 1 run tellraw @s [{"text":"CHALKBOARD — Record Species of the Quarter: ","color":"aqua"},{"text":"COD","color":"white","bold":true},{"text":" (+75 CD if one lands in your three)","color":"gray"}]
execute if score #species ci_classic matches 2 run tellraw @s [{"text":"CHALKBOARD — Record Species of the Quarter: ","color":"aqua"},{"text":"SALMON","color":"white","bold":true},{"text":" (+75 CD if one lands in your three)","color":"gray"}]
execute if score #species ci_classic matches 3 run tellraw @s [{"text":"CHALKBOARD — Record Species of the Quarter: ","color":"aqua"},{"text":"PUFFERFISH","color":"white","bold":true},{"text":" (+75 CD if one lands in your three)","color":"gray"}]
execute if score #species ci_classic matches 4 run tellraw @s [{"text":"CHALKBOARD — Record Species of the Quarter: ","color":"aqua"},{"text":"TROPICAL FISH","color":"white","bold":true},{"text":" (+75 CD if one lands in your three)","color":"gray"}]
schedule function cobblemon_initiative:sidequest/derby/second 1s

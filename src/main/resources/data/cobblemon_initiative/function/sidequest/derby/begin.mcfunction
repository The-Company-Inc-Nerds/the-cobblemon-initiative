# Sango Classic — take the entry fee and start the six-minute quarter. Run as the player.
# NOTE: cobbledollars remove with an empty wallet is UNVERIFIED — no balance pre-check
# primitive exists; doc fallback is free entry with a smaller purse (see quest doc).
cobbledollars remove @s 150
tag @s add classic_active
scoreboard players set #on ci_classic 1
scoreboard players set #time ci_classic 360
bossbar set cobblemon_initiative:sango_classic value 360
bossbar set cobblemon_initiative:sango_classic players @s
bossbar set cobblemon_initiative:sango_classic visible true
title @s title [{"text":"THE SANGO CLASSIC","color":"aqua","bold":true}]
title @s subtitle [{"text":"Five fish off the pier before the bar empties","color":"gray"}]
schedule function cobblemon_initiative:sidequest/derby/second 1s

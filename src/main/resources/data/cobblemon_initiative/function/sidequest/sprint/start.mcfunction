# Quarterly Sprint — start the run. Run as the player (dialog button, as_player).
# TUNE: 2400 ticks = 120 seconds (round-13: 180s was unfailable — walking finishes in ~135s) for the ~580-block Blossom Path run (Sango west edge
# x2505 to the Takehara arch at 1923,2584). Adjust after one on-camera test run.
scoreboard players set @s ci_sprint 2400
bossbar set cobblemon_initiative:sprint max 2400
bossbar set cobblemon_initiative:sprint value 2400
# Daily rematch runs a tighter morning route: 2000 ticks = 100 seconds (round-13).
execute if entity @s[tag=ci_sprint_daily] run scoreboard players set @s ci_sprint 2000
execute if entity @s[tag=ci_sprint_daily] run bossbar set cobblemon_initiative:sprint max 2000
execute if entity @s[tag=ci_sprint_daily] run bossbar set cobblemon_initiative:sprint value 2000
bossbar set cobblemon_initiative:sprint players @s
bossbar set cobblemon_initiative:sprint visible true
title @s title [{"text":"RUN","color":"yellow","bold":true}]
title @s subtitle [{"text":"Ring the delivery bell at the Takehara arch","color":"gold"}]
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1

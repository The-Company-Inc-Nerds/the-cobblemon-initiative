# Quarterly Sprint — start the run. Run as the player (dialog button, as_player).
# TUNE: 3600 ticks = 180 seconds for the ~580-block Blossom Path run (Sango west edge
# x2505 to the Takehara arch at 1923,2584). Adjust after one on-camera test run.
scoreboard players set @s ci_sprint 3600
bossbar set cobblemon_initiative:sprint max 3600
bossbar set cobblemon_initiative:sprint value 3600
bossbar set cobblemon_initiative:sprint players @s
bossbar set cobblemon_initiative:sprint visible true
title @s title [{"text":"RUN","color":"yellow","bold":true}]
title @s subtitle [{"text":"Ring the delivery bell at the Takehara arch","color":"gold"}]
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1

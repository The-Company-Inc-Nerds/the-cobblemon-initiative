# Clean crossing — the player reached the leader-side marker and no sentinel ever
# turned. Run as/at the player (gym/nifl_whiteout_run). Latches nifl_whiteout_clear
# once per save (the caller gates on tag=!nifl_whiteout_clear, so this never
# re-fires). The reward is acknowledgment only: aurora FX, the line, and the Boreas
# respect entry in dialog/gym_leader_nifl — deliberately NO buff, NO item, NO money
# (hardcore fairness; the showrunner tailwind idea was cut).
tag @s add nifl_whiteout_clear
title @s actionbar [{"text":"You crossed unseen. The cold respects that.","color":"aqua"}]
playsound minecraft:block.amethyst_block.chime player @s ~ ~ ~ 1 1.4
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 0.8 1.8
# Aurora ribbon over the crossing point.
particle minecraft:end_rod ~ ~2.2 ~ 1.6 0.5 1.6 0.02 40 normal
particle minecraft:glow ~ ~2.6 ~ 2.2 0.7 2.2 0.01 30 normal
particle minecraft:snowflake ~ ~1.5 ~ 1.2 0.8 1.2 0.02 20 normal

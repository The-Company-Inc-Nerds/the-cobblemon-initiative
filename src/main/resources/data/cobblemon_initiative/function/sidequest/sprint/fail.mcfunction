# Quarterly Sprint — timer expired. Free retry: back to the start line, zero damage,
# no daily claim consumed (race_daily_claimed is only set on a win). Run as/at the runner.
tag @s remove ci_sprinting
tag @s remove ci_sprint_daily
bossbar set cobblemon_initiative:sprint visible false
title @s title [{"text":"MISSED THE BELL","color":"red"}]
title @s subtitle [{"text":"Walk of shame waived — run it again free","color":"gray"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.6
# VERIFY-IN-GAME BEFORE SHIP (hardcore run): provisional start-line coordinates at the
# Sango-side mouth of Blossom Path. Must be fall-safe AND suffocation-safe. Update these
# to the exact block in front of Courier Mio once she is placed.
tp @s 2505 71 2850

# Trident Tide-Race — timer expired. No teleport, no damage: the course is over open water, so the
# racer just swims back to the buoy. Free retries at Gianna on the beach.
title @a[tag=ci_trident_racing] title [{"text":"THE TIDE TURNED","color":"red","bold":true}]
title @a[tag=ci_trident_racing] subtitle [{"text":"Gianna waves you back - free retries","color":"gray"}]
execute as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.bass player @s ~ ~ ~ 1 0.6
# The tide takes its trident back when you fail (kept ONLY on a clean run — see win_first).
clear @a[tag=ci_trident_racing] minecraft:trident[minecraft:custom_data~{ci_tide_trident:1b}]
scoreboard players reset @a[tag=ci_trident_racing] ci_trace_cp
tag @a remove ci_trident_racing
bossbar set cobblemon_initiative:trident_race visible false

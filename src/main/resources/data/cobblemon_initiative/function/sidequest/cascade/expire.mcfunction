# Cascade Ascent — timer expired. No teleport, no damage: the course is routed over the
# plunge pools, so the runner simply walks back down. Free retries at Falls Warden Shou
# (the board keeper — never Ayame; accidental name glitches poison the erasure motif).
title @a[tag=ci_ascending] title [{"text":"TIME EXPIRED","color":"red","bold":true}]
title @a[tag=ci_ascending] subtitle [{"text":"Shou resets the clock - free retries","color":"gray"}]
execute as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.bass player @s ~ ~ ~ 1 0.6
tag @a remove ci_ascending
bossbar set cobblemon_initiative:cascade visible false

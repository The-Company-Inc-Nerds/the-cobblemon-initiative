# An auditor caught the player mid-errand. Zero damage - it only goes on the record.
# Bumps this carry's count, arms the 3s cooldown, and latches the persistent blown flag.
scoreboard players add @s obs_count 1
scoreboard players set @s obs_cd 60
tag @s add off_record_blown
title @s actionbar [{"text":"OBSERVATION LOGGED","color":"red","bold":true},{"text":" - the clipboard moves. Break their line of sight.","color":"gray"}]
playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1.4

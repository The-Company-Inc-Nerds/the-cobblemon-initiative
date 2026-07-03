# NIGHT SHIFT contract signing. Run as the player (dialog button, as_player).
# Idempotent: re-clicking while already on shift does NOT zero the tally.
execute unless entity @s[tag=work_night_active] run scoreboard players set @s ci_kill_zombie 0
execute unless entity @s[tag=work_night_active] run scoreboard players set @s ci_kill_skeleton 0
execute unless entity @s[tag=work_night_active] run scoreboard players set @s ci_kill_spider 0
execute unless entity @s[tag=work_night_active] run scoreboard players set @s ci_kill_total 0
execute unless entity @s[tag=work_night_active] run tellraw @s [{"text":"NIGHT SHIFT signed: ","color":"yellow","bold":true},{"text":"cull 8 hostiles (zombies, skeletons, spiders) on Blossom Path after dark, then report to Forewoman Tetsu.","color":"gray"}]
execute if entity @s[tag=work_night_active] run tellraw @s [{"text":"You are already on the night shift. ","color":"gray"},{"text":"The tally stands.","color":"yellow"}]
tag @s add work_night_active

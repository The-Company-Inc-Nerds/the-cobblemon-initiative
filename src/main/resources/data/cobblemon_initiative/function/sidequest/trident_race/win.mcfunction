# Trident Tide-Race — all rings + finish cleared in time. Run as/at the winning player.
# ORDER MATTERS: the repeat message checks the done tag BEFORE win_first adds it.
tag @s remove ci_trident_racing
bossbar set cobblemon_initiative:trident_race visible false
execute at @s run playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 1 1
execute if entity @s[tag=sq_trident_race_done] run tellraw @s [{"text":"Clean tide. ","color":"aqua","bold":true},{"text":"You ran the rings again - the sea remembers a good rider.","color":"gray"}]
execute unless entity @s[tag=sq_trident_race_done] run function cobblemon_initiative:sidequest/trident_race/win_first

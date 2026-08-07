# Augmented Ring Run — all 37 rings cleared in time. Run as/at the winning player.
# ORDER MATTERS: the repeat message checks the done tag BEFORE win_first adds it.
tag @s remove ci_aug_racing
bossbar set cobblemon_initiative:augmented_race visible false
# Drop the augment now for immediacy (AugmentedRaceManager also clears both when the tag is gone).
effect clear @s minecraft:speed
effect clear @s minecraft:jump_boost
scoreboard players reset @s ci_augrace_cp
execute at @s run playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 1 1
execute if entity @s[tag=sq_augmented_race_done] run tellraw @s [{"text":"Clean run. ","color":"light_purple","bold":true},{"text":"You ran the rings again - Arlo logs the lap and grins.","color":"gray"}]
execute unless entity @s[tag=sq_augmented_race_done] run function cobblemon_initiative:sidequest/augmented_race/win_first

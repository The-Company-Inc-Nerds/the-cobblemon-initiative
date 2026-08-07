# Augmented Ring Run — start an attempt. Run AS the player (dialog button, as_player):
#   function cobblemon_initiative:sidequest/augmented_race/start {ticks:900}   (45 s clock)
# Guard: ignore re-clicks while a run is live (no timer-reset exploit).
execute if entity @s[tag=ci_aug_racing] run return 0
# Arm the augment: Speed II + Jump Boost II (amplifier 1 = "1 and 1" per the playtest note). Applied here
# for immediacy AND re-asserted every tick by AugmentedRaceManager (keyed on ci_aug_racing) so a mid-run
# relog re-arms them; the manager clears both when the tag drops (win/expire/relog). ambient+hidden icon.
effect give @s minecraft:speed 999999 1 true
effect give @s minecraft:jump_boost 999999 1 true
scoreboard players set @s ci_augrace_cp 0
$scoreboard players set #time ci_augrace $(ticks)
scoreboard players operation #secs ci_augrace = #time ci_augrace
scoreboard players operation #secs ci_augrace /= #twenty ci_augrace
execute store result bossbar cobblemon_initiative:augmented_race max run scoreboard players get #secs ci_augrace
execute store result bossbar cobblemon_initiative:augmented_race value run scoreboard players get #secs ci_augrace
tag @s add ci_aug_racing
bossbar set cobblemon_initiative:augmented_race players @a
bossbar set cobblemon_initiative:augmented_race visible true
title @s title [{"text":"AUGMENTED RING RUN","color":"light_purple","bold":true}]
title @s subtitle [{"text":"Speed + spring engaged - crouch as you land to roll","color":"gray"}]
execute at @s run playsound minecraft:block.beacon.activate player @s ~ ~ ~ 1 1.4

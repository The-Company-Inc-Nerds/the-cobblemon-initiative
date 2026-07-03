# Cascade Ascent — start an attempt. Run AS the player (dialog button, as_player):
#   function cobblemon_initiative:sidequest/cascade/start {ticks:1800}   (90 s first run)
#   function cobblemon_initiative:sidequest/cascade/start {ticks:1200}   (60 s gold rematch)
# Guard: ignore re-clicks while a run is live (no timer-reset exploit).
execute if entity @s[tag=ci_ascending] run return 0
# Dev safety: warn loudly if the crest marker was never placed (set_crest not yet run).
execute unless entity @e[type=minecraft:marker,tag=ci_cascade_crest] run tellraw @s [{"text":"WARNING: no crest marker set. Stand at the crest and run function cobblemon_initiative:sidequest/cascade/set_crest first.","color":"red"}]
$scoreboard players set #time ci_cascade $(ticks)
scoreboard players operation #secs ci_cascade = #time ci_cascade
scoreboard players operation #secs ci_cascade /= #twenty ci_cascade
execute store result bossbar cobblemon_initiative:cascade max run scoreboard players get #secs ci_cascade
execute store result bossbar cobblemon_initiative:cascade value run scoreboard players get #secs ci_cascade
tag @s add ci_ascending
bossbar set cobblemon_initiative:cascade players @a
bossbar set cobblemon_initiative:cascade visible true
title @s title [{"text":"THE CASCADE ASCENT","color":"aqua","bold":true}]
title @s subtitle [{"text":"Base to crest before the clock dies","color":"gray"}]
execute at @s run playsound minecraft:block.note_block.bell player @s ~ ~ ~ 1 1.5

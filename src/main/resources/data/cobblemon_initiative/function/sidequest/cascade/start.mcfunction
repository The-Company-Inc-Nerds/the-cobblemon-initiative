# Cascade Ascent — start an attempt. Run AS the player (dialog button, as_player):
#   function cobblemon_initiative:sidequest/cascade/start {ticks:1200}   (60 s, default clock)
# Guard: ignore re-clicks while a run is live (no timer-reset exploit).
execute if entity @s[tag=ci_ascending] run return 0
# Grant the keepsake Cascade Boots (Depth Strider I, blue) and arm the checkpoint counter.
loot give @s loot cobblemon_initiative:npc_gift/depth_strider_boots
scoreboard players set @s ci_cascade_cp 0
# (Finish is now the fixed FINISH box in tick_run — the old ci_cascade_crest marker/set_crest
# dev check is retired; the 5 ordered checkpoints replace it.)
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

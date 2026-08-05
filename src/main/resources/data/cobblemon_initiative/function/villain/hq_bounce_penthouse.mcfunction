# Company HQ penthouse floors — ESCORTED OUT. Run as/at a penthouse-box player who lacks
# hq_basement_cleared (villain/hq_tick b — the executive floors stay sealed while the
# lower floors are contested). Same cooldown-first shape as villain/hq_bounce; the walk
# ends at the same street spot, because the lift only goes one direction for you today.
scoreboard players set @s ci_hq_kick_cd 200
tellraw @s [{"text":"A security associate materializes at your elbow. ","color":"gray","italic":true},{"text":"You are walked back to the ground-floor lift, ridden down in courteous silence, and shown the weather.","color":"gray"}]
title @s actionbar [{"text":"ESCORTED OUT","color":"red","bold":true},{"text":" — the executive floors are not receiving visitors.","color":"gray"}]
playsound minecraft:entity.villager.no player @s ~ ~ ~ 1 0.8
tp @s 1619.5 92 1120.5 facing 1619.5 92 1116.5
execute at @s run playsound minecraft:block.wooden_door.close player @s ~ ~ ~ 1 0.9

# Cascade Ascent — active-run tick: countdown, bossbar, warning pings, finish + expiry.
# Warning thresholds (sprint pattern): 60/30/10/5/3/2/1 seconds = 1200/600/200/100/60/40/20 ticks.
scoreboard players remove #time ci_cascade 1
scoreboard players operation #secs ci_cascade = #time ci_cascade
scoreboard players operation #secs ci_cascade /= #twenty ci_cascade
execute store result bossbar cobblemon_initiative:cascade value run scoreboard players get #secs ci_cascade
execute if score #time ci_cascade matches 1200 run title @a[tag=ci_ascending] actionbar [{"text":"60 seconds","color":"yellow"}]
execute if score #time ci_cascade matches 1200 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1
execute if score #time ci_cascade matches 600 run title @a[tag=ci_ascending] actionbar [{"text":"30 seconds","color":"yellow"}]
execute if score #time ci_cascade matches 600 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1.2
execute if score #time ci_cascade matches 200 run title @a[tag=ci_ascending] actionbar [{"text":"10 seconds","color":"gold"}]
execute if score #time ci_cascade matches 200 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1
execute if score #time ci_cascade matches 100 run title @a[tag=ci_ascending] actionbar [{"text":"5","color":"red"}]
execute if score #time ci_cascade matches 100 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.4
execute if score #time ci_cascade matches 60 run title @a[tag=ci_ascending] actionbar [{"text":"3","color":"red"}]
execute if score #time ci_cascade matches 60 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.6
execute if score #time ci_cascade matches 40 run title @a[tag=ci_ascending] actionbar [{"text":"2","color":"red"}]
execute if score #time ci_cascade matches 40 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.8
execute if score #time ci_cascade matches 20 run title @a[tag=ci_ascending] actionbar [{"text":"1","color":"dark_red","bold":true}]
execute if score #time ci_cascade matches 20 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 2
# Finish: within 4 blocks of the crest marker placed by set_crest.
execute as @a[tag=ci_ascending] at @s if entity @e[type=minecraft:marker,tag=ci_cascade_crest,distance=..4] run function cobblemon_initiative:sidequest/cascade/win
# Expiry: clock hits zero.
execute if score #time ci_cascade matches ..0 run function cobblemon_initiative:sidequest/cascade/expire

# Cascade Ascent — active-run tick: countdown, bossbar, warning pings, ORDERED checkpoints, expiry.
# 50 s clock (1000 ticks). Warning thresholds: 30/20/10/5/3/2/1 s = 600/400/200/100/60/40/20 ticks.
scoreboard players remove #time ci_cascade 1
scoreboard players operation #secs ci_cascade = #time ci_cascade
scoreboard players operation #secs ci_cascade /= #twenty ci_cascade
execute store result bossbar cobblemon_initiative:cascade value run scoreboard players get #secs ci_cascade
execute if score #time ci_cascade matches 600 run title @a[tag=ci_ascending] actionbar [{"text":"30 seconds","color":"yellow"}]
execute if score #time ci_cascade matches 600 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1
execute if score #time ci_cascade matches 400 run title @a[tag=ci_ascending] actionbar [{"text":"20 seconds","color":"yellow"}]
execute if score #time ci_cascade matches 400 as @a[tag=ci_ascending] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1.2
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
# Vertical ring markers around the current target checkpoint (that runner only).
execute as @a[tag=ci_ascending] at @s run function cobblemon_initiative:sidequest/cascade/rings
# ORDERED checkpoints — each only advances the counter from the immediately prior value,
# so the runner must clear them in sequence (no skipping CP1 -> CP3). Boxes are ~3-block
# cubes centered on the F3 coords; passing one chimes + actionbars and bumps ci_cascade_cp.
# CP1 1935/106/2419
execute as @a[tag=ci_ascending] if score @s ci_cascade_cp matches 0 if entity @s[x=1933.5,dx=3,y=104.5,dy=4,z=2417.5,dz=3] run function cobblemon_initiative:sidequest/cascade/cp {n:1,total:5}
# CP2 1942/109/2417
execute as @a[tag=ci_ascending] if score @s ci_cascade_cp matches 1 if entity @s[x=1940.5,dx=3,y=107.5,dy=4,z=2415.5,dz=3] run function cobblemon_initiative:sidequest/cascade/cp {n:2,total:5}
# CP3 1957/122/2421
execute as @a[tag=ci_ascending] if score @s ci_cascade_cp matches 2 if entity @s[x=1955.5,dx=3,y=120.5,dy=4,z=2419.5,dz=3] run function cobblemon_initiative:sidequest/cascade/cp {n:3,total:5}
# CP4 1974/135/2429
execute as @a[tag=ci_ascending] if score @s ci_cascade_cp matches 3 if entity @s[x=1972.5,dx=3,y=133.5,dy=4,z=2427.5,dz=3] run function cobblemon_initiative:sidequest/cascade/cp {n:4,total:5}
# FINISH 1982/142/2569 — only once all four rings are behind you.
execute as @a[tag=ci_ascending] if score @s ci_cascade_cp matches 4 at @s if entity @s[x=1980.5,dx=3,y=140.5,dy=4,z=2567.5,dz=3] run function cobblemon_initiative:sidequest/cascade/win
# Expiry: clock hits zero.
execute if score #time ci_cascade matches ..0 run function cobblemon_initiative:sidequest/cascade/expire

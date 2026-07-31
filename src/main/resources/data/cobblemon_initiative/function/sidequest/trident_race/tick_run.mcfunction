# Trident Tide-Race — active-run tick: countdown, bossbar, warning pings, ORDERED ocean rings, expiry.
# 30 s clock (600 ticks). Warning thresholds: 20/10/5/3/2/1 s = 400/200/100/60/40/20 ticks.
scoreboard players remove #time ci_trace 1
scoreboard players operation #secs ci_trace = #time ci_trace
scoreboard players operation #secs ci_trace /= #twenty ci_trace
execute store result bossbar cobblemon_initiative:trident_race value run scoreboard players get #secs ci_trace
execute if score #time ci_trace matches 400 run title @a[tag=ci_trident_racing] actionbar [{"text":"20 seconds","color":"yellow"}]
execute if score #time ci_trace matches 400 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 0.9
execute if score #time ci_trace matches 200 run title @a[tag=ci_trident_racing] actionbar [{"text":"10 seconds","color":"gold"}]
execute if score #time ci_trace matches 200 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1
execute if score #time ci_trace matches 100 run title @a[tag=ci_trident_racing] actionbar [{"text":"5","color":"red"}]
execute if score #time ci_trace matches 100 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.4
execute if score #time ci_trace matches 60 run title @a[tag=ci_trident_racing] actionbar [{"text":"3","color":"red"}]
execute if score #time ci_trace matches 60 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.6
execute if score #time ci_trace matches 40 run title @a[tag=ci_trident_racing] actionbar [{"text":"2","color":"red"}]
execute if score #time ci_trace matches 40 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.8
execute if score #time ci_trace matches 20 run title @a[tag=ci_trident_racing] actionbar [{"text":"1","color":"dark_red","bold":true}]
execute if score #time ci_trace matches 20 as @a[tag=ci_trident_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 2
# Ocean ring marker at the current target ring (that runner only).
execute as @a[tag=ci_trident_racing] at @s run function cobblemon_initiative:sidequest/trident_race/rings
# ORDERED rings — PLACEHOLDER coords heading west off Gianna's beach (475/63/3589) into open water.
# NEEDS in-world F3 tuning like the cascade course (walk it, re-record each ring + the finish box).
# Boxes ~3-wide; dy widened to 4 so a fast riptide pass still registers on a tick. Gated on the
# immediately-prior counter so rings must be cleared in sequence (no skipping).
# Ring 1 462/61/3589
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 0 if entity @s[x=460.5,dx=3,y=59.5,dy=4,z=3587.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:1,total:5}
# Ring 2 450/60/3592
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 1 if entity @s[x=448.5,dx=3,y=58.5,dy=4,z=3590.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:2,total:5}
# Ring 3 438/60/3585
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 2 if entity @s[x=436.5,dx=3,y=58.5,dy=4,z=3583.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:3,total:5}
# Ring 4 426/61/3591
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 3 if entity @s[x=424.5,dx=3,y=59.5,dy=4,z=3589.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:4,total:5}
# Ring 5 414/61/3587
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 4 if entity @s[x=412.5,dx=3,y=59.5,dy=4,z=3585.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:5,total:5}
# FINISH 402/62/3589 — only once all five rings are behind you.
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 5 at @s if entity @s[x=400.5,dx=3,y=60.5,dy=4,z=3587.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/win
# Expiry: clock hits zero.
execute if score #time ci_trace matches ..0 run function cobblemon_initiative:sidequest/trident_race/expire

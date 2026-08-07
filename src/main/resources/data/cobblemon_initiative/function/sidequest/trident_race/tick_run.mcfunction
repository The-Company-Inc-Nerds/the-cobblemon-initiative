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
# ORDERED ocean rings — the full 15-ring loop off Gianna's Westwind beach (playtest 2026-08-06 N1:
# "where are the other ~10 rings"). Coords are the showrunner's marks P7..P21, run in order. Boxes
# are ~3 wide (x/z coord-1.5, dx/dz=3) with dy=4 around each ring's marked Y so a fast riptide pass
# registers on a tick. Gated on the immediately-prior counter so rings must be cleared in sequence
# (no skipping). FINISH folds into the last ring (cp==15) per note 4. Showrunner: riptide the loop
# once and nudge any ring that lands on a piling or dry shore.
# Ring 1  P7  464.7/62.1/3590.0
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 0 if entity @s[x=463.2,dx=3,y=60.6,dy=4,z=3588.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:1,total:15}
# Ring 2  P8  452.4/62.1/3591.9
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 1 if entity @s[x=450.9,dx=3,y=60.6,dy=4,z=3590.4,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:2,total:15}
# Ring 3  P9  440.6/58.7/3593.7
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 2 if entity @s[x=439.1,dx=3,y=57.2,dy=4,z=3592.2,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:3,total:15}
# Ring 4  P10 421.2/58.7/3596.7
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 3 if entity @s[x=419.7,dx=3,y=57.2,dy=4,z=3595.2,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:4,total:15}
# Ring 5  P11 403.1/60.9/3608.3
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 4 if entity @s[x=401.6,dx=3,y=59.4,dy=4,z=3606.8,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:5,total:15}
# Ring 6  P12 404.2/62.1/3627.0
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 5 if entity @s[x=402.7,dx=3,y=60.6,dy=4,z=3625.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:6,total:15}
# Ring 7  P13 408.6/64.5/3633.2
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 6 if entity @s[x=407.1,dx=3,y=63.0,dy=4,z=3631.7,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:7,total:15}
# Ring 8  P14 415.2/61.9/3642.5
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 7 if entity @s[x=413.7,dx=3,y=60.4,dy=4,z=3641.0,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:8,total:15}
# Ring 9  P15 424.3/61.9/3642.6
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 8 if entity @s[x=422.8,dx=3,y=60.4,dy=4,z=3641.1,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:9,total:15}
# Ring 10 P16 436.9/58.9/3635.3
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 9 if entity @s[x=435.4,dx=3,y=57.4,dy=4,z=3633.8,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:10,total:15}
# Ring 11 P17 443.6/62.2/3628.9
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 10 if entity @s[x=442.1,dx=3,y=60.7,dy=4,z=3627.4,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:11,total:15}
# Ring 12 P18 450.2/66.4/3622.6
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 11 if entity @s[x=448.7,dx=3,y=64.9,dy=4,z=3621.1,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:12,total:15}
# Ring 13 P19 460.0/61.5/3613.1
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 12 if entity @s[x=458.5,dx=3,y=60.0,dy=4,z=3611.6,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:13,total:15}
# Ring 14 P20 468.1/61.5/3605.3
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 13 if entity @s[x=466.6,dx=3,y=60.0,dy=4,z=3603.8,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:14,total:15}
# Ring 15 P21 478.4/63.0/3595.7
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 14 if entity @s[x=476.9,dx=3,y=61.5,dy=4,z=3594.2,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:15,total:15}
# FINISH — passing the LAST (15th) ring ends the race immediately (no separate finish box).
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 15 at @s run function cobblemon_initiative:sidequest/trident_race/win
# Expiry: clock hits zero.
execute if score #time ci_trace matches ..0 run function cobblemon_initiative:sidequest/trident_race/expire

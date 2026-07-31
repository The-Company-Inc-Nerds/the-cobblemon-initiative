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
# ORDERED ocean rings — an out-and-back loop off Gianna's Westwind beach. Start is the P7 buoy
# (465/61/3600, the marked "ocean start"); rings 1-3 sweep WEST into open water, ring 4 is the far
# turn, ring 5 returns, and the FINISH sits back by the buoy in front of Gianna. Boxes are ~3 wide;
# dy widened to 4 so a fast riptide pass still registers on a tick. Sea level ~y62, so boxes span
# y60.5..64.5 (swimmer at/just under the surface where Riptide fires). Gated on the immediately-prior
# counter so rings must be cleared in sequence (no skipping). Desk-placed from the P7 mark — a
# showrunner should riptide the loop once and nudge any ring that lands on a piling or dry shore.
# Ring 1 455/62/3598
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 0 if entity @s[x=453.5,dx=3,y=60.5,dy=4,z=3596.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:1,total:5}
# Ring 2 440/62/3592
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 1 if entity @s[x=438.5,dx=3,y=60.5,dy=4,z=3590.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:2,total:5}
# Ring 3 425/62/3588
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 2 if entity @s[x=423.5,dx=3,y=60.5,dy=4,z=3586.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:3,total:5}
# Ring 4 412/62/3595 (far turn)
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 3 if entity @s[x=410.5,dx=3,y=60.5,dy=4,z=3593.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:4,total:5}
# Ring 5 424/62/3606 (return)
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 4 if entity @s[x=422.5,dx=3,y=60.5,dy=4,z=3604.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/cp {n:5,total:5}
# FINISH 458/62/3603 — back by the buoy, only once all five rings are behind you.
execute as @a[tag=ci_trident_racing] if score @s ci_trace_cp matches 5 at @s if entity @s[x=456.5,dx=3,y=60.5,dy=4,z=3601.5,dz=3] run function cobblemon_initiative:sidequest/trident_race/win
# Expiry: clock hits zero.
execute if score #time ci_trace matches ..0 run function cobblemon_initiative:sidequest/trident_race/expire

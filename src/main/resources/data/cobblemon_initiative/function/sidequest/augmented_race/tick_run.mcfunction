# Augmented Ring Run — active-run tick: countdown, bossbar, warning pings, ORDERED tower rings, floor-fail,
# expiry. 45 s clock (900 ticks). Warning thresholds: 45/30/20/10/5/3/2/1 s = 900/600/400/200/100/60/40/20 ticks.
scoreboard players remove #time ci_augrace 1
scoreboard players operation #secs ci_augrace = #time ci_augrace
scoreboard players operation #secs ci_augrace /= #twenty ci_augrace
execute store result bossbar cobblemon_initiative:augmented_race value run scoreboard players get #secs ci_augrace
execute if score #time ci_augrace matches 600 run title @a[tag=ci_aug_racing] actionbar [{"text":"30 seconds","color":"yellow"}]
execute if score #time ci_augrace matches 600 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 0.9
execute if score #time ci_augrace matches 400 run title @a[tag=ci_aug_racing] actionbar [{"text":"20 seconds","color":"yellow"}]
execute if score #time ci_augrace matches 400 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.hat player @s ~ ~ ~ 1 1
execute if score #time ci_augrace matches 200 run title @a[tag=ci_aug_racing] actionbar [{"text":"10 seconds","color":"gold"}]
execute if score #time ci_augrace matches 200 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1
execute if score #time ci_augrace matches 100 run title @a[tag=ci_aug_racing] actionbar [{"text":"5","color":"red"}]
execute if score #time ci_augrace matches 100 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.4
execute if score #time ci_augrace matches 60 run title @a[tag=ci_aug_racing] actionbar [{"text":"3","color":"red"}]
execute if score #time ci_augrace matches 60 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.6
execute if score #time ci_augrace matches 40 run title @a[tag=ci_aug_racing] actionbar [{"text":"2","color":"red"}]
execute if score #time ci_augrace matches 40 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1.8
execute if score #time ci_augrace matches 20 run title @a[tag=ci_aug_racing] actionbar [{"text":"1","color":"dark_red","bold":true}]
execute if score #time ci_augrace matches 20 as @a[tag=ci_aug_racing] at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 2
# Ring marker at the current target ring (that runner only).
execute as @a[tag=ci_aug_racing] at @s run function cobblemon_initiative:sidequest/augmented_race/rings
# ORDERED tower rings — a vertical parkour threading the Cyber City tower cluster (x1421-1638 / y88-178 /
# z1073-1286). Start is Arlo's rig deck (~1445/178/1268); ring 1 sits just off the deck, the run plunges
# the antenna stack, crosses the plaza and the HQ-approach towers, climbs the far spires and returns up
# the central shaft to finish back at Arlo. Boxes are 4x5x4 (dx/dz=4, dy=5) — widened for the augment's
# faster/higher arcs so a fast pass still registers on a tick. Gated on the immediately-prior counter so
# rings must be cleared in sequence (no skipping). Marker 21 does NOT exist (the showrunner's mark stack
# skips it) — pass order runs 1..37 over markers 1-20,22-38. Desk-placed from the marker bounds; a
# showrunner should run the augment once and nudge any ring that lands on a ledge or clips a wall.
# Ring 1/37 (marker 1) center 1459 178 1270.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 0 if entity @s[x=1457,dx=4,y=175.5,dy=5,z=1268.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:1,total:37}
# Ring 2/37 (marker 2) center 1465.5 173.6 1275.1
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 1 if entity @s[x=1463.5,dx=4,y=171.1,dy=5,z=1273.1,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:2,total:37}
# Ring 3/37 (marker 3) center 1467 156.4 1275.8
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 2 if entity @s[x=1465,dx=4,y=153.9,dy=5,z=1273.8,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:3,total:37}
# Ring 4/37 (marker 4) center 1468.8 139.1 1276.8
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 3 if entity @s[x=1466.8,dx=4,y=136.6,dy=5,z=1274.8,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:4,total:37}
# Ring 5/37 (marker 5) center 1470.7 114.4 1277.8
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 4 if entity @s[x=1468.7,dx=4,y=111.9,dy=5,z=1275.8,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:5,total:37}
# Ring 6/37 (marker 6) center 1454 91 1283
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 5 if entity @s[x=1452,dx=4,y=88.5,dy=5,z=1281,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:6,total:37}
# Ring 7/37 (marker 7) center 1434 89 1271
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 6 if entity @s[x=1432,dx=4,y=86.5,dy=5,z=1269,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:7,total:37}
# Ring 8/37 (marker 8) center 1424.5 88 1243
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 7 if entity @s[x=1422.5,dx=4,y=85.5,dy=5,z=1241,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:8,total:37}
# Ring 9/37 (marker 9) center 1471 88 1222
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 8 if entity @s[x=1469,dx=4,y=85.5,dy=5,z=1220,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:9,total:37}
# Ring 10/37 (marker 10) center 1608.5 88 1222
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 9 if entity @s[x=1606.5,dx=4,y=85.5,dy=5,z=1220,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:10,total:37}
# Ring 11/37 (marker 11) center 1632.5 88 1182.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 10 if entity @s[x=1630.5,dx=4,y=85.5,dy=5,z=1180.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:11,total:37}
# Ring 12/37 (marker 12) center 1596 88 1153.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 11 if entity @s[x=1594,dx=4,y=85.5,dy=5,z=1151.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:12,total:37}
# Ring 13/37 (marker 13) center 1552 97 1170
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 12 if entity @s[x=1550,dx=4,y=94.5,dy=5,z=1168,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:13,total:37}
# Ring 14/37 (marker 14) center 1544 111 1221.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 13 if entity @s[x=1542,dx=4,y=108.5,dy=5,z=1219.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:14,total:37}
# Ring 15/37 (marker 15) center 1501 121 1223
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 14 if entity @s[x=1499,dx=4,y=118.5,dy=5,z=1221,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:15,total:37}
# Ring 16/37 (marker 16) center 1495 132 1195
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 15 if entity @s[x=1493,dx=4,y=129.5,dy=5,z=1193,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:16,total:37}
# Ring 17/37 (marker 17) center 1554 141 1195
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 16 if entity @s[x=1552,dx=4,y=138.5,dy=5,z=1193,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:17,total:37}
# Ring 18/37 (marker 18) center 1582 141 1179
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 17 if entity @s[x=1580,dx=4,y=138.5,dy=5,z=1177,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:18,total:37}
# Ring 19/37 (marker 19) center 1578 141 1083.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 18 if entity @s[x=1576,dx=4,y=138.5,dy=5,z=1081.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:19,total:37}
# Ring 20/37 (marker 20) center 1529 141 1076.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 19 if entity @s[x=1527,dx=4,y=138.5,dy=5,z=1074.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:20,total:37}
# Ring 21/37 (marker 22) center 1499.5 142 1097
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 20 if entity @s[x=1497.5,dx=4,y=139.5,dy=5,z=1095,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:21,total:37}
# Ring 22/37 (marker 23) center 1504 132.5 1118
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 21 if entity @s[x=1502,dx=4,y=130,dy=5,z=1116,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:22,total:37}
# Ring 23/37 (marker 24) center 1499.5 88 1143
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 22 if entity @s[x=1497.5,dx=4,y=85.5,dy=5,z=1141,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:23,total:37}
# Ring 24/37 (marker 25) center 1488 90 1189.5
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 23 if entity @s[x=1486,dx=4,y=87.5,dy=5,z=1187.5,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:24,total:37}
# Ring 25/37 (marker 26) center 1474.5 90 1208
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 24 if entity @s[x=1472.5,dx=4,y=87.5,dy=5,z=1206,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:25,total:37}
# Ring 26/37 (marker 27) center 1461 92.5 1249
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 25 if entity @s[x=1459,dx=4,y=90,dy=5,z=1247,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:26,total:37}
# Ring 27/37 (marker 28) center 1462 91 1269
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 26 if entity @s[x=1460,dx=4,y=88.5,dy=5,z=1267,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:27,total:37}
# Ring 28/37 (marker 29) center 1457.5 94.5 1283
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 27 if entity @s[x=1455.5,dx=4,y=92,dy=5,z=1281,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:28,total:37}
# Ring 29/37 (marker 30) center 1467 105 1258
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 28 if entity @s[x=1465,dx=4,y=102.5,dy=5,z=1256,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:29,total:37}
# Ring 30/37 (marker 31) center 1467 114 1258
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 29 if entity @s[x=1465,dx=4,y=111.5,dy=5,z=1256,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:30,total:37}
# Ring 31/37 (marker 32) center 1467 123 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 30 if entity @s[x=1465,dx=4,y=120.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:31,total:37}
# Ring 32/37 (marker 33) center 1467 132 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 31 if entity @s[x=1465,dx=4,y=129.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:32,total:37}
# Ring 33/37 (marker 34) center 1467 141 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 32 if entity @s[x=1465,dx=4,y=138.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:33,total:37}
# Ring 34/37 (marker 35) center 1467 150 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 33 if entity @s[x=1465,dx=4,y=147.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:34,total:37}
# Ring 35/37 (marker 36) center 1467 159 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 34 if entity @s[x=1465,dx=4,y=156.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:35,total:37}
# Ring 36/37 (marker 37) center 1467 168 1259
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 35 if entity @s[x=1465,dx=4,y=165.5,dy=5,z=1257,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:36,total:37}
# Ring 37/37 (marker 38) center 1451 177 1268 — the last ring, back on Arlo's deck.
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 36 if entity @s[x=1449,dx=4,y=174.5,dy=5,z=1266,dz=4] run function cobblemon_initiative:sidequest/augmented_race/cp {n:37,total:37}
# FINISH — passing the LAST (37th) ring ends the race immediately (no separate finish box; cp reaches 37
# and this line fires the same pass).
execute as @a[tag=ci_aug_racing] if score @s ci_augrace_cp matches 37 at @s run function cobblemon_initiative:sidequest/augmented_race/win
# FLOOR FAIL — a fall off the towers below the course floor (y80; plaza is y88) fails the run and
# returns the runner to Arlo's rig deck. Per-runner so one player's fall never yanks another.
execute as @a[tag=ci_aug_racing] at @s if entity @s[y=-64,dy=144] run function cobblemon_initiative:sidequest/augmented_race/fail
# Expiry: clock hits zero.
execute if score #time ci_augrace matches ..0 run function cobblemon_initiative:sidequest/augmented_race/expire

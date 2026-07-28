# Orc encampments — per-tick driver (registered in #minecraft:tick). Two cheap jobs per
# camp, each behind ONE latch score check so idle camps cost a single failed matches test:
#
# 1) RAISE: latch 0 + any player within 48 of the pin -> summon the camp (spawn fn sets
#    the latch to 1 first, so this can never double-fire even inside one tick).
# 2) CLEARED POLL: latch 1 + player still within 48 (chunk-load guard — PersistenceRequired
#    mobs in UNLOADED chunks are invisible to selectors, so polling without a nearby player
#    would false-clear every camp the moment the raiser walked away) + no tagged camp mob
#    within r=32 of the pin -> one-shot cleared ceremony (latch -> 2).
# Selectors are typed via type=!minecraft:player (camps mix vindicator + husk; bare @e is
# banned by house law). Positioned at the pin so distance anchors on the camp, not the
# function origin. All eight pins are wilderness mountains — safe-zone rule satisfied.
execute if score #orc_camp_p2 ci_ambient matches 0 if entity @a[x=1299.5,y=212,z=3649.5,distance=..48] positioned 1299.5 212 3649.5 run function cobblemon_initiative:orc/spawn_camp_p2
execute if score #orc_camp_p2 ci_ambient matches 1 if entity @a[x=1299.5,y=212,z=3649.5,distance=..48] positioned 1299.5 212 3649.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p2,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p2
execute if score #orc_camp_p3 ci_ambient matches 0 if entity @a[x=1467.5,y=167,z=3554.5,distance=..48] positioned 1467.5 167 3554.5 run function cobblemon_initiative:orc/spawn_camp_p3
execute if score #orc_camp_p3 ci_ambient matches 1 if entity @a[x=1467.5,y=167,z=3554.5,distance=..48] positioned 1467.5 167 3554.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p3,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p3
execute if score #orc_camp_p4 ci_ambient matches 0 if entity @a[x=1604.5,y=184,z=3511.5,distance=..48] positioned 1604.5 184 3511.5 run function cobblemon_initiative:orc/spawn_camp_p4
execute if score #orc_camp_p4 ci_ambient matches 1 if entity @a[x=1604.5,y=184,z=3511.5,distance=..48] positioned 1604.5 184 3511.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p4,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p4
execute if score #orc_camp_p5 ci_ambient matches 0 if entity @a[x=830.5,y=167,z=3154.5,distance=..48] positioned 830.5 167 3154.5 run function cobblemon_initiative:orc/spawn_camp_p5
execute if score #orc_camp_p5 ci_ambient matches 1 if entity @a[x=830.5,y=167,z=3154.5,distance=..48] positioned 830.5 167 3154.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p5,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p5
execute if score #orc_camp_p6 ci_ambient matches 0 if entity @a[x=644.5,y=139,z=3124.5,distance=..48] positioned 644.5 139 3124.5 run function cobblemon_initiative:orc/spawn_camp_p6
execute if score #orc_camp_p6 ci_ambient matches 1 if entity @a[x=644.5,y=139,z=3124.5,distance=..48] positioned 644.5 139 3124.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p6,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p6
execute if score #orc_camp_p7 ci_ambient matches 0 if entity @a[x=825.5,y=137,z=3297.5,distance=..48] positioned 825.5 137 3297.5 run function cobblemon_initiative:orc/spawn_camp_p7
execute if score #orc_camp_p7 ci_ambient matches 1 if entity @a[x=825.5,y=137,z=3297.5,distance=..48] positioned 825.5 137 3297.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p7,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p7
execute if score #orc_camp_p8 ci_ambient matches 0 if entity @a[x=932.5,y=114,z=3468.5,distance=..48] positioned 932.5 114 3468.5 run function cobblemon_initiative:orc/spawn_camp_p8
execute if score #orc_camp_p8 ci_ambient matches 1 if entity @a[x=932.5,y=114,z=3468.5,distance=..48] positioned 932.5 114 3468.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p8,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p8
execute if score #orc_camp_p9 ci_ambient matches 0 if entity @a[x=790.5,y=63,z=3403.5,distance=..48] positioned 790.5 63 3403.5 run function cobblemon_initiative:orc/spawn_camp_p9
execute if score #orc_camp_p9 ci_ambient matches 1 if entity @a[x=790.5,y=63,z=3403.5,distance=..48] positioned 790.5 63 3403.5 unless entity @e[type=!minecraft:player,tag=orc_camp_p9,distance=..32] run function cobblemon_initiative:orc/camp_cleared_p9

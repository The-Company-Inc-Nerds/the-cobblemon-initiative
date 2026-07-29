# Orc encampments — per-tick driver (registered in #minecraft:tick). a22 REDESIGN: ONE rotating
# camp. Day-latch (economy/dawn idiom): on a day change, if the previous camp is cleared
# (#orc_active 0), roll a new random pin (orc/relocate). Then per-pin RAISE + CLEARED, each gated on
# #orc_active == that pin so only the active pin is ever live. RAISE: pin active, not yet spawned
# (#orc_raised 0), a player within 48 -> orc/spawn_camp (sets #orc_raised 1 first). CLEARED poll:
# spawned + a player near (chunk-load guard — unloaded PersistenceRequired bodies vanish from
# selectors, so polling without a nearby player false-clears) + no ci_orc mob within 40 -> orc/camp_cleared.
# Selectors typed type=!minecraft:player (orcs mix easy_npc:orc + orc_warrior; bare @e is house-banned).
execute store result score #orc_day ci_ambient run time query day
execute unless score #orc_day ci_ambient = #orc_last_day ci_ambient if score #orc_active ci_ambient matches 0 run function cobblemon_initiative:orc/relocate
scoreboard players operation #orc_last_day ci_ambient = #orc_day ci_ambient

execute if score #orc_active ci_ambient matches 2 if score #orc_raised ci_ambient matches 0 if entity @a[x=1299.5,y=212,z=3649.5,distance=..48] positioned 1299.5 212 3649.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 2 if score #orc_raised ci_ambient matches 1 if entity @a[x=1299.5,y=212,z=3649.5,distance=..48] positioned 1299.5 212 3649.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 3 if score #orc_raised ci_ambient matches 0 if entity @a[x=1467.5,y=167,z=3554.5,distance=..48] positioned 1467.5 167 3554.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 3 if score #orc_raised ci_ambient matches 1 if entity @a[x=1467.5,y=167,z=3554.5,distance=..48] positioned 1467.5 167 3554.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 4 if score #orc_raised ci_ambient matches 0 if entity @a[x=1604.5,y=184,z=3511.5,distance=..48] positioned 1604.5 184 3511.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 4 if score #orc_raised ci_ambient matches 1 if entity @a[x=1604.5,y=184,z=3511.5,distance=..48] positioned 1604.5 184 3511.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 5 if score #orc_raised ci_ambient matches 0 if entity @a[x=830.5,y=167,z=3154.5,distance=..48] positioned 830.5 167 3154.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 5 if score #orc_raised ci_ambient matches 1 if entity @a[x=830.5,y=167,z=3154.5,distance=..48] positioned 830.5 167 3154.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 6 if score #orc_raised ci_ambient matches 0 if entity @a[x=644.5,y=139,z=3124.5,distance=..48] positioned 644.5 139 3124.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 6 if score #orc_raised ci_ambient matches 1 if entity @a[x=644.5,y=139,z=3124.5,distance=..48] positioned 644.5 139 3124.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 7 if score #orc_raised ci_ambient matches 0 if entity @a[x=825.5,y=137,z=3297.5,distance=..48] positioned 825.5 137 3297.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 7 if score #orc_raised ci_ambient matches 1 if entity @a[x=825.5,y=137,z=3297.5,distance=..48] positioned 825.5 137 3297.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 8 if score #orc_raised ci_ambient matches 0 if entity @a[x=932.5,y=114,z=3468.5,distance=..48] positioned 932.5 114 3468.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 8 if score #orc_raised ci_ambient matches 1 if entity @a[x=932.5,y=114,z=3468.5,distance=..48] positioned 932.5 114 3468.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared
execute if score #orc_active ci_ambient matches 9 if score #orc_raised ci_ambient matches 0 if entity @a[x=790.5,y=63,z=3403.5,distance=..48] positioned 790.5 63 3403.5 run function cobblemon_initiative:orc/spawn_camp
execute if score #orc_active ci_ambient matches 9 if score #orc_raised ci_ambient matches 1 if entity @a[x=790.5,y=63,z=3403.5,distance=..48] positioned 790.5 63 3403.5 unless entity @e[type=!minecraft:player,tag=ci_orc,distance=..64] run function cobblemon_initiative:orc/camp_cleared

# repairs wave a8 — apply: see arm.

execute positioned 1998 70 921 run kill @e[type=!minecraft:player,name="Dragon Acolyte Wyrm",distance=..48]
scoreboard players reset #amb_dragon_shrine_cultist_1 ci_ambient
execute positioned 2001 65 924 run kill @e[type=!minecraft:player,name="Dragon Zealot Scale",distance=..48]
scoreboard players reset #amb_dragon_shrine_cultist_2 ci_ambient
execute positioned 947 4 2715 run kill @e[type=!minecraft:player,name="Fae Acolyte Pixie",distance=..48]
scoreboard players reset #amb_fairy_shrine_cultist_1 ci_ambient
execute positioned 950 8 2718 run kill @e[type=!minecraft:player,name="Fae Zealot Sprite",distance=..48]
scoreboard players reset #amb_fairy_shrine_cultist_2 ci_ambient
execute positioned 3500 51 4702 run kill @e[type=!minecraft:player,name="Flame Acolyte Pyra",distance=..48]
scoreboard players reset #amb_fire_shrine_cultist_1 ci_ambient
execute positioned 3503 55 4705 run kill @e[type=!minecraft:player,name="Flame Zealot Cinder",distance=..48]
scoreboard players reset #amb_fire_shrine_cultist_2 ci_ambient
execute positioned 1899 80 4049 run kill @e[type=!minecraft:player,name="Earth Acolyte Clay",distance=..48]
scoreboard players reset #amb_ground_shrine_cultist_1 ci_ambient
execute positioned 1902 88 4052 run kill @e[type=!minecraft:player,name="Earth Zealot Stone",distance=..48]
scoreboard players reset #amb_ground_shrine_cultist_2 ci_ambient
execute positioned 3634 69 1960 run kill @e[type=!minecraft:player,name="Frost Acolyte Neve",distance=..48]
scoreboard players reset #amb_ice_shrine_cultist_1 ci_ambient
execute positioned 3637 74 1963 run kill @e[type=!minecraft:player,name="Frost Zealot Hail",distance=..48]
scoreboard players reset #amb_ice_shrine_cultist_2 ci_ambient
forceload remove 944 2704
forceload remove 1888 4048
forceload remove 1984 912
forceload remove 2000 912
forceload remove 3488 4688
forceload remove 3488 4704
forceload remove 3632 1952
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a8: shrine cultists retired — the keepers hold the trials now.","color":"gray"}]

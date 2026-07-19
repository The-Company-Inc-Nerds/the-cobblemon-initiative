# repairs wave a7 — apply: kill stale bodies at the OLD buried sites, re-arm latches.

execute positioned 950 8 2718 run kill @e[type=!minecraft:player,name="Fae Zealot Sprite",distance=..48]
scoreboard players set #amb_fairy_shrine_cultist_2 ci_ambient 0
execute positioned 1902 88 4052 run kill @e[type=!minecraft:player,name="Earth Zealot Stone",distance=..48]
scoreboard players set #amb_ground_shrine_cultist_2 ci_ambient 0
execute positioned 3634 69 1960 run kill @e[type=!minecraft:player,name="Frost Acolyte Neve",distance=..48]
scoreboard players set #amb_ice_shrine_cultist_1 ci_ambient 0
execute positioned 3637 74 1963 run kill @e[type=!minecraft:player,name="Frost Zealot Hail",distance=..48]
scoreboard players set #amb_ice_shrine_cultist_2 ci_ambient 0
execute positioned 947 4 2715 run kill @e[type=!minecraft:player,name="Fae Acolyte Pixie",distance=..48]
scoreboard players set #amb_fairy_shrine_cultist_1 ci_ambient 0
execute positioned 3503 55 4705 run kill @e[type=!minecraft:player,name="Flame Zealot Cinder",distance=..48]
scoreboard players set #amb_fire_shrine_cultist_2 ci_ambient 0
execute positioned 2001 65 924 run kill @e[type=!minecraft:player,name="Dragon Zealot Scale",distance=..48]
scoreboard players set #amb_dragon_shrine_cultist_2 ci_ambient 0
execute positioned 945 9 2712 run kill @e[type=!minecraft:player,name="The Last Pilgrim",distance=..48]
scoreboard players set #amb_shrine_pilgrim ci_ambient 0
execute positioned 1998 70 921 run kill @e[type=!minecraft:player,name="Dragon Acolyte Wyrm",distance=..48]
scoreboard players set #amb_dragon_shrine_cultist_1 ci_ambient 0
execute positioned 1899 80 4049 run kill @e[type=!minecraft:player,name="Earth Acolyte Clay",distance=..48]
scoreboard players set #amb_ground_shrine_cultist_1 ci_ambient 0
execute positioned 1393 77 1065 run kill @e[type=!minecraft:player,name="Grid Warden Cass",distance=..48]
scoreboard players set #amb_noble_giver_zapdos_warden ci_ambient 0
execute positioned 2760 63 3490 run kill @e[type=!minecraft:player,name="The Deep Chamber",distance=..48]
scoreboard players set #amb_manaphy_giver ci_ambient 0
forceload remove 944 2704
forceload remove 1392 1056
forceload remove 1888 4048
forceload remove 1984 912
forceload remove 2000 912
forceload remove 2752 3488
forceload remove 3488 4704
forceload remove 3632 1952
tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a7: shrine + noble-giver casts re-grounded (12 NPCs re-latch at probed surface).","color":"gray"}]

# repairs wave a6 — apply (scheduled by repairs_a6_arm): kill each stale spec-cast
# body at its OLD (buried/floating) site by display name, re-arm its latch, then
# drop the forceloads. The latch re-spawns the body at the corrected dialog-src
# placement on the next player approach (within 40b).

execute positioned 1518 89 1096 run kill @e[type=!minecraft:player,name="Archive Drop",distance=..48]
scoreboard players set #amb_cyber_archive_1 ci_ambient 0
execute positioned 1524 90 1104 run kill @e[type=!minecraft:player,name="Archive Drop",distance=..48]
scoreboard players set #amb_cyber_archive_2 ci_ambient 0
execute positioned 1512 89 1092 run kill @e[type=!minecraft:player,name="Archive Drop",distance=..48]
scoreboard players set #amb_cyber_archive_3 ci_ambient 0
execute positioned 1490 90 1132 run kill @e[type=!minecraft:player,name="Glitching Billboard",distance=..48]
scoreboard players set #amb_cyber_board_1 ci_ambient 0
execute positioned 1560 91 1090 run kill @e[type=!minecraft:player,name="Glitching Billboard",distance=..48]
scoreboard players set #amb_cyber_board_2 ci_ambient 0
execute positioned 1610 92 1110 run kill @e[type=!minecraft:player,name="Glitching Billboard",distance=..48]
scoreboard players set #amb_cyber_board_3 ci_ambient 0
execute positioned 1520 89 1100 run kill @e[type=!minecraft:player,name="Off-Records Clerk Maren",distance=..48]
scoreboard players set #amb_cyber_defector_maren ci_ambient 0
execute positioned 1500 91 1120 run kill @e[type=!minecraft:player,name="Verified Value Teller",distance=..48]
scoreboard players set #amb_cyber_exchange_teller ci_ambient 0
execute positioned 1555 92 1108 run kill @e[type=!minecraft:player,name="Grid Broker Ohmond",distance=..48]
scoreboard players set #amb_cyber_grid_broker ci_ambient 0
execute positioned 1478 89 1150 run kill @e[type=!minecraft:player,name="Nurse Ampere",distance=..48]
scoreboard players set #amb_cyber_nurse_rumor ci_ambient 0
execute positioned 1490 91 1128 run kill @e[type=!minecraft:player,name="Reserve Tag",distance=..48]
scoreboard players set #amb_cyber_reserve_1 ci_ambient 0
execute positioned 1560 93 1092 run kill @e[type=!minecraft:player,name="Reserve Tag",distance=..48]
scoreboard players set #amb_cyber_reserve_2 ci_ambient 0
execute positioned 1610 92 1112 run kill @e[type=!minecraft:player,name="Reserve Tag",distance=..48]
scoreboard players set #amb_cyber_reserve_3 ci_ambient 0
execute positioned 1470 89 1140 run kill @e[type=!minecraft:player,name="Signal Tech Rell",distance=..48]
scoreboard players set #amb_cyber_signal_tech ci_ambient 0
execute positioned 985 114 3120 run kill @e[type=!minecraft:player,name="Abandoned Branch Office",distance=..48]
scoreboard players set #amb_deepcore_deep_office ci_ambient 0
execute positioned 1120 111 3300 run kill @e[type=!minecraft:player,name="Foreman Kang",distance=..48]
scoreboard players set #amb_deepcore_foreman_kang ci_ambient 0
execute positioned 1060 119 3200 run kill @e[type=!minecraft:player,name="Old Dun",distance=..48]
scoreboard players set #amb_deepcore_ladder_barker ci_ambient 0
execute positioned 1152 146 3284 run kill @e[type=!minecraft:player,name="Re-Verified Reserve Ledger",distance=..48]
scoreboard players set #amb_deepcore_ledger_board ci_ambient 0
execute positioned 1100 111 3215 run kill @e[type=!minecraft:player,name="Sten Vale",distance=..48]
scoreboard players set #amb_deepcore_martkeeper ci_ambient 0
execute positioned 1110 109 3230 run kill @e[type=!minecraft:player,name="Miner Rill",distance=..48]
scoreboard players set #amb_deepcore_miner_rill ci_ambient 0
execute positioned 1092 114 3208 run kill @e[type=!minecraft:player,name="Nurse Rurik",distance=..48]
scoreboard players set #amb_deepcore_nurse ci_ambient 0
execute positioned 566 84 3560 run kill @e[type=!minecraft:player,name="Dockmaster Kaito",distance=..48]
scoreboard players set #amb_gaviota_dockmaster ci_ambient 0
execute positioned 552 81 3552 run kill @e[type=!minecraft:player,name="Fence Odessa",distance=..48]
scoreboard players set #amb_gaviota_fence ci_ambient 0
execute positioned 605 93 3660 run kill @e[type=!minecraft:player,name="Netmender Bosun Rui",distance=..48]
scoreboard players set #amb_gaviota_fisher ci_ambient 0
execute positioned 566 85 3564 run kill @e[type=!minecraft:player,name="Tally Clerk Pell",distance=..48]
scoreboard players set #amb_gaviota_manifest_a ci_ambient 0
execute positioned 588 86 3600 run kill @e[type=!minecraft:player,name="Tally Clerk Odile",distance=..48]
scoreboard players set #amb_gaviota_manifest_b ci_ambient 0
execute positioned 605 87 3650 run kill @e[type=!minecraft:player,name="Tally Clerk Bram",distance=..48]
scoreboard players set #amb_gaviota_manifest_c ci_ambient 0
execute positioned 560 82 3540 run kill @e[type=!minecraft:player,name="Nurse Coralie",distance=..48]
scoreboard players set #amb_gaviota_nurse ci_ambient 0
execute positioned 700 103 3255 run kill @e[type=!minecraft:player,name="Sable",distance=..48]
scoreboard players set #amb_gaviota_smuggler ci_ambient 0
execute positioned 580 86 3600 run kill @e[type=!minecraft:player,name="Dock Hand Mattias",distance=..48]
scoreboard players set #amb_gaviota_union_hand ci_ambient 0
execute positioned 1972 117 3945 run kill @e[type=!minecraft:player,name="Grain Factor",distance=..48]
scoreboard players set #amb_grain_factor_kalahar ci_ambient 0
execute positioned 2040 136 4100 run kill @e[type=!minecraft:player,name="Well-Keeper Marisol",distance=..48]
scoreboard players set #amb_kalahar_rumor_marisol ci_ambient 0
execute positioned 1980 120 3960 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_1 ci_ambient 0
execute positioned 2140 132 3900 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_2 ci_ambient 0
execute positioned 2318 83 3542 run kill @e[type=!minecraft:player,name="Guarded Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_3 ci_ambient 0
execute positioned 1076 66 2452 run kill @e[type=!minecraft:player,name="Charm-Weaver Marigold",distance=..48]
scoreboard players set #amb_mm_charm_seller ci_ambient 0
execute positioned 1082 66 2448 run kill @e[type=!minecraft:player,name="Verified Clerk Osric",distance=..48]
scoreboard players set #amb_mm_exchange_clerk ci_ambient 0
execute positioned 1229 90 2820 run kill @e[type=!minecraft:player,name="Steward Halvard",distance=..48]
scoreboard players set #amb_mm_field_guard ci_ambient 0
execute positioned 1068 65 2465 run kill @e[type=!minecraft:player,name="Fen-Nurse Wisteria",distance=..48]
scoreboard players set #amb_mm_nurse ci_ambient 0
execute positioned 1058 78 2478 run kill @e[type=!minecraft:player,name="Sedge",distance=..48]
scoreboard players set #amb_mm_wheat_trader ci_ambient 0
execute positioned 1064 65 2470 run kill @e[type=!minecraft:player,name="Marsh-Child Bryn",distance=..48]
scoreboard players set #amb_mm_will_o_wisp_child ci_ambient 0
execute positioned 1150 146 3282 run kill @e[type=!minecraft:player,name="Roderick",distance=..48]
scoreboard players set #amb_sq_deepcore_assessor ci_ambient 0
execute positioned 2050 129 4085 run kill @e[type=!minecraft:player,name="Warden Ossa",distance=..48]
scoreboard players set #amb_warden_ossa ci_ambient 0
execute positioned 1180 123 3260 run kill @e[type=!minecraft:player,name="Corliss",distance=..48]
scoreboard players set #amb_wheat_trader_deepcore ci_ambient 0
forceload remove 544 3552
forceload remove 560 3536
forceload remove 560 3552
forceload remove 576 3600
forceload remove 592 3648
forceload remove 688 3248
forceload remove 976 3120
forceload remove 1056 2464
forceload remove 1056 3200
forceload remove 1072 2448
forceload remove 1088 3200
forceload remove 1104 3216
forceload remove 1120 3296
forceload remove 1136 3280
forceload remove 1152 3280
forceload remove 1168 3248
forceload remove 1216 2816
forceload remove 1456 1136
forceload remove 1472 1136
forceload remove 1488 1120
forceload remove 1504 1088
forceload remove 1520 1088
forceload remove 1520 1104
forceload remove 1552 1088
forceload remove 1552 1104
forceload remove 1600 1104
forceload remove 1968 3936
forceload remove 1968 3952
forceload remove 2032 4096
forceload remove 2048 4080
forceload remove 2128 3888
forceload remove 2304 3536
tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a6: town-pack cast re-grounded (44 NPCs re-latch at probed surface).","color":"gray"}]

# repairs wave a9 — apply (scheduled by repairs_a9_arm): skin dress pass repaint.
# Kill each stale undressed body at its authored site by display name, re-arm its
# latch, then drop the forceloads. The latch re-spawns the body from the freshly
# compiled preset (now carrying SkinData) on the next player approach.

execute positioned 1738.5 114 4192.5 run kill @e[type=!minecraft:player,name="Site Foreman",distance=..48]
scoreboard players set #amb_agent_pump_foreman ci_ambient 0
execute positioned 1742.5 114 4192.5 run kill @e[type=!minecraft:player,name="Yield Officer",distance=..48]
scoreboard players set #amb_agent_pump_officer ci_ambient 0
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
execute positioned 1092 114 3208 run kill @e[type=!minecraft:player,name="Nurse Rilka",distance=..48]
scoreboard players set #amb_deepcore_nurse ci_ambient 0
execute positioned 2004 71 919 run kill @e[type=!minecraft:player,name="High Priest Draconis",distance=..48]
scoreboard players set #amb_dragon_shrine_leader ci_ambient 0
execute positioned 951 3 2715 run kill @e[type=!minecraft:player,name="High Priestess Aurora",distance=..48]
scoreboard players set #amb_fairy_shrine_leader ci_ambient 0
execute positioned 3510 51 4702 run kill @e[type=!minecraft:player,name="High Priest Ignis",distance=..48]
scoreboard players set #amb_fire_shrine_leader ci_ambient 0
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
execute positioned 1498 86 2050 run kill @e[type=!minecraft:player,name="A Clean Square of Wall",distance=..48]
scoreboard players set #amb_greenspace_plaque_square ci_ambient 0
execute positioned 1910 89 4049 run kill @e[type=!minecraft:player,name="High Priest Terran",distance=..48]
scoreboard players set #amb_ground_shrine_leader ci_ambient 0
execute positioned 1512 85 2082 run kill @e[type=!minecraft:player,name="Bo Huan",distance=..48]
scoreboard players set #amb_hz_trader_apricorns ci_ambient 0
execute positioned 1538 86 2064 run kill @e[type=!minecraft:player,name="Auntie Song",distance=..48]
scoreboard players set #amb_hz_trader_berries ci_ambient 0
execute positioned 1488 87 2090 run kill @e[type=!minecraft:player,name="Madam Qiu",distance=..48]
scoreboard players set #amb_hz_trader_mints ci_ambient 0
execute positioned 3644 81 1960 run kill @e[type=!minecraft:player,name="High Priest Glacius",distance=..48]
scoreboard players set #amb_ice_shrine_leader ci_ambient 0
execute positioned 2058 126 4075 run kill @e[type=!minecraft:player,name="Nurse Sabine",distance=..48]
scoreboard players set #amb_kalahar_nurse ci_ambient 0
execute positioned 2040 136 4100 run kill @e[type=!minecraft:player,name="Well-Keeper Marisol",distance=..48]
scoreboard players set #amb_kalahar_rumor_marisol ci_ambient 0
execute positioned 1980 120 3960 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_1 ci_ambient 0
execute positioned 2140 132 3900 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_2 ci_ambient 0
execute positioned 2318 83 3542 run kill @e[type=!minecraft:player,name="Guarded Survey Stone",distance=..48]
scoreboard players set #amb_kalahar_survey_stone_3 ci_ambient 0
execute positioned 2760.5 63 3490.5 run kill @e[type=!minecraft:player,name="The Deep Chamber",distance=..48]
scoreboard players set #amb_manaphy_giver ci_ambient 0
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
execute positioned 3462 66 2016 run kill @e[type=!minecraft:player,name="Old Halla",distance=..48]
scoreboard players set #amb_nifl_civilian_frost ci_ambient 0
execute positioned 3520 68 1982 run kill @e[type=!minecraft:player,name="Auditor Corvin",distance=..48]
scoreboard players set #amb_nifl_cold_auditor ci_ambient 0
execute positioned 3500 65 1960 run kill @e[type=!minecraft:player,name="Keeper Vetra",distance=..48]
scoreboard players set #amb_nifl_keeper_vetra ci_ambient 0
execute positioned 3480 66 2010 run kill @e[type=!minecraft:player,name="Kestrel Vane",distance=..48]
scoreboard players set #amb_nifl_martkeeper ci_ambient 0
execute positioned 3470 66 2000 run kill @e[type=!minecraft:player,name="Nurse Sabra",distance=..48]
scoreboard players set #amb_nifl_nurse ci_ambient 0
execute positioned 3540 68 1975 run kill @e[type=!minecraft:player,name="Records Officer Halden",distance=..48]
scoreboard players set #amb_nifl_records_officer ci_ambient 0
execute positioned 3450 70 2030 run kill @e[type=!minecraft:player,name="Warrant Officer Dain",distance=..48]
scoreboard players set #amb_nifl_warrant_officer ci_ambient 0
execute positioned 1788.5 114 4212.5 run kill @e[type=!minecraft:player,name="A Giggle in the Grass",distance=..48]
scoreboard players set #amb_noble_giver_mew_wisp ci_ambient 0
execute positioned 1393 77 1065 run kill @e[type=!minecraft:player,name="Grid Warden Cass",distance=..48]
scoreboard players set #amb_noble_giver_zapdos_warden ci_ambient 0
execute positioned 3805 302 3806 run kill @e[type=!minecraft:player,name="Crater Warding Stone",distance=..48]
scoreboard players set #amb_noble_monument_groudon ci_ambient 0
execute positioned 234 65 2347 run kill @e[type=!minecraft:player,name="Warning Buoy",distance=..48]
scoreboard players set #amb_noble_monument_kyogre ci_ambient 0
execute positioned 744 77 4589 run kill @e[type=!minecraft:player,name="Sky-Altar",distance=..48]
scoreboard players set #amb_noble_monument_rayquaza ci_ambient 0
execute positioned 1740.5 116 4190.5 run kill @e[type=!minecraft:player,name="Pump Manifold",distance=..48]
scoreboard players set #amb_oasis_pump_manifold ci_ambient 0
execute positioned 1525 64 1698 run kill @e[type=!minecraft:player,name="Grid-Reader Chike",distance=..48]
scoreboard players set #amb_r12_lorekeeper_pylon ci_ambient 0
execute positioned 1520 64 1710 run kill @e[type=!minecraft:player,name="Line Tech Volta",distance=..48]
scoreboard players set #amb_r12_spotter_pylon ci_ambient 0
execute positioned 1975 64 1036 run kill @e[type=!minecraft:player,name="Dragonrider Kaen",distance=..48]
scoreboard players set #amb_r13_trainer_spine ci_ambient 0
execute positioned 1968 64 1044 run kill @e[type=!minecraft:player,name="Ashen Pilgrim Uzo",distance=..48]
scoreboard players set #amb_r13_traveler_pilgrim ci_ambient 0
execute positioned 3049 64 2480 run kill @e[type=!minecraft:player,name="Snowline Hermit Bran",distance=..48]
scoreboard players set #amb_r14_flavor_hermit ci_ambient 0
execute positioned 3042 64 2490 run kill @e[type=!minecraft:player,name="Frostwarden Neve",distance=..48]
scoreboard players set #amb_r14_spotter_frost ci_ambient 0
execute positioned 1503 86 2041 run kill @e[type=!minecraft:player,name="Notice of Preliminary Rezoning",distance=..48]
scoreboard players set #amb_rezoning_notice_board ci_ambient 0
execute positioned 2145 64 901 run kill @e[type=!minecraft:player,name="The Sovereign Charter",distance=..48]
scoreboard players set #amb_ryujin_charter_lectern ci_ambient 0
execute positioned 2143 64 902 run kill @e[type=!minecraft:player,name="Heritage Acquisitions Envoy",distance=..48]
scoreboard players set #amb_ryujin_heritage_envoy ci_ambient 0
execute positioned 2146 64 900 run kill @e[type=!minecraft:player,name="Keepwarden Hana",distance=..48]
scoreboard players set #amb_ryujin_keeper_hana ci_ambient 0
execute positioned 2160 64 890 run kill @e[type=!minecraft:player,name="A Nervous Clerk",distance=..48]
scoreboard players set #amb_ryujin_records_officer ci_ambient 0
execute positioned 2120 66 900 run kill @e[type=!minecraft:player,name="Skywatcher Rei",distance=..48]
scoreboard players set #amb_ryujin_skywatcher_rei ci_ambient 0
execute positioned 2148 64 897 run kill @e[type=!minecraft:player,name="Dragonsmith Tetsu",distance=..48]
scoreboard players set #amb_ryujin_smith_tetsu ci_ambient 0
execute positioned 3672 68 4576 run kill @e[type=!minecraft:player,name="Nurse Ember",distance=..48]
scoreboard players set #amb_scorchspire_healer ci_ambient 0
execute positioned 3640 68 4520 run kill @e[type=!minecraft:player,name="Cinders",distance=..48]
scoreboard players set #amb_scorchspire_wheat_trader ci_ambient 0
execute positioned 945 9 2712 run kill @e[type=!minecraft:player,name="The Last Pilgrim",distance=..48]
scoreboard players set #amb_shrine_pilgrim ci_ambient 0
execute positioned 3620 66 4658 run kill @e[type=!minecraft:player,name="Recovery Lead Vance",distance=..48]
scoreboard players set #amb_sq_asset_recovery_lead ci_ambient 0
execute positioned 3620 66 4662 run kill @e[type=!minecraft:player,name="Recovery Agent Doss",distance=..48]
scoreboard players set #amb_sq_asset_recovery_second ci_ambient 0
execute positioned 1150 146 3282 run kill @e[type=!minecraft:player,name="Roderick",distance=..48]
scoreboard players set #amb_sq_deepcore_assessor ci_ambient 0
execute positioned 3670 68 4560 run kill @e[type=!minecraft:player,name="Forgemaster Sena",distance=..48]
scoreboard players set #amb_sq_forge_sena ci_ambient 0
execute positioned 3660 68 4600 run kill @e[type=!minecraft:player,name="Old Marren",distance=..48]
scoreboard players set #amb_sq_oldsmith_marren ci_ambient 0
execute positioned 3665 68 4552 run kill @e[type=!minecraft:player,name="Recovery Agent Kessler",distance=..48]
scoreboard players set #amb_sq_recovery_agent ci_ambient 0
execute positioned 3620 66 4660 run kill @e[type=!minecraft:player,name="Clerk Severance",distance=..48]
scoreboard players set #amb_sq_severance ci_ambient 0
execute positioned 3684 68 4588 run kill @e[type=!minecraft:player,name="Bladesmith Hollis",distance=..48]
scoreboard players set #amb_sq_temper_hollis ci_ambient 0
execute positioned 1450 93 2052 run kill @e[type=!minecraft:player,name="Moss Court Station Plaque",distance=..48]
scoreboard players set #amb_station_moss ci_ambient 0
execute positioned 1432 85 1964 run kill @e[type=!minecraft:player,name="Orchard Station Plaque",distance=..48]
scoreboard players set #amb_station_orchard ci_ambient 0
execute positioned 1484 87 2160 run kill @e[type=!minecraft:player,name="Pond Station Plaque",distance=..48]
scoreboard players set #amb_station_pond ci_ambient 0
execute positioned 1478 87 2098 run kill @e[type=!minecraft:player,name="Terrace Station Plaque",distance=..48]
scoreboard players set #amb_station_terrace ci_ambient 0
execute positioned 1560 88 2380 run kill @e[type=!minecraft:player,name="Survey Wagon",distance=..48]
scoreboard players set #amb_survey_wagon ci_ambient 0
execute positioned 1904 113 2606 run kill @e[type=!minecraft:player,name="Machine Counter Mika",distance=..48]
scoreboard players set #amb_tm_counter_mika ci_ambient 0
execute positioned 2050 129 4085 run kill @e[type=!minecraft:player,name="Warden Ossa",distance=..48]
scoreboard players set #amb_warden_ossa ci_ambient 0
execute positioned 1550 88 2470 run kill @e[type=!minecraft:player,name="The Gate Lantern",distance=..48]
scoreboard players set #amb_watch_lantern ci_ambient 0
execute positioned 1180 123 3260 run kill @e[type=!minecraft:player,name="Corliss",distance=..48]
scoreboard players set #amb_wheat_trader_deepcore ci_ambient 0
forceload remove 224 2336
forceload remove 544 3552
forceload remove 560 3536
forceload remove 560 3552
forceload remove 576 3600
forceload remove 592 3648
forceload remove 688 3248
forceload remove 736 4576
forceload remove 944 2704
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
forceload remove 1392 1056
forceload remove 1424 1952
forceload remove 1440 2048
forceload remove 1456 1136
forceload remove 1472 1136
forceload remove 1472 2096
forceload remove 1472 2160
forceload remove 1488 1120
forceload remove 1488 2032
forceload remove 1488 2048
forceload remove 1488 2080
forceload remove 1504 1088
forceload remove 1504 2080
forceload remove 1520 1088
forceload remove 1520 1104
forceload remove 1520 1696
forceload remove 1536 2064
forceload remove 1536 2464
forceload remove 1552 1088
forceload remove 1552 1104
forceload remove 1552 2368
forceload remove 1600 1104
forceload remove 1728 4176
forceload remove 1728 4192
forceload remove 1776 4208
forceload remove 1904 2592
forceload remove 1904 4048
forceload remove 1968 1024
forceload remove 1968 1040
forceload remove 1968 3936
forceload remove 1968 3952
forceload remove 2000 912
forceload remove 2032 4096
forceload remove 2048 4064
forceload remove 2048 4080
forceload remove 2112 896
forceload remove 2128 896
forceload remove 2128 3888
forceload remove 2144 896
forceload remove 2160 880
forceload remove 2304 3536
forceload remove 2752 3488
forceload remove 3040 2480
forceload remove 3440 2016
forceload remove 3456 2000
forceload remove 3456 2016
forceload remove 3472 2000
forceload remove 3488 1952
forceload remove 3504 4688
forceload remove 3520 1968
forceload remove 3536 1968
forceload remove 3616 4656
forceload remove 3632 1952
forceload remove 3632 4512
forceload remove 3648 4592
forceload remove 3664 4544
forceload remove 3664 4560
forceload remove 3664 4576
forceload remove 3680 4576
forceload remove 3792 3792
tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a9: dress pass — 99 locals re-latch in their proper outfits.","color":"gray"}]

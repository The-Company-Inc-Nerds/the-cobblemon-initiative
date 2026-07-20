# repairs wave a9 — arm: skin dress pass repaint (0.6.0-alpha.9). skin_scout gave
# 99 latch-placed civilians/props authored 'skin' blocks (12 new textures in the
# trainer_textures pack: shrine keepers, nurses, shopkeepers, wheat factors, props).
# Skins are baked into the compiled presets at SPAWN time, so bodies already latched
# in existing worlds keep their old Steve/default look forever. This wave kills each
# stale body at its authored spot and re-arms its latch; the latch re-spawns the
# dressed body on the next player approach (within 40b). Positions did NOT move —
# same coords, new outfit. Mark done, load every affected site, schedule the apply
# pass 3s out (forceloaded chunks need a tick or two before their ENTITIES exist).
scoreboard players set #repair_a9 ci_ambient 1

forceload add 234 2347
forceload add 552 3552
forceload add 560 3540
forceload add 566 3560
forceload add 588 3600
forceload add 605 3660
forceload add 700 3255
forceload add 744 4589
forceload add 951 2715
forceload add 985 3120
forceload add 1060 3200
forceload add 1068 2465
forceload add 1076 2452
forceload add 1100 3215
forceload add 1110 3230
forceload add 1120 3300
forceload add 1150 3282
forceload add 1152 3284
forceload add 1180 3260
forceload add 1229 2820
forceload add 1393 1065
forceload add 1432 1964
forceload add 1450 2052
forceload add 1470 1140
forceload add 1478 1150
forceload add 1478 2098
forceload add 1484 2160
forceload add 1488 2090
forceload add 1490 1132
forceload add 1498 2050
forceload add 1503 2041
forceload add 1512 2082
forceload add 1518 1096
forceload add 1520 1100
forceload add 1524 1104
forceload add 1525 1698
forceload add 1538 2064
forceload add 1550 2470
forceload add 1555 1108
forceload add 1560 1090
forceload add 1560 2380
forceload add 1610 1110
forceload add 1738 4192
forceload add 1740 4190
forceload add 1788 4212
forceload add 1904 2606
forceload add 1910 4049
forceload add 1968 1044
forceload add 1972 3945
forceload add 1975 1036
forceload add 1980 3960
forceload add 2004 919
forceload add 2040 4100
forceload add 2050 4085
forceload add 2058 4075
forceload add 2120 900
forceload add 2140 3900
forceload add 2143 902
forceload add 2145 901
forceload add 2160 890
forceload add 2318 3542
forceload add 2760 3490
forceload add 3049 2480
forceload add 3450 2030
forceload add 3462 2016
forceload add 3470 2000
forceload add 3480 2010
forceload add 3500 1960
forceload add 3510 4702
forceload add 3520 1982
forceload add 3540 1975
forceload add 3620 4658
forceload add 3640 4520
forceload add 3644 1960
forceload add 3660 4600
forceload add 3665 4552
forceload add 3670 4560
forceload add 3672 4576
forceload add 3684 4588
forceload add 3805 3806

schedule function cobblemon_initiative:install/repairs_a9_apply 3s

# repairs wave a6 — arm: mark done, load every affected site, schedule the apply
# pass 3s out (forceloaded chunks need a tick or two before their ENTITIES exist).
# Scope: the 44 gyms-3-7 town-pack spec-cast NPCs (alpha.17/alpha.20) whose authored
# placement Y was a placeholder — a live block-probe 2026-07-18 found only 4/48 spots
# valid (rest buried 1-26 blocks or floating; Deepcore's whole cast sat inside the
# mountain above the real y~109-123 city floor, the deep office at y40 vs real y114).
# dialog-src placements now carry probed feet-Y; this wave kills the stale buried
# bodies and re-arms their latches so they re-spawn grounded on next approach.
scoreboard players set #repair_a6 ci_ambient 1

forceload add 1518 1096
forceload add 1524 1104
forceload add 1490 1132
forceload add 1560 1090
forceload add 1610 1110
forceload add 1520 1100
forceload add 1555 1108
forceload add 1478 1150
forceload add 1470 1140
forceload add 985 3120
forceload add 1120 3300
forceload add 1060 3200
forceload add 1152 3284
forceload add 1100 3215
forceload add 1110 3230
forceload add 566 3560
forceload add 552 3552
forceload add 605 3660
forceload add 588 3600
forceload add 560 3540
forceload add 700 3255
forceload add 1972 3945
forceload add 2040 4100
forceload add 1980 3960
forceload add 2140 3900
forceload add 2318 3542
forceload add 1076 2452
forceload add 1229 2820
forceload add 1068 2465
forceload add 1150 3282
forceload add 2050 4085
forceload add 1180 3260

schedule function cobblemon_initiative:install/repairs_a6_apply 3s

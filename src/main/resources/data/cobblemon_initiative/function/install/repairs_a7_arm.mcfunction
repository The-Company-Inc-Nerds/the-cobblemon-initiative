# repairs wave a7 — arm (same pattern as a6): the shrine cultist + noble-giver casts
# were never ground-probed (alpha.18 placeholder Ys — fairy shrine cast at y=-7, the
# Manaphy giver 30 blocks under the reef). 12 placements re-based 2026-07-18; this
# wave kills the stale buried bodies and re-arms their latches.
scoreboard players set #repair_a7 ci_ambient 1

forceload add 950 2718
forceload add 1902 4052
forceload add 3634 1960
forceload add 3503 4705
forceload add 2001 924
forceload add 1998 921
forceload add 1393 1065
forceload add 2760 3490

schedule function cobblemon_initiative:install/repairs_a7_apply 3s

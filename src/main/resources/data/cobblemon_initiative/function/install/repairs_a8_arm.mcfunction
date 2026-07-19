# repairs wave a8 — arm: shrine-structure ruling 2026-07-19 removed all 10 shrine
# cultists (no trial mechanically calls for them; leaders are the crystal-giving
# keepers). Kill the stale bodies in existing worlds + drop their orphan latches.
scoreboard players set #repair_a8 ci_ambient 1

forceload add 1998 921
forceload add 2001 924
forceload add 947 2715
forceload add 3500 4702
forceload add 3503 4705
forceload add 1899 4049
forceload add 3634 1960

schedule function cobblemon_initiative:install/repairs_a8_apply 3s

# repairs wave a25 — arm: 0.7.0-alpha.8 playtest wave.
# Scope: Liora nurse nudge (+1 z into the Center doorway), Korrin moved to the north-boardwalk
# corner toward Route 3, and the Mystic Marsh fairy household re-latches (Thimble down onto the
# fen bank, Bramblea +1 y). Forceload each site so the uuid tps resolve and the latch kills land.
scoreboard players set #repair_a25 ci_ambient 1
forceload add 1168 2352 1182 2366
forceload add 1116 2372 1168 2410
forceload add 1022 2500 1034 2510
schedule function cobblemon_initiative:install/repairs_a25_apply 3s

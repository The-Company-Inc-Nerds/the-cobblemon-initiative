# repairs wave a26 — arm: 0.7.0-alpha.10 playtest wave.
# Scope: Apprentice Faye nudged 922.4/92/2439.5 -> 921.5/92/2439.5 at the gym still pool.
scoreboard players set #repair_a26 ci_ambient 1
forceload add 916 2434 928 2446
schedule function cobblemon_initiative:install/repairs_a26_apply 3s

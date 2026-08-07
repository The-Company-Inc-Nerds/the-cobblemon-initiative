# repairs wave a35 — arm: playtest 2026-08-06 — Cyber City power-plant physical blocks.
# The engine (PowerPlantManager) loads the 9 bulb + 8 lever coords from the bundled
# powerplant.json and toggles LIT / intercepts lever clicks, but only on blocks that EXIST —
# the finished map never carried the copper bulbs + levers, so on a fresh install the gym-7
# puzzle had nothing to light. Place them at install (world is built; all world setup rides
# the install run). Guarded once-per-world by #repair_a35 like every other wave.
scoreboard players set #repair_a35 ci_ambient 1
# Power-plant room: bulbs x1414-1434, z928-942; guard pad 1424/90/936 (spans chunks 88-89 / 58).
forceload add 1414 928 1434 942
schedule function cobblemon_initiative:install/repairs_a35_apply 3s

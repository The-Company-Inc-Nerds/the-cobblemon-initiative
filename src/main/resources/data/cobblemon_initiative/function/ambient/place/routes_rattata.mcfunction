# companion_routes_rattata — latch FIRST; reset #amb_routes_rattata to 0 (+ kill the body) to respawn.
scoreboard players set #amb_routes_rattata ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_routes_rattata.npc.snbt 1456.5 86 1683.5

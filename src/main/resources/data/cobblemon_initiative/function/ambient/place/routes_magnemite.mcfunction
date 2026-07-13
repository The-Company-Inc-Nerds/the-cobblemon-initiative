# companion_routes_magnemite — latch FIRST; reset #amb_routes_magnemite to 0 (+ kill the body) to respawn.
scoreboard players set #amb_routes_magnemite ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_routes_magnemite.npc.snbt 1781.5 90 1192.5

# companion_routes_geodude — latch FIRST; reset #amb_routes_geodude to 0 (+ kill the body) to respawn.
scoreboard players set #amb_routes_geodude ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_routes_geodude.npc.snbt 4109.5 88 2990.5

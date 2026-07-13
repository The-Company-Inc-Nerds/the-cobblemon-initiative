# companion_route_geodude — latch FIRST; reset #amb_route_geodude to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_geodude ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_geodude.npc.snbt 4278.5 81 2986.5

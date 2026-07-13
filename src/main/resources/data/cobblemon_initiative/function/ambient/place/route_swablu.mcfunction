# companion_route_swablu — latch FIRST; reset #amb_route_swablu to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_swablu ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_swablu.npc.snbt 2254.5 210 1088.5

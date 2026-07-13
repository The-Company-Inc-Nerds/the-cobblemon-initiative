# companion_routes_swablu — latch FIRST; reset #amb_routes_swablu to 0 (+ kill the body) to respawn.
scoreboard players set #amb_routes_swablu ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_routes_swablu.npc.snbt 2391.5 202 890.5

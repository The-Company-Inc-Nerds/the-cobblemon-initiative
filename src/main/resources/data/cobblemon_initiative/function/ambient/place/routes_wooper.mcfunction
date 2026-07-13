# companion_routes_wooper — latch FIRST; reset #amb_routes_wooper to 0 (+ kill the body) to respawn.
scoreboard players set #amb_routes_wooper ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_routes_wooper.npc.snbt 4043.5 91 2953.5

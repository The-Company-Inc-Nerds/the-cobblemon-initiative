# companion_route_wooper — latch FIRST; reset #amb_route_wooper to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_wooper ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_wooper.npc.snbt 4262.5 45 2792.5

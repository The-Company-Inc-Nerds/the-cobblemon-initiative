# companion_route_wingull — latch FIRST; reset #amb_route_wingull to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_wingull ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_wingull.npc.snbt 4214.5 66 2814.5

# companion_route_blitzle — latch FIRST; reset #amb_route_blitzle to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_blitzle ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_blitzle.npc.snbt 1714.5 89 1202.5

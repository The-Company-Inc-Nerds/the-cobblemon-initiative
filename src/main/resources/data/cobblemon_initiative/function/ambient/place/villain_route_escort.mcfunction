# villain_route_escort — latch FIRST; reset #amb_villain_route_escort to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_route_escort ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_route_escort.npc.snbt 1563.5 88 2382.5

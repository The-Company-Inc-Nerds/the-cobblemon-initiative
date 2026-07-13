# companion_route_noibat — latch FIRST; reset #amb_route_noibat to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_noibat ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_noibat.npc.snbt 2272.5 211 1108.5

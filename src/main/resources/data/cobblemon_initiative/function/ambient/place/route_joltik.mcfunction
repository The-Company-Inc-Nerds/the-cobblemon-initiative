# companion_route_joltik — latch FIRST; reset #amb_route_joltik to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_joltik ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_joltik.npc.snbt 1428.5 90 954.5

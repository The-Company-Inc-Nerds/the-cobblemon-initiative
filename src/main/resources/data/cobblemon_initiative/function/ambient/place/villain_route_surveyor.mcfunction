# villain_route_surveyor — latch FIRST; reset #amb_villain_route_surveyor to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_route_surveyor ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_route_surveyor.npc.snbt 1558.5 88 2378.5

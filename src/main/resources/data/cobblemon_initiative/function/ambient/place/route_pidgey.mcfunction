# companion_route_pidgey — latch FIRST; reset #amb_route_pidgey to 0 (+ kill the body) to respawn.
scoreboard players set #amb_route_pidgey ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_pidgey.npc.snbt 1490.5 84 1676.5

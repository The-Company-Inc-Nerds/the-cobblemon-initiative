# station_pond — latch FIRST; reset #amb_station_pond to 0 (+ kill the body) to respawn.
scoreboard players set #amb_station_pond ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/station_pond.npc.snbt 1484.5 87 2160.5

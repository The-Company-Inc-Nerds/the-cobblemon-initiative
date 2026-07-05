# station_terrace — latch FIRST; reset #amb_station_terrace to 0 (+ kill the body) to respawn.
scoreboard players set #amb_station_terrace ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/station_terrace.npc.snbt 1478.5 87 2098.5

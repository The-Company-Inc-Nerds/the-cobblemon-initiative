# station_moss — latch FIRST; reset #amb_station_moss to 0 (+ kill the body) to respawn.
scoreboard players set #amb_station_moss ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/station_moss.npc.snbt 1450.5 93 2052.5

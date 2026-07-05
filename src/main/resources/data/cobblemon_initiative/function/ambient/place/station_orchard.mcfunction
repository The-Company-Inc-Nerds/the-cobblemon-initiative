# station_orchard — latch FIRST; reset #amb_station_orchard to 0 (+ kill the body) to respawn.
scoreboard players set #amb_station_orchard ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/station_orchard.npc.snbt 1432.5 85 1964.5

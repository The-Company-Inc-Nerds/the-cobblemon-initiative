# r7_traveler_tidepool — latch FIRST; reset #amb_r7_traveler_tidepool to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r7_traveler_tidepool ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r7_traveler_tidepool.npc.snbt 815.5 64 3345.5

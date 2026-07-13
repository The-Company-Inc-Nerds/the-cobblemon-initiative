# r13_traveler_pilgrim — latch FIRST; reset #amb_r13_traveler_pilgrim to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r13_traveler_pilgrim ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r13_traveler_pilgrim.npc.snbt 1968.5 64 1044.5

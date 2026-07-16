# deepcore_nurse — latch FIRST; reset #amb_deepcore_nurse to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_nurse ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_nurse.npc.snbt 1092.5 129 3208.5

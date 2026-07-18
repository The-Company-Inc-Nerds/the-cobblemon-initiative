# mm_nurse — latch FIRST; reset #amb_mm_nurse to 0 (+ kill the body) to respawn.
scoreboard players set #amb_mm_nurse ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/mm_nurse.npc.snbt 1068.5 65 2465.5

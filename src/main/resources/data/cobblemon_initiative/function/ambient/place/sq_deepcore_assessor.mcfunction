# sq_deepcore_assessor — latch FIRST; reset #amb_sq_deepcore_assessor to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_deepcore_assessor ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_deepcore_assessor.npc.snbt 1150.5 146 3282.5

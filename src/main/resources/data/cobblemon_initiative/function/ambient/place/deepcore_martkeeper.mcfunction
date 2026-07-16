# deepcore_martkeeper — latch FIRST; reset #amb_deepcore_martkeeper to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_martkeeper ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_martkeeper.npc.snbt 1100.5 129 3215.5

# r5_trainer_quarry — latch FIRST; reset #amb_r5_trainer_quarry to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r5_trainer_quarry ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r5_trainer_quarry.npc.snbt 1150.5 64 2830.5

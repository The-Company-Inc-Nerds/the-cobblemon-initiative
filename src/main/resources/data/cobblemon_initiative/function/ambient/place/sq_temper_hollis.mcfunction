# sq_temper_hollis — latch FIRST; reset #amb_sq_temper_hollis to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_temper_hollis ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_temper_hollis.npc.snbt 3684.5 68 4588.5

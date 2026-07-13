# r5_merchant_stranded — latch FIRST; reset #amb_r5_merchant_stranded to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r5_merchant_stranded ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r5_merchant_stranded.npc.snbt 1150.5 64 2830.5

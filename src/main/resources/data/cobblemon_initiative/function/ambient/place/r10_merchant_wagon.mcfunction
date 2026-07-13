# r10_merchant_wagon — latch FIRST; reset #amb_r10_merchant_wagon to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r10_merchant_wagon ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r10_merchant_wagon.npc.snbt 2245.5 64 3520.5

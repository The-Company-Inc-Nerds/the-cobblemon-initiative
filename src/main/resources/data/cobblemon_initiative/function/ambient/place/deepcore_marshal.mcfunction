# deepcore_marshal — latch FIRST; reset #amb_deepcore_marshal to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_marshal ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_marshal.npc.snbt 996.5 129 3188.5

# gaviota_union_hand — latch FIRST; reset #amb_gaviota_union_hand to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_union_hand ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_union_hand.npc.snbt 580.5 86 3600.5

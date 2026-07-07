# tr_peddler_tadashi — latch FIRST; reset #amb_tr_peddler_tadashi to 0 (+ kill the body) to respawn.
scoreboard players set #amb_tr_peddler_tadashi ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/tr_peddler_tadashi.npc.snbt 2558.5 110 2872.5

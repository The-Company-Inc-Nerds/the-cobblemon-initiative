# villain_yield_analyst — latch FIRST; reset #amb_villain_yield_analyst to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_yield_analyst ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_yield_analyst.npc.snbt 1546.5 84 2002.5

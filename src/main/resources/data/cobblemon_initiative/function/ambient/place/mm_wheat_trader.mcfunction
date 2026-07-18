# mm_wheat_trader — latch FIRST; reset #amb_mm_wheat_trader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_mm_wheat_trader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/mm_wheat_trader.npc.snbt 1058.5 78 2478.5

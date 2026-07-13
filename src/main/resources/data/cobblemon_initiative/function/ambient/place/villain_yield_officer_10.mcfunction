# villain_yield_officer_10 — latch FIRST; reset #amb_villain_yield_officer_10 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_yield_officer_10 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_yield_officer_10.npc.snbt 3300.5 108 4008.5

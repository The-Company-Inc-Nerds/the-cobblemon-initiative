# villain_yield_officer — latch FIRST; reset #amb_villain_yield_officer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_yield_officer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_yield_officer.npc.snbt 1579.5 88 2459.5

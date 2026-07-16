# cyber_reserve_1 — latch FIRST; reset #amb_cyber_reserve_1 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_cyber_reserve_1 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/cyber_reserve_1.npc.snbt 1490.5 64 1128.5

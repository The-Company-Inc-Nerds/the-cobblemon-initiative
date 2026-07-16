# cyber_reserve_2 — latch FIRST; reset #amb_cyber_reserve_2 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_cyber_reserve_2 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/cyber_reserve_2.npc.snbt 1560.5 64 1092.5

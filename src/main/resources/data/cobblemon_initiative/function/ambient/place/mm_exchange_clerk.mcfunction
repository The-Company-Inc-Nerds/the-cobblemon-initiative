# mm_exchange_clerk — latch FIRST; reset #amb_mm_exchange_clerk to 0 (+ kill the body) to respawn.
scoreboard players set #amb_mm_exchange_clerk ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/mm_exchange_clerk.npc.snbt 1111.5 66 2444.5

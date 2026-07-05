# company_courier — latch FIRST; reset #amb_company_courier to 0 (+ kill the body) to respawn.
scoreboard players set #amb_company_courier ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/company_courier.npc.snbt 2592.5 111 2815.5

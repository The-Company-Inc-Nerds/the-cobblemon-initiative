# cyber_exchange_teller — latch FIRST; reset #amb_cyber_exchange_teller to 0 (+ kill the body) to respawn.
scoreboard players set #amb_cyber_exchange_teller ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/cyber_exchange_teller.npc.snbt 1500.5 65 1120.5

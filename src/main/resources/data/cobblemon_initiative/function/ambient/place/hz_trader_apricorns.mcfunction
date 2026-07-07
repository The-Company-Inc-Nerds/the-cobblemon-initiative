# hz_trader_apricorns — latch FIRST; reset #amb_hz_trader_apricorns to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_trader_apricorns ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_trader_apricorns.npc.snbt 1512.5 85 2082.5

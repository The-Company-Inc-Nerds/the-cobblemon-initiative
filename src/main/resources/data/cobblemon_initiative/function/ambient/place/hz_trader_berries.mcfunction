# hz_trader_berries — latch FIRST; reset #amb_hz_trader_berries to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_trader_berries ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_trader_berries.npc.snbt 1538.5 86 2064.5

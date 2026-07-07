# hz_trader_mints — latch FIRST; reset #amb_hz_trader_mints to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_trader_mints ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_trader_mints.npc.snbt 1488.5 87 2090.5

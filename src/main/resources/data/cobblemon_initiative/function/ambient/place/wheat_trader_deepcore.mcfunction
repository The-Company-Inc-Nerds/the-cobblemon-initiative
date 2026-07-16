# wheat_trader_deepcore — latch FIRST; reset #amb_wheat_trader_deepcore to 0 (+ kill the body) to respawn.
scoreboard players set #amb_wheat_trader_deepcore ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/wheat_trader_deepcore.npc.snbt 1180.5 129 3260.5

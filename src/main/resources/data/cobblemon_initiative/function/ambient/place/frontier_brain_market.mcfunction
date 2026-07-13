# frontier_brain_market — latch FIRST; reset #amb_frontier_brain_market to 0 (+ kill the body) to respawn.
scoreboard players set #amb_frontier_brain_market ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/frontier_brain_market.npc.snbt 3800.5 159 2999.5

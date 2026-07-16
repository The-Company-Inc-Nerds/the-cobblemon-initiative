# frontier_registrar — latch FIRST; reset #amb_frontier_registrar to 0 (+ kill the body) to respawn.
scoreboard players set #amb_frontier_registrar ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/frontier_registrar.npc.snbt 3800.5 159 2997.5

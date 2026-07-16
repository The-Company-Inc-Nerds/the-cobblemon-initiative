# deepcore_miner_rill — latch FIRST; reset #amb_deepcore_miner_rill to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_miner_rill ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_miner_rill.npc.snbt 1110.5 129 3230.5

# companion_deepcore_machop — latch FIRST; reset #amb_deepcore_machop to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_machop ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_deepcore_machop.npc.snbt 1021.5 129 3188.5

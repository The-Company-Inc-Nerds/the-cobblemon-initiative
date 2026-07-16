# sq_severance — latch FIRST; reset #amb_sq_severance to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_severance ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_severance.npc.snbt 3620.5 66 4660.5

# sq_recovery_agent — latch FIRST; reset #amb_sq_recovery_agent to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_recovery_agent ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_recovery_agent.npc.snbt 3665.5 68 4552.5

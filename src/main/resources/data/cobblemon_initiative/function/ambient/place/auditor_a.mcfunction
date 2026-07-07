# auditor_a — latch FIRST; reset #amb_auditor_a to 0 (+ kill the body) to respawn.
scoreboard players set #amb_auditor_a ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/auditor_a.npc.snbt 2611.5 110 2792.5

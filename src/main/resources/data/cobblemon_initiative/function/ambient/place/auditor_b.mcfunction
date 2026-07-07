# auditor_b — latch FIRST; reset #amb_auditor_b to 0 (+ kill the body) to respawn.
scoreboard players set #amb_auditor_b ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/auditor_b.npc.snbt 2578.5 108 2942.5

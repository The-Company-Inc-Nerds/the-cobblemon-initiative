# nifl_cold_auditor — latch FIRST; reset #amb_nifl_cold_auditor to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_cold_auditor ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_cold_auditor.npc.snbt 3520.5 68 1982.5

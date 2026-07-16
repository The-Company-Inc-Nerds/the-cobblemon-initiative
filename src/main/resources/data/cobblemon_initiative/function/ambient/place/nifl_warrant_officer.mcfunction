# nifl_warrant_officer — latch FIRST; reset #amb_nifl_warrant_officer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_warrant_officer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_warrant_officer.npc.snbt 3450.5 70 2030.5

# nifl_records_officer — latch FIRST; reset #amb_nifl_records_officer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_records_officer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_records_officer.npc.snbt 3540.5 68 1975.5

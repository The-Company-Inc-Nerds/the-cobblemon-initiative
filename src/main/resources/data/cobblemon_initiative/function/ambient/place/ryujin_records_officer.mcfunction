# ryujin_records_officer — latch FIRST; reset #amb_ryujin_records_officer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ryujin_records_officer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ryujin_records_officer.npc.snbt 2160.5 64 890.5

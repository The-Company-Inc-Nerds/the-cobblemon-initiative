# nifl_keeper_vetra — latch FIRST; reset #amb_nifl_keeper_vetra to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_keeper_vetra ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_keeper_vetra.npc.snbt 3500.5 65 1960.5

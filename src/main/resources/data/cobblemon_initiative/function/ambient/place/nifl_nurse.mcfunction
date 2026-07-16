# nifl_nurse — latch FIRST; reset #amb_nifl_nurse to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_nurse ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_nurse.npc.snbt 3470.5 66 2000.5

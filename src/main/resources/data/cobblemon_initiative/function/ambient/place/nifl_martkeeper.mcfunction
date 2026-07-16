# nifl_martkeeper — latch FIRST; reset #amb_nifl_martkeeper to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_martkeeper ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_martkeeper.npc.snbt 3480.5 66 2010.5

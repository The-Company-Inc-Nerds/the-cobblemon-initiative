# nifl_civilian_frost — latch FIRST; reset #amb_nifl_civilian_frost to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_civilian_frost ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_civilian_frost.npc.snbt 3462.5 66 2016.5

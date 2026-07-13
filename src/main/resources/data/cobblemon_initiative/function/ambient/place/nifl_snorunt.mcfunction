# companion_nifl_snorunt — latch FIRST; reset #amb_nifl_snorunt to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_snorunt ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_nifl_snorunt.npc.snbt 3607.5 120 1906.5

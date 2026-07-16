# grain_factor_kalahar — latch FIRST; reset #amb_grain_factor_kalahar to 0 (+ kill the body) to respawn.
scoreboard players set #amb_grain_factor_kalahar ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/grain_factor_kalahar.npc.snbt 1972.5 126 3945.5

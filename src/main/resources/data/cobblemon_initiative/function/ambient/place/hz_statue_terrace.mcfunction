# hz_statue_terrace — latch FIRST; reset #amb_hz_statue_terrace to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_statue_terrace ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_statue_terrace.npc.snbt 1456.5 103 2098.1

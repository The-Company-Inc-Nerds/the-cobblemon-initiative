# hz_statue_pond — latch FIRST; reset #amb_hz_statue_pond to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_statue_pond ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_statue_pond.npc.snbt 1538.5 85 2026.5

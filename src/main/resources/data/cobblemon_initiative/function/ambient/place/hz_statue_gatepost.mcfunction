# hz_statue_gatepost — latch FIRST; reset #amb_hz_statue_gatepost to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_statue_gatepost ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_statue_gatepost.npc.snbt 1451.8 90.0 2026.3

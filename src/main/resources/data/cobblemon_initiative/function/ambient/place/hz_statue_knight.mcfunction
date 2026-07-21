# hz_statue_knight — latch FIRST; reset #amb_hz_statue_knight to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_statue_knight ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_statue_knight.npc.snbt 1479.9 87.0 2112.6

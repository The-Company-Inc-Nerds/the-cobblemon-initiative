# r7_spotter_coast — latch FIRST; reset #amb_r7_spotter_coast to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r7_spotter_coast ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r7_spotter_coast.npc.snbt 801.5 64 3323.5

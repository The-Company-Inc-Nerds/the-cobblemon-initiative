# r14_spotter_frost — latch FIRST; reset #amb_r14_spotter_frost to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r14_spotter_frost ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r14_spotter_frost.npc.snbt 3042.5 64 2490.5

# fenceline_farmer — latch FIRST; reset #amb_fenceline_farmer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_fenceline_farmer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/fenceline_farmer.npc.snbt 1549.5 76 1738.5

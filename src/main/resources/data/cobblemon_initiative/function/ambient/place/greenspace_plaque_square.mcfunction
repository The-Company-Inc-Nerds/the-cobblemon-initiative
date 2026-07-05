# greenspace_plaque_square — latch FIRST; reset #amb_greenspace_plaque_square to 0 (+ kill the body) to respawn.
scoreboard players set #amb_greenspace_plaque_square ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/greenspace_plaque_square.npc.snbt 1498.5 86 2050.5

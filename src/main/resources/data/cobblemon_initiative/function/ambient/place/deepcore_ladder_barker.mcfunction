# deepcore_ladder_barker — latch FIRST; reset #amb_deepcore_ladder_barker to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_ladder_barker ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_ladder_barker.npc.snbt 1060.5 129 3200.5

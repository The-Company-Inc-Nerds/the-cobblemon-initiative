# gaviota_dockmaster — latch FIRST; reset #amb_gaviota_dockmaster to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_dockmaster ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_dockmaster.npc.snbt 566.5 84 3560.5

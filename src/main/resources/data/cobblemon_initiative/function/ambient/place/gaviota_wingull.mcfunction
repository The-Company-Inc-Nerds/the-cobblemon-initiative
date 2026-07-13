# companion_gaviota_wingull — latch FIRST; reset #amb_gaviota_wingull to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_wingull ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_gaviota_wingull.npc.snbt 501.5 68 3498.5

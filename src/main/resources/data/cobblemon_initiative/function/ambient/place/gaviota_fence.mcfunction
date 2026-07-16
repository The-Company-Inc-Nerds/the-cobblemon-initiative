# gaviota_fence — latch FIRST; reset #amb_gaviota_fence to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_fence ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_fence.npc.snbt 552.5 63 3552.5

# gaviota_manifest_b — latch FIRST; reset #amb_gaviota_manifest_b to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_manifest_b ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_manifest_b.npc.snbt 588.5 64 3600.5

# gaviota_manifest_c — latch FIRST; reset #amb_gaviota_manifest_c to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_manifest_c ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_manifest_c.npc.snbt 605.5 64 3650.5

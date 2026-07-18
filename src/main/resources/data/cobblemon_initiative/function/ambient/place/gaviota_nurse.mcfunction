# gaviota_nurse — latch FIRST; reset #amb_gaviota_nurse to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_nurse ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_nurse.npc.snbt 560.5 82 3540.5

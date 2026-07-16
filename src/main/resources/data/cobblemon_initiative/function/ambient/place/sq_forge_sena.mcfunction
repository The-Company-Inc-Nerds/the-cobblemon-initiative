# sq_forge_sena — latch FIRST; reset #amb_sq_forge_sena to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_forge_sena ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_forge_sena.npc.snbt 3670.5 68 4560.5

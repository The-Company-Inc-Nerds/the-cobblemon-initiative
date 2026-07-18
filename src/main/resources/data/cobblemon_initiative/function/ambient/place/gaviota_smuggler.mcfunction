# gaviota_smuggler — latch FIRST; reset #amb_gaviota_smuggler to 0 (+ kill the body) to respawn.
scoreboard players set #amb_gaviota_smuggler ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/gaviota_smuggler.npc.snbt 700.5 103 3255.5

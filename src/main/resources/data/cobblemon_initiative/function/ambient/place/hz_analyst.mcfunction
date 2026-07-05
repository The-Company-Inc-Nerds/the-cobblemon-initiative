# hz_analyst — latch FIRST; reset #amb_hz_analyst to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_analyst ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_analyst.npc.snbt 1532.5 93 2005.5

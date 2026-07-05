# companion_psyduck — latch FIRST; reset #amb_psyduck to 0 (+ kill the body) to respawn.
scoreboard players set #amb_psyduck ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_psyduck.npc.snbt 1893.5 105 2470.5

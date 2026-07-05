# hz_receptionist — latch FIRST; reset #amb_hz_receptionist to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hz_receptionist ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hz_receptionist.npc.snbt 1540.5 86 2001.5

# warden_ossa — latch FIRST; reset #amb_warden_ossa to 0 (+ kill the body) to respawn.
scoreboard players set #amb_warden_ossa ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/warden_ossa.npc.snbt 2050.5 129 4085.5

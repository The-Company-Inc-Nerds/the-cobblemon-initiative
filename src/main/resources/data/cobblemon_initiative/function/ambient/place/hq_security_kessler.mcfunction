# hq_security_kessler — latch FIRST; reset #amb_hq_security_kessler to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hq_security_kessler ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hq_security_kessler.npc.snbt 1621.5 92 1116.5

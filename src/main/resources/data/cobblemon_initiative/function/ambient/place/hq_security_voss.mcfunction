# hq_security_voss — latch FIRST; reset #amb_hq_security_voss to 0 (+ kill the body) to respawn.
scoreboard players set #amb_hq_security_voss ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/hq_security_voss.npc.snbt 1617.5 92 1116.5

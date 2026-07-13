# r3_spotter_marsh — latch FIRST; reset #amb_r3_spotter_marsh to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r3_spotter_marsh ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r3_spotter_marsh.npc.snbt 1372.5 64 2329.5

# bramblea_mossglen — latch FIRST; reset #amb_bramblea_mossglen to 0 (+ kill the body) to respawn.
scoreboard players set #amb_bramblea_mossglen ci_ambient 1
easy_npc preset import_new data easy_npc:preset/fairy/bramblea_mossglen.npc.snbt 1028.5 75 2505.5

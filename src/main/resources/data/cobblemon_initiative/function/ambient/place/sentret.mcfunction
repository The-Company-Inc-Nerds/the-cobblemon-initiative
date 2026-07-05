# companion_sentret — latch FIRST; reset #amb_sentret to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sentret ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_sentret.npc.snbt 2588.5 107 2957.5

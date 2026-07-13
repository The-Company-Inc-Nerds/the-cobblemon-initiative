# castle_challenger_2 — latch FIRST; reset #amb_castle_challenger_2 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_castle_challenger_2 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/castle_challenger_2.npc.snbt 3791.5 159 2962.5

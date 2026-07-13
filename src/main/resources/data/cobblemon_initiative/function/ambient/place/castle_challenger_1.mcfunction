# castle_challenger_1 — latch FIRST; reset #amb_castle_challenger_1 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_castle_challenger_1 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/castle_challenger_1.npc.snbt 3785.5 159 2962.5

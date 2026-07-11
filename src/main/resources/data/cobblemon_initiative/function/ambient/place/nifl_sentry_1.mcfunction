# nifl_sentry_1 — latch FIRST; reset #amb_nifl_sentry_1 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_sentry_1 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_sentry_1.npc.snbt 3628.5 119 1912.5

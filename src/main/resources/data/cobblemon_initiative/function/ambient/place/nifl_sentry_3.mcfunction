# nifl_sentry_3 — latch FIRST; reset #amb_nifl_sentry_3 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_sentry_3 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_sentry_3.npc.snbt 3632.5 119 1928.5

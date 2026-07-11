# nifl_sentry_2 — latch FIRST; reset #amb_nifl_sentry_2 to 0 (+ kill the body) to respawn.
scoreboard players set #amb_nifl_sentry_2 ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/nifl_sentry_2.npc.snbt 3624.5 119 1920.5

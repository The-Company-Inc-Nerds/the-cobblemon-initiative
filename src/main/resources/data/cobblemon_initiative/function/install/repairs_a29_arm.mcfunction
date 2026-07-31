# repairs wave a29 — arm: 0.7.0-alpha.14 playtest wave.
# Scope: uuid-body relocations (playtest NPC notes) — Tide-Caller Gianna onto the Westwind beach
# (she hosts the trident tide-ring race), Syla Ironchalk to the Deepcore gallery landing (ore
# trader), Curator Kenji beside the museum resurrection platform. All three are STATIONARY bodies,
# so the apply uses a plain tp (no Navigation.Home tweak — that is only for ambient_wander like
# a25 Korrin). Forceload old + new sites so the uuid tps resolve. Bundled-map entities region is
# edited to match for fresh installs (gitignored, manual — see GIT_COMMIT_MSG note).
scoreboard players set #repair_a29 ci_ambient 1
forceload add 595 3660 605 3670
forceload add 472 3585 481 3594
forceload add 1098 3256 1106 3264
forceload add 1139 3176 1148 3185
forceload add 1896 2314 1906 2342
schedule function cobblemon_initiative:install/repairs_a29_apply 3s

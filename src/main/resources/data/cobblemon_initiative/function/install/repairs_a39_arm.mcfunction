# repairs wave a39 — arm: 0.7.0-alpha.23 playtest wave (2026-08-07 log).
# Scope: (1) Route-15 Cinderfall approach SIGHT TRAINERS — 14 corridor spotters (r13/r14/
# r16/cinderfall) were reworked from wrongly-created NEW latch bodies into uuid-keyed
# adoptions of the EXISTING physical CSV TRAINER bodies (they were exact duplicates of the
# builder-placed bodies). No repairs needed for those — the uuid'd bodies adopt their
# presets in place via update_npc_presets / NpcPresetRefreshManager (a17 uuid-recast rule),
# and their sight now wires by uuid through register_sight. (2) Sindra Blazewing — a BUILDER
# uuid body (a5cdb052…, CSV Wilderness/Routes TRAINER, NOT authored in dialog-src) tps to her
# authored post at the Cinderfall wheat-war approach. uuid tp fires on a fresh install's
# install-run too (a14 rule) — no bundled-map nbt edit needed. This is the ONLY repair this
# wave carries; the 14 sight-trainer + 3 villain-latch moves ride the recompiled presets/
# placements with no repair.
scoreboard players set #repair_a39 ci_ambient 1
# Sindra Blazewing — old body (3294 106 3970) + new post (3291.5 104 3929.5), ~40 blocks
# apart in z; a single block-range add covers both (the post-tp data modify needs the
# destination chunk loaded).
forceload add 3291 3929 3294 3970
schedule function cobblemon_initiative:install/repairs_a39_apply 3s

# repairs wave a34 — arm: 0.7.0-alpha.22 playtest wave (2026-08-05 log).
# Scope: (1) gym-4 dojo — legacy generic "Knocked-Out Fighter" corpse sweep (sleepers are
# per-character now). (2) Cyber City cast — six gym trainers spread out of the old y100
# cluster (latch kills + resets), Nurse Ampere latch body dies (cyber_nurse_rumor is a
# uuid re-cast onto Dr. Orion Synapse's body), Orion + Vera uuid tps. (3) HQ act-2 canon —
# the two lobby prop screens move street-side (latch kills + resets), Victor Node tps to
# the penthouse lift landing. uuid tps also fire on a fresh install's install-run (a14
# rule) — no bundled-map nbt edit needed.
scoreboard players set #repair_a34 ci_ambient 1
# gym-4 dojo box (posts x958-1016 z3156-3192) — corpses can lie anywhere on the mats
forceload add 958 3156 1016 3192
# Cyber gym — six old trainer posts (1303-1309 / 1187-1193, single chunk)
forceload add 1303 1187 1309 1193
# Cyber Center strip — Orion's old post (1474 1054, tp destination shares this chunk) +
# the old Nurse Ampere latch body (1478 1150)
forceload add 1474 1054 1478 1150
# Vera Bitstorm — old post (1306 1226) + new post (1317 1211) in one block-range add
# (the post-tp data modify needs the destination loaded)
forceload add 1306 1211 1317 1226
# HQ tower base — old prop bodies (1610 1110) + Victor Node body/destination (1612 1094)
forceload add 1610 1110
forceload add 1612 1094
schedule function cobblemon_initiative:install/repairs_a34_apply 3s

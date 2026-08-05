# repairs wave a34 — apply: 0.7.0-alpha.22 playtest wave. Chunks forceloaded by the arm.

# ── (1) Gym-4 dojo: legacy corpse sweep — pre-a34 saves hold generic "Knocked-Out
# Fighter" bodies; the knocked sleepers are per-character now and the reset/raise
# functions sweep their own. One typed kill catches every mat.
kill @e[type=easy_npc:humanoid,tag=dc_knocked]

# ── (2a) Cyber gym circuit spread (playtest N4/N5/N7-N10): kill each old latch body
# (cluster spacing ~3.1 blocks — distance=..1.5 cannot eat a neighbour) + reset the
# latch so the next approach respawns from the recompiled placement.
execute positioned 1306.5 100 1193.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_jr_apprentice ci_ambient 0
execute positioned 1306.5 100 1187.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_apprentice ci_ambient 0
execute positioned 1303.5 100 1188.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_trainer_1 ci_ambient 0
execute positioned 1309.5 100 1188.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_trainer_2 ci_ambient 0
execute positioned 1303.5 100 1192.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_trainer_3 ci_ambient 0
execute positioned 1309.5 100 1192.5 run kill @e[type=easy_npc:humanoid,distance=..1.5]
scoreboard players set #amb_cyber_trainer_4 ci_ambient 0

# ── (2b) Nurse re-cast (playtest N3): old Nurse Ampere latch body dies; RESET-form
# because the latch is dropped for good — cyber_nurse_rumor is a uuid char (Orion's
# body) now.
execute positioned 1478.5 89 1150.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_nurse_rumor ci_ambient

# ── (2c) Dr. Orion Synapse (uuid) onto the Center desk + Home pin (his old char file
# wandered; the re-cast preset is stationary).
tp c623358e-e69c-44b0-8d92-e81f5c156b67 1473.5 91.0 1053.5
data modify entity c623358e-e69c-44b0-8d92-e81f5c156b67 Navigation.Home set value {X:1473,Y:91,Z:1053}

# ── (2d) Vera Bitstorm uuid-body move (playtest N6).
tp 273f62df-7604-44ff-96fd-5a379630f5ec 1317.5 98.0 1211.5
data modify entity 273f62df-7604-44ff-96fd-5a379630f5ec Navigation.Home set value {X:1317,Y:98,Z:1211}

# ── (3a) HQ act-2 canon (playtest N11/N12): clear the lobby floor for the door-guard
# box — the two prop screens re-latch street-side. Old bodies are 2.0 blocks apart
# (z1112.5 vs z1110.5): distance ..0.9 keeps the positioned kills disjoint. NO name=
# guard — Easy NPC CustomName is a JSON component and name= selectors never match it
# (a14 lesson); position + type + tight radius is the whole guard.
execute positioned 1610.5 92 1112.5 run kill @e[type=easy_npc:humanoid,distance=..0.9]
execute positioned 1610.5 92 1110.5 run kill @e[type=easy_npc:humanoid,distance=..0.9]
scoreboard players set #amb_cyber_reserve_3 ci_ambient 0
scoreboard players set #amb_cyber_board_3 ci_ambient 0

# ── (3b) Victor Node to the penthouse lift landing (playtest N13 re-role). tp forces
# the target chunk; the y171 landing shares his old body's generated chunk column.
tp cd9030f6-a948-4079-806d-7dc228ab4336 1611.5 171.0 1098.5
data modify entity cd9030f6-a948-4079-806d-7dc228ab4336 Navigation.Home set value {X:1611,Y:171,Z:1098}

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 958 3156 1016 3192
forceload remove 1303 1187 1309 1193
forceload remove 1474 1054 1478 1150
forceload remove 1306 1211 1317 1226
forceload remove 1610 1110
forceload remove 1612 1094

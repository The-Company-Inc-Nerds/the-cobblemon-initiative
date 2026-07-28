# repairs wave a17 — apply: kill the stale alpha.24 bodies with their chunks live and
# reset each latch to 0 so the recompiled placements respawn them at the CURRENT
# authored coords (with the new presets) the next time a player comes within 40.
# Kill shapes follow the a14/a15 lessons: positioned+type for solo sites, tag-scoped
# where a uuid body stands near enough to be collateral (Ning vs Ping; Yan; Lan).

# Four warden statues — re-latch for the posed visuals (coords unchanged; a14 precedent
# killed at these exact sites)
execute positioned 1456.5 103.0 2098.1 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_moss ci_ambient 0
execute positioned 1451.8 90.0 2026.3 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_orchard ci_ambient 0
execute positioned 1479.9 87.0 2112.6 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_terrace ci_ambient 0
execute positioned 1538.5 85.0 2026.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_pond ci_ambient 0

# Yan — gym gate -> branch office door (tag-scoped)
execute positioned 1505.5 86.0 2043.5 run kill @e[type=easy_npc:humanoid,tag=yield_analyst,distance=..6]
scoreboard players set #amb_villain_yield_analyst ci_ambient 0

# Auntie Song — moved + reskinned + stop-3 dialog
execute positioned 1538.5 86.0 2064.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_trader_berries ci_ambient 0

# Madam Qiu — released (character deleted; no latch reset, her placement row is gone)
execute positioned 1488.5 87.0 2090.5 run kill @e[type=easy_npc:humanoid,distance=..3]

# Bo Huan — seven-colors page split re-latch (coords unchanged)
execute positioned 1512.5 85.0 2082.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_trader_apricorns ci_ambient 0

# Ning — moved to the sofa corner (tag-scoped: Ping's uuid body sits one desk over)
execute positioned 1540.5 86.0 2001.5 run kill @e[type=easy_npc:humanoid,tag=hz_office_staff,distance=..4]
scoreboard players set #amb_hz_receptionist ci_ambient 0

# Lan — hz_office_staff sensor re-tag re-latch (tag-scoped, coords unchanged)
execute positioned 1532.5 93.0 2005.5 run kill @e[type=easy_npc:humanoid,tag=hz_analyst,distance=..4]
scoreboard players set #amb_hz_analyst ci_ambient 0

# Cloud — home moved off the mill ledge; unique dedup tag catches the drifted body
kill @e[type=easy_npc:cobblemon_npc,tag=ci_amb_companion_wooloo]
scoreboard players set #amb_wooloo ci_ambient 0

# Scorchspire healer — rumor-hub page split re-latch (coords unchanged)
execute positioned 3672.5 68.0 4576.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_scorchspire_healer ci_ambient 0

# Nurse Mei Lin — uuid body (40d88258, the ex-stall-Mei body that stands in the Center).
# The identity swap itself rides update_npc_presets; this line answers playtest ping P1
# (1435.3 90 2151.5, the counter spot 3.5 blocks from where she stood) with a bare
# tp-by-uuid, the cicada_lift precedent. Chunk is inside the forceload rectangle.
tp 40d88258-2eed-4cb8-afc4-6f7edf77d69c 1435.3 90.0 2151.5

forceload remove 1424 1968 1560 2160
forceload remove 3664 4568 3680 4584

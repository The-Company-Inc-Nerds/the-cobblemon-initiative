# repairs wave a18 — apply: kill/tp the stale alpha.25 bodies with their chunks live
# and reset each affected latch to 0 so the recompiled placements respawn them at the
# CURRENT authored coords the next time a player comes within 40. Kill shapes follow
# the a14/a15/a17 lessons: positioned+type for solo sites, dedup-tag for wanderers,
# bare uuid for released/moved persisted bodies.

# Stone Knight — re-latch for the scale-1.3 visuals (coords unchanged; a17 precedent)
execute positioned 1479.9 87.0 2112.6 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_terrace ci_ambient 0

# Warden of the Quiet Garden — lowered y 85 -> 84 (N2)
execute positioned 1538.5 85.0 2026.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_statue_pond ci_ambient 0

# Nurse Mei Lin — uuid body, counter spot refined (N3; the a17 tp precedent)
tp 40d88258-2eed-4cb8-afc4-6f7edf77d69c 1432.5 90.0 2151.5

# Nana the Chansey — moved to the Center beside the nurse (N6); unique dedup tag
# catches the wander-drifted body (observed at 1532.7 99 1992.3)
kill @e[type=easy_npc:cobblemon_npc,tag=ci_amb_companion_chansey_anong]
scoreboard players set #amb_chansey_anong ci_ambient 0

# Rong — uuid body, Kaito-house roof -> branch office loft (N7)
tp f45f3fda-5de3-4f69-9485-e4a8f5df225f 1541.5 96.0 1992.5

# Bo Huan — moved 3 south off the wool-house line (N5)
execute positioned 1512.5 85.0 2082.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_trader_apricorns ci_ambient 0

# Leader Blossom — only if already transformed in this world: re-spawn her body at the
# P1 battle spot with the corrected (swapped-back) skin. The store/kill/import order
# matters: remember she existed, clear the old body, then import at the new spot.
# Untransformed worlds no-op here and get the new spot from aya_transform itself.
execute store success score #a18_blossom ci_ambient if entity @e[tag=hz_leader_body]
kill @e[tag=hz_leader_body]
execute if score #a18_blossom ci_ambient matches 1 run easy_npc preset import_new data easy_npc:preset/humanoid/hua_zhan_leader.npc.snbt 1381.2 93 2047.4

# ── Mystic Marsh ──

# Elowen Mistbloom — uuid body, tucked behind her stall counter (N13)
tp e4cb2cb0-8e00-4e94-bb77-7879d1b1898c 1236.5 65.0 2442.5

# Branith Lumenveil — uuid body, moved NW off the gym plaza (N21; Osric takes it)
tp 328509dc-fc8e-4354-b999-258eda3cbb29 1094.5 66.0 2421.5

# Rowan — uuid body, claimed as the final route gatekeeper; tp to the bend (N12)
tp 1f5eaff3-56c0-4b4f-bca5-ec2e49d93660 1233.5 65.0 2368.5

# Verified Clerk Osric — moved exchange kiosk -> gym plaza (N20)
execute positioned 1082.5 66.0 2448.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_mm_exchange_clerk ci_ambient 0

# Sedge — re-latched for the perch-gift dialog (N19; coords unchanged)
execute positioned 1058.5 78.0 2478.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_mm_wheat_trader ci_ambient 0

# Bramblea Mossglen — uuid body RELEASED (converted to a fairy-type latch upstairs;
# no latch reset needed, the new #amb_bramblea_mossglen initializes at 0)
kill 53e3e897-7d40-4108-b25e-879f14e8519a

# Morveth Marshbloom — uuid body RELEASED (converted to the bogged-type latch)
kill 99b82337-4a6b-43a6-a606-28f0b2cac6b6

# ── Fairy shrine floor ──

# The Last Pilgrim — released (character deleted; no latch reset, his placement row
# is gone — the Qiu precedent)
execute positioned 945.5 9.0 2712.5 run kill @e[type=easy_npc:humanoid,distance=..3]

# High Priestess Aurora — old humanoid body cleared; the reset latch respawns her as
# the fairy-type body on the drowned floor (947.5 0 2703.8) on next approach
execute positioned 951.5 3.0 2715.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_fairy_shrine_leader ci_ambient 0

forceload remove 1368 1960 1560 2170
forceload remove 1020 2320 1250 2530
forceload remove 936 2700 960 2724

# repairs wave a21 — apply: 0.7.0-alpha.3 playtest wave. Chunks are forceloaded by the arm.
# Typed kills only (a10/a13/a14 laws); kill lines precede their latch resets; holder RESET
# for cut characters, set 0 for re-latches (a19/a20 hygiene).

# ── Fairy shrine ──────────────────────────────────────────────────────────────────
# The First Vow — re-latch for the a21 one-button start (kill at the a2 latch spot; r3
# absorbs allay hover drift; vow 2 is 22 blocks down-stair, clear of r3).
execute positioned 943.4 41.0 2644.5 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players set #amb_fairy_allay_1 ci_ambient 0

# ── Mystic Marsh ──────────────────────────────────────────────────────────────────
# Thistrel Fogroot — uuid tp home to the North Reeds stall (wander -> stationary vendor;
# tp heals live-world wander drift; Puddle's latch nearby is untouched — tp only).
tp d4a1bcd5-5f21-435d-aaa8-ad1e6ce915c4 1122.5 65.0 2330.5

# Marsh-Child Bryn — re-latch to pick up the new single/marsh_child skin (latch bodies
# never repaint). r8 absorbs her wander drift; nearest other humanoids 16+ blocks out.
execute positioned 1064.5 65.0 2470.5 run kill @e[type=easy_npc:humanoid,distance=..8]
scoreboard players set #amb_mm_will_o_wisp_child ci_ambient 0

# Leader Titania — re-aim at rest (yaw 180 = faces the arena; LOOK_AT_RESET eases her
# head back to body yaw, which used to park her facing away from the stage).
tp b665f300-628d-445b-8a3e-42a5cefca1bd 943.5 69.0 2444.0 180 0

# Stray phone-caller body — park it off the battle stage to the SE lane. MUST precede the
# call_mom_watch_done clear below (a re-rung call's deliver sweep would delete it mid-park).
execute positioned 943.4 69.0 2434.1 run tp @e[type=easy_npc:humanoid,tag=ci_phone_caller,distance=..4] 1210.5 65.0 2502.5

# Mom-call re-ring heal: the a2 same-pass double-deliver ate her badge-3 congratulation
# call (the cut Unknown Number trigger collided with it). Clearing the latch lets phone/tick
# re-ring her once — the recompiled invisible-caller preset ships in this same build.
tag @a[tag=defeated_mystic_leader] remove call_mom_watch_done

# ── Mirebloom Paddies (N16 Halvard re-role) ───────────────────────────────────────
# Kill at the old fence anchor: takes BOTH the stale Steward Halvard body AND Nao's stacked
# body (villain_site_manager_2 latch shares the block). Halvard respawns re-voiced at the
# paddy house (1222.5 91 2821.5); Nao respawns at the fence with his free_field lever
# stripped (the liberation now finishes on Halvard's sluice button). Kai (yield officer)
# stands at 1221.5/2826.5 — 10.0 blocks from the kill origin, well outside r3.
execute positioned 1229.5 90.0 2820.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_mm_field_guard ci_ambient 0
scoreboard players set #amb_villain_site_manager_2 ci_ambient 0

# ── Deepcore City ─────────────────────────────────────────────────────────────────
# Sten Vale — re-latch pit-head -> east-row Pokemart (nearest cast body is Rilka's latch
# ~11 blocks NW, itself re-latched below — r3 + type safe).
execute positioned 1100.5 111.0 3215.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_deepcore_martkeeper ci_ambient 0

# Rilka — SAME-SPOT re-latch (id deepcore_nurse re-roled to quarry civilian; new role/skin
# ride the re-fired latch). set 0, NOT reset — her placement row still exists.
execute positioned 1092.5 114.0 3208.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_deepcore_nurse ci_ambient 0

# Gauntlet Marshal Osei — character CUT (Bruno two-track). Holder RESET not set (placement
# row gone). Nearest humanoids: Bruno 5.8 blocks, Striker 14 blocks — r3 safe.
execute positioned 996.5 129.0 3188.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players reset #amb_deepcore_marshal ci_ambient
tag @a remove dc_gauntlet_started

# Apprentice Ken — re-latch so a fresh import_new carries Tags dc_pit_lead (import onto an
# existing body ignores preset Tags). Nearest humanoid Striker 13.5 blocks — r3 safe.
execute positioned 984.3 129.0 3173.2 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_deepcore_apprentice ci_ambient 0

# ── Gaviota Port ──────────────────────────────────────────────────────────────────
# Nurse Coralie — latch character retired (gaviota_nurse is now Lucia Marelli's uuid body).
# Positioned kill at the exact latch spot (nearest other humanoid 40+ blocks: safe), holder
# RESET (placement row gone). MUST run BEFORE the Lucia tp below lands on this spot.
execute positioned 560.5 82.0 3540.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players reset #amb_gaviota_nurse ci_ambient

# Lucia Marelli — uuid tp INTO the Center, AFTER the kill (bundled-map nbt moved too).
tp 603263f0-b8ef-4da3-945e-fdb7d0c16c5b 560.5 82.0 3540.5

# Titleholder Marlin — uuid tp behind the mart CobbleMerchant (register live-scanned at
# 655.3 83 3533.5 yaw 90; ground-probed air over concrete).
tp f7295ea6-9fbf-4985-a9e1-5bec4131e6ac 656.5 83.0 3533.5 90 0

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 935 2636 951 2652
forceload remove 1052 2458 1076 2482
forceload remove 1110 2318 1134 2342
forceload remove 936 2427 950 2451
forceload remove 1203 2495 1218 2510
forceload remove 1215 2810 1235 2830
forceload remove 950 3150 1020 3200
forceload remove 1085 3200 1168 3276
forceload remove 555 3430 592 3546
forceload remove 650 3528 661 3543

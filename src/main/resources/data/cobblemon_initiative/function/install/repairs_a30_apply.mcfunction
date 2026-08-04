# repairs wave a30 — apply: 0.7.0-alpha.18 playtest wave. Chunks forceloaded by the arm.

# ── (1) Kalahar mirage-hunt rework migration ──────────────────────────────────────
# Latch-spawned bodies never receive preset updates (not in preset_map), so the six reals are
# killed by their ci_kal_* tags and their latches re-armed — they respawn from the NEW presets
# (mirage-text dialog, new town coords) on approach. The retired guide + any leftover decoys go
# with them; the hunt flags reset so Tarek's proximity start re-scatters from the new pool.
kill @e[tag=ci_kal_dune]
kill @e[tag=ci_kal_terra]
kill @e[tag=ci_kal_boulder]
kill @e[tag=ci_kal_juno]
kill @e[tag=ci_kal_dustin]
kill @e[tag=ci_kal_vince]
kill @e[tag=ci_kalahar_guide]
kill @e[tag=ci_mirage_fake]
kill @e[tag=ci_mirage_doppler]
scoreboard players set #amb_kalahar_jr_apprentice ci_ambient 0
scoreboard players set #amb_kalahar_apprentice ci_ambient 0
scoreboard players set #amb_kalahar_trainer_1 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_2 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_3 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_4 ci_ambient 0
# Migrated-save backfill (review-found): a17 players may have BEATEN students that predate the
# found_* tags — without these, a beaten student re-presents as an unresolved mirage and its
# beaten entry sits unreachable behind the priority-40 mirage gate. Defeat implies found.
tag @a[tag=defeated_kalahar_jr_apprentice] add found_kalahar_jr_apprentice
tag @a[tag=defeated_kalahar_apprentice] add found_kalahar_apprentice
tag @a[tag=defeated_kalahar_trainer_1] add found_kalahar_trainer_1
tag @a[tag=defeated_kalahar_trainer_2] add found_kalahar_trainer_2
tag @a[tag=defeated_kalahar_trainer_3] add found_kalahar_trainer_3
tag @a[tag=defeated_kalahar_trainer_4] add found_kalahar_trainer_4
scoreboard players set #started ci_kalahar_hunt 0
scoreboard players set #cleaned ci_kalahar_hunt 0

# ── (2) Cyclops reseed (nameless preset + a18 throw) ──────────────────────────────
# Killing the bodies + clearing the per-world seed flag makes the NEXT server start re-import
# all six from the updated preset (CyclopsManager.seedOnServerStarted force-loads its own chunks).
kill @e[tag=ci_cyclops]
scoreboard players set #spawned ci_cyclops_spawned 0

# ── (3) farm_5 gold-pattern (Crossroads Granary) ─────────────────────────────────
# Old Guarded Survey Stone body dies; the farmer Suhail latch-places at the same spot (new char).
execute positioned 2318.5 83 3542.5 run kill @e[type=!minecraft:player,name="Guarded Survey Stone",distance=..4]
# Nao re-latch (his old preset still carried the on_win free_field + premature title cards).
execute positioned 2309.5 83 3540.5 run kill @e[type=!minecraft:player,name="Nao",distance=..4]
scoreboard players set #amb_villain_site_manager_5 ci_ambient 0
# Aki moves from the perimeter gate to the P3 field edge (2316.5/83/3519.5-ish latch).
execute positioned 2262.5 96 3500.5 run kill @e[type=!minecraft:player,name="Aki",distance=..6]
scoreboard players set #amb_villain_yield_officer_5 ci_ambient 0

# ── (4) Manaphy into the treasure room ───────────────────────────────────────────
# The old surface prop ("The Deep Chamber", y63) dies; the new Manaphy-bodied latch places
# underwater at 2767.9/33/3490 on approach (a7/a9 precedent lines).
execute positioned 2760.5 63 3490.5 run kill @e[type=!minecraft:player,name="The Deep Chamber",distance=..48]
scoreboard players set #amb_manaphy_giver ci_ambient 0

# ── (5) Rashid Anwar uuid-body move (playtest N3) ────────────────────────────────
# Stationary body — plain tp, no Navigation.Home. Bundled-map entities edit deferred (the
# repairs tp also fires on a fresh install's install-run, a14 rule).
tp d939ab8b-4cec-47fd-9016-389e4da7422a 1963.5 113.0 3839.5

# ── (6) Sun-Dried Sentinel y nudge (playtest N9: 174 -> 172.75) ──────────────────
execute positioned 1861.4 174 4381.4 run tp @e[type=easy_npc:husk,distance=..6,limit=1] 1861.4 172.75 4381.4

# ── (7) Town-well drain (playtest M1) ────────────────────────────────────────────
# The Company squats the Oasis -> the town well stands dry. Skip if this save already cleared
# the survey (oasis_pump_off on any player); well_drain_arm forceloads + schedules its own sets.
execute unless entity @a[tag=oasis_pump_off] run function cobblemon_initiative:oasis/well_drain_arm

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 1976 4134
forceload remove 1998 4102
forceload remove 2022 3974
forceload remove 1984 4056
forceload remove 2149 3986
forceload remove 2076 3948
forceload remove 1978 4085
forceload remove 2004 4098
forceload remove 1992 4108
forceload remove 1982 4130
forceload remove 1970 4140
forceload remove 2028 3980
forceload remove 2016 3968
forceload remove 2143 3992
forceload remove 2155 3980
forceload remove 1990 4050
forceload remove 1978 4062
forceload remove 2082 3954
forceload remove 2070 3942
forceload remove 2058 4075
forceload remove 2088 3928
forceload remove 2050 4030
forceload remove 2100 4100
forceload remove 845 2216 877 2248
forceload remove 885 2168 917 2200
forceload remove 844 2134 876 2166
forceload remove 728 2182 760 2214
forceload remove 716 2251 748 2283
forceload remove 755 2287 787 2319
forceload remove 2318 3542
forceload remove 2309 3540
forceload remove 2262 3500
forceload remove 2760 3490
forceload remove 1949 3910
forceload remove 1963 3839
forceload remove 1861 4381

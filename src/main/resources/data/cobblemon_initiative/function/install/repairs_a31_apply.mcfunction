# repairs wave a31 — apply: 0.7.0-alpha.19 corrections wave. Chunks forceloaded by the arm.

# ── (1) Kalahar geometry fix (playtest 2026-08-04 note 1 + N1) ────────────────────
# `kalahar clear` (perm-2 mod command; function source qualifies) chunk-loads the 31-spot
# scatter pool, kills every fake + Doppler, and resets #started/#cleaned — the hunt re-arms
# and now starts from Tarek Ramessu's dialog button.
cobblemon-initiative kalahar clear
# The reals die wherever they stand (the a18 pin spots, or the old hollow if already found).
# a19: reals are SUMMON-ONLY now — no latches to re-arm; the next Tarek-button start deals
# every unfound student (real + mirages) onto the 31-pin pool. Beaten students stay beaten
# via their tags; only their cosmetic station bodies vacate.
kill @e[tag=ci_kal_dune]
kill @e[tag=ci_kal_terra]
kill @e[tag=ci_kal_boulder]
kill @e[tag=ci_kal_juno]
kill @e[tag=ci_kal_dustin]
kill @e[tag=ci_kal_vince]
# Un-find every UNDEFEATED student (found-but-unbeaten under the wrong geometry would battle at
# a town spot); DEFEATED students keep found — their beaten ladder stays reachable.
execute as @a[tag=!defeated_kalahar_jr_apprentice] run tag @s remove found_kalahar_jr_apprentice
execute as @a[tag=!defeated_kalahar_apprentice] run tag @s remove found_kalahar_apprentice
execute as @a[tag=!defeated_kalahar_trainer_1] run tag @s remove found_kalahar_trainer_1
execute as @a[tag=!defeated_kalahar_trainer_2] run tag @s remove found_kalahar_trainer_2
execute as @a[tag=!defeated_kalahar_trainer_3] run tag @s remove found_kalahar_trainer_3
execute as @a[tag=!defeated_kalahar_trainer_4] run tag @s remove found_kalahar_trainer_4

# ── (2) Well-Keeper Marisol out of the blocks (playtest 2026-08-04 N3) ──────────
execute positioned 2040.5 136 4100.5 run kill @e[type=!minecraft:player,name="Well-Keeper Marisol",distance=..4]
scoreboard players set #amb_kalahar_rumor_marisol ci_ambient 0

# ── (2b) Boundary Stones retired -> repeatable cache recoveries (2026-08-04 follow-up) ──
# Kill the two basalt stone props (both named "Basalt Survey Stone"; positioned + tight radius)
# and strip every stones-quest tag — mid-quest players get a clean slate; anyone who already
# FILED keeps their payout, and the founder beat replays properly on their first cache turn-in.
execute positioned 1980.5 120 3960.5 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..6]
execute positioned 2140.5 132 3900.5 run kill @e[type=!minecraft:player,name="Basalt Survey Stone",distance=..6]
tag @a remove seal_stone_1
tag @a remove seal_stone_2
tag @a remove seal_stone_3
tag @a remove stone3_guard_clear
tag @a remove boundary_stones_active
tag @a remove boundary_stones_done

# ── (3) Noura Ma-at to her new corner (playtest N2) — wanders, so Home moves too ──
tp 5d49a0d9-b283-48ae-9b30-39f8093122cc 1965.5 136.0 4060.5
data modify entity 5d49a0d9-b283-48ae-9b30-39f8093122cc Navigation.Home set value {X:1965,Y:136,Z:4060}

# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 1978 4142
forceload remove 1978 4032
forceload remove 1934 4043
forceload remove 1934 4131
forceload remove 2022 4043
forceload remove 2022 4131
forceload remove 1978 4092
forceload remove 2040 4100
forceload remove 1980 3960
forceload remove 2140 3900
forceload remove 1938 4048
forceload remove 1965 4060

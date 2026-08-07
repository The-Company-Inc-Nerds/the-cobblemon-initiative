# repairs wave a38 — arm: playtest 2026-08-06 — Kalahar desert quest cluster physical blocks.
# REWORKED to the final REPEATABLE treasure-hunt design (playtest N18): the found treasure chests
# are NO LONGER static-placed at install — both Kalahar hunts (Nadia's kiln hunt + Warden Ossa's
# warden hunt) now bury their chest at hunt-START (sidequest/kalahar_hunt/arm_* -> place_*) and
# retire it on find, so nothing findable is placed here anymore. All that remains static is the
# ONE flavor prop:
#   Warden Ossa's WARDEN'S DEPOSIT — a custom-named (empty) chest at her records post. Pure
#   flavor: the actual turn-in is Ossa's dialog probing the player's carried Warden's Cache item.
# (M35 second-well fill needs NO repairs — the well is EMPTY at rest, so a fresh install already
#  looks unfilled; the fill fires on oasis_pump_off and its rescue tick runs from tick.json.)
# Guarded once-per-world by #repair_a38 like every wave.
scoreboard players set #repair_a38 ci_ambient 1
# Warden's Deposit beside Ossa's records post (Ossa @ 2050/129/4085; chest at 2051/129/4085).
forceload add 2051 4085 2051 4085
schedule function cobblemon_initiative:install/repairs_a38_apply 3s

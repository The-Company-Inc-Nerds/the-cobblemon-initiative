# Kalahar Reach (gym 6) mirage-hunt MIGRATION — run ONCE on a world that placed the gym cast
# BEFORE the mirage-hunt rework (the six students used to stand in the sandstorm hollow).
#   /function cobblemon_initiative:gym/kalahar_hunt_setup
# Fresh installs do NOT need this — the students latch straight to their new town coords.
#
# Placement latches are once-per-world, so the students will not relocate on their own. This
# removes the six OLD hollow bodies by their exact former coords (distance ..1.5 — never reaches
# Leader Gaia at ~1978/131/4092, who stays put) and re-arms the six student latches + the guide
# latch so they re-place at their scattered town coords on next approach.
#
# GUARDED so it is safe to invoke more than once: the #kalahar_migrated flag makes any repeat a
# no-op. This matters because a beaten student, once its on_win teleports it BACK to the hollow,
# sits on the very coords this function kills — without the guard a second run would delete an
# already-beaten student (progression survives via the defeated_kalahar_* tag, but the hollow
# body would vanish and its latch re-fire a duplicate in town). Run it once; the guard handles
# accidental re-runs. To deliberately re-migrate, first `scoreboard players set #kalahar_migrated ci_ambient 0`.
execute if score #kalahar_migrated ci_ambient matches 1.. run return 0
scoreboard players set #kalahar_migrated ci_ambient 1

# 1) remove the old hollow bodies (former placements) — Gaia (uuid e0fd76d6-...) is >3 blocks away
kill @e[type=easy_npc:humanoid,x=1980,y=131,z=4089,distance=..1.5]
kill @e[type=easy_npc:humanoid,x=1976,y=131,z=4095,distance=..1.5]
kill @e[type=easy_npc:humanoid,x=1974,y=131,z=4090,distance=..1.5]
kill @e[type=easy_npc:humanoid,x=1982,y=131,z=4090,distance=..1.5]
kill @e[type=easy_npc:humanoid,x=1974,y=131,z=4094,distance=..1.5]
kill @e[type=easy_npc:humanoid,x=1982,y=131,z=4094,distance=..1.5]

# 2) re-arm the latches so the students + guide re-place at their new (town / entrance) coords
scoreboard players set #amb_kalahar_apprentice ci_ambient 0
scoreboard players set #amb_kalahar_jr_apprentice ci_ambient 0
scoreboard players set #amb_kalahar_trainer_1 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_2 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_3 ci_ambient 0
scoreboard players set #amb_kalahar_trainer_4 ci_ambient 0
scoreboard players set #amb_kalahar_guide ci_ambient 0

tellraw @a {"text":"[Kalahar] Old hollow bodies cleared and latches re-armed. Walk the town to re-place the scattered cast.","color":"gold"}

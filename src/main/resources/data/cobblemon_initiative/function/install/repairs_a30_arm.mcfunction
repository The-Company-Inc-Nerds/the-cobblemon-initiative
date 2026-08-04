# repairs wave a30 — arm: 0.7.0-alpha.18 playtest wave (2026-08-03 log).
# Scope: (1) Kalahar mirage-hunt REWORK migration — kill the a17-placed real students (their
# presets/coords changed wholesale: mirage-text dialogs + new town spots), the retired dedicated
# gym guide (Tarek took the role), and any leftover fakes/Dopplers at the old 16-spot scatter
# pool; re-arm the six real latches + reset the hunt flags so Tarek's proximity re-scatters from
# the new 31-spot pool. (2) Cyclops reseed (nameless preset + stronger throw — next SERVER_STARTED
# re-imports). (3) farm_5 gold-pattern: old Guarded Survey Stone body -> the farmer Suhail spawns
# at the same spot; Nao re-latch (on_win stripped); Aki re-latch at the P3 field edge.
# (4) Manaphy re-latch into the monument treasure room. (5) Rashid Anwar uuid-body move (N3).
# (6) Sun-Dried Sentinel y nudge 174 -> 172.75. (7) Town-well drain (M1) unless the Oasis is
# already cleared. Forceload every kill/tp site; the apply tears them down.
scoreboard players set #repair_a30 ci_ambient 1
# Kalahar: old real spots + old guide
forceload add 1976 4134
forceload add 1998 4102
forceload add 2022 3974
forceload add 1984 4056
forceload add 2149 3986
forceload add 2076 3948
forceload add 1978 4085
# Kalahar: the 16 OLD scatter spots (leftover fakes may be standing on them)
forceload add 2004 4098
forceload add 1992 4108
forceload add 1982 4130
forceload add 1970 4140
forceload add 2028 3980
forceload add 2016 3968
forceload add 2143 3992
forceload add 2155 3980
forceload add 1990 4050
forceload add 1978 4062
forceload add 2082 3954
forceload add 2070 3942
forceload add 2058 4075
forceload add 2088 3928
forceload add 2050 4030
forceload add 2100 4100
# Cyclops spawn points (mushroom island) — ±16-block areas: the bodies wander/chase
# (WATER_AVOIDING stroll + ATTACK_PLAYER, no home tether), so a single chunk misses drifters
forceload add 845 2216 877 2248
forceload add 885 2168 917 2200
forceload add 844 2134 876 2166
forceload add 728 2182 760 2214
forceload add 716 2251 748 2283
forceload add 755 2287 787 2319
# farm_5 cluster: survey-stone spot + Nao + old Aki gate
forceload add 2318 3542
forceload add 2309 3540
forceload add 2262 3500
# Manaphy old surface latch spot
forceload add 2760 3490
# Rashid Anwar: old body site + new post
forceload add 1949 3910
forceload add 1963 3839
# Sun-Dried Sentinel dune
forceload add 1861 4381
schedule function cobblemon_initiative:install/repairs_a30_apply 3s

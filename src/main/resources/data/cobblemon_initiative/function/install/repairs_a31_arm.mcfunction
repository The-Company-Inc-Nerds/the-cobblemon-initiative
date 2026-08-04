# repairs wave a31 — arm: 0.7.0-alpha.19 corrections wave (2026-08-04 log).
# Scope: (1) Kalahar GEOMETRY FIX — the 2026-08-03 N11–N16 pins turned out to be the FOUND-teleport
# DESTINATIONS (gym stations), not town placements: kill the a18-placed reals (standing on the
# pins) + any found bodies in the old hollow, re-arm the six latches (they respawn at the reverted
# a17 town hiding spots), un-find every UNDEFEATED student so the beat replays under the corrected
# geometry, and `kalahar clear` sweeps fakes/Dopplers (it chunk-loads the scatter pool itself) +
# resets the hunt flags — the hunt now re-starts from Tarek's dialog button, never proximity.
# (2) Well-Keeper Marisol out of the blocks -> the town-well rim. (3) Noura Ma-at to her new
# corner (uuid body, wanders -> tp + Navigation.Home, the a25 Korrin idiom).
scoreboard players set #repair_a31 ci_ambient 1
# Kalahar: the six a18 pin spots (current real bodies) + the old hollow (found bodies)
forceload add 1978 4142
forceload add 1978 4032
forceload add 1934 4043
forceload add 1934 4131
forceload add 2022 4043
forceload add 2022 4131
forceload add 1978 4092
# Marisol old (buried) spot
forceload add 2040 4100
# The two retired boundary-stone props
forceload add 1980 3960
forceload add 2140 3900
# Noura old + new corners
forceload add 1938 4048
forceload add 1965 4060
schedule function cobblemon_initiative:install/repairs_a31_apply 3s

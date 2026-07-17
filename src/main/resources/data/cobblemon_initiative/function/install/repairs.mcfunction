# One-shot world repairs for content that moved AFTER a world already latched it.
# Dispatched by `/cobblemon-initiative install run` (and safe to run by hand); each
# wave guards itself with a #repair_* flag in ci_ambient so it applies exactly once
# per world. Pattern per wave: arm (forceload the affected sites + schedule) → apply
# (kill the stale bodies with chunks live, reset their latches, unload). Latches then
# respawn the bodies at the CURRENT authored coords the next time a player visits.

# ── wave a2 (0.6.0-alpha.2): Takehara greenhouse cast, Sango auditor leashes,
#    mew-wisp giver + Oasis pump crew (all moved; old bodies stale or buried)
execute unless score #repair_a2 ci_ambient matches 1 run function cobblemon_initiative:install/repairs_a2_arm

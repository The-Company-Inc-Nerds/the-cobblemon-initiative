# Sign the Frontier ledger. Called from Frontier Registrar Odette's sign button, which is
# gated on `not_tag frontier_registered`, so this fires exactly once. Grants the first hall
# pass gratis (the money finally holds still — passes are cheap now). frontier_passes is a
# soft/flavor sink: it is never consumed, never blocks a battle (all frontier fights are
# opt-in). See registers economy/load for the objective.
tag @s add frontier_registered
scoreboard players add @s frontier_passes 1
title @s title [{"text":"FRONTIER LEDGER SIGNED","color":"gold"}]
title @s subtitle [{"text":"The halls are yours. One hall pass, on the house.","color":"gray"}]

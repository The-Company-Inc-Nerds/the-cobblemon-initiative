# Dry Season (Q2) - shut the Oasis pump manifold. Run AS the player (manifold shut button;
# the dialog ready gate already guarantees both crew are down + not already off). Latches
# oasis_pump_off, then claws the CobbleDollar instability index back -3 (the diversion stops
# starving farm_5's water). cd_instability lives on the #idx fake-player, NOT @s - mirror the
# floor-at-0 idiom from liberation/free_field_apply exactly. Title sting on the shut.
execute unless entity @s[tag=oasis_pump_off] run scoreboard players remove #idx cd_instability 3
execute if score #idx cd_instability matches ..-1 run scoreboard players set #idx cd_instability 0
# a18 (playtest M1): the moment the survey dies, the TOWN WELL refills — forceload + scheduled
# setblocks (the well is ~350 blocks away, unloaded). Idempotent, so no oasis_pump_off guard needed.
function cobblemon_initiative:oasis/well_restore_arm
# a38 (playtest M35): the SECOND well (empty at rest) fills on the same beat — same forceload+
# schedule idiom. Its rescue tick self-disables once oasis_pump_off is set below.
function cobblemon_initiative:oasis/second_well_fill_arm
tag @s add oasis_pump_off
title @s title [{"text":"SAMPLES SPIKED","color":"aqua"}]
title @s subtitle [{"text":"The Company numbers come back worthless. Far away, a town well remembers how to fill.","color":"gray"}]

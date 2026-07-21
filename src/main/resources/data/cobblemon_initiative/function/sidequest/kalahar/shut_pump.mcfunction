# Dry Season (Q2) - shut the Oasis pump manifold. Run AS the player (manifold shut button;
# the dialog ready gate already guarantees both crew are down + not already off). Latches
# oasis_pump_off, then claws the CobbleDollar instability index back -3 (the diversion stops
# starving farm_5's water). cd_instability lives on the #idx fake-player, NOT @s - mirror the
# floor-at-0 idiom from liberation/free_field_apply exactly. Title sting on the shut.
execute unless entity @s[tag=oasis_pump_off] run scoreboard players remove #idx cd_instability 3
execute if score #idx cd_instability matches ..-1 run scoreboard players set #idx cd_instability 0
tag @s add oasis_pump_off
title @s title [{"text":"SAMPLES SPIKED","color":"aqua"}]
title @s subtitle [{"text":"The Company numbers come back worthless. The Oasis is not worth their while.","color":"gray"}]

# Resurrection machine cinematic - PHASE 2 (mid, +30t). Scheduled from revive_begin; runs at world level,
# so fx are positioned absolutely and the ping targets the reviving player(s) by the shared ci_reviving tag.
playsound cobblemon:block.fossil_machine.active_loop master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound cobblemon:block.fossil_machine.dna_full master @a[distance=..24] 1902.5 116 2313.8 1 1
execute positioned 1902.5 116 2313.8 run particle minecraft:electric_spark ~ ~ ~ 0.6 0.7 0.6 0.15 50
execute positioned 1902.5 116 2313.8 run particle minecraft:end_rod ~ ~ ~ 0.4 0.6 0.4 0.02 30
title @a[tag=ci_reviving] actionbar [{"text":"Sediment sloughs away. Something is moving in the tank...","color":"aqua"}]

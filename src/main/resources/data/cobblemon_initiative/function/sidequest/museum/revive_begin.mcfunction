# Resurrection machine cinematic — PHASE 1 (begin). Called AS THE PLAYER from turnin/<fossil>, right
# after the fossil item is floated over the platform (1902.5/116/2313.8). Seals the tank and schedules
# the mid + finish beats. All fx are positioned absolutely on the fixed platform; the scheduled beats
# run at world level and find the reviving player via the ci_reviving_* carry tags.
playsound cobblemon:fossilmachine.insert_fossil master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound minecraft:block.conduit.activate master @a[distance=..24] 1902.5 116 2313.8 0.7 1.2
execute positioned 1902.5 116 2313.8 run particle minecraft:enchant ~ ~ ~ 0.5 0.6 0.5 0.1 60
title @s actionbar [{"text":"The restoration tank seals and hums to life...","color":"aqua"}]
schedule function cobblemon_initiative:sidequest/museum/revive_mid 30t
schedule function cobblemon_initiative:sidequest/museum/revive_finish 60t

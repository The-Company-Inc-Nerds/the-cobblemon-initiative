# Only show particles if debug is enabled
execute if score #global debug_particles matches 1 run particle flame ~ ~ ~ 0 0 0 0 1 force

scoreboard players remove #raycast raycast_steps 1

execute if entity @p[distance=..2] run function cobblemon-initiative:hit

# Continue through transparent blocks
execute if block ~ ~ ~ #cobblemon-initiative:transparent if score #raycast raycast_steps matches 1.. positioned ^ ^ ^0.2 run function cobblemon-initiative:raycast

# Continue through open doors
execute if block ~ ~ ~ #minecraft:doors[open=true] if score #raycast raycast_steps matches 1.. positioned ^ ^ ^0.2 run function cobblemon-initiative:raycast

# Continue through open fence gates
execute if block ~ ~ ~ #minecraft:fence_gates[open=true] if score #raycast raycast_steps matches 1.. positioned ^ ^ ^0.2 run function cobblemon-initiative:raycast

# Continue through open trapdoors
execute if block ~ ~ ~ #minecraft:trapdoors[open=true] if score #raycast raycast_steps matches 1.. positioned ^ ^ ^0.2 run function cobblemon-initiative:raycast


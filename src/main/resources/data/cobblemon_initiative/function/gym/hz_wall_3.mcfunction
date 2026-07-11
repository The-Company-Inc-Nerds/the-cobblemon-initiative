# FOUR GARDENS wall 3/4 — the Water Terrace vine wall crumbles when Horticulturist
# Fang falls. Run as/at @s = the winning player (hua_zhan_trainer_3 battle on_win
# appends `execute as @1 at @1 run function cobblemon_initiative:gym/hz_wall_3`).
# Replace-filtered fills only (vine + any leaf block) so a mis-sized box can never
# cut into arena structure. Walls can fall in ANY order; hz_walls_check fires the
# all-four finale exactly once (hz_walls_done player tag).
#
# Wall box: TODO(showrunner): confirm — placeholder 9x5x1 panel ~6 blocks north of
# the terrace warden statue at (1478 87 2098).
fill 1474 87 2092 1482 91 2092 minecraft:air replace minecraft:vine
fill 1474 87 2092 1482 91 2092 minecraft:air replace #minecraft:leaves
particle minecraft:cherry_leaves 1478 89 2092 4.5 2.5 0.5 0 220 force @s
particle minecraft:composter 1478 89 2092 4.5 2.5 0.5 0 160 force @s
playsound minecraft:block.grass.break block @s 1478 89 2092 3 0.8
playsound minecraft:block.beacon.deactivate block @s 1478 89 2092 2 1.1
title @s actionbar {"text":"The vine wall crumbles — the water terrace stands open.","color":"green"}
function cobblemon_initiative:gym/hz_walls_check

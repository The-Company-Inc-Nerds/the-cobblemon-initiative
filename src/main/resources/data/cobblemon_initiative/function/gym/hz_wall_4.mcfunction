# FOUR GARDENS wall 4/4 — the Still Pond vine wall crumbles when Ranger Xiu falls.
# Run as/at @s = the winning player (hua_zhan_trainer_4 battle on_win appends
# `execute as @1 at @1 run function cobblemon_initiative:gym/hz_wall_4`).
# Replace-filtered fills only (vine + any leaf block) so a mis-sized box can never
# cut into arena structure. Walls can fall in ANY order — this file owns NO special
# finale; hz_walls_check (called by every wall) fires the all-four beat exactly once
# (hz_walls_done player tag), which doubles as the visual for the existing
# all-4-wardens Aya gate (that wiring lives in groundskeeper_aya.json, untouched).
#
# Wall box: TODO(showrunner): confirm — placeholder 9x5x1 panel ~6 blocks north of
# the pond warden statue at (1484 87 2160).
fill 1480 87 2154 1488 91 2154 minecraft:air replace minecraft:vine
fill 1480 87 2154 1488 91 2154 minecraft:air replace #minecraft:leaves
particle minecraft:cherry_leaves 1484 89 2154 4.5 2.5 0.5 0 220 force @s
particle minecraft:composter 1484 89 2154 4.5 2.5 0.5 0 160 force @s
playsound minecraft:block.grass.break block @s 1484 89 2154 3 0.8
playsound minecraft:block.beacon.deactivate block @s 1484 89 2154 2 1.1
title @s actionbar {"text":"The vine wall crumbles — the still pond stands open.","color":"green"}
function cobblemon_initiative:gym/hz_walls_check

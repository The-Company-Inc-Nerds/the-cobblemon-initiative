# FOUR GARDENS wall 1/4 — the Moss Court vine wall crumbles when Gardener Lin falls.
# Run as/at @s = the winning player (hua_zhan_trainer_1 battle on_win appends
# `execute as @1 at @1 run function cobblemon_initiative:gym/hz_wall_1`).
# Replace-filtered fills only (vine + any leaf block) so a mis-sized box can never
# cut into arena structure. Walls can fall in ANY order; hz_walls_check fires the
# all-four finale exactly once (hz_walls_done player tag).
#
# Wall box: TODO(showrunner): confirm — placeholder 9x5x1 panel ~6 blocks north of
# the moss court warden statue at (1450 93 2052).
fill 1446 93 2046 1454 97 2046 minecraft:air replace minecraft:vine
fill 1446 93 2046 1454 97 2046 minecraft:air replace #minecraft:leaves
particle minecraft:cherry_leaves 1450 95 2046 4.5 2.5 0.5 0 220 force @s
particle minecraft:composter 1450 95 2046 4.5 2.5 0.5 0 160 force @s
playsound minecraft:block.grass.break block @s 1450 95 2046 3 0.8
playsound minecraft:block.beacon.deactivate block @s 1450 95 2046 2 1.1
title @s actionbar {"text":"The vine wall crumbles — the moss court stands open.","color":"green"}
function cobblemon_initiative:gym/hz_walls_check

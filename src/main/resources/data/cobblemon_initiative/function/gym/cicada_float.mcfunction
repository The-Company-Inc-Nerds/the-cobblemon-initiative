# Leader Cicada's hover — the brood-mother floats over the open-top floor-1 arena and
# glides down to meet a challenger. Registered in #minecraft:tick; objective ci_gym is
# added in gym/load. Body: persisted world uuid c577141c-305f-4c20-a6bb-444d3b4d5ae0;
# arena floor 2055.5 / 138.0 / 2460.5; perch y173; conversation hover y139 (one block
# off the floor — always floating, comfortably inside right-click reach).
#
# The preset's NoGravity (character `float: true`) HOLDS whatever Y the tp leaves him
# at — vertical motion is these 0.5-block tp steps (~10 blocks/s), state is just his Y:
#   player who beat the apprentice ON the floor (y137-141 box) -> glide DOWN toward y139
#   nobody within 26                          -> drift back UP toward y173
#   in between                                -> hold (hysteresis; battles keep him down)
# The brood-mother stays aloft on her perch until the challenger has earned the audience:
# she only descends for a player carrying defeated_takehara_apprentice (showrunner 2026-07-20).
#
# One-shot lift (import-then-tp) arms the whole behavior the first time his chunk loads.
execute unless score #cicada_lift ci_gym matches 1 if entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 run function cobblemon_initiative:gym/cicada_lift

# His Y as x10 fixed-point (1390 = y139.0, 1730 = y173.0) — exact step comparisons.
execute if entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 store result score #cicada_y ci_gym run data get entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 Pos[1] 10

# Glide down to conversation height while a challenger who has beaten the gym apprentice stands
# ON the arena floor. Flat box (y137-141, ±14 blocks around the floor point), NOT a sphere — the
# old distance=..14 reached the y151 walkway above and the approach below the arena, so he
# descended before the challenger ever entered it (showrunner report 2026-07-17). The
# tag=defeated_takehara_apprentice gate keeps her perched until the apprentice is cleared.
execute if score #cicada_y ci_gym matches 1391.. if entity @a[x=2041.5,y=137,z=2446.5,dx=28,dy=4,dz=28,tag=defeated_takehara_apprentice] as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s run tp @s 2055.5 ~-0.5 2460.5

# Drift back up to the perch once the arena has emptied.
execute if score #cicada_y ci_gym matches ..1729 positioned 2055.5 138.0 2460.5 unless entity @a[distance=..26] as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s run tp @s 2055.5 ~0.5 2460.5

# Hover shimmer while airborne (only when someone is around to see it).
execute if score #cicada_y ci_gym matches 1391.. as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s if entity @a[distance=..64] run particle minecraft:end_rod ~ ~-0.2 ~ 0.35 0.12 0.35 0.004 2 normal

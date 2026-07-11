# Leader Cicada's hover — the brood-mother floats over the open-top floor-1 arena and
# glides down to meet a challenger. Registered in #minecraft:tick; objective ci_gym is
# added in gym/load. Body: persisted world uuid c577141c-305f-4c20-a6bb-444d3b4d5ae0;
# arena floor 2055.5 / 138.0 / 2460.5; perch y173; conversation hover y139 (one block
# off the floor — always floating, comfortably inside right-click reach).
#
# The preset's NoGravity (character `float: true`) HOLDS whatever Y the tp leaves him
# at — vertical motion is these 0.5-block tp steps (~10 blocks/s), state is just his Y:
#   player within 14 of the floor point  -> glide DOWN toward y139
#   nobody within 26                     -> drift back UP toward y173
#   in between                           -> hold (hysteresis; battles keep him down)
#
# One-shot lift (import-then-tp) arms the whole behavior the first time his chunk loads.
execute unless score #cicada_lift ci_gym matches 1 if entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 run function cobblemon_initiative:gym/cicada_lift

# His Y as x10 fixed-point (1390 = y139.0, 1730 = y173.0) — exact step comparisons.
execute if entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 store result score #cicada_y ci_gym run data get entity c577141c-305f-4c20-a6bb-444d3b4d5ae0 Pos[1] 10

# Glide down to conversation height while a challenger stands in the arena.
execute if score #cicada_y ci_gym matches 1391.. positioned 2055.5 138.0 2460.5 if entity @a[distance=..14] as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s run tp @s 2055.5 ~-0.5 2460.5

# Drift back up to the perch once the arena has emptied.
execute if score #cicada_y ci_gym matches ..1729 positioned 2055.5 138.0 2460.5 unless entity @a[distance=..26] as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s run tp @s 2055.5 ~0.5 2460.5

# Hover shimmer while airborne (only when someone is around to see it).
execute if score #cicada_y ci_gym matches 1391.. as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s if entity @a[distance=..64] run particle minecraft:end_rod ~ ~-0.2 ~ 0.35 0.12 0.35 0.004 2 normal

# MACRO — restore plain sand over the (possibly part-brushed) site + drop the forceload. The
# scan guarantees all three columns were pure surface sand, so this is an exact restore.
$setblock $(x1) $(y1) $(z1) minecraft:sand
$setblock $(x2) $(y2) $(z2) minecraft:sand
$setblock $(x3) $(y3) $(z3) minecraft:sand
$forceload remove $(cx) $(cz)

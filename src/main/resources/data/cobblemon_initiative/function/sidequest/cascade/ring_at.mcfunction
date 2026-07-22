# Cascade Ascent — draw one vertical hoop at the positioned checkpoint center. Run positioned
# at the checkpoint coord (see rings.mcfunction) as/at the runner so `force @s` targets them.
# 10 end_rod points on a radius-1.3 circle in the vertical Y-Z plane (X fixed), raised ~1.2 so
# the runner walks through it: the hoop faces along the eastbound climb (CP1->CP4 travel is
# mostly +X), so the ring is rotated 90 deg from the old X-Y plane and the runner passes THROUGH
# it instead of alongside it. Offsets are (0, y, z) = the old (x, y) rotated about the vertical.
particle minecraft:end_rod ~ ~1.2 ~1.3 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~1.96 ~1.05 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~2.44 ~0.4 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~2.44 ~-0.4 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~1.96 ~-1.05 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~1.2 ~-1.3 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~0.44 ~-1.05 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~-0.04 ~-0.4 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~-0.04 ~0.4 0 0 0 0 1 force @s
particle minecraft:end_rod ~ ~0.44 ~1.05 0 0 0 0 1 force @s

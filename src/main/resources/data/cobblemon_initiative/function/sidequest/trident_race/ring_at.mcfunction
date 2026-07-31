# Trident Tide-Race — draw one hoop at the positioned ring center. Run positioned at the ring
# coord (see rings.mcfunction) as/at the runner so `force @s` targets them. 10 end_rod points on
# a radius-1.3 circle in the vertical Y-Z plane (X fixed) — the course runs east<->west, so the
# hoop faces the swim direction and the runner riptides THROUGH it. end_rod reads clearly underwater.
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

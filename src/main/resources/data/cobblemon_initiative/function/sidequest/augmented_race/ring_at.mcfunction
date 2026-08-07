# Augmented Ring Run — draw one hoop at the positioned ring center. Run positioned at the ring coord
# (see rings.mcfunction) as/at the runner so `force @s` targets them. The course threads the towers in
# every direction (up/down/N-S/E-W), so a single-plane hoop would face wrong at some rings — this draws
# a DOUBLE ring (10 points in the Y-Z plane + 10 in the X-Y plane) so the target reads clearly from any
# approach. end_rod is bright and carries through the city haze.
# --- Y-Z plane (X fixed) ---
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
# --- X-Y plane (Z fixed) ---
particle minecraft:end_rod ~1.3 ~1.2 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~1.05 ~1.96 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~0.4 ~2.44 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~-0.4 ~2.44 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~-1.05 ~1.96 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~-1.3 ~1.2 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~-1.05 ~0.44 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~-0.4 ~-0.04 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~0.4 ~-0.04 ~ 0 0 0 0 1 force @s
particle minecraft:end_rod ~1.05 ~0.44 ~ 0 0 0 0 1 force @s

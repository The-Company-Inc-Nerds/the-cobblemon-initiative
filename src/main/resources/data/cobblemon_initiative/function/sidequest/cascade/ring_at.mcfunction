# Cascade Ascent — draw one vertical hoop at the positioned checkpoint center. Run positioned
# at the checkpoint coord (see rings.mcfunction) as/at the runner so `force @s` targets them.
# 10 end_rod points on a radius-1.3 circle in the vertical X-Y plane (Z fixed), raised ~1.2 so
# the runner walks through it: the "ring to go through" marker for the current target.
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

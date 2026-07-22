# Cascade Ascent — vertical "ring to go through" marker at the CURRENT target checkpoint only.
# Run as/at the ascending runner (called from tick_run). Each block is gated on the runner's
# ci_cascade_cp so exactly one hoop is lit: cp 0 -> CP1's ring, cp 1 -> CP2 ... cp 3 -> CP4.
# The hoop is 10 end_rod points on a radius-1.3 circle in the vertical X-Y plane, centered on
# the checkpoint's F3 coord (absolute positions; the runner's position only gates visibility).
# CP1 1935/106/2419 (target while cp==0)
execute if score @s ci_cascade_cp matches 0 positioned 1935 106 2419 run function cobblemon_initiative:sidequest/cascade/ring_at
# CP2 1942/109/2417 (target while cp==1)
execute if score @s ci_cascade_cp matches 1 positioned 1942 109 2417 run function cobblemon_initiative:sidequest/cascade/ring_at
# CP3 1957/122/2421 (target while cp==2)
execute if score @s ci_cascade_cp matches 2 positioned 1957 122 2421 run function cobblemon_initiative:sidequest/cascade/ring_at
# CP4 1974/135/2429 (target while cp==3)
execute if score @s ci_cascade_cp matches 3 positioned 1974 135 2429 run function cobblemon_initiative:sidequest/cascade/ring_at

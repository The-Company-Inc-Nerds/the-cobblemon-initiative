# Trident Tide-Race — the "ring to riptide through" marker at the CURRENT target ring only.
# Run as/at the racing runner (called from tick_run). Gated on ci_trace_cp so exactly one hoop
# is lit: cp 0 -> Ring 1, cp 1 -> Ring 2 ... cp 4 -> Ring 5. Coords match tick_run's boxes.
execute if score @s ci_trace_cp matches 0 positioned 455 62 3598 run function cobblemon_initiative:sidequest/trident_race/ring_at
execute if score @s ci_trace_cp matches 1 positioned 440 62 3592 run function cobblemon_initiative:sidequest/trident_race/ring_at
execute if score @s ci_trace_cp matches 2 positioned 425 62 3588 run function cobblemon_initiative:sidequest/trident_race/ring_at
execute if score @s ci_trace_cp matches 3 positioned 412 62 3595 run function cobblemon_initiative:sidequest/trident_race/ring_at
execute if score @s ci_trace_cp matches 4 positioned 424 62 3606 run function cobblemon_initiative:sidequest/trident_race/ring_at

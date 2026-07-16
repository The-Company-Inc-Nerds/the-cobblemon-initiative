# Verified Weather - watch the marsh exchange board flicker. Run as the player. Increments the
# read counter; each read prints a flicker line; the third read triggers the (short) witness fee.
scoreboard players add @s ci_mm_reads 1
execute if score @s ci_mm_reads matches 1 run title @s actionbar [{"text":"The board blinks: ","color":"gray"},{"text":"41 ... 38 ... 41 CD to the star. Verified.","color":"aqua"}]
execute if score @s ci_mm_reads matches 2 run title @s actionbar [{"text":"The board blinks: ","color":"gray"},{"text":"41 ... 36 ... it settles at 39. A healthy adjustment.","color":"aqua"}]
execute if score @s ci_mm_reads matches 3.. unless entity @s[tag=mm_board_done] run function cobblemon_initiative:sidequest/exchange_board/witness_pay

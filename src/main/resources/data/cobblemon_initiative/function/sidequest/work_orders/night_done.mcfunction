# NIGHT SHIFT quota met. Run as the player by check_kills.
tag @s remove work_night_active
tag @s add work_night_done
title @s actionbar [{"text":"NIGHT SHIFT: 8 of 8 verified","color":"yellow"}]
playsound minecraft:entity.experience_orb.pickup master @s ~ ~ ~ 1 1
tellraw @s [{"text":"Quota met. ","color":"green"},{"text":"Report to Forewoman Tetsu at the waystation for the Night Shift payout.","color":"gray"}]

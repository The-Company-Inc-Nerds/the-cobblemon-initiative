# One-time effect of liberating field $(field). Run as @s = the player. Guarded by free_field.
# Latch this field so it can never be re-liberated (world data — irreversible, relog-safe).
$scoreboard players set $(field) field_freed 1
# Feed the SHARED counter the wheat-trader poller reads: 0-1 trade -> 2-3 suspicious -> 4+ ambush.
scoreboard players add @s fields_liberated 1
# Push CobbleDollar instability back DOWN (gyms add +8 each; a field claws back 6). Floor at 0.
# TUNABLE tug-of-war knobs — magnitude (6) and floor (0): revisit alongside the field count (TODO P4).
scoreboard players remove #idx cd_instability 6
execute if score #idx cd_instability matches ..-1 run scoreboard players set #idx cd_instability 0
execute store result storage cobblemon_initiative:economy idx int 1 run scoreboard players get #idx cd_instability
# Player-facing beat (wheat-gold), in the founder-reclaiming-their-own-system register.
title @s actionbar {"text":"◆ Field liberated — the wheat-backed currency loses ground.","color":"#C9A227"}

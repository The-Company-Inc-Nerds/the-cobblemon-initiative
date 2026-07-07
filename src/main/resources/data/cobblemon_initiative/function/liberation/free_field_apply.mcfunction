# One-time effect of liberating field $(field). Run as @s = the player. Guarded by free_field.
# Latch this field so it can never be re-liberated (world data — irreversible, relog-safe).
$scoreboard players set $(field) field_freed 1
# Feed the SHARED counter the wheat-trader poller reads: 0-1 trade -> 2-3 suspicious -> 4+ ambush.
scoreboard players add @s fields_liberated 1
# The first liberation makes the Wheat War personal — light the HUD objective line
# (quest/render shows "Liberate the occupied fields n/6" while the player has this tag).
tag @s add wheat_war_active
# Push CobbleDollar instability back DOWN (gyms add +8 each; a field claws back 6). Floor at 0.
# TUNABLE tug-of-war knobs — magnitude (6) and floor (0): revisit alongside the field count (TODO P4).
scoreboard players remove #idx cd_instability 6
execute if score #idx cd_instability matches ..-1 run scoreboard players set #idx cd_instability 0
execute store result storage cobblemon_initiative:economy idx int 1 run scoreboard players get #idx cd_instability
# Re-apply the active shop tier so the liberation-relief level kicks in immediately
# (ShopTierManager resolves <base>_relief<r> from fields_liberated; granary follows in lockstep).
cobblemon-initiative shop refresh
# Player-facing beat (wheat-gold), in the founder-reclaiming-their-own-system register.
title @s actionbar {"text":"◆ Field liberated — the commodity currency loses ground.","color":"#C9A227"}
# CEREMONY: the n/6 counter is the villain arc's scoreboard — give each step its screen.
# Resolve the field's display name (map seeded in liberation/load; safe fallback first,
# since a missing names.<field> would leave the previous run's name in storage), stage
# the liberation count, then hand off to the title/fireworks macro.
data modify storage cobblemon_initiative:liberation display set value "THE PARCEL"
$data modify storage cobblemon_initiative:liberation display set from storage cobblemon_initiative:liberation names.$(field)
execute store result storage cobblemon_initiative:liberation n int 1 run scoreboard players get @s fields_liberated
function cobblemon_initiative:liberation/ceremony with storage cobblemon_initiative:liberation

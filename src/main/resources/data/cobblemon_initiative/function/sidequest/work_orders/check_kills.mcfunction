# NIGHT SHIFT kill poller. NEEDS tick registration (reported via tickFunctions; no tag
# JSON edited here). Cheap: selectors are tag-guarded, so it is a no-op unless a shift
# is active. Sums the three criteria objectives and latches work_night_done at >= 8.
execute as @a[tag=work_night_active] run scoreboard players operation @s ci_kill_total = @s ci_kill_zombie
execute as @a[tag=work_night_active] run scoreboard players operation @s ci_kill_total += @s ci_kill_skeleton
execute as @a[tag=work_night_active] run scoreboard players operation @s ci_kill_total += @s ci_kill_spider
execute as @a[tag=work_night_active,scores={ci_kill_total=8..}] run function cobblemon_initiative:sidequest/work_orders/night_done

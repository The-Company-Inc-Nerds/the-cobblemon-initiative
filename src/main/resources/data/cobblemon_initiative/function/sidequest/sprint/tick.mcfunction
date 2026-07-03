# Quarterly Sprint — per-tick driver. NEEDS tick registration (reported via
# tickFunctions; no tag JSON edited here). Two jobs:
#
# 1) DAY LATCH: reset the daily-rematch claim at dawn. time query day increments at each
#    new Minecraft day; when it moves past the stored value, clear race_daily_claimed.
execute store result score #day ci_sprint_day run time query day
execute unless score #day ci_sprint_day = #last_day ci_sprint_day run tag @a remove race_daily_claimed
scoreboard players operation #last_day ci_sprint_day = #day ci_sprint_day
#
# 2) RUNNER COUNTDOWN: tag-guarded, so a no-op except mid-run (single-player: one runner).
execute as @a[tag=ci_sprinting] at @s run function cobblemon_initiative:sidequest/sprint/run_tick

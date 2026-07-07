# Cascade Ascent — per-tick guard. Needs the #minecraft:tick tag (listed in the agent
# report; do NOT edit function tags directly per workflow rules).
# Cheap when idle: one selector check, nothing else runs unless a climb is live.
# DAY LATCH (round-13: gold-time paid 300 CD unbounded — the sprint's own memo bans
# exactly this): reset the daily gold claim at dawn, sprint/tick pattern verbatim.
execute store result score #day ci_cascade_day run time query day
execute unless score #day ci_cascade_day = #last_day ci_cascade_day run tag @a remove cascade_gold_claimed
scoreboard players operation #last_day ci_cascade_day = #day ci_cascade_day
execute if entity @a[tag=ci_ascending] run function cobblemon_initiative:sidequest/cascade/tick_run

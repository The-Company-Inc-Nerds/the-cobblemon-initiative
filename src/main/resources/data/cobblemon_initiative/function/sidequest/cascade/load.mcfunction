# Cascade Ascent — timer objective + bossbar setup. Needs the #minecraft:load tag
# (listed in the agent report; do NOT edit function tags directly per workflow rules).
# Idempotent: #init guards the one-time bossbar add; style/color re-assert on every load.
scoreboard objectives add ci_cascade dummy
scoreboard players set #twenty ci_cascade 20
execute unless score #init ci_cascade matches 1 run bossbar add cobblemon_initiative:cascade [{"text":"The Cascade Ascent","color":"aqua"}]
scoreboard players set #init ci_cascade 1
bossbar set cobblemon_initiative:cascade color blue
bossbar set cobblemon_initiative:cascade style notched_10
bossbar set cobblemon_initiative:cascade visible false
# A relog mid-run abandons the attempt cleanly (tags persist; timer state resets).
tag @a remove ci_ascending
scoreboard objectives add ci_cascade_day dummy
# Per-player ordered-checkpoint counter (0 = none passed, advances 1..4 through the 5 rings).
scoreboard objectives add ci_cascade_cp dummy

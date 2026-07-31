# Trident Tide-Race — timer objective + bossbar setup. Needs #minecraft:load (added to load.json).
# Cloned from sidequest/cascade (the proven ordered-ring timed race), relocated to the ocean off
# Gianna's Westwind beach with a Riptide trident grant. Idempotent: #init guards the one-time add.
scoreboard objectives add ci_trace dummy
scoreboard players set #twenty ci_trace 20
execute unless score #init ci_trace matches 1 run bossbar add cobblemon_initiative:trident_race [{"text":"The Tide-Ring Race","color":"aqua"}]
scoreboard players set #init ci_trace 1
bossbar set cobblemon_initiative:trident_race color blue
bossbar set cobblemon_initiative:trident_race style notched_10
bossbar set cobblemon_initiative:trident_race visible false
# A relog mid-run abandons the attempt cleanly (tags persist; timer state resets).
tag @a remove ci_trident_racing
# Per-player ordered-ring counter (0 = none passed, advances 1..5 through the 5 rings).
scoreboard objectives add ci_trace_cp dummy

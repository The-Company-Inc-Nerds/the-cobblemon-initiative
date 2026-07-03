# Cascade Ascent — SHOWRUNNER ONE-COMMAND SETUP. Stand at the crest finish spot and run:
#   /function cobblemon_initiative:sidequest/cascade/set_crest
# Places (or moves) the invisible finish marker the tick function distance-checks (4 blocks).
# Re-run anywhere to relocate the finish line.
kill @e[type=minecraft:marker,tag=ci_cascade_crest]
summon minecraft:marker ~ ~ ~ {Tags:["ci_cascade_crest"]}
tellraw @s [{"text":"Cascade Ascent crest marker set at your position. Finish radius: 4 blocks.","color":"green"}]

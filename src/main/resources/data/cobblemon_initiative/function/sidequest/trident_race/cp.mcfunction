# Trident Tide-Race — an ocean ring was cleared. Run AS the runner with {n:<1..15>,total:15}.
# Advances the ordered counter, chimes, and actionbars progress. FINISH is handled in tick_run
# once ci_trace_cp reaches 15.
$scoreboard players set @s ci_trace_cp $(n)
$title @s actionbar [{"text":"Ring $(n)","color":"aqua","bold":true},{"text":" / $(total) - ride the tide","color":"gray"}]
execute at @s run playsound minecraft:block.note_block.bell player @s ~ ~ ~ 1 1.5
execute at @s run particle minecraft:bubble ~ ~1 ~ 0.3 0.6 0.3 0.05 24 force @s

# Cascade Ascent — a checkpoint ring was cleared. Run AS the runner with {n:<1..4>,total:5}.
# Advances the ordered counter, chimes, and actionbars the progress. The FINISH box is
# handled in tick_run once ci_cascade_cp reaches 4.
$scoreboard players set @s ci_cascade_cp $(n)
$title @s actionbar [{"text":"Checkpoint $(n)","color":"aqua","bold":true},{"text":" / $(total) - keep climbing","color":"gray"}]
execute at @s run playsound minecraft:block.note_block.bell player @s ~ ~ ~ 1 1.5
execute at @s run particle minecraft:end_rod ~ ~1 ~ 0.3 0.6 0.3 0.02 24 force @s

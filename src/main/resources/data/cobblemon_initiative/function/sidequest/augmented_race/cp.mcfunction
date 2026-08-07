# Augmented Ring Run — a tower ring was cleared. Run AS the runner with {n:<1..37>,total:37}.
# Advances the ordered counter, chimes, and actionbars progress. FINISH is handled in tick_run
# once ci_augrace_cp reaches 37.
$scoreboard players set @s ci_augrace_cp $(n)
$title @s actionbar [{"text":"Ring $(n)","color":"light_purple","bold":true},{"text":" / $(total) - keep the arc","color":"gray"}]
execute at @s run playsound minecraft:block.note_block.bit player @s ~ ~ ~ 1 1.5
execute at @s run particle minecraft:electric_spark ~ ~1 ~ 0.3 0.6 0.3 0.05 24 force @s

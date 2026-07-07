# A Company Surveyor caught the player holding pulled Revision Notices — audit reset.
# Never damages: she just adjusts her clipboard. Visible strings carry no quotes or
# apostrophes (macro/SNBT safety). Zeroes the count and strips the per-board latches so
# every notice can be pulled again.
title @s actionbar [{"text":"LOGGED. ","color":"red","bold":true},{"text":"Audit trail reset — all three notices go back on the wall.","color":"gray"}]
playsound minecraft:block.note_block.bass player @s ~ ~ ~ 1 0.7
scoreboard players set @s ci_notices 0
tag @s remove notice_1
tag @s remove notice_2
tag @s remove notice_3

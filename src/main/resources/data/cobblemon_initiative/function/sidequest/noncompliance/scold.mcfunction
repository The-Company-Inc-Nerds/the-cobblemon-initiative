# Notice of Non-Compliance — the canvasser caught the player mid-paste. Nobody gets hurt
# over paper; the paste is voided (retry) and the clean run is marked failed. Visible
# strings carry no quotes or apostrophes. Run as the player.
tag @s add sq_poster_scolded
title @s actionbar [{"text":"CAUGHT MID-PASTE. ","color":"red","bold":true},{"text":"This is defacement of verified materials. Please cease. The paste is voided — time it cleaner.","color":"gray"}]
playsound minecraft:block.note_block.bass player @s ~ ~ ~ 1 0.7

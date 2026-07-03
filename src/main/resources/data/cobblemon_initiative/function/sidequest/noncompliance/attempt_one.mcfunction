# Notice of Non-Compliance — paste moth print 1 (gym entrance) over a Company notice.
# Called by the sq_ume_notices post_one button (run as the player). Caught if the canvasser
# has eyes on a player right now (NPC Sight scoreboard IPC on the ci_canvasser entity);
# a caught paste is voided and marks the clean run failed. Unseen: the print goes up.
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run function cobblemon_initiative:sidequest/noncompliance/scold
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run return fail
tag @s add sq_poster_one
title @s actionbar [{"text":"Print one is up — gym entrance. ","color":"green"},{"text":"He never turned.","color":"gray"}]

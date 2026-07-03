# The Incomplete File stage 2 — pull Revision Notice 2 (shore-warehouse door). Called by
# the notice_post_2 prop button (run as the player). Same sight rule as notice 1.
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run return fail
tag @s add notice_2
scoreboard players add @s ci_notices 1
title @s actionbar [{"text":"Notice pulled — warehouse door. ","color":"green"},{"text":"Intact. The corners have not curled.","color":"gray"}]

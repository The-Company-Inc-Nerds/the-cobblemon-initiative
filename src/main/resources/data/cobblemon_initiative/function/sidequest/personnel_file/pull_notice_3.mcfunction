# The Incomplete File stage 2 — pull Revision Notice 3 (town-hall board). Called by the
# notice_post_3 prop button (run as the player). Same sight rule as notice 1.
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run return fail
tag @s add notice_3
scoreboard players add @s ci_notices 1
title @s actionbar [{"text":"Notice pulled — town-hall board. ","color":"green"},{"text":"Three of three. Now walk, do not run.","color":"gray"}]

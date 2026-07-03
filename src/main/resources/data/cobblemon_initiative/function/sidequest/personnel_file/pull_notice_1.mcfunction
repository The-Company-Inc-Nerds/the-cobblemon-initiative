# The Incomplete File stage 2 — pull Revision Notice 1 (gym-entrance wall). Called by the
# notice_post_1 prop button (run as the player). Caught if a surveyor has eyes on you as
# your hand closes on the paper: audit resets and nothing is granted (free retry). Unseen:
# the notice comes free and the counter ticks up.
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run function cobblemon_initiative:sidequest/personnel_file/notice_logged
execute at @s if entity @e[tag=surveyor,scores={can_see_player=1..},distance=..24] run return fail
tag @s add notice_1
scoreboard players add @s ci_notices 1
title @s actionbar [{"text":"Notice pulled — gym entrance. ","color":"green"},{"text":"Intact. The paste is still wet.","color":"gray"}]

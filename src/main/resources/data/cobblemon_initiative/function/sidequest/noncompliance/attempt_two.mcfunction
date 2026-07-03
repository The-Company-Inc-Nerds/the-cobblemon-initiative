# Notice of Non-Compliance — paste moth print 2 (falls overlook). Called by the
# sq_ume_notices post_two button (run as the player). Same sight rule as print 1.
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run function cobblemon_initiative:sidequest/noncompliance/scold
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run return fail
tag @s add sq_poster_two
title @s actionbar [{"text":"Print two is up — falls overlook. ","color":"green"},{"text":"The mist covered you.","color":"gray"}]

# Notice of Non-Compliance — paste moth print 3 (the bridge mural). Called by the
# sq_ume_notices post_three button (run as the player). Same sight rule as print 1. This
# board comes down off an old mural whose central figure was painted out years ago.
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run function cobblemon_initiative:sidequest/noncompliance/scold
execute if entity @e[tag=ci_canvasser,scores={can_see_player=1..}] run return fail
tag @s add sq_poster_three
title @s actionbar [{"text":"Print three is up — the bridge mural. ","color":"green"},{"text":"Under the old paste, a face someone painted out.","color":"gray"}]

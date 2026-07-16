# The Tide Market - accept Odessa's fetch. Run as the player from her start button.
# Latches odessa_crate_started (the accept latch; completion state is odessa_crate_recovered).
# Lights the q.side_odessa HUD line via quest/refresh and points the waypoint at the
# customs-float prop coord (register stage 1).
tag @s add odessa_crate_started
title @s title [{"text":"THE TIDE MARKET","color":"gold","bold":true}]
title @s subtitle [{"text":"Customs float, far slip. The crate bobs where they left it as a warning.","color":"gray"}]
tellraw @s [{"text":"THE TIDE MARKET - ","color":"gold","bold":true},{"text":"customs floated a seized crate off the far slip. Recover it and bring it back under the boardwalk to open Odessa slip.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

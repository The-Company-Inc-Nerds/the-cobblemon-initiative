# EXCHANGE RATE (Cyber City DATA quest) - accept. Run as the player from the teller accept button.
# ci_reserves_active is the permanent accepted-latch (never removed; completion state is
# ci_reserves_done). Lights the HUD side line via quest/refresh.
tag @s add ci_reserves_active
title @s title [{"text":"THE EXCHANGE RATE","color":"gold","bold":true}]
title @s subtitle [{"text":"Re-verify three reserve tags posted downtown.","color":"gray"}]
tellraw @s [{"text":"EXCHANGE RATE - ","color":"gold","bold":true},{"text":"re-verify three nether-star reserve placards scattered downtown, then report the count to the teller at the kiosk.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

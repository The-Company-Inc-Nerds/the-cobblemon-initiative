# SIGNAL INTEGRITY (Cyber City SURVEILLANCE quest) - accept. Run as the player from Rell accept.
# ci_signal_active is the permanent accepted-latch (never removed; completion is ci_signal_done).
# Lights the HUD side line via quest/refresh.
tag @s add ci_signal_active
title @s title [{"text":"SIGNAL INTEGRITY","color":"gold","bold":true}]
title @s subtitle [{"text":"Scrub three glitching billboards downtown.","color":"gray"}]
tellraw @s [{"text":"SIGNAL INTEGRITY - ","color":"gold","bold":true},{"text":"three downtown boards are leaking scrubbed memos in the clear. Scrub each corrupted feed, then report to Rell at the comms van.","color":"gray"}]
playsound minecraft:block.beacon.deactivate master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

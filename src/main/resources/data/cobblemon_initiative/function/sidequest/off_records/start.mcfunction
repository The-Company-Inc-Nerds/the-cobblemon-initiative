# OFF THE RECORDS (Cyber City WHISTLEBLOWER quest) - accept. Run as the player from Maren accept.
# ci_file_active is the permanent accepted-latch (never removed; completion is ci_file_done).
# Delivers the frag_7 town-echo in the founder-voice register (clean text). Lights the HUD line.
tag @s add ci_file_active
title @s title [{"text":"OFF THE RECORDS","color":"gold","bold":true}]
title @s subtitle [{"text":"You signed this charter.","color":"dark_red"}]
tellraw @s [{"text":"OFF THE RECORDS - ","color":"gold","bold":true},{"text":"recover three file pages from the archive drops in the records annex before the furnace takes them, then bring the count to Maren.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 0.9
function cobblemon_initiative:quest/refresh

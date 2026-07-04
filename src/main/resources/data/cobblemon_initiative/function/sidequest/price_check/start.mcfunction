# ADJUSTED RETAIL (The Price Check) — accept. Run as the player from Kaito's accept button.
# hz_price_check_active is the permanent accepted-latch (never removed; completion state is
# hz_prices_done). Lights the HUD side line via quest/refresh.
tag @s add hz_price_check_active
title @s title [{"text":"THE PRICE CHECK","color":"gold","bold":true}]
title @s subtitle [{"text":"Three stalls. Three tickets. Write down what the index did.","color":"gray"}]
tellraw @s [{"text":"PRICE CHECK — ","color":"gold","bold":true},{"text":"note the ticket at three stalls: Linh Hua (produce, east market), Wei Shun (tools), Mei Lin (southwest lane). Bring the numbers back to Kaito at the mart.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

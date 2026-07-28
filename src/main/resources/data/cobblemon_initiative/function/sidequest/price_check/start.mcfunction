# ADJUSTED RETAIL (The Price Check) — accept. Run as the player from Wei Feng's accept
# button (alpha.25: the martkeeper is Wei Feng at the Pokemart counter; stop 3 is Auntie
# Song's berry stand). hz_price_check_active is the permanent accepted-latch (never
# removed; completion state is hz_prices_done). Lights the HUD side line via quest/refresh.
tag @s add hz_price_check_active
title @s title [{"text":"THE PRICE CHECK","color":"gold","bold":true}]
title @s subtitle [{"text":"Three stalls. Three tickets. Write down what the index did.","color":"gray"}]
tellraw @s [{"text":"PRICE CHECK — ","color":"gold","bold":true},{"text":"note the ticket at three stalls: Linh Hua (produce, east market), Wei Shun (tools), Auntie Song (the berry stand). Bring the numbers back to Wei Feng at the Pokemart.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

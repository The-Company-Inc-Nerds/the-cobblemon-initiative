# Price-check stop 2 — Wei Shun, tool stall. Run as the player from the note button.
execute unless entity @s[tag=hz_price_2] run tellraw @s [{"text":"Noted ","color":"gold"},{"text":"(2/3)","color":"yellow"},{"text":": the tool board — the ticket moved, and the rep quotes it in wheat equivalent, unprompted.","color":"gray"}]
execute unless entity @s[tag=hz_price_2] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add hz_price_2
function cobblemon_initiative:quest/refresh

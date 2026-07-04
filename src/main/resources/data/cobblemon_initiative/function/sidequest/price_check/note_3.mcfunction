# Price-check stop 3 — Mei Lin, the unverified stall (the counter-beat). Run as the player.
execute unless entity @s[tag=hz_price_3] run tellraw @s [{"text":"Noted ","color":"gold"},{"text":"(3/3)","color":"yellow"},{"text":": the southwest lane — prices did NOT move. Nobody verifies her.","color":"gray"}]
execute unless entity @s[tag=hz_price_3] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add hz_price_3
function cobblemon_initiative:quest/refresh

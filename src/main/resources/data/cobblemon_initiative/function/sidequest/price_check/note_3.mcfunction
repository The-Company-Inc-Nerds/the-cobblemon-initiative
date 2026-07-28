# Price-check stop 3 — Auntie Song, the berry stand (the counter-beat; alpha.25 moved
# this stop off the retired stall-Mei cast onto Song, whose flat undercut IS the beat).
# Run as the player.
execute unless entity @s[tag=hz_price_3] run tellraw @s [{"text":"Noted ","color":"gold"},{"text":"(3/3)","color":"yellow"},{"text":": the berry stand — prices did NOT move. Nobody verifies Auntie Song.","color":"gray"}]
execute unless entity @s[tag=hz_price_3] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add hz_price_3
function cobblemon_initiative:quest/refresh

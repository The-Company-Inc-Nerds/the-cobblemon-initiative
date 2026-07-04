# Price-check stop 1 — Linh Hua, produce stall. Run as the player from the note button.
# Idempotent: the tellraw only fires on the first note; the tag add re-asserts harmlessly.
execute unless entity @s[tag=hz_price_1] run tellraw @s [{"text":"Noted ","color":"gold"},{"text":"(1/3)","color":"yellow"},{"text":": the produce crate — the ticket moved, the scale is Company calibrated.","color":"gray"}]
execute unless entity @s[tag=hz_price_1] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add hz_price_1
function cobblemon_initiative:quest/refresh

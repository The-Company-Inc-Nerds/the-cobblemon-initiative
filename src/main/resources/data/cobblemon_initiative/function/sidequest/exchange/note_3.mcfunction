# EXCHANGE RATE - reserve tag 3 of 3. Run as the player from the prop verify button.
# Idempotent: the tellraw + sound only fire on the first re-verify; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_reserve_3] run tellraw @s [{"text":"Re-verified ","color":"gold"},{"text":"(3/3)","color":"yellow"},{"text":": the last placard, re-signed like the rest. Three tags, three cover stories. Take the count to the teller.","color":"gray"}]
execute unless entity @s[tag=ci_reserve_3] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add ci_reserve_3
function cobblemon_initiative:quest/refresh

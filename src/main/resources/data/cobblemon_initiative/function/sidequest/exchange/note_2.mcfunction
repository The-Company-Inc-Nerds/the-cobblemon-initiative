# EXCHANGE RATE - reserve tag 2 of 3. Run as the player from the prop verify button.
# Idempotent: the tellraw + sound only fire on the first re-verify; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_reserve_2] run tellraw @s [{"text":"Re-verified ","color":"gold"},{"text":"(2/3)","color":"yellow"},{"text":": another name sanded off, another stamp laid fresh over it. Same short reserve.","color":"gray"}]
execute unless entity @s[tag=ci_reserve_2] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add ci_reserve_2
function cobblemon_initiative:quest/refresh

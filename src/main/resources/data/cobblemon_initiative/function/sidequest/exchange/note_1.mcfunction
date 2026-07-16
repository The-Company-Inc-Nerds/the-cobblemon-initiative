# EXCHANGE RATE - reserve tag 1 of 3. Run as the player from the prop verify button.
# Idempotent: the tellraw + sound only fire on the first re-verify; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_reserve_1] run tellraw @s [{"text":"Re-verified ","color":"gold"},{"text":"(1/3)","color":"yellow"},{"text":": the signature is fresh ink over a sanded-off older one. The reserve reads short.","color":"gray"}]
execute unless entity @s[tag=ci_reserve_1] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add ci_reserve_1
function cobblemon_initiative:quest/refresh

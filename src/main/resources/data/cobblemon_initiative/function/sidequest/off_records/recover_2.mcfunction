# OFF THE RECORDS - archive drop 2 of 3. Run as the player from the prop recover button.
# Idempotent: the actionbar + sound only fire on the first recovery; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_page_2] run title @s actionbar [{"text":"Page recovered ","color":"gold"},{"text":"(2/3)","color":"yellow"},{"text":": another page bound for the furnace, another signature you almost recognize.","color":"gray"}]
execute unless entity @s[tag=ci_page_2] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
tag @s add ci_page_2
function cobblemon_initiative:quest/refresh

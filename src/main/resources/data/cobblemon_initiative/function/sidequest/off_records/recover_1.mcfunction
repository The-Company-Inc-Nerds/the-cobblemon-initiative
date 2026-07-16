# OFF THE RECORDS - archive drop 1 of 3. Run as the player from the prop recover button.
# Idempotent: the actionbar + sound only fire on the first recovery; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_page_1] run title @s actionbar [{"text":"Page recovered ","color":"gold"},{"text":"(1/3)","color":"yellow"},{"text":": the name column is razored out, but the photo is still clipped to the corner.","color":"gray"}]
execute unless entity @s[tag=ci_page_1] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
tag @s add ci_page_1
function cobblemon_initiative:quest/refresh

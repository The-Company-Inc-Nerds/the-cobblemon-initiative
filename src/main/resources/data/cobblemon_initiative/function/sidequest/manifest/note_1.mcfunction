# Manifest audit stop 1 - the main-pier freight stack (Tally Clerk Pell). Run as the player
# from the note button. Idempotent: the tellraw only fires on the first log; the tag add
# re-asserts harmlessly. quest/refresh recomputes #manifests and the sidebar.
execute unless entity @s[tag=gaviota_manifest_1] run tellraw @s [{"text":"Logged ","color":"gold"},{"text":"(1/3)","color":"yellow"},{"text":": forty claimed, thirty-six aboard. Four barrels that sailed without a boat.","color":"gray"}]
execute unless entity @s[tag=gaviota_manifest_1] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add gaviota_manifest_1
function cobblemon_initiative:quest/refresh

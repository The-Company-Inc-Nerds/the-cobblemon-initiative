# Manifest audit stop 2 - the wharf crates (Tally Clerk Odile). Run as the player from the
# note button. Idempotent: tellraw once, tag re-asserts. quest/refresh recomputes the count.
execute unless entity @s[tag=gaviota_manifest_2] run tellraw @s [{"text":"Logged ","color":"gold"},{"text":"(2/3)","color":"yellow"},{"text":": tariff booked at nine, paid at twelve. Three parts into a schedule nobody signed.","color":"gray"}]
execute unless entity @s[tag=gaviota_manifest_2] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add gaviota_manifest_2
function cobblemon_initiative:quest/refresh

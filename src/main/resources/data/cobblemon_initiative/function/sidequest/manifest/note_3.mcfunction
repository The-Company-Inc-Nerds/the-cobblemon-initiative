# Manifest audit stop 3 - the deep-pier Dunewind load (Tally Clerk Bram). Run as the player
# from the note button. Idempotent: tellraw once, tag re-asserts. quest/refresh recomputes the
# count and, at 3/3, unlocks the turn-in back at Kaito. This is the desert-bound leg (forward hook).
execute unless entity @s[tag=gaviota_manifest_3] run tellraw @s [{"text":"Logged ","color":"gold"},{"text":"(3/3)","color":"yellow"},{"text":": two hundred claimed for Dunewind, one ninety-one aboard. Nine crates lost to the desert road.","color":"gray"}]
execute unless entity @s[tag=gaviota_manifest_3] run playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.2
tag @s add gaviota_manifest_3
function cobblemon_initiative:quest/refresh

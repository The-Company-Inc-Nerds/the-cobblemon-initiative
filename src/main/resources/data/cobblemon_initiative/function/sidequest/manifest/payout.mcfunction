# Adjusted Freight - the payout. Run as the player (via turn_in guard: #manifests 3.. and
# not yet filed). 400 CD union honorarium through the skewed payout rail - the haircut is
# thematically perfect here, the audit itself gets shorted on camera. Latches gaviota_manifests_filed
# so the quest ends and Kaito flips to his filed entry.
function cobblemon_initiative:economy/payout {amount:400}
tag @s add gaviota_manifests_filed
title @s title [{"text":"ADJUSTMENT LOGGED","color":"gold","bold":true}]
title @s subtitle [{"text":"Rounding, in the Company favour - now on the record.","color":"gray"}]
tellraw @s [{"text":"Kaito signs the union sheet and slides it into the shack ledger.","color":"gray","italic":true}]
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.6 1.2
function cobblemon_initiative:quest/refresh

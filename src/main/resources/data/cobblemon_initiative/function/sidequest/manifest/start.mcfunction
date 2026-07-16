# Adjusted Freight (The Manifest Audit) - accept. Run as the player from Kaito's accept button.
# gaviota_manifest_check_active is the permanent accepted-latch (never removed; completion state
# is gaviota_manifests_filed). Lights the q.side_freight HUD line via quest/refresh.
tag @s add gaviota_manifest_check_active
title @s title [{"text":"ADJUSTED FREIGHT","color":"gold","bold":true}]
title @s subtitle [{"text":"Three wharf points. Read the paper, count the barrels.","color":"gray"}]
tellraw @s [{"text":"MANIFEST AUDIT - ","color":"gold","bold":true},{"text":"log the shortfall at three wharf points: the main-pier freight stack, the wharf crates, and the deep-pier Dunewind load. Bring all three counts back to Kaito at the shack.","color":"gray"}]
playsound minecraft:item.book.page_turn master @s ~ ~ ~ 0.8 1.0
function cobblemon_initiative:quest/refresh

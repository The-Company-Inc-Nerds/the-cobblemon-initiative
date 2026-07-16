# Adjusted Freight - turn-in, run as the player from Kaito's report button. The dialog button
# is ungated (dialog gates read @s, not the #manifests scratch holder), so this guards the
# count internally like price_check/payout: recompute #manifests from the three per-point tags,
# then pay ONLY at 3/3 and not-yet-filed. Under 3/3 it tells the player which points remain.
scoreboard players set #manifests quest_hud 0
execute if entity @s[tag=gaviota_manifest_1] run scoreboard players add #manifests quest_hud 1
execute if entity @s[tag=gaviota_manifest_2] run scoreboard players add #manifests quest_hud 1
execute if entity @s[tag=gaviota_manifest_3] run scoreboard players add #manifests quest_hud 1
execute if score #manifests quest_hud matches 3.. unless entity @s[tag=gaviota_manifests_filed] run function cobblemon_initiative:sidequest/manifest/payout
execute if score #manifests quest_hud matches ..2 run tellraw @s [{"text":"Kaito taps the sheet. ","color":"gray"},{"text":"Not all three yet. Log the shortfall at every wharf point - the main pier, the wharf crates, and the deep-pier desert load - then come back.","color":"yellow"}]

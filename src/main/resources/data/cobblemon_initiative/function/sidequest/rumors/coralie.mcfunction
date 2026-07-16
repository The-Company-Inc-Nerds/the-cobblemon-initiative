# Nurse Coralie's rumor board - Gaviota Port's arrival hub. Run as the player from her
# "What is the word on the docks" dialog button. Rolls one of five town rumors on the
# quest_hud scratch pad (#-holders per the sidebar contract), serves it only while its
# quest is still open, and falls back to a static page (with the Kalahar/Dunewind forward
# hook) when the rolled quest is already done. All read-only: rumors gate on the quests own
# done-latches, no new state. Spot 4 (Westwind) gates on the ambush-win tag defeated_gaviota_wheat_sea
# (field_freed is a scoreboard not a tag). Spot 5 is a back-echo (always eligible, no gate).
execute store result score #rumor quest_hud run random value 1..5
scoreboard players set #rumor_hit quest_hud 0

execute if score #rumor quest_hud matches 1 if entity @s[tag=!bosun_net_done] run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"Netmender Rui hung his nets to dry and will not fish. Bring the man deep-sea line - eight lengths - and he may remember the fleet owes you a lesson.","color":"gray"}]
execute if score #rumor quest_hud matches 1 if entity @s[tag=!bosun_net_done] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 2 if entity @s[tag=!odessa_crate_recovered] run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"There is a woman under the fish-market boards who sells what the tariff took. She wants a crate back off the customs float before she trusts you.","color":"gray"}]
execute if score #rumor quest_hud matches 2 if entity @s[tag=!odessa_crate_recovered] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 3 if entity @s[tag=!gaviota_manifests_filed] run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"Kaito on the main pier is short three manifests and shorter three pays. He is counting barrels the Company already counted for him.","color":"gray"}]
execute if score #rumor quest_hud matches 3 if entity @s[tag=!gaviota_manifests_filed] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 4 if entity @s[tag=!defeated_gaviota_wheat_sea] run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"They are barging grain out of Westwind Fields now, east along the coast. Wheat by sea. Somebody should cut the ropes.","color":"gray"}]
execute if score #rumor quest_hud matches 4 if entity @s[tag=!defeated_gaviota_wheat_sea] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 5 run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"The dock pays came up light again. Same as Deepcore, same story - the money is not the money it was. You feel it in the till before you feel it anywhere else.","color":"gray"}]
execute if score #rumor quest_hud matches 5 run scoreboard players set #rumor_hit quest_hud 1

# Fallback: the rolled rumor already resolved - name the standing work and the road east.
execute if score #rumor_hit quest_hud matches 0 run tellraw @s [{"text":"Coralie, quietly: ","color":"aqua"},{"text":"Slow tide for gossip. The freight talk is all one word - Dunewind. The desert road east to Kalahar Reach, and everything the port cannot sell here goes that way now.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9

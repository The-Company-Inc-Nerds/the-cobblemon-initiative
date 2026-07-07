# Nurse Lila's rumor board — Takehara's arrival hub. Run as the player from her
# "What is the word around town" dialog button. Rolls one of five town rumors on the
# quest_hud scratch pad (#-holders per the sidebar contract), serves it only while its
# quest is still open, and falls back to a static three-spot page when the rolled quest
# is already done. All read-only: rumors gate on the quests own done-latches, no new
# state. Spot list deliberately skips Performance Review (hidden meta — surfacing it
# would spoil the ghost run).
execute store result score #rumor quest_hud run random value 1..5
scoreboard players set #rumor_hit quest_hud 0

execute if score #rumor quest_hud matches 1 if entity @s[tag=!sq_cascade_done] run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"The warden at the falls keeps a record board — base to crest against the clock. Shou pretends the times do not matter to him. The chalk says otherwise.","color":"gray"}]
execute if score #rumor quest_hud matches 1 if entity @s[tag=!sq_cascade_done] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 2 if entity @s[tag=!sq_museum_donation_done] run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"Kenji at the museum pays finders fees for dig work below the falls, and Sayuri wants bones for the anatomy case. Honest money, and you come back smelling of river.","color":"gray"}]
execute if score #rumor quest_hud matches 2 if entity @s[tag=!sq_museum_donation_done] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 3 if entity @s[tag=!genji_rod_done] run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"Genji hung an out-of-office sign on his own boat. He is down at the water sulking about tackle — bring the man some line and he may remember he owes the town a favor.","color":"gray"}]
execute if score #rumor quest_hud matches 3 if entity @s[tag=!genji_rod_done] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 4 if entity @s[tag=!defeated_sq_mayor_suits] run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"Two grey suits took the gym stairs this morning. The mayor went up to meet them. He has not come down.","color":"gray"}]
execute if score #rumor quest_hud matches 4 if entity @s[tag=!defeated_sq_mayor_suits] run scoreboard players set #rumor_hit quest_hud 1

execute if score #rumor quest_hud matches 5 if entity @s[tag=!sting_reward_paid] run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"Tomo out by the Blossom arch has paperwork stapled to his hive trees now. Asset under valuation, it says. The bees have filed their own opinion.","color":"gray"}]
execute if score #rumor quest_hud matches 5 if entity @s[tag=!sting_reward_paid] run scoreboard players set #rumor_hit quest_hud 1

# Fallback: the rolled rumor already resolved — name three standing spots instead.
execute if score #rumor_hit quest_hud matches 0 run tellraw @s [{"text":"Lila, quietly: ","color":"aqua"},{"text":"Slow week for gossip. The usual three still stand — the record board at the falls, the museum dig, and whatever the printmaker is feuding with the canvasser about this time.","color":"gray"}]
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 0.9

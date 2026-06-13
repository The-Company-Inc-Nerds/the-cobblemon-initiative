# Quest tracker poller (registered in #minecraft:tick). Throttled to ~once/second.
# Only recomputes while the HUD is shown.
scoreboard players add #t quest_hud 1
execute if score #t quest_hud matches 20.. run scoreboard players set #t quest_hud 0
execute if score #t quest_hud matches 0 if score #hud quest_hud matches 1 run function cobblemon_initiative:quest/refresh

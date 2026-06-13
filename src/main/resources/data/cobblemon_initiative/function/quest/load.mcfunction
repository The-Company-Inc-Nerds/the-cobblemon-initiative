# Quest tracker — set up the objective boss bar + sidebar log. Registered in #minecraft:load.
# Idempotent + relog-safe: #init guards one-time defaults so a /reload respects the player's
# current show/hide choice instead of force-showing.
scoreboard objectives add ci_quest dummy [{"text":"⚜ THE INITIATIVE","color":"gold","bold":true}]
scoreboard objectives modify ci_quest numberformat blank
scoreboard objectives add quest_hud dummy
execute unless score #init quest_hud matches 1 run bossbar add cobblemon_initiative:objective [{"text":"Objective","color":"gold"}]
execute unless score #init quest_hud matches 1 run scoreboard players set #hud quest_hud 1
bossbar set cobblemon_initiative:objective color yellow
bossbar set cobblemon_initiative:objective style notched_10
scoreboard players set #init quest_hud 1
# Re-apply whatever the current visibility state is.
execute if score #hud quest_hud matches 1 run function cobblemon_initiative:quest/show
execute if score #hud quest_hud matches 0 run function cobblemon_initiative:quest/hide

# Quest tracker — set up the sidebar log. Registered in #minecraft:load.
# Idempotent + relog-safe: #init guards one-time defaults so a /reload respects the player's
# current show/hide choice instead of force-showing.
# The old top "Objective" boss bar was removed (showrunner call, 2026-07-04) — the sidebar's
# main line carries the story objective; `bossbar remove` clears it from existing worlds.
scoreboard objectives add ci_quest dummy [{"text":"⚜ THE INITIATIVE","color":"gold","bold":true}]
scoreboard objectives modify ci_quest numberformat blank
scoreboard objectives add quest_hud dummy
bossbar remove cobblemon_initiative:objective
execute unless score #init quest_hud matches 1 run scoreboard players set #hud quest_hud 1
scoreboard players set #init quest_hud 1
# Re-apply whatever the current visibility state is.
execute if score #hud quest_hud matches 1 run function cobblemon_initiative:quest/show
execute if score #hud quest_hud matches 0 run function cobblemon_initiative:quest/hide

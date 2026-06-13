# Show the quest HUD (boss bar + sidebar). Bound to /ca quest show.
scoreboard players set #hud quest_hud 1
scoreboard objectives setdisplay sidebar ci_quest
bossbar set cobblemon_initiative:objective players @a
bossbar set cobblemon_initiative:objective visible true
function cobblemon_initiative:quest/refresh

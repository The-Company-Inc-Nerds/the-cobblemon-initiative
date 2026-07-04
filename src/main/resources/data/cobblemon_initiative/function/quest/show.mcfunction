# Show the quest HUD (sidebar). Bound to /ca quest show.
scoreboard players set #hud quest_hud 1
scoreboard objectives setdisplay sidebar ci_quest
function cobblemon_initiative:quest/refresh

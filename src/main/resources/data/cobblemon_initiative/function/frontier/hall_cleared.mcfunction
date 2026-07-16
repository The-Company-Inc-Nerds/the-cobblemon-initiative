# Fired from every Frontier BRAIN on_win (execute as @1 = the player). Flavor stings only —
# the N/7 counter + per-hall waypoint retarget live in quest/render (q.side_frontier_hall).
# Each brain's win_line already name-drops its facility, so this stays generic.
title @s actionbar [{"text":"A Frontier hall falls.","color":"aqua"}]
# Count the seven facility brains cleared; the Deep Dark door opens on the seventh.
scoreboard players set #halls quest_hud 0
execute if entity @s[tag=frontier_tower_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_factory_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_castle_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_arcade_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_port_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_pyramid_cleared] run scoreboard players add #halls quest_hud 1
execute if entity @s[tag=frontier_market_cleared] run scoreboard players add #halls quest_hud 1
# The Warden stirs the moment the seventh bows (once — guarded by frontier_warden_stirred).
execute if score #halls quest_hud matches 7.. unless entity @s[tag=frontier_warden_stirred] run title @s title [{"text":"THE WARDEN STIRS","color":"dark_aqua"}]
execute if score #halls quest_hud matches 7.. unless entity @s[tag=frontier_warden_stirred] run title @s subtitle [{"text":"The Deep Dark door is open.","color":"gray"}]
execute if score #halls quest_hud matches 7.. run tag @s add frontier_warden_stirred

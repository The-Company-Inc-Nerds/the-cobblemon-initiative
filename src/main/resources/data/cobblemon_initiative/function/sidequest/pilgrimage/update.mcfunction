# Four Gardens Pilgrimage — seal counter refresh. Run AS the player right after a station
# plaque grants a seal tag (the plaque press buttons call this as_player). Counts the four
# seal tags, toasts progress on the actionbar via the set-style macro (mirrors
# quest/set_wheat), points home to Wei at 4/4, then re-renders the sidebar side line
# immediately so the HUD does not wait on the once-a-second quest poller.
# Seal-press feedback (statue round): a soft stone-and-crystal chime + a leaf-green
# shimmer at the plaque the moment the seal takes. Runs as+at the pressing player,
# which is standing at the plaque.
execute at @s run playsound minecraft:block.amethyst_block.chime player @s ~ ~ ~ 0.9 0.7
execute at @s run playsound minecraft:block.stone.place player @s ~ ~ ~ 0.8 0.6
execute at @s run particle minecraft:happy_villager ~ ~1.2 ~ 0.4 0.5 0.4 0.01 18 force @s
scoreboard players set #seals quest_hud 0
execute if entity @s[tag=seal_moss] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_orchard] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_terrace] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_pond] run scoreboard players add #seals quest_hud 1
execute store result storage cobblemon_initiative:quest seals int 1 run scoreboard players get #seals quest_hud
function cobblemon_initiative:sidequest/pilgrimage/toast with storage cobblemon_initiative:quest
execute if score #seals quest_hud matches 4 run tellraw @s [{"text":"Four seals. ","color":"green","bold":true},{"text":"Return to Garden Master Wei on the west hill for the blessing.","color":"gray"}]
function cobblemon_initiative:sidequest/pilgrimage/hud

# Four Gardens Pilgrimage — sidebar side objective (mirrors the #side_wheat block at the
# bottom of quest/render; the orchestrator appends the call there). Run as @s = the player.
# Shows the Garden seals n/4 line once the first seal is pressed; clears once Wei grants
# the blessing (pilgrimage_done latch). Also called directly from the plaque update and
# the blessing button for an instant refresh. #side_pilgrim rides ci_quest at 78 — just
# below the wheat-war line (80), above nothing else.
scoreboard players reset #side_pilgrim ci_quest
scoreboard players set #seals quest_hud 0
execute if entity @s[tag=seal_moss] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_orchard] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_terrace] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=seal_pond] run scoreboard players add #seals quest_hud 1
execute if entity @s[tag=pilgrimage_done] run return 0
execute if score #seals quest_hud matches ..0 run return 0
scoreboard players set #side_pilgrim ci_quest 78
execute store result storage cobblemon_initiative:quest seals int 1 run scoreboard players get #seals quest_hud
function cobblemon_initiative:sidequest/pilgrimage/set_line with storage cobblemon_initiative:quest

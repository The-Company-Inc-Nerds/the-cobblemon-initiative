# A Board member has fallen. Run as @s = the player (each board_* battle on_win calls
# `execute as @1 at @1 run function cobblemon_initiative:reveal/board_fell`).
#
# Recounts the defeated_board_* tags (same derivation quest/render uses — no new state)
# and fires an oblique narrative beat per emptied seat. The Founder's nameplate stays
# fully §k-obfuscated throughout — the name is only spoken at the mirror's defeat
# (reveal/founder_defeated), and it is the player's own. These beats circle it; they
# never close it (LORE_BIBLE 9).
scoreboard players set #board quest_hud 0
execute if entity @s[tag=defeated_board_madeline] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_matt] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_micah] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_lauren] run scoreboard players add #board quest_hud 1

execute if score #board quest_hud matches 1 run title @s title {"text":" "}
execute if score #board quest_hud matches 1 run title @s subtitle {"text":"A seat empties. Upstairs, something static-wrapped shifts in its chair.","color":"gray","italic":true}

execute if score #board quest_hud matches 2 run title @s title {"text":" "}
execute if score #board quest_hud matches 2 run title @s subtitle {"text":"Half the table is silent. The static does not thin. It waits.","color":"gray","italic":true}

execute if score #board quest_hud matches 3 run title @s title {"text":" "}
execute if score #board quest_hud matches 3 run title @s subtitle {"text":"One chair left between you and the name. You already move like you know it.","color":"gray","italic":true}

execute if score #board quest_hud matches 4 run title @s title {"text":" "}
execute if score #board quest_hud matches 4 run title @s subtitle {"text":"The room is cleared. The static holds one name, and it is waiting for you.","color":"gold","italic":true}
execute if score #board quest_hud matches 4 run tellraw @s [{"text":"The Boardroom is empty now, except for the chair at the end. ","color":"gray"},{"text":"Go and read the name on it.","color":"gold"}]

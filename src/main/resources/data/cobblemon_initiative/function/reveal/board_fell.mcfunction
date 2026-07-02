# A Board member has fallen. Run as @s = the player (each board_* battle on_win calls
# `execute as @1 at @1 run function cobblemon_initiative:reveal/board_fell`).
#
# Recounts the defeated_board_* tags (same derivation quest/render uses — no new state),
# fires the matching narrative beat, and re-imports the Founder's preset at the matching
# reveal stage: one letter of the name surfaces through the §k static per seat, the full
# name only completing when the whole Board is down. LORE rule: the beats stay oblique —
# the name is never spoken here; the nameplate does the talking.
scoreboard players set #board quest_hud 0
execute if entity @s[tag=defeated_board_madeline] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_matt] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_micah] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_lauren] run scoreboard players add #board quest_hud 1

execute if score #board quest_hud matches 1 run title @s subtitle {"text":"A seat empties. Somewhere, a letter surfaces through the static.","color":"gray","italic":true}
execute if score #board quest_hud matches 1 run title @s title {"text":" "}
execute if score #board quest_hud matches 1 run function cobblemon_initiative:reveal/apply_stage_1

execute if score #board quest_hud matches 2 run title @s subtitle {"text":"The static is thinning. The Board is not holding.","color":"gray","italic":true}
execute if score #board quest_hud matches 2 run title @s title {"text":" "}
execute if score #board quest_hud matches 2 run function cobblemon_initiative:reveal/apply_stage_2

execute if score #board quest_hud matches 3 run title @s subtitle {"text":"The last letter will not surface. It is waiting for the chair to be empty.","color":"gray","italic":true}
execute if score #board quest_hud matches 3 run title @s title {"text":" "}
execute if score #board quest_hud matches 3 run function cobblemon_initiative:reveal/apply_stage_3

execute if score #board quest_hud matches 4 run title @s subtitle {"text":"Four seats. Four letters. The static cannot hold what you already know.","color":"gold","italic":true}
execute if score #board quest_hud matches 4 run title @s title {"text":" "}
execute if score #board quest_hud matches 4 run tellraw @s [{"text":"The Boardroom is empty now, except for the chair at the end. ","color":"gray"},{"text":"Go and read the name on it.","color":"gold"}]
execute if score #board quest_hud matches 4 run function cobblemon_initiative:reveal/apply_stage_4

# SQ3 The Hand That Signed It - Marren checks whether you carry the plate. Run as the player
# (dialog button). Reuses the shared ci_sq_scratch objective (created by museum/load): clear
# with maxCount 0 counts without removing, a verifiable check instead of has_item. The plate is
# a minecraft:heavy_core (place_plate gives exactly one) so this cannot collide with SQ2 iron.
execute store result score @s ci_sq_scratch run clear @s minecraft:heavy_core 0
execute if score @s ci_sq_scratch matches 1.. unless entity @s[tag=the_hand_plate] run tag @s add the_hand_plate
execute if score @s ci_sq_scratch matches 1.. unless entity @s[tag=the_hand_done] run title @s actionbar [{"text":"The plate is on the anvil. ","color":"gold"},{"text":"Look at the mark with Marren.","color":"gray"}]
execute if score @s ci_sq_scratch matches 0 run tellraw @s [{"text":"Marren shakes his head. ","color":"gray"},{"text":"No plate in hand yet. Dig it out of the forge slag heap and bring it here.","color":"yellow"}]

# Map the badge count (memory_fragment 0..9) to the next gym town, then render the gym objective.
# Run as @s = player.
execute if score @s memory_fragment matches 0 run data modify storage cobblemon_initiative:quest town set value "Takehara Falls"
execute if score @s memory_fragment matches 1 run data modify storage cobblemon_initiative:quest town set value "Hua Zhan City"
execute if score @s memory_fragment matches 2 run data modify storage cobblemon_initiative:quest town set value "Mystic Marsh"
execute if score @s memory_fragment matches 3 run data modify storage cobblemon_initiative:quest town set value "Deepcore City"
execute if score @s memory_fragment matches 4 run data modify storage cobblemon_initiative:quest town set value "Gaviota Port"
execute if score @s memory_fragment matches 5 run data modify storage cobblemon_initiative:quest town set value "Kalahar Reach"
execute if score @s memory_fragment matches 6 run data modify storage cobblemon_initiative:quest town set value "Cyber City"
execute if score @s memory_fragment matches 7 run data modify storage cobblemon_initiative:quest town set value "Ryujin Keep"
execute if score @s memory_fragment matches 8 run data modify storage cobblemon_initiative:quest town set value "Nifl Town"
execute if score @s memory_fragment matches 9 run data modify storage cobblemon_initiative:quest town set value "Scorchspire"
function cobblemon_initiative:quest/set_gym with storage cobblemon_initiative:quest

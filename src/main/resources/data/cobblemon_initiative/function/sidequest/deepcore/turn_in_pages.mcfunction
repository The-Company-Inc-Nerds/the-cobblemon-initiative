# Deep Restructuring - Kang reads the fraud once you bring 6 struck pages. Run as the
# player (dialog button). Reuses the shared ci_sq_scratch objective (created by museum/load):
# clear with maxCount 0 counts without removing, a verifiable check instead of has_item.
execute store result score @s ci_sq_scratch run clear @s minecraft:paper 0
execute if score @s ci_sq_scratch matches 6.. unless entity @s[tag=deepcore_restructure_done] run function cobblemon_initiative:sidequest/deepcore/restructure_success
execute if score @s ci_sq_scratch matches ..5 run tellraw @s [{"text":"Kang squares the pages and counts them twice. ","color":"gray"},{"text":"Not six yet. The struck pages pile up in the shredder bins around the Company field office - bring six.","color":"yellow"}]

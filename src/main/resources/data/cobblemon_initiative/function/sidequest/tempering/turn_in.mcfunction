# SQ2 The Tempering - Hollis quenches a keepsake once you bring 8 iron ingots. Run as the
# player (dialog button). Reuses the shared ci_sq_scratch objective (created by museum/load):
# clear with maxCount 0 counts without removing, a verifiable check instead of has_item.
execute store result score @s ci_sq_scratch run clear @s minecraft:iron_ingot 0
execute if score @s ci_sq_scratch matches 8.. unless entity @s[tag=temper_blade_done] run function cobblemon_initiative:sidequest/tempering/quench
execute if score @s ci_sq_scratch matches ..7 run tellraw @s [{"text":"Hollis counts the ingots twice. ","color":"gray"},{"text":"Not eight yet. Iron off the Kalahar deeps, or any ore you have banked - bring eight ingots.","color":"yellow"}]

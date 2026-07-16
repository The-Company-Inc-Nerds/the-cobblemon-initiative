# The Broken Mail (Ryujin SQ3) - Tetsu mends the mail once you bring 8 dragon scales. Run
# as the player (dialog turn_in button). GOLD count-check: reuses the shared ci_sq_scratch
# objective (created by museum/load); clear with maxCount 0 counts without removing, a
# verifiable check instead of has_item. cobblemon:dragon_scale is jar-valid (1.7.3 items.txt).
execute store result score @s ci_sq_scratch run clear @s cobblemon:dragon_scale 0
execute if score @s ci_sq_scratch matches 8.. unless entity @s[tag=ryujin_mail_done] run function cobblemon_initiative:sidequest/ryujin/mail_success
execute if score @s ci_sq_scratch matches ..7 run tellraw @s [{"text":"Tetsu counts the scales twice. ","color":"gray"},{"text":"Not eight yet. The wyrms shed them on the high ledges - bring eight.","color":"yellow"}]

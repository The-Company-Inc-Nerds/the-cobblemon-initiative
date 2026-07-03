# THE CUTTING contract turn-in at Forewoman Tetsu. Run as the player (dialog button).
execute store result score @s ci_wo_count run clear @s minecraft:coal 0
execute if score @s ci_wo_count matches 12.. unless entity @s[tag=work_mine_done] run function cobblemon_initiative:sidequest/work_orders/mine_success
execute if score @s ci_wo_count matches ..11 unless entity @s[tag=work_mine_done] run tellraw @s [{"text":"Tetsu weighs the sack with one hand. ","color":"gray"},{"text":"Short of twelve coal. The marked cutting is on the falls side of the road.","color":"yellow"}]

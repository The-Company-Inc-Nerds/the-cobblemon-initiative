# INVENTORY contract (fork B) turn-in at Apiarist Sumi. Run as the player (dialog button).
# clear with maxCount 0 removes nothing and returns the held count — a verifiable item
# check instead of the experimental HAS_ITEM_IN_INVENTORY dialog condition.
execute store result score @s ci_wo_count run clear @s minecraft:honey_bottle 0
execute if score @s ci_wo_count matches 3.. unless entity @s[tag=work_fetch_done] run function cobblemon_initiative:sidequest/work_orders/fetch_success
execute if score @s ci_wo_count matches ..2 unless entity @s[tag=work_fetch_done] run tellraw @s [{"text":"Sumi counts the bottles twice. ","color":"gray"},{"text":"Not three yet. The wild hives east of the waystation drip slow — bring three honey bottles.","color":"yellow"}]

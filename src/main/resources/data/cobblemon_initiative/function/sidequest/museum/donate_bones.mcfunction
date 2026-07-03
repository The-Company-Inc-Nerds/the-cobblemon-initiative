# Museum bone donation — count-checked turn-in (the quest_fetch pattern, no has_item needed).
# Run AS the player from Tamikos donate button. clear with maxCount 0 counts without removing.
execute if entity @s[tag=sq_museum_donation_done] run return 0
execute store result score @s ci_sq_scratch run clear @s minecraft:bone 0
execute if score @s ci_sq_scratch matches 6.. run function cobblemon_initiative:sidequest/museum/donate_pay
execute unless score @s ci_sq_scratch matches 6.. run tellraw @s [{"text":"Tamiko counts the specimens twice. Six assorted bones complete the exhibit set — keep brushing.","color":"gray"}]

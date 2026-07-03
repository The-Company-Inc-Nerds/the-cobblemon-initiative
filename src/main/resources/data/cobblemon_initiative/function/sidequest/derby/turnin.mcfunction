# Sango Classic — hand-in. Run AS THE PLAYER from Bess dialog (ExecAsUser).
# Fish counting happens here, not at catch time: store-result clear with maxCount 0 counts
# without removing. Only clears 5 on success (take_fish). Branch is decided before any
# mutation so first-win and repeat-win can never double-pay.
execute unless entity @s[tag=classic_active] run tellraw @s [{"text":"No quarter is running. Talk to Bess to enter the Classic.","color":"gray"}]
execute unless entity @s[tag=classic_active] run return 0
execute store result score @s ci_fish_cod run clear @s minecraft:cod 0
execute store result score @s ci_fish_salmon run clear @s minecraft:salmon 0
scoreboard players operation @s ci_fish_total = @s ci_fish_cod
scoreboard players operation @s ci_fish_total += @s ci_fish_salmon
scoreboard players set @s ci_classic_win 0
execute if score @s ci_fish_total matches 5.. run scoreboard players set @s ci_classic_win 1
execute if score @s ci_classic_win matches 1 if entity @s[tag=sango_classic_champion] run scoreboard players set @s ci_classic_win 2
execute if score @s ci_classic_win matches 0 run tellraw @s [{"text":"Bess counts the bucket: ","color":"gray"},{"score":{"name":"@s","objective":"ci_fish_total"},"color":"aqua"},{"text":" of 5. Keep casting.","color":"gray"}]
execute if score @s ci_classic_win matches 1.. run function cobblemon_initiative:sidequest/derby/take_fish
execute if score @s ci_classic_win matches 1 run function cobblemon_initiative:sidequest/derby/win_first
execute if score @s ci_classic_win matches 2 run function cobblemon_initiative:sidequest/derby/win_repeat
execute if score @s ci_classic_win matches 1.. run function cobblemon_initiative:sidequest/derby/win_common

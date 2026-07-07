# Sango Classic — hand-in. Run AS THE PLAYER from Deka dialog (ExecAsUser).
# Fish counting happens here, not at catch time: store-result clear with maxCount 0 counts
# without removing. Only clears 3 on success (take_fish). Branch is decided before any
# mutation so first-win and repeat-win can never double-pay.
execute unless entity @s[tag=classic_active] run tellraw @s [{"text":"No quarter is running. Talk to Deka to enter the Classic.","color":"gray"}]
execute unless entity @s[tag=classic_active] run return 0
execute store result score @s ci_fish_cod run clear @s minecraft:cod 0
execute store result score @s ci_fish_salmon run clear @s minecraft:salmon 0
execute store result score @s ci_fish_puffer run clear @s minecraft:pufferfish 0
execute store result score @s ci_fish_tropical run clear @s minecraft:tropical_fish 0
# Delta vs the begin-snapshot: only this-quarter catches count (clamped at 0 in case
# the player dropped pre-carried fish mid-run).
scoreboard players operation @s ci_fish_cod -= @s ci_fish_base_cod
scoreboard players operation @s ci_fish_salmon -= @s ci_fish_base_salmon
scoreboard players operation @s ci_fish_puffer -= @s ci_fish_base_puffer
scoreboard players operation @s ci_fish_tropical -= @s ci_fish_base_tropical
execute if score @s ci_fish_cod matches ..-1 run scoreboard players set @s ci_fish_cod 0
execute if score @s ci_fish_salmon matches ..-1 run scoreboard players set @s ci_fish_salmon 0
execute if score @s ci_fish_puffer matches ..-1 run scoreboard players set @s ci_fish_puffer 0
execute if score @s ci_fish_tropical matches ..-1 run scoreboard players set @s ci_fish_tropical 0
scoreboard players operation @s ci_fish_total = @s ci_fish_cod
scoreboard players operation @s ci_fish_total += @s ci_fish_salmon
scoreboard players operation @s ci_fish_total += @s ci_fish_puffer
scoreboard players operation @s ci_fish_total += @s ci_fish_tropical
scoreboard players set @s ci_classic_win 0
execute if score @s ci_fish_total matches 3.. run scoreboard players set @s ci_classic_win 1
execute if score @s ci_classic_win matches 1 if entity @s[tag=sango_classic_champion] run scoreboard players set @s ci_classic_win 2
execute if score @s ci_classic_win matches 0 run tellraw @s [{"text":"Deka counts the bucket: ","color":"gray"},{"score":{"name":"@s","objective":"ci_fish_total"},"color":"aqua"},{"text":" of 3. Keep casting.","color":"gray"}]
execute if score @s ci_classic_win matches 1.. run function cobblemon_initiative:sidequest/derby/take_fish
execute if score @s ci_classic_win matches 1 run function cobblemon_initiative:sidequest/derby/win_first
execute if score @s ci_classic_win matches 2 run function cobblemon_initiative:sidequest/derby/win_repeat
execute if score @s ci_classic_win matches 1.. run function cobblemon_initiative:sidequest/derby/win_common

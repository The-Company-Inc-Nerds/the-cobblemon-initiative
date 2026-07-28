# Gaviota Open (alpha.26 audit rulings) — hand-in at the scale. Run AS THE PLAYER from
# the Weighmaster Enzo dialog (ExecAsUser). Sango derby/turnin clone: proof counting
# happens here, not at catch time — store-result clear with maxCount 0 counts without
# removing. Only clears 10 on success (take_fish). Branch is decided before any
# mutation so first-win and repeat-win can never double-pay.
execute unless entity @s[tag=gaviota_open_active] run tellraw @s [{"text":"No round is running. See Weighmaster Enzo at the podium to enter the Open.","color":"gray"}]
execute unless entity @s[tag=gaviota_open_active] run return 0
execute store result score @s ci_open_cod run clear @s minecraft:cod 0
execute store result score @s ci_open_salmon run clear @s minecraft:salmon 0
execute store result score @s ci_open_puffer run clear @s minecraft:pufferfish 0
execute store result score @s ci_open_ink run clear @s minecraft:ink_sac 0
# Delta vs the begin-snapshot: only this-round proofs count (clamped at 0 in case the
# player dropped pre-carried items mid-run).
scoreboard players operation @s ci_open_cod -= @s ci_open_base_cod
scoreboard players operation @s ci_open_salmon -= @s ci_open_base_salmon
scoreboard players operation @s ci_open_puffer -= @s ci_open_base_puffer
scoreboard players operation @s ci_open_ink -= @s ci_open_base_ink
execute if score @s ci_open_cod matches ..-1 run scoreboard players set @s ci_open_cod 0
execute if score @s ci_open_salmon matches ..-1 run scoreboard players set @s ci_open_salmon 0
execute if score @s ci_open_puffer matches ..-1 run scoreboard players set @s ci_open_puffer 0
execute if score @s ci_open_ink matches ..-1 run scoreboard players set @s ci_open_ink 0
scoreboard players operation @s ci_open_total = @s ci_open_cod
scoreboard players operation @s ci_open_total += @s ci_open_salmon
scoreboard players operation @s ci_open_total += @s ci_open_puffer
scoreboard players operation @s ci_open_total += @s ci_open_ink
scoreboard players set @s ci_open_win 0
execute if score @s ci_open_total matches 10.. run scoreboard players set @s ci_open_win 1
execute if score @s ci_open_win matches 1 if entity @s[tag=gaviota_open_champion] run scoreboard players set @s ci_open_win 2
execute if score @s ci_open_win matches 0 run tellraw @s [{"text":"The scale counts: ","color":"gray"},{"score":{"name":"@s","objective":"ci_open_total"},"color":"aqua"},{"text":" of 10. Back to the water.","color":"gray"}]
execute if score @s ci_open_win matches 1.. run function cobblemon_initiative:sidequest/gaviota_open/take_fish
execute if score @s ci_open_win matches 1 run function cobblemon_initiative:sidequest/gaviota_open/win_first
execute if score @s ci_open_win matches 2 run function cobblemon_initiative:sidequest/gaviota_open/win_repeat
execute if score @s ci_open_win matches 1.. run function cobblemon_initiative:sidequest/gaviota_open/win_common

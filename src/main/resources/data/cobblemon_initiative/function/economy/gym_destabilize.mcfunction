# The Company's plot advances one notch (called from a gym leader's command reward, run as @s=player).
# Raises the CobbleDollar instability index by 8 (clamped to 100) and narrates the slip.
scoreboard players add #idx cd_instability 8
execute if score #idx cd_instability matches 101.. run scoreboard players set #idx cd_instability 100
execute store result storage cobblemon_initiative:economy idx int 1 run scoreboard players get #idx cd_instability
function cobblemon_initiative:economy/announce with storage cobblemon_initiative:economy

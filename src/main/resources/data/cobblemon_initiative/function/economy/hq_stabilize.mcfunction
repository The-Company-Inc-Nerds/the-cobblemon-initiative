# Acting CEO defeated / HQ raided (called from villain_boss command reward, run as @s=player).
# Resets the instability index to the post-raid floor and plays the stabilization beat.
scoreboard players set #idx cd_instability 25
execute store result storage cobblemon_initiative:economy idx int 1 run scoreboard players get #idx cd_instability
function cobblemon_initiative:economy/stabilized with storage cobblemon_initiative:economy

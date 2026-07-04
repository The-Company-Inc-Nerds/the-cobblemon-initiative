# Acting CEO defeated / HQ raided (called from villain_boss command reward, run as @s=player).
# Clamps the instability index DOWN to the post-raid ceiling and plays the stabilization
# beat. Downward-only: a full-liberation player can arrive below 25, and "stabilizing"
# must never RAISE the index they earned. Idempotent under the double-fire (the reward
# and the onwin both call this).
execute if score #idx cd_instability matches 26.. run scoreboard players set #idx cd_instability 25
execute store result storage cobblemon_initiative:economy idx int 1 run scoreboard players get #idx cd_instability
function cobblemon_initiative:economy/stabilized with storage cobblemon_initiative:economy

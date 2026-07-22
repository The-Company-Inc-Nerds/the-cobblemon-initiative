# ☎ Professor Acacia — 50-dex research grant. Video call. One-shot. @s. Grants 5 Great Balls.
tag @s add call_acacia_dex_done
title @s actionbar {"text":"☎ Incoming call — Professor Acacia","color":"aqua"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
give @s cobblemon:great_ball 5
function cobblemon_initiative:phone/deliver {caller:"phone_acacia",tag:"ci_phone_acacia",label:"call_dex"}

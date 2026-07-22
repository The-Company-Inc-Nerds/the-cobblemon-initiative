# ☎ Professor Acacia — third starter waiting (30 dex). Video call. One-shot. @s.
tag @s add call_acacia_third_done
title @s actionbar {"text":"☎ Incoming call — Professor Acacia","color":"aqua"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
function cobblemon_initiative:phone/deliver {caller:"phone_acacia",tag:"ci_phone_acacia",label:"call_third"}

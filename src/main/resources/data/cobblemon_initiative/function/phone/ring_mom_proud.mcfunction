# ☎ Mom — proud call at 5 badges. Video call. One-shot. @s.
tag @s add call_mom_proud_done
title @s actionbar {"text":"☎ Incoming call — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1.2
function cobblemon_initiative:phone/deliver {caller:"phone_mom",tag:"ci_phone_mom",label:"call_proud"}

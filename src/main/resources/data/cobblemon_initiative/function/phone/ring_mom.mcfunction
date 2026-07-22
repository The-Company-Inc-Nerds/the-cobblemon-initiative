# ☎ Mom — friendship-care intro after the 3rd badge. Video call. One-shot. Run AS @s.
tag @s add call_mom_watch_done
title @s actionbar {"text":"☎ Incoming call — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1.2
function cobblemon_initiative:phone/deliver {caller:"phone_mom",tag:"ci_phone_mom",label:"call_watch"}

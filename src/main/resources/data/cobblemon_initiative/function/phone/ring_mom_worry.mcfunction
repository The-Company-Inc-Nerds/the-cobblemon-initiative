# ☎ Mom — worried call after the first Nuzlocke loss (nuzlocke_lost_one). Video call. One-shot. @s.
tag @s add call_mom_worry_done
title @s actionbar {"text":"☎ Incoming call — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.9
function cobblemon_initiative:phone/deliver {caller:"phone_mom",tag:"ci_phone_mom",label:"call_worry"}

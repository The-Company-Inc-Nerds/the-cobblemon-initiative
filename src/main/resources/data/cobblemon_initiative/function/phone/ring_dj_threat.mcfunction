# ☎ Acting CEO DJ — named threat at 7 badges (HQ era), unless already beaten. Video call. One-shot. @s.
tag @s add call_dj_threat_done
title @s actionbar {"text":"☎ Incoming call — Acting CEO DJ","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.7
function cobblemon_initiative:phone/deliver {caller:"phone_dj",tag:"ci_phone_dj",label:"call_threat"}

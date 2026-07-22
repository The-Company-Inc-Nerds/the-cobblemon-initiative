# ☎ Unknown Number — first Company recognition taunt (3 badges). Silhouette video call. One-shot. @s.
tag @s add call_company_watch_done
title @s actionbar {"text":"☎ Incoming call — Unknown Number","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.6
function cobblemon_initiative:phone/deliver {caller:"phone_unknown",tag:"ci_phone_unknown",label:"call_watch"}

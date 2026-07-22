# ☎ The Board — act-3 foreshadow gloat once the Royal League is cleared. Silhouette video call. One-shot. @s.
tag @s add call_board_gloat_done
title @s actionbar {"text":"☎ Incoming call — The Board","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.5
function cobblemon_initiative:phone/deliver {caller:"phone_board",tag:"ci_phone_board",label:"call_gloat"}

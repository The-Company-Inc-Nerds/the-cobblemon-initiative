# ☎ The Founder — pre-finale call, the shadow self. Fires once the Board is cleared (board_cleared),
# the last beat before the mirror battle. Silhouette video call. One-shot. @s.
# NOTE: dormant until act-3 Board content emits a `board_cleared` player tag (not built yet).
tag @s add call_founder_done
title @s actionbar {"text":"☎ Incoming call — The Founder","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.5
function cobblemon_initiative:phone/deliver {caller:"phone_founder",tag:"ci_phone_founder",label:"call_founder"}

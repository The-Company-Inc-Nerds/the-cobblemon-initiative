# ☎ Mayor Liang — a new homestead beacon is in stock and affordable. Video call; the "Buy the
# beacon" option is a dialog button now (phone_liang call_beacon). One-shot. @s.
tag @s add call_beacon_stock_done
title @s actionbar {"text":"☎ Incoming call — Mayor Liang","color":"gold"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
function cobblemon_initiative:phone/deliver {caller:"phone_liang",tag:"ci_phone_liang",label:"call_beacon"}

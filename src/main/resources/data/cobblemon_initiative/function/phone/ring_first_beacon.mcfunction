# ☎ Mayor Liang — nudge back to him for the free FIRST beacon (freed a field, no beacon yet).
# Fired from homestead/need_beacon when Liang is freed (defeated_sq_mayor_suits) and the first
# beacon has not been given. Video call. One-shot (call_first_beacon_done). @s = the player.
tag @s add call_first_beacon_done
title @s actionbar {"text":"☎ Incoming call — Mayor Liang","color":"gold"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
function cobblemon_initiative:phone/deliver {caller:"phone_liang",tag:"ci_phone_liang",label:"call_first"}

# ☎ Mayor Liang's call — the player freed a field and tried to homestead it with NO beacon.
# The town gifts the FIRST beacon free, but only Liang hands it over (dialog beacon_gift). This
# call just NUDGES them back to him. One-shot (call_first_beacon_done). Fired from
# homestead/need_beacon ONLY when Liang is already freed (defeated_sq_mayor_suits) and the first
# beacon has not been given. @s = the player.
tag @s add call_first_beacon_done
title @s actionbar {"text":"☎ Phone ringing — Mayor Liang","color":"gold"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
tellraw @s ["",{"text":"☎ ","color":"gold"},{"text":"Mayor Liang: ","color":"gold","bold":true},{"text":"You cleared a field back from them and have nothing to raise on it? The town keeps the first beacon for anyone doing that work. Come by the roof — it is yours, no charge. Then set it on the field you freed.","color":"white"}]

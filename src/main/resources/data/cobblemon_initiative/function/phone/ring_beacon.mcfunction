# ☎ Mayor Suzune's call — a new homestead beacon is in stock and you can afford it. One-shot. @s.
tag @s add call_beacon_stock_done
title @s actionbar {"text":"☎ Phone ringing — Mayor Suzune","color":"gold"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
tellraw @s ["",{"text":"☎ ","color":"gold"},{"text":"Mayor Suzune: ","color":"gold","bold":true},{"text":"\"Another beacon came in off the coast. If your purse is ready, so is mine — raise it on a field you have freed.\"","color":"white"}]
tellraw @s [{"text":"   [Buy the beacon now]","color":"aqua","underlined":true,"clickEvent":{"action":"run_command","value":"/cobblemon-initiative homestead buy"},"hoverEvent":{"action":"show_text","contents":[{"text":"Buy Suzune's next beacon (price escalates each time)","color":"gray"}]}}]

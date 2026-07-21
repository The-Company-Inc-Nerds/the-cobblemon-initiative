# ☎ Professor Acacia — 50-dex research grant. One-shot. @s. A small ball grant funds the field work.
tag @s add call_acacia_dex_done
title @s actionbar {"text":"☎ Phone ringing — Professor Acacia","color":"aqua"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
tellraw @s ["",{"text":"☎ ","color":"aqua"},{"text":"Professor Acacia: ","color":"aqua","bold":true},{"text":"Fifty entries logged. That is real field work — fifty living things that let you close the distance. For once the grant committee agreed with me.","color":"white"}]
give @s cobblemon:great_ball 5
tellraw @s [{"text":"   A small research grant to keep the book growing: five Great Balls, on the lab. Keep the map lighting up behind you.","color":"gray","italic":true}]

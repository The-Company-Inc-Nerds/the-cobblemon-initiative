# ☎ Professor Acacia — a third starter is waiting (unlocked at 30 dex catches). One-shot. @s.
tag @s add call_acacia_third_done
title @s actionbar {"text":"☎ Phone ringing — Professor Acacia","color":"aqua"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
tellraw @s ["",{"text":"☎ ","color":"aqua"},{"text":"Professor Acacia: ","color":"aqua","bold":true},{"text":"Thirty species now. I stopped calling this a hobby three towns ago. It is a craft, and you are good at it.","color":"white"}]
tellraw @s [{"text":"   There is a third partner at the lab with your name on the paperwork. Come and choose the next time you are near Sango.","color":"gray","italic":true}]

# ☎ Mom — proud call at 5 badges. One-shot. @s.
tag @s add call_mom_proud_done
title @s actionbar {"text":"☎ Phone ringing — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1.2
tellraw @s ["",{"text":"☎ ","color":"light_purple"},{"text":"Mom: ","color":"light_purple","bold":true},{"text":"Five badges. Five. I told the neighbors and old Mr. Fen actually put his paper down. You have never seen him put the paper down.","color":"white"}]
tellraw @s [{"text":"   I am proud of you. I do not say it enough down a phone, but I am. Come home for a meal when the road lets you.","color":"gray","italic":true}]

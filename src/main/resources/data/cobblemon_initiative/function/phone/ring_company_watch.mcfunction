# ☎ Unknown Number — the first Company recognition taunt (3 badges). One-shot. @s. The amnesia arc, remote.
tag @s add call_company_watch_done
title @s actionbar {"text":"☎ Phone ringing — Unknown Number","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.6
tellraw @s ["",{"text":"☎ ","color":"dark_red"},{"text":"Unknown Number: ","color":"dark_red","bold":true},{"text":"We know your face. It sits in a file with a black bar where the name should be. And here you are, collecting badges as if they were yours to collect.","color":"white"}]
tellraw @s [{"text":"   The line goes dead before you can answer.","color":"gray","italic":true}]

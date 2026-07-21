# ☎ Professor Acacia — a second starter is waiting (unlocked at 15 dex catches). One-shot. @s.
tag @s add call_acacia_second_done
title @s actionbar {"text":"☎ Phone ringing — Professor Acacia","color":"aqua"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1
tellraw @s ["",{"text":"☎ ","color":"aqua"},{"text":"Professor Acacia: ","color":"aqua","bold":true},{"text":"Your Pokedex is filling out faster than I can log it. Fifteen species and counting — you have the hands for this work.","color":"white"}]
tellraw @s [{"text":"   Come back to the lab when you pass through Sango. A trainer who catches like you have earned a second partner. I set one aside.","color":"gray","italic":true}]

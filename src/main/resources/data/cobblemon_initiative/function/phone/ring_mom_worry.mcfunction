# ☎ Mom — worried call after the first Nuzlocke loss (nuzlocke_lost_one, set by NuzlockeInit). One-shot. @s.
tag @s add call_mom_worry_done
title @s actionbar {"text":"☎ Phone ringing — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.9
tellraw @s ["",{"text":"☎ ","color":"light_purple"},{"text":"Mom: ","color":"light_purple","bold":true},{"text":"I felt it. Do not ask me how a mother knows — I just do. You lost one of them out there, did you not.","color":"white"}]
tellraw @s [{"text":"   Come see me when you can. Bring the team. A house is good for a heart that is carrying something heavy. Be careful out there. Please.","color":"gray","italic":true}]

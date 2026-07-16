# ☎ Mom's call — introduces her friendship-care service after the 3rd badge. One-shot. Run AS @s.
tag @s add call_mom_watch_done
title @s actionbar {"text":"☎ Phone ringing — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1.2
tellraw @s ["",{"text":"☎ ","color":"light_purple"},{"text":"Mom: ","color":"light_purple","bold":true},{"text":"\"You have a real team now. I can hear it in how you talk about them.\"","color":"white"}]
tellraw @s [{"text":"   \"Leave one with me when you pass through Sango — a mother is good for a Pokémon's heart. It comes home closer to you. Come see me when you can.\"","color":"gray","italic":true}]

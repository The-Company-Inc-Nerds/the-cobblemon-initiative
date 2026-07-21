# ☎ The Board — act-3 foreshadow gloat once the Royal League is cleared (royal_league_champion). One-shot. @s.
tag @s add call_board_gloat_done
title @s actionbar {"text":"☎ Phone ringing — The Board","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.5
tellraw @s ["",{"text":"☎ ","color":"dark_red"},{"text":"The Board: ","color":"dark_red","bold":true},{"text":"Champion. The little map calls you its best now. How quaint. We watched every step of the climb from a room you will never be invited to.","color":"white"}]
tellraw @s [{"text":"   A colder voice, and many others behind it. You have their attention now — which was always the more dangerous prize.","color":"gray","italic":true}]

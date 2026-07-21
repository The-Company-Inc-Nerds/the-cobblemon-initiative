# ☎ The Founder — the pre-finale call, the shadow self. Fires once the Board is cleared
# (board_cleared), the last beat before the mirror battle. One-shot. @s.
# NOTE: dormant until the act-3 Board content emits a `board_cleared` player tag (not built yet).
tag @s add call_founder_done
title @s actionbar {"text":"☎ Phone ringing — The Founder","color":"dark_red"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 0.5
tellraw @s ["",{"text":"☎ ","color":"dark_red"},{"text":"The Founder: ","color":"dark_red","bold":true},{"text":"You tore through my whole board to reach this room. The grunts, the management, every Director I hired to keep the seat warm while you were away. One chair left to settle now.","color":"white"}]
tellraw @s [{"text":"   You have chased me the length of the road and still not caught the joke. Come up. Stand across from me. You will understand the moment you see my face — it is the one you have been washing out of every river since you woke.","color":"gray","italic":true}]

# ☎ Mom — the strange Pokemon by home (Latios home-base reward). TEXT call (no video body).
# Fired ONCE from HomeBaseManager the moment the block-placed threshold is passed (the manager
# guards the one-shot with the home_base_built tag, so no completion tag is needed here). Run AS @s.
title @s actionbar {"text":"☎ Incoming call — Mom","color":"light_purple"}
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 1 1.2
tellraw @s [{"text":"☎ Phone Call from Mom: ","color":"light_purple","bold":true},{"text":"Sweetheart, come home when you can — something strange turned up right by the old town. A big blue one, floating over the roofs, watching the place you have been building. It will not come to me. I think it is waiting for you. Hurry, before it changes its mind.","color":"white","bold":false}]

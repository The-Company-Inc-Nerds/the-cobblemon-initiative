# Sango send-off card — fires when Mom hands over the Running Shoes (the opening chain
# closes and the run proper begins). Run as the player from the take_shoes button.
# Warm, quiet, and gone in four seconds — the last soft beat before the road west.
title @s times 10 80 20
title @s subtitle [{"text":"Sango raised you. The road west does not know you yet.","color":"gray","italic":true}]
title @s title [{"text":"THE ROAD WEST","color":"gold","bold":true}]
playsound minecraft:entity.villager.celebrate master @s ~ ~ ~ 0.8 1
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 0.9

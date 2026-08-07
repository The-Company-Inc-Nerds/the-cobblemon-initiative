# Augmented Ring Run — a single runner FAILS (fell below the course floor, or the clock expired).
# Run AS/AT that runner (@s). Returns them to Arlo's rig deck (the vertical course means a fall drops
# them far below the start, so unlike the trident/cascade water courses this one teleports back).
# ORDER: strip state, drop the augment, THEN teleport (tp needs the player, not the tag).
effect clear @s minecraft:speed
effect clear @s minecraft:jump_boost
scoreboard players reset @s ci_augrace_cp
tag @s remove ci_aug_racing
playsound minecraft:block.note_block.bass player @s ~ ~ ~ 1 0.6
tp @s 1445.5 178 1268.5 facing 1459 178 1270.5
title @s title [{"text":"AUGMENT RESET","color":"red","bold":true}]
title @s subtitle [{"text":"Arlo pulls you back to the deck - free retries","color":"gray"}]

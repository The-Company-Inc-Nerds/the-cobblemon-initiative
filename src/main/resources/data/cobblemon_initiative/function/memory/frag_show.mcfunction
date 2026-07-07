# Memory fragment presentation. Run as/at the player with storage
# cobblemon_initiative:memory frag {title, sub} (staged by grant_fragment, delivered
# 4s later via frag_title). The purple silence after the fireworks — the show's thesis.

# Cinematic delivery: clear any lingering title, set styled title + subtitle, then show.
title @s times 10 70 20
$title @s subtitle {"text":"$(sub)","color":"dark_gray","italic":true}
$title @s title {"text":"$(title)","color":"#7A5CA8","bold":true}

# Subtle "shadow self / memory surfacing" sound — quiet, low pitch.
playsound minecraft:block.sculk_sensor.clicking master @s ~ ~ ~ 0.6 0.5
playsound minecraft:particle.soul_escape master @s ~ ~ ~ 0.4 0.8

# Chat echo so the line is re-readable in the log during the stream.
$tellraw @s [{"text":"[Memory] ","color":"#7A5CA8","bold":true},{"text":"$(title)","color":"gray","italic":true}]

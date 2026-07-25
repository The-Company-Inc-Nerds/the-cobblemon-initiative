# VERIFIED GROWTH — THE REVEAL. Run as @s = the player, at @s. Now a SPOKEN beat: called from
# the greenhouse archivist's dispatch-board reading (Shu, hz_greenhouse_archivist) once the player
# names the crop out loud — the old catwalk position sensor (sidequest/greenhouse/tick) was retired
# 2026-07-24 (the Company building is a house to the south, not a glass tower to climb). One-time
# per player (wheat_named latch). Also sets heard_wheat_pitch (so a house-first player unlocks the
# wheat HUD and Blossom traders_word without having met the market traders) and toured_greenhouse
# (the tag Blossom traders_word fans out on). No advancement grant — the_word_is_wheat has no
# advancement JSON in-repo and that dir is outside this package's write scope (see report).
tag @s add wheat_named
tag @s add heard_wheat_pitch
tag @s add toured_greenhouse
title @s times 10 70 20
title @s title {"text":"THE WORD IS WHEAT","color":"#C9A227","bold":true}
title @s subtitle {"text":"Ten fields. One crop. One buyer.","color":"gray"}
tellraw @s [{"text":"THE DISPATCH BOARD — ","color":"#C9A227","bold":true},{"text":"ten farm names, one seed source, one buyer, one crop, read out every morning without ever saying the word. Firstfurrow was line one. It was never a secret. It was a supply chain.","color":"gray"}]
playsound minecraft:block.bell.resonate master @s ~ ~ ~ 1 0.7

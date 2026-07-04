# VERIFIED GROWTH (The Greenhouse Tour) — THE REVEAL. Run as @s = the player, at @s, from the
# catwalk position sensor (sidequest/greenhouse/tick). One-time per player (wheat_named latch).
# The whole span under glass is one crop, horizon to horizon, and the word is said under Company
# glass on the catwalk. Also sets heard_wheat_pitch (so a tour-first player unlocks the wheat HUD
# and Blossom traders_word without having met the market traders) and toured_greenhouse (the tag
# Blossom traders_word fans out on). No advancement grant — the_word_is_wheat has no advancement
# JSON in-repo and that dir is outside this package's write scope (see report).
tag @s add wheat_named
tag @s add heard_wheat_pitch
tag @s add toured_greenhouse
title @s times 10 70 20
title @s title {"text":"THE WORD IS WHEAT","color":"#C9A227","bold":true}
title @s subtitle {"text":"Ten fields. One crop. One buyer.","color":"gray"}
tellraw @s [{"text":"THE GREENHOUSE — ","color":"#C9A227","bold":true},{"text":"the whole span under glass is one crop, from the door to the far wall you cannot see. Firstfurrow was line one. The word was never a secret. It was a supply chain.","color":"gray"}]
playsound minecraft:block.bell.resonate master @s ~ ~ ~ 1 0.7

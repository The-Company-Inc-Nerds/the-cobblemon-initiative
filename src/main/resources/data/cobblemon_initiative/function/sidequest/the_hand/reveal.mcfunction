# SQ3 The Hand That Signed It - Marren sets the plate against your stance and goes quiet.
# Run as the player (guarded by the look button: the_hand_plate present). Consume the plate,
# latch the_hand_done, pay 500 via the skewed payout + a major training gift, set the story
# breadcrumb scrubbing_artifact_plate (one-way, feeds a future Board/Founder synthesis chain),
# and land the recognition sting. NO name, NO obfuscated reveal - this circles the founder
# identity, it does not close it (the reveal is the Act-3 mirror).
clear @s minecraft:heavy_core 1
tag @s add the_hand_done
tag @s add scrubbing_artifact_plate
function cobblemon_initiative:economy/payout {amount:500}
function cobblemon_initiative:economy/reward/major
title @s title [{"text":"THE SAME HAND","color":"gold"}]
title @s subtitle [{"text":"you flinch every time I say sign","color":"gray"}]

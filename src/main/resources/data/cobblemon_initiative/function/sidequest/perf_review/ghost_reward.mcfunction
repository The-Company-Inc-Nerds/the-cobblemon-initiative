# Ghost path, stage 3: badge earned with gym1_seen still 0, fired 5s late by ghost_fire.
# Run as the player. One-time: ghost_arm already latched perf_review_resolved; the
# pending tag comes off first so a double schedule could never double-pay. Cache
# contents ride npc_gift/silent_stakeholder; the 400 CD component goes through the
# skew-aware payout. Note (tower-optional era): ghosting means Cicada was fought at
# FULL strength — the risk bought this cache.
tag @s remove perf_review_ghost_pending
loot give @s loot cobblemon_initiative:npc_gift/silent_stakeholder
function cobblemon_initiative:economy/payout {amount:400}
loot give @s loot cobblemon_initiative:npc_gift/training_major
tag @s add perf_review_ghost
tag @s add perf_review_resolved
title @s times 10 70 20
title @s subtitle [{"text":"You moved like the brood underground — unseen and patient.","color":"gray","italic":true}]
title @s title [{"text":"SILENT STAKEHOLDER","color":"gold","bold":true}]
tellraw @s [{"text":"SILENT STAKEHOLDER","color":"gold","bold":true},{"text":" — you moved like the brood underground: unseen and patient. The rafter cache finds its way into your bag.","color":"gray"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 1 1.2
playsound minecraft:block.sculk_sensor.clicking player @s ~ ~ ~ 0.6 0.6

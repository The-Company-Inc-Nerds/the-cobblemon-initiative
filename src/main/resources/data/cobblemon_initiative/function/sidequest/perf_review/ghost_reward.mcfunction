# Ghost path: badge earned with gym1_seen still 0. Run as the player. One-time via
# perf_review_resolved. Cache contents ride npc_gift/silent_stakeholder; the 400 CD
# component goes through the skew-aware payout.
loot give @s loot cobblemon_initiative:npc_gift/silent_stakeholder
function cobblemon_initiative:economy/payout {amount:400}
tag @s add perf_review_ghost
tag @s add perf_review_resolved
tellraw @s [{"text":"SILENT STAKEHOLDER","color":"gold","bold":true},{"text":" — you moved like the brood underground: unseen and patient. The rafter cache finds its way into your bag.","color":"gray"}]
playsound minecraft:block.note_block.chime player @s ~ ~ ~ 1 1.2

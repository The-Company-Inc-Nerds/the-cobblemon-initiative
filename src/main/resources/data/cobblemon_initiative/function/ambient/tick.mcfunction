# Proximity spawn checks for all latch-placed NPCs (generated from `placement` fields).
function cobblemon_initiative:ambient/placements
# Despawn a starter stand-in once its species is claimed. The choose button tags the
# PLAYER claimed_starter_<id> (entity-path @s resolves to the player), so we kill the
# matching stand-in — tagged ci_standin_<species> in its preset NBT — from here. Only
# the claimed one dies; the other two remain as cry-only per the design.
execute if entity @a[tag=claimed_starter_skiddo] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_skiddo]
execute if entity @a[tag=claimed_starter_totodile] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_totodile]
execute if entity @a[tag=claimed_starter_hisuian_growlithe] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_growlithe]
# Victor's reveal — the tower-top apprentice was Victini. A qualified player (heard of him
# from Kesi + refused to sell the founder's papers) tags themselves victor_transformed via
# his dialog; next tick, positioned at the humanoid, spawn the Victini form + despawn him.
# Guard: skip once the Victini form already exists, so it happens exactly once.
execute as @a[tag=victor_transformed] at @e[tag=victor_apprentice,type=!minecraft:player,limit=1] unless entity @e[tag=victor_victini,type=!minecraft:player] run function cobblemon_initiative:sango/victor_transform
# Once Victini has joined the player's party, remove the tower-top Victini NPC.
execute if entity @a[tag=victini_joined] run kill @e[type=easy_npc:cobblemon_npc,tag=victor_victini]
# Kalahar mirage sweep — the gym 6 hunt scatters heat-shimmer doubles of the gym cast
# across the Reach (tag ci_mirage, baked into their presets via entity_tags). The
# Reach out button tags the double ci_mirage_popped ENTITY-PATH with a self-selecting
# @e[tag=ci_mirage,distance=..1] (never @s — the compiler rewrites dialog @s to the
# player); killing it here, one tick later, avoids racing the deferred CLOSE_DIALOG
# packet, same as the starter stand-ins above. FX lines first, then the kill.
execute at @e[tag=ci_mirage_popped] run particle minecraft:block{block_state:{Name:"minecraft:sand"}} ~ ~1 ~ 0.4 0.8 0.4 0 50 force
execute at @e[tag=ci_mirage_popped] run particle minecraft:cloud ~ ~1.2 ~ 0.35 0.7 0.35 0.02 30 force
execute at @e[tag=ci_mirage_popped] run particle minecraft:poof ~ ~1 ~ 0.3 0.8 0.3 0.05 40 force
execute at @e[tag=ci_mirage_popped] run playsound minecraft:block.sand.break player @a[distance=..24] ~ ~ ~ 1 0.6
execute at @e[tag=ci_mirage_popped] run playsound minecraft:entity.breeze.idle_air player @a[distance=..24] ~ ~ ~ 1 1.3
kill @e[tag=ci_mirage_popped]
# Aya's reveal — the west-stair groundskeeper (uuid a9ed3a64) was Leader Blossom all along.
# A challenger who cleared all four garden wardens tags themselves aya_transformed via her
# dialog; next tick, positioned at her body, spawn Leader Blossom + despawn the groundskeeper.
# Guard: skip once the leader body already exists, so it happens exactly once.
execute as @a[tag=aya_transformed] at @e[tag=aya_groundskeeper,type=!minecraft:player,limit=1] unless entity @e[tag=hz_leader_body,type=!minecraft:player] run function cobblemon_initiative:hua_zhan/aya_transform

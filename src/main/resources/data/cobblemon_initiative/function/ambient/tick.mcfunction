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

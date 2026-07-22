# Proximity spawn checks for all latch-placed NPCs (generated from `placement` fields).
function cobblemon_initiative:ambient/placements
# Despawn a starter stand-in once its species is claimed. The choose button tags the
# PLAYER claimed_starter_<id> (entity-path @s resolves to the player), so we kill the
# matching stand-in — tagged ci_standin_<species> in its preset NBT — from here. Only
# the claimed one dies; the other two remain as cry-only per the design.
execute if entity @a[tag=claimed_starter_skiddo] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_skiddo]
execute if entity @a[tag=claimed_starter_totodile] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_totodile]
execute if entity @a[tag=claimed_starter_hisuian_growlithe] run kill @e[type=easy_npc:cobblemon_npc,tag=ci_standin_growlithe]
# Victor's reveal — the apprentice at his reveal spot was Victini. A qualified player (heard
# of him from Kesi + refused to sell the founder's papers, &c.) tags themselves
# victor_transformed via his dialog; next tick this plays the reveal cutscene IN PLACE.
# Guard: a DEDICATED one-shot player tag (victor_transform_fired, set on victor_transform's
# first line). The old guard `unless entity victor_victini` looped forever — victor_victini
# only spawns at cutscene tick 60, but this line re-ran victor_transform EVERY tick, and
# CutsceneManager.play() restarts an already-active scene, so the scene reset each tick and
# never reached tick 60. The player was trapped in a scene that never revealed (game-break).
execute as @a[tag=victor_transformed,tag=!victor_transform_fired] run function cobblemon_initiative:sango/victor_transform
# Soft-lock recovery: if the scene ended BEFORE its tick-60 swap (mid-scene logout or a
# hardcore death — CutsceneManager.end() fires without spawning Victini), the player keeps
# victor_transform_fired forever and can never re-trigger. Strip it so the one-shot above
# re-dispatches a fresh scene. Guards: gamemode=!spectator (the player is a SPECTATOR for the
# whole live scene, so this NEVER fires mid-scene, which would restart the loop) AND the
# apprentice body must STILL EXIST — i.e. the scene aborted BEFORE the tick-60 swap killed him.
# If the swap already ran (apprentice gone) but Victini never materialised, re-dispatching
# would just replay the scene forever, so we don't: recover only the genuine pre-swap abort.
execute as @a[tag=victor_transformed,tag=victor_transform_fired,tag=!victini_joined,gamemode=!spectator] unless entity @e[tag=victor_victini,type=!minecraft:player] if entity @e[tag=victor_apprentice,type=!minecraft:player] run tag @s remove victor_transform_fired
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

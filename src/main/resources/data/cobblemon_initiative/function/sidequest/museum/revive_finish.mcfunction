# Resurrection machine cinematic - PHASE 3 (finish, +60t). Scheduled from revive_begin. Removes the
# floating fossil, plays the birth beat, then spawns an INTERACTABLE Easy NPC of the revived species
# in front of the tank (import_new the revived_<sp> gift body) - the player walks up and talks to it
# to add it to the team (do NOT auto-open its dialog: a just-spawned body is not client-tracked yet).
# Branch on the ci_reviving_<species> carry tag, then clear all carry tags so the platform is idle.
kill @e[tag=ci_fossil_float]
playsound cobblemon:block.fossil_machine.finished master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound cobblemon:block.fossil_machine.retrieve_pokemon master @a[distance=..24] 1902.5 116 2313.8 1 1
playsound minecraft:entity.player.levelup master @a[distance=..24] 1902.5 116 2313.8 0.7 0.8
execute positioned 1902.5 116 2313.8 run particle minecraft:end_rod ~ ~ ~ 0.5 0.7 0.5 0.06 60
execute positioned 1902.5 116 2313.8 run particle minecraft:glow ~ ~ ~ 0.4 0.6 0.4 0.0 24
# Spawn the revived-species gift body in front of the tank (per species).
execute if entity @a[tag=ci_reviving_kabuto] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_kabuto.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_omanyte] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_omanyte.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_aerodactyl] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_aerodactyl.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_lileep] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_lileep.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_anorith] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_anorith.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_cranidos] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_cranidos.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_shieldon] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_shieldon.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_tirtouga] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_tirtouga.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_archen] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_archen.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_tyrunt] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_tyrunt.npc.snbt 1902.5 115 2315.5
execute if entity @a[tag=ci_reviving_amaura] run easy_npc preset import_new data easy_npc:preset/humanoid/revived_amaura.npc.snbt 1902.5 115 2315.5
# Generic prompt to the reviving player (species-specific flavour lives on the gift body's dialog).
execute as @a[tag=ci_reviving] run tellraw @s [{"text":"The tank drains. Something ancient steps onto the plate, blinking in the museum light - go and meet it.","color":"gold"}]
# Clear the carry tags (shared + every species) so the platform is idle again.
tag @a remove ci_reviving
tag @a remove ci_reviving_kabuto
tag @a remove ci_reviving_omanyte
tag @a remove ci_reviving_aerodactyl
tag @a remove ci_reviving_lileep
tag @a remove ci_reviving_anorith
tag @a remove ci_reviving_cranidos
tag @a remove ci_reviving_shieldon
tag @a remove ci_reviving_tirtouga
tag @a remove ci_reviving_archen
tag @a remove ci_reviving_tyrunt
tag @a remove ci_reviving_amaura

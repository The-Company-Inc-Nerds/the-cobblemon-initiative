# Curator Kenji resurrection machine — claw fossil -> Anorith, on the museum platform
# (float at 1902.5/116/2313.8). Run AS THE PLAYER from the revive_claw button (gated not_tag
# sq_revived_anorith — per-species). PHASE 0: consume, latch, float the item, kick off the shared
# cinematic (museum/revive_begin -> revive_mid -> revive_finish). See dome_fossil for the pattern.
execute store result score #turnin ci_item run clear @s cobblemon:claw_fossil 0
execute if score #turnin ci_item matches ..0 run title @s actionbar [{"text":"You have no Claw Fossil to revive.","color":"red"}]
execute if score #turnin ci_item matches 1.. run clear @s cobblemon:claw_fossil 1
execute if score #turnin ci_item matches 1.. run tag @s add sq_revived_anorith
execute if score #turnin ci_item matches 1.. run tag @s add ci_reviving_anorith
execute if score #turnin ci_item matches 1.. positioned 1902.5 116 2313.8 run summon minecraft:item ~ ~ ~ {Item:{id:"cobblemon:claw_fossil",count:1},NoGravity:1b,Invulnerable:1b,PickupDelay:32767s,Age:-32768s,Tags:["ci_fossil_float"],CustomName:'{"text":"Claw Fossil","color":"gold"}'}
execute if score #turnin ci_item matches 1.. run function cobblemon_initiative:sidequest/museum/revive_begin

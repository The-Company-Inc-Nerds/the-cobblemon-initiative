# Curator Kenji resurrection machine — dome fossil -> Kabuto, on the museum platform
# (top 1902.5/115/2313.8; the fossil floats at +1 = 1902.5/116/2313.8). Run AS THE PLAYER from the
# revive_dome button (gated not_tag sq_revived_kabuto — per-species, so each dug fossil revives once).
# PHASE 0: consume the fossil, latch, float the item over the platform, kick off the shared machine
# cinematic (museum/revive_begin schedules revive_mid + revive_finish; the finish removes the float and
# gives the revived mon). Consume-probe idiom unchanged (clear 0 counts without removing).
execute store result score #turnin ci_item run clear @s cobblemon:dome_fossil 0
execute if score #turnin ci_item matches ..0 run title @s actionbar [{"text":"You have no Dome Fossil to revive.","color":"red"}]
execute if score #turnin ci_item matches 1.. run clear @s cobblemon:dome_fossil 1
execute if score #turnin ci_item matches 1.. run tag @s add sq_revived_kabuto
execute if score #turnin ci_item matches 1.. run tag @s add ci_reviving_kabuto
execute if score #turnin ci_item matches 1.. positioned 1902.5 116 2313.8 run summon minecraft:item ~ ~ ~ {Item:{id:"cobblemon:dome_fossil",count:1},NoGravity:1b,Invulnerable:1b,PickupDelay:32767s,Age:-32768s,Tags:["ci_fossil_float"],CustomName:'{"text":"Dome Fossil","color":"gold"}'}
execute if score #turnin ci_item matches 1.. run function cobblemon_initiative:sidequest/museum/revive_begin

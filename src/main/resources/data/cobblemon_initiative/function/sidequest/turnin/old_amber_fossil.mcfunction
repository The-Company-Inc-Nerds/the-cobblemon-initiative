# Curator Kenji resurrection machine - Old Amber -> Aerodactyl, on the museum platform (top 1902.5/115/
# 2313.8; the fossil floats at +1 = 1902.5/116/2313.8). Run AS THE PLAYER via turnin/use_machine (one-button flow, a18)
# — sq_revived_aerodactyl is now TELEMETRY ONLY (a18: the per-species lockout is removed). Consume-probe
# idiom: `clear @s <item> 0` counts without removing; only latch + float + begin if the player holds
# one. Carry tags: shared ci_reviving (generic mid/finish beats) + ci_reviving_aerodactyl (finish branches
# on it to spawn the right revived_aerodactyl gift body). Kept as a literal turnin (not a macro) so
# dialog_lint sees the sq_revived_aerodactyl grant.
# Guard: one revive at a time. If the tank is already mid-cinematic (shared ci_reviving tag), reject
# BEFORE the consume so a rapid second click can't consume a fossil the in-flight finish then discards.
execute if entity @s[tag=ci_reviving] run title @s actionbar [{"text":"The tank is still working - wait for it to finish.","color":"red"}]
execute if entity @s[tag=ci_reviving] run return 0
execute store result score #turnin ci_item run clear @s cobblemon:old_amber_fossil 0
execute if score #turnin ci_item matches ..0 run title @s actionbar [{"text":"You have no Aerodactyl to revive.","color":"red"}]
execute if score #turnin ci_item matches 1.. run clear @s cobblemon:old_amber_fossil 1
execute if score #turnin ci_item matches 1.. run tag @s add sq_revived_aerodactyl
execute if score #turnin ci_item matches 1.. run tag @s add ci_reviving
execute if score #turnin ci_item matches 1.. run tag @s add ci_reviving_aerodactyl
execute if score #turnin ci_item matches 1.. positioned 1902.5 116 2313.8 run summon minecraft:item ~ ~ ~ {Item:{id:"cobblemon:old_amber_fossil",count:1},NoGravity:1b,Invulnerable:1b,PickupDelay:32767s,Age:-32768s,Tags:["ci_fossil_float"],CustomName:'{"text":"Aerodactyl","color":"gold"}'}
execute if score #turnin ci_item matches 1.. run function cobblemon_initiative:sidequest/museum/revive_begin

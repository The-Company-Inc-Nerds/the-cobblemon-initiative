# Take a cache-recovery mission. Run as @s = the player (Ossa's accept button; the button also
# tags @s reach_cache_active first). Guard: one active site at a time. Hands over a field brush if
# the player carries none (suspicious sand needs brushing), rolls a site, copies its compound to
# storage `active`, and arms it (forceload -> scheduled placement -> chalk-bearing tellraw).
execute if score #cache_site ci_cache matches 1.. run tellraw @s [{"text":"Ossa taps the bearing already chalked on her board - the marked cache is still out there. Bring back the deed, or ask her to re-mark it.","color":"gray"}]
execute if score #cache_site ci_cache matches 1.. run return 0
execute store result score #cache_t ci_cache run clear @s minecraft:brush 0
execute if score #cache_t ci_cache matches 0 run give @s minecraft:brush 1
execute if score #cache_t ci_cache matches 0 run tellraw @s [{"text":"Ossa hands you a field brush from the records drawer. Brush, never shovel - a shovel scatters what the sand kept.","color":"gray"}]
execute store result score #cache_site ci_cache run random value 1..10
execute if score #cache_site ci_cache matches 1 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[0]
execute if score #cache_site ci_cache matches 2 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[1]
execute if score #cache_site ci_cache matches 3 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[2]
execute if score #cache_site ci_cache matches 4 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[3]
execute if score #cache_site ci_cache matches 5 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[4]
execute if score #cache_site ci_cache matches 6 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[5]
execute if score #cache_site ci_cache matches 7 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[6]
execute if score #cache_site ci_cache matches 8 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[7]
execute if score #cache_site ci_cache matches 9 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[8]
execute if score #cache_site ci_cache matches 10 run data modify storage cobblemon_initiative:cache active set from storage cobblemon_initiative:cache sites[9]
function cobblemon_initiative:sidequest/reach_cache/arm with storage cobblemon_initiative:cache active

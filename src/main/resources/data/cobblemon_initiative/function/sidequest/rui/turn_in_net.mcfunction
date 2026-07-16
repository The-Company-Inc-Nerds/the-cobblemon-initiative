# Mending the Deep Nets - Rui restrings the nets once you bring 8 string. Run as the
# player (dialog button). Reuses the shared ci_sq_scratch objective (created by museum/load):
# clear with maxCount 0 counts without removing, a verifiable check instead of has_item.
execute store result score @s ci_sq_scratch run clear @s minecraft:string 0
execute if score @s ci_sq_scratch matches 8.. unless entity @s[tag=bosun_net_done] run function cobblemon_initiative:sidequest/rui/net_success
execute if score @s ci_sq_scratch matches ..7 run tellraw @s [{"text":"Rui counts the lengths twice. ","color":"gray"},{"text":"Not eight yet. String comes off the wharf spiders after dark - bring eight lengths.","color":"yellow"}]

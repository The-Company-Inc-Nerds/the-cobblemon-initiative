# Beekeeper Masumi hand-in: 8 wild honeycomb. Run as the player. Sets sq_honey_done,
# which the queen_cell Combee claim now also requires (so no honey => no Combee).
execute store result score #turnin ci_item run clear @s minecraft:honeycomb 0
execute if score #turnin ci_item matches ..7 run title @s actionbar [{"text":"You need 8 wild honeycomb.","color":"red"}]
execute if score #turnin ci_item matches 8.. run clear @s minecraft:honeycomb 8
execute if score #turnin ci_item matches 8.. run loot give @s loot cobblemon_initiative:npc_gift/training_standard
execute if score #turnin ci_item matches 8.. run function cobblemon_initiative:economy/payout {amount:300}
execute if score #turnin ci_item matches 8.. run tag @s add sq_honey_done

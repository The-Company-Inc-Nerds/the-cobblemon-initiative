# Curator Kenji revival: claw fossil -> Anorith. Run as the player. One de-extinction
# per run is enforced by the button's not_tag sq_museum_revived gate.
execute store result score #turnin ci_item run clear @s cobblemon:claw_fossil 0
execute if score #turnin ci_item matches ..0 run title @s actionbar [{"text":"You have no Claw Fossil to revive.","color":"red"}]
execute if score #turnin ci_item matches 1.. run clear @s cobblemon:claw_fossil 1
execute if score #turnin ci_item matches 1.. run givepokemonother @s anorith level=10
execute if score #turnin ci_item matches 1.. run tag @s add sq_museum_revived
execute if score #turnin ci_item matches 1.. run tellraw @s [{"text":"The claw uncurls after an age asleep. Anorith swims once more.","color":"gold"}]

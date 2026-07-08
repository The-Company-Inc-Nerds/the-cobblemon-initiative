# Curator Kenji revival: dome fossil -> Kabuto. Run as the player. One de-extinction
# per run is enforced by the button's not_tag sq_museum_revived gate.
execute store result score #turnin ci_item run clear @s cobblemon:dome_fossil 0
execute if score #turnin ci_item matches ..0 run title @s actionbar [{"text":"You have no Dome Fossil to revive.","color":"red"}]
execute if score #turnin ci_item matches 1.. run clear @s cobblemon:dome_fossil 1
execute if score #turnin ci_item matches 1.. run givepokemonother @s kabuto level=10
execute if score #turnin ci_item matches 1.. run tag @s add sq_museum_revived
execute if score #turnin ci_item matches 1.. run tellraw @s [{"text":"Ten thousand years of sediment let go. Kabuto lives again.","color":"gold"}]

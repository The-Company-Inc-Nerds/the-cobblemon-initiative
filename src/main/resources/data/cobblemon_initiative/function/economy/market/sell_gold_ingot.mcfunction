# Syla Ironchalk — Deepcore gallery ore-trader: buys 8 Gold Ingots for 200 CD.
# Run AS THE PLAYER from a dialog sell button (ExecAsUser). Count-probe + batch-of-8, repeatable.
execute store result score #turnin ci_item run clear @s minecraft:gold_ingot 0
execute if score #turnin ci_item matches ..7 run title @s actionbar [{"text":"Bring at least 8 gold ingots. ","color":"red"},{"text":"Syla buys them by the crate.","color":"gray"}]
execute if score #turnin ci_item matches 8.. run clear @s minecraft:gold_ingot 8
execute if score #turnin ci_item matches 8.. run cobbledollars give @s 200
execute if score #turnin ci_item matches 8.. run title @s actionbar [{"text":"Sold 8 Gold Ingots. ","color":"green"},{"text":"+200 CD","color":"gold"}]

# Syla Ironchalk — Deepcore gallery ore-trader: buys 4 Diamonds for 320 CD.
# Run AS THE PLAYER from a dialog sell button (ExecAsUser). Count-probe + batch-of-4, repeatable.
execute store result score #turnin ci_item run clear @s minecraft:diamond 0
execute if score #turnin ci_item matches ..3 run title @s actionbar [{"text":"Bring at least 4 diamonds. ","color":"red"},{"text":"Syla weighs them by the fistful.","color":"gray"}]
execute if score #turnin ci_item matches 4.. run clear @s minecraft:diamond 4
execute if score #turnin ci_item matches 4.. run cobbledollars give @s 320
execute if score #turnin ci_item matches 4.. run title @s actionbar [{"text":"Sold 4 Diamonds. ","color":"green"},{"text":"+320 CD","color":"gold"}]

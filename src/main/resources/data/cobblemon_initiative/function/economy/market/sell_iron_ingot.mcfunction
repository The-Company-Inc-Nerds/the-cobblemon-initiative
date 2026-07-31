# Syla Ironchalk — Deepcore gallery ore-trader: buys 8 Iron Ingots for 96 CD.
# Run AS THE PLAYER from a dialog sell button (ExecAsUser). `clear @s <item> 0` probes the
# count WITHOUT removing (vanilla); a batch of 8 so #turnin can't be salami-sliced one at a
# time. Repeatable (no done-tag). Flat price via `cobbledollars give` (predictable trader).
execute store result score #turnin ci_item run clear @s minecraft:iron_ingot 0
execute if score #turnin ci_item matches ..7 run title @s actionbar [{"text":"Bring at least 8 iron ingots. ","color":"red"},{"text":"Syla buys them by the crate.","color":"gray"}]
execute if score #turnin ci_item matches 8.. run clear @s minecraft:iron_ingot 8
execute if score #turnin ci_item matches 8.. run cobbledollars give @s 96
execute if score #turnin ci_item matches 8.. run title @s actionbar [{"text":"Sold 8 Iron Ingots. ","color":"green"},{"text":"+96 CD","color":"gold"}]

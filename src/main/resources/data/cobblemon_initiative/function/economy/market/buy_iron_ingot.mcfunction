# Deepcore Rubble Chute salvage counter — Vorn Gravelhand: Iron Ingot (120 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:120}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:iron_ingot 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Iron Ingot. ","color":"green"},{"text":"-120 CD","color":"gray"}]

# Deepcore Rubble Chute salvage counter — Vorn Gravelhand: Coal x8 (80 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:80}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:coal 8
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Coal x8. ","color":"green"},{"text":"-80 CD","color":"gray"}]

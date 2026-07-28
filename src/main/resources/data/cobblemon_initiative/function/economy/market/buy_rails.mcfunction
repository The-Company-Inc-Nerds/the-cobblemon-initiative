# Deepcore Rubble Chute salvage counter — Vorn Gravelhand: Rail x8 (150 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:150}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:rail 8
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Rail x8. ","color":"green"},{"text":"-150 CD","color":"gray"}]

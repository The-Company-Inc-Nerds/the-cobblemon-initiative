# Deepcore Rubble Chute salvage counter — Vorn Gravelhand: Torch x16 (60 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:60}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:torch 16
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Torch x16. ","color":"green"},{"text":"-60 CD","color":"gray"}]

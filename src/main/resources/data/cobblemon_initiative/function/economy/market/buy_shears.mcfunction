# East Market house counter — Kaito Zhang, the wool shelf: Shears (300 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:300}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:shears 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Shears. ","color":"green"},{"text":"-300 CD","color":"gray"}]

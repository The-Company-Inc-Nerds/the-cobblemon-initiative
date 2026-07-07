# East Market Street — Madam Qiu, the mint apothecary (curio shelf): Sun Stone (2800 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:2800}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:sun_stone 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Sun Stone. ","color":"green"},{"text":"-2800 CD","color":"gray"}]
